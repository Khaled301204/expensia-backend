package com.expensia.backend.config;

import com.expensia.backend.model.entity.Category;
import com.expensia.backend.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public DataSeeder(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        seedCategory("Food & Dining", "restaurant", "#FF9800");
        seedCategory("Transportation", "directions_car", "#2196F3");
        seedCategory("Shopping", "shopping_bag", "#E91E63");
        seedCategory("Entertainment", "movie", "#673AB7");
        seedCategory("Bills & Utilities", "receipt", "#9C27B0");
        seedCategory("Healthcare", "local_hospital", "#F44336");
        seedCategory("Education", "school", "#3F51B5");
        seedCategory("Travel", "flight", "#00BCD4");
        seedCategory("Personal Care", "spa", "#4CAF50");
        seedCategory("Electronics & Tech", "devices", "#795548");
        seedCategory("Other", "category", "#607D8B");
    }

    private void seedCategory(String name, String icon, String color) {
        if (categoryRepository.findByNameIgnoreCase(name).isPresent()) {
            return;
        }

        Category category = new Category();
        category.setName(name);
        category.setIcon(icon);
        category.setColor(color);

        categoryRepository.save(category);
    }
}