package org.ritika.cognitbackend.repository;

import org.ritika.cognitbackend.entity.PostView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface PostViewRepository extends JpaRepository<PostView, Long> {

    boolean existsByPostIdAndUserIdAndViewedOn(Long postId, Long userId, LocalDate date);

    boolean existsByPostIdAndFingerprintAndViewedOn(Long postId, String fingerprint, LocalDate date);
}