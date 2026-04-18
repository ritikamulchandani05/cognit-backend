package org.ritika.cognitbackend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ritika.cognitbackend.service.PostService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheWarmingConfig implements ApplicationRunner {
    private final PostService postService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Cache warming started - pre-loading popular data into Redis");

        warmPostsCache();

        log.info("Cache warming completed");
    }
    private void warmPostsCache() {
        try{
            postService.getAllPosts(0,10,"publishedAt", "desc");
            log.info("Cache Warming - first page of posts loaded (page=0, size=20)");
        } catch (Exception ex){
            log.warn("Cache Warming - failed to get pre-loaded posts - {}" , ex.getMessage());
        }
    }
}
