package com.expensia.backend.dto.response;

public class CategoryResponse {

    private Long categoryId;
    private String name;
    private String icon;
    private String color;

    public CategoryResponse(
            Long categoryId,
            String name,
            String icon,
            String color
    ) {
        this.categoryId = categoryId;
        this.name = name;
        this.icon = icon;
        this.color = color;
    }

    public Long getCategoryId() { return categoryId; }
    public String getName() { return name; }
    public String getIcon() { return icon; }
    public String getColor() { return color; }
}