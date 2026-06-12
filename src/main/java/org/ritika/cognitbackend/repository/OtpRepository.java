package org.ritika.cognitbackend.repository;

import org.ritika.cognitbackend.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {

    // Most-recent valid, unused OTP for a user
    Optional<Otp> findTopByUserIdAndUsedFalseAndValidTillAfterOrderByCreatedAtDesc(
            Long userId, LocalDateTime now);

    @Modifying
    @Query("DELETE FROM Otp o WHERE o.validTill < :cutoff")
    int deleteExpiredOtps(@Param("cutoff") LocalDateTime cutoff);
}