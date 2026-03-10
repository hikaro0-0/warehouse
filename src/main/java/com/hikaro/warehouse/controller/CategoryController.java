package com.hikaro.warehouse.controller;

import com.hikaro.warehouse.dto.CategoryRequestDto;
import com.hikaro.warehouse.dto.CategoryResponseDto;
import com.hikaro.warehouse.entity.Category;
import com.hikaro.warehouse.service.CategoryService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryResponseDto> findAll() {
        return categoryService.findAll().stream().map(this::toResponseDto).toList();
    }

    @GetMapping("/{id}")
    public CategoryResponseDto getById(@PathVariable Long id) {
        return toResponseDto(categoryService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponseDto create(@RequestBody CategoryRequestDto request) {
        return toResponseDto(categoryService.create(toEntity(request)));
    }

    @PutMapping("/{id}")
    public CategoryResponseDto update(
            @PathVariable Long id,
            @RequestBody CategoryRequestDto request
    ) {
        return toResponseDto(categoryService.update(id, toEntity(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }

    private Category toEntity(CategoryRequestDto request) {
        return new Category(null, request.name(), request.description());
    }

    private CategoryResponseDto toResponseDto(Category category) {
        return new CategoryResponseDto(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }
}
