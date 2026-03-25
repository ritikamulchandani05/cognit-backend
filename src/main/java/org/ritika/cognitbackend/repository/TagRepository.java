package org.ritika.cognitbackend.repository;

import org.ritika.cognitbackend.entity.Category;
import org.ritika.cognitbackend.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TagRepository extends JpaRepository<Tag, Long>, SluggableRepository {
    Optional<Tag> findBySlug(String slug);

    Optional<Tag> findByName(String name);

    List<Tag> findByNameIn(List<String> names);

    Set<Tag> findByNameIn(Set<String> names);

    boolean existsBySlug(String slug);

    boolean existsByName(String name);

    List<Tag> findAllByOrderByNameAsc();

    @Query("SELECT t FROM Tag t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Tag> searchByName(@Param("searchTerm") String searchTerm);

    @Query("SELECT t FROM Tag t WHERE LOWER(t.name) LIKE LOWER(CONCAT(:prefix, '%')) ORDER BY t.name")
    List<Tag> findByNameStartingWith(@Param("prefix") String prefix);

    @Query("SELECT t FROM Tag t LEFT JOIN t.posts p "+
            "WHERE p.isDeleted = false AND p.status = 'PUBLISHED'" +
            "GROUP BY t ORDER BY COUNT(p) DESC"
    )
    List<Tag> findPopularTags(@Param("limit") int limit);

    @Query("SELECT DISTINCT t FROM Tag t JOIN t.posts p " +
            "WHERE p.user.id = :userId AND p.isDeleted = false"
    )
    List<Tag> findTagsByAuthor(@Param("userId") Long userId);


}

