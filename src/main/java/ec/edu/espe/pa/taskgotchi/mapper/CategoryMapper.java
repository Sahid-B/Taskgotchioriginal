package ec.edu.espe.pa.taskgotchi.mapper;

import ec.edu.espe.pa.taskgotchi.dto.CategoryRequest;
import ec.edu.espe.pa.taskgotchi.dto.CategoryResponse;
import ec.edu.espe.pa.taskgotchi.model.Category;
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
