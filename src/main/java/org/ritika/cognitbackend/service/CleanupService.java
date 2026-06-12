package org.ritika.cognitbackend.service;

public interface CleanupService {
    void purgeDeletedPosts();
    void purgeDeletedUsers();
    void logDatabaseStats();
    void purgeExpiredOtps();
}
