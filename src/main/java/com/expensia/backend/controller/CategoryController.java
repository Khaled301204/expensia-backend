package com.expensia.backend.controller;

import com.expensia.backend.dto.response.ApiResponse;
import com.expensia.backend.dto.response.CategoryResponse;
import com.expensia.backend.service.ai.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Categories retrieved successfully",
                        categoryService.getAllCategories()
                )
        );
    }
}
