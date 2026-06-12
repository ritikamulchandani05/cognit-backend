package org.ritika.cognitbackend.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.time.Duration;

@Slf4j
@Configuration
public class RateLimitConfig {

    // Bandwidth constants - one Bandwidth per role, reused by RateLimitFilter

    public static final Bandwidth SUBSCRIBER_BANDWIDTH =
            Bandwidth.builder()
                    .capacity(10)
                    .refillGreedy(10, Duration.ofHours(1))
                    .build();

    public static final Bandwidth AUTHOR_BANDWIDTH =
            Bandwidth.builder()
                    .capacity(500)
                    .refillGreedy(500, Duration.ofHours(1))
                    .build();

    public static final Bandwidth ADMIN_BANDWIDTH =
            Bandwidth.builder()
                    .capacity(1_000_000)
                    .refillGreedy(1_000_000, Duration.ofHours(1))
                    .build();

    // ProxyManager Bean
    @Bean
    public LettuceBasedProxyManager<byte[]> rateLimitProxyManager(
            LettuceConnectionFactory lettuceConnectionFactory
    ) {
        //getNativeClient() returns AbstractRedisClient; cast is safe when not
        //using Redis Cluster (LettuceConnectionFactory wraps a plain RedisClient)
        RedisClient redisClient = (RedisClient) lettuceConnectionFactory.getNativeClient();

        log.info("Initialising Bucket4j LettuceBasedProxyManager for distributed rate limiting");

        return LettuceBasedProxyManager.builderFor(redisClient)
                .withExpirationStrategy(ExpirationAfterWriteStrategy.fixedTimeToLive(Duration.ofHours(2)))
                .build();
    }

}
