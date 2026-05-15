package com.expensia.backend.service.ai;

import com.expensia.backend.dto.response.CategoryResponse;
import com.expensia.backend.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> getAllCategories() {

        return categoryRepository.findAll()
                .stream()
                .map(category -> new CategoryResponse(
                        category.getCategoryId(),
                        category.getName(),
                        category.getIcon(),
                        category.getColor()
                ))
                .toList();
    }
}
