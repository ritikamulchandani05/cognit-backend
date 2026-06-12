package org.ritika.cognitbackend.repository;

import org.ritika.cognitbackend.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.post.id = :postId AND c.isDeleted = false ORDER BY c.createdAt ASC")
    Page<Comment> findByPostIdAndIsDeletedFalse(@Param("postId") Long postId, Pageable pageable);

    long countByPostIdAndIsDeletedFalse(Long postId);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.isDeleted = true AND c.updatedAt < :cutoff")
    int hardDeleteByIsDeletedTrueAndUpdatedAtBefore(@Param("cutoff") LocalDateTime cutoff);

    long countByIsDeletedTrue();

    Page<Comment> findByAuthorIdAndIsDeletedFalse(Long authorId, Pageable pageable);
}
