package org.ritika.cognitbackend.repository;

import org.ritika.cognitbackend.entity.Post;
import org.ritika.cognitbackend.enums.PostStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    @EntityGraph(attributePaths = {"user","category","tags"})
    Page<Post> findByStatusAndIsDeletedFalse(PostStatus status, Pageable pageable);
    @EntityGraph(attributePaths = {"user","category","tags"})
    Page<Post> findByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);
    @EntityGraph(attributePaths = {"user","category","tags"})
    Optional<Post> findBySlugAndIsDeletedFalse(String slug);

    Optional<Post> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @Query("SELECT p FROM Post p WHERE p.isDeleted = false " +
            "AND p.status = 'PUBLISHED'" +
            "AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')))"
    )
    @EntityGraph(attributePaths = {"user","category","tags"})
    Page<Post> searchPublishedPosts(@Param("searchTerm") String searchTerm, Pageable pageable);
    @EntityGraph(attributePaths = {"user","category","tags"})
    Page<Post> findByCategoryIdAndStatusAndIsDeletedFalse(Long id, PostStatus status, Pageable pageable);
    @EntityGraph(attributePaths = {"user","category","tags"})
    List<Post> findByTagsIdAndStatusAndIsDeletedFalse(Long id, PostStatus postStatus);

    long countByCategoryId(Long categoryId);

    long countByCategoryIdAndStatusAndIsDeletedFalse(Long categoryId, PostStatus postStatus);

    long countByUserIdAndIsDeletedFalse(Long userId);
    @EntityGraph(attributePaths = {"user","category","tags"})
    Page<Post> findByIsDeletedFalse(Pageable pageable);
    @EntityGraph(attributePaths = {"user","category","tags"})
    Page<Post> findByUserIdAndStatusAndIsDeletedFalse(Long userId, PostStatus postStatus, Pageable pageable);

    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :postId")
    void incrementLikeCount(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE Post p SET p.likeCount = CASE WHEN p.likeCount > 0 THEN p.likeCount - 1 ELSE 0 END WHERE p.id = :postId")
    void decrementLikeCount(@Param("postId") Long postId);




}

