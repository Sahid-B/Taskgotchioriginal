package ec.edu.espe.pa.taskgotchi.service.impl;

import ec.edu.espe.pa.taskgotchi.dto.CategoryRequest;
import ec.edu.espe.pa.taskgotchi.dto.CategoryResponse;
import ec.edu.espe.pa.taskgotchi.exception.ResourceNotFoundException;
import ec.edu.espe.pa.taskgotchi.mapper.CategoryMapper;
import ec.edu.espe.pa.taskgotchi.model.Category;
import ec.edu.espe.pa.taskgotchi.model.User;
import ec.edu.espe.pa.taskgotchi.repository.CategoryRepository;
import ec.edu.espe.pa.taskgotchi.repository.UserRepository;
import ec.edu.espe.pa.taskgotchi.security.CurrentUserProvider;
import ec.edu.espe.pa.taskgotchi.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        Long userId = currentUserProvider.getCurrentUserId();
        return categoryRepository.findByUserId(userId).stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        Category category = categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada o no pertenece al usuario"));
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Category category = categoryMapper.toEntity(request);
        category.setUser(user);
        category = categoryRepository.save(category);

        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        Category category = categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada o no pertenece al usuario"));

        category.setName(request.name());
        category.setColor(request.color());
        category = categoryRepository.save(category);

        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        Category category = categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada o no pertenece al usuario"));
        categoryRepository.delete(category);
    }
}
