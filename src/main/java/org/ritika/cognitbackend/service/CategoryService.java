package org.ritika.cognitbackend.service;

import org.ritika.cognitbackend.dto.request.CreateCategoryRequest;
import org.ritika.cognitbackend.dto.response.CategoryResponse;

import java.util.List;


public interface CategoryService {


    CategoryResponse createCategory(CreateCategoryRequest request);


    CategoryResponse updateCategory(Long id, CreateCategoryRequest request);


    void deleteCategory(Long id);


    CategoryResponse getCategoryById(Long id);


    CategoryResponse getCategoryBySlug(String slug);


    List<CategoryResponse> getAllCategories();


    List<CategoryResponse> searchCategories(String searchTerm);


    boolean existsByName(String name);
}

