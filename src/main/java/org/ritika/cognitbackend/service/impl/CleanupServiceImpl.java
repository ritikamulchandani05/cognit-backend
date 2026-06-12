package org.ritika.cognitbackend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ritika.cognitbackend.repository.OtpRepository;
import org.ritika.cognitbackend.repository.PostRepository;
import org.ritika.cognitbackend.repository.UserRepository;
import org.ritika.cognitbackend.service.CleanupService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class CleanupServiceImpl implements CleanupService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final OtpRepository otpRepository;

    /**
     * Number of days a soft-deleted record is kept before hard-deletion.
     * Defaults to 30 if the property is absent (e.g., in test context).
     */
    @Value("${cleanup.deleted-retention-days:30}")
    private int retentionDays;

    /**
     * Purge posts soft-deleted more than {@code retentionDays} days ago.
     *
     * Fires at 02:00 every night (low-traffic window).
     * Uses a direct JPQL DELETE to avoid loading post entities into memory -
     * the same principle as @Modifying increment/decrrement queries in PostRepository
     */
    @Override
    @Scheduled(cron = "${cleanup.cron.purge-posts}")
    @Transactional
    public void purgeDeletedPosts() {
        long start = System.currentTimeMillis();
        log.info("[CleanupJob] purgeDeletedPosts starting - retention window: {} days", retentionDays);
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        int deleted = postRepository.hardDeleteByIsDeletedTrueAndUpdatedAtBefore(cutoff);
        long duration = System.currentTimeMillis() - start;
        log.info("[CleanupJob] purgeDeletedPosts completed - {} post(s) hard-deleted in {}ms", deleted, duration);
    }

    @Override
    @Transactional
    @Scheduled(cron = "${cleanup.cron.purge-users}")
    public void purgeDeletedUsers() {
        long start = System.currentTimeMillis();
        log.info("[CleanupJob] purgeDeletedUsers starting - retention window: {} days", retentionDays);
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        int deleted = userRepository.hardDeleteByIsDeletedTrueAndUpdatedAtBefore(cutoff);
        long duration = System.currentTimeMillis() - start;
        log.info("[CleanupJob] purgeDeletedUsers completed - {} post(s) hard-deleted in {}ms", deleted, duration);

    }

    @Override
    @Scheduled(cron = "${cleanup.cron.stats}")
    @Transactional(readOnly = true)
    public void logDatabaseStats() {
        long start = System.currentTimeMillis();
        log.info("[CleanupJob] logDatabaseStats starting");
        long softDeletedPosts = postRepository.countByIsDeletedTrue();
        long softDeletedUsers = userRepository.countByIsDeletedTrue();

        long duration = System.currentTimeMillis() - start;
        log.info("[CleanupJob] logDatabaseStats complete in {}ms - " + "soft-deleted posts: {}, soft-deleted users:{}", duration, softDeletedPosts, softDeletedUsers);

    }
    @Override
    @Scheduled(cron = "${cleanup.cron.purge-otps}")
    @Transactional
    public void purgeExpiredOtps() {
        long start = System.currentTimeMillis();
        log.info("[CleanupJob] purgeExpiredOtps starting");
        // 2-3 day buffer: keep rows for debugging even after expiry
        LocalDateTime cutoff = LocalDateTime.now().minusDays(2);
        int deleted = otpRepository.deleteExpiredOtps(cutoff);
        long duration = System.currentTimeMillis() - start;
        log.info("[CleanupJob] purgeExpiredOtps completed - {} row(s) removed in {}ms", deleted, duration);
    }
}
