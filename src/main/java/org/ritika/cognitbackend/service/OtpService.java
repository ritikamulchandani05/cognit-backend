package org.ritika.cognitbackend.service;

public interface OtpService {
    /** Generate OTP, persist to Redis + DB, send email. */
    void generateAndSend(Long userId);

    /** Validate OTP; returns true and marks used on success. */
    boolean verify(Long userId, String otp);
}