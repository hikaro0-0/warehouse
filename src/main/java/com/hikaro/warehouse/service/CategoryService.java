package com.hikaro.warehouse.service;

import com.hikaro.warehouse.entity.Category;
import com.hikaro.warehouse.exception.ResourceNotFoundException;
import com.hikaro.warehouse.repository.CategoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Category with id " + id + " not found"
                        )
                );
    }

    public Category create(Category category) {
        return categoryRepository.save(category);
    }

    public Category update(Long id, Category updatedCategory) {
        Category category = getById(id);
        category.setName(updatedCategory.getName());
        category.setDescription(updatedCategory.getDescription());
        return categoryRepository.save(category);
    }

    public void delete(Long id) {
        Category category = getById(id);
        categoryRepository.delete(category);
    }
}
