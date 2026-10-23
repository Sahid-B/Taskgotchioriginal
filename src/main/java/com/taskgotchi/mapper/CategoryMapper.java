package com.taskgotchi.mapper;

import com.taskgotchi.dto.CategoryRequest;
import com.taskgotchi.dto.CategoryResponse;
import com.taskgotchi.model.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        if (category == null) return null;
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getColor()
        );
    }

    public Category toEntity(CategoryRequest request) {
        if (request == null) return null;
        return Category.builder()
                .name(request.name())
                .color(request.color())
                .build();
    }
}
