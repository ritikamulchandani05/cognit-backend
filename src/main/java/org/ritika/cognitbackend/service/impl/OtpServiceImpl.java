package org.ritika.cognitbackend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ritika.cognitbackend.entity.Otp;
import org.ritika.cognitbackend.entity.User;
import org.ritika.cognitbackend.exception.ResourceNotFoundException;
import org.ritika.cognitbackend.repository.OtpRepository;
import org.ritika.cognitbackend.repository.UserRepository;
import org.ritika.cognitbackend.service.EmailService;
import org.ritika.cognitbackend.service.OtpService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OtpServiceImpl implements OtpService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String REDIS_KEY_PREFIX = "otp:";

    private final OtpRepository otpRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final StringRedisTemplate redisTemplate;

    @Value("${otp.validity-minutes:10}")
    private int validityMinutes;

    @Override
    @Transactional
    public void generateAndSend(Long userId) {
        User user = userRepository.findById(userId)
                .filter(u -> !u.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        String otpValue = String.format("%06d", RANDOM.nextInt(1_000_000));
        LocalDateTime now       = LocalDateTime.now();
        LocalDateTime validTill = now.plusMinutes(validityMinutes);

        // 1. Persist to DB (source of truth)
        Otp otp = Otp.builder()
                .user(user)
                .otpValue(otpValue)
                .emailReqAt(now)
                .validTill(validTill)
                .used(false)
                .build();
        otpRepository.save(otp);

        // 2. Try to also cache in Redis (best-effort; never block the flow)
        try {
            String redisKey = REDIS_KEY_PREFIX + userId;
            redisTemplate.opsForValue().set(redisKey, otpValue, validityMinutes, TimeUnit.MINUTES);
            log.debug("[OTP] Cached in Redis for user {}", userId);
        } catch (Exception ex) {
            log.warn("[OTP] Redis write failed for user {} – DB fallback active: {}", userId, ex.getMessage());
        }

        emailService.sendOtpEmail(user, otpValue);
    }

    @Override
    @Transactional
    public boolean verify(Long userId, String submittedOtp) {
        // 1. Try Redis first (fastest path)
        try {
            String redisKey = REDIS_KEY_PREFIX + userId;
            String cached   = redisTemplate.opsForValue().get(redisKey);
            if (cached != null) {
                if (!cached.equals(submittedOtp)) {
                    log.warn("[OTP] Redis: wrong OTP for user {}", userId);
                    return false;
                }
                redisTemplate.delete(redisKey);
                markDbOtpUsed(userId);
                log.info("[OTP] Verified via Redis for user {}", userId);
                return true;
            }
        } catch (Exception ex) {
            log.warn("[OTP] Redis read failed for user {} – falling back to DB: {}", userId, ex.getMessage());
        }

        // 2. Fall back to DB
        return otpRepository
                .findTopByUserIdAndUsedFalseAndValidTillAfterOrderByCreatedAtDesc(userId, LocalDateTime.now())
                .map(otp -> {
                    if (!otp.getOtpValue().equals(submittedOtp)) {
                        log.warn("[OTP] DB: wrong OTP for user {}", userId);
                        return false;
                    }
                    otp.setUsed(true);
                    otpRepository.save(otp);
                    log.info("[OTP] Verified via DB for user {}", userId);
                    return true;
                })
                .orElseGet(() -> {
                    log.warn("[OTP] No valid OTP found in DB for user {}", userId);
                    return false;
                });
    }

    private void markDbOtpUsed(Long userId) {
        otpRepository
                .findTopByUserIdAndUsedFalseAndValidTillAfterOrderByCreatedAtDesc(userId, LocalDateTime.now())
                .ifPresent(otp -> {
                    otp.setUsed(true);
                    otpRepository.save(otp);
                });
    }
}