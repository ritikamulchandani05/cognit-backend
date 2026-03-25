package org.ritika.cognitbackend.repository;

public interface SluggableRepository {
    boolean existsBySlug(String slug);
}
