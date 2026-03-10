package com.hikaro.warehouse.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hikaro.warehouse.entity.Category;
import com.hikaro.warehouse.entity.Supplier;
import com.hikaro.warehouse.entity.Warehouse;
import com.hikaro.warehouse.exception.ResourceNotFoundException;
import com.hikaro.warehouse.repository.CategoryRepository;
import com.hikaro.warehouse.repository.SupplierRepository;
import com.hikaro.warehouse.repository.WarehouseRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CrudServicesTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private CategoryService categoryService;

    @InjectMocks
    private SupplierService supplierService;

    @InjectMocks
    private WarehouseService warehouseService;

    @Test
    void categoryServiceShouldHandleCrudOperations() {
        Category existing = new Category(1L, "Hardware", "Devices");
        Category updated = new Category(null, "Peripherals", "Updated");

        when(categoryRepository.findAll()).thenReturn(List.of(existing));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(existing)).thenReturn(existing);
        when(categoryRepository.save(updated)).thenReturn(updated);

        assertEquals(List.of(existing), categoryService.findAll());
        assertSame(existing, categoryService.getById(1L));
        assertSame(updated, categoryService.create(updated));

        Category saved = categoryService.update(1L, updated);
        assertSame(existing, saved);
        assertEquals("Peripherals", existing.getName());
        assertEquals("Updated", existing.getDescription());

        categoryService.delete(1L);
        verify(categoryRepository).delete(existing);
    }

    @Test
    void categoryServiceShouldThrowWhenCategoryMissing() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.getById(99L)
        );

        assertEquals("Category with id 99 not found", exception.getMessage());
    }

    @Test
    void supplierServiceShouldHandleCrudOperations() {
        Supplier existing = new Supplier(1L, "ACME", "acme@example.com");
        Supplier updated = new Supplier(null, "Contoso", "contoso@example.com");

        when(supplierRepository.findAll()).thenReturn(List.of(existing));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(supplierRepository.save(existing)).thenReturn(existing);
        when(supplierRepository.save(updated)).thenReturn(updated);

        assertEquals(List.of(existing), supplierService.findAll());
        assertSame(existing, supplierService.getById(1L));
        assertSame(updated, supplierService.create(updated));

        Supplier saved = supplierService.update(1L, updated);
        assertSame(existing, saved);
        assertEquals("Contoso", existing.getName());
        assertEquals("contoso@example.com", existing.getContactEmail());

        supplierService.delete(1L);
        verify(supplierRepository).delete(existing);
    }

    @Test
    void supplierServiceShouldThrowWhenSupplierMissing() {
        when(supplierRepository.findById(77L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> supplierService.getById(77L)
        );

        assertEquals("Supplier with id 77 not found", exception.getMessage());
    }

    @Test
    void warehouseServiceShouldHandleCrudOperations() {
        Warehouse existing = new Warehouse(1L, "Main", "Street 1");
        Warehouse updated = new Warehouse(null, "Backup", "Street 2");

        when(warehouseRepository.findAll()).thenReturn(List.of(existing));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(warehouseRepository.save(existing)).thenReturn(existing);
        when(warehouseRepository.save(updated)).thenReturn(updated);

        assertEquals(List.of(existing), warehouseService.findAll());
        assertSame(existing, warehouseService.getById(1L));
        assertSame(updated, warehouseService.create(updated));

        Warehouse saved = warehouseService.update(1L, updated);
        assertSame(existing, saved);
        assertEquals("Backup", existing.getName());
        assertEquals("Street 2", existing.getAddress());

        warehouseService.delete(1L);
        verify(warehouseRepository).delete(existing);
    }

    @Test
    void warehouseServiceShouldThrowWhenWarehouseMissing() {
        when(warehouseRepository.findById(55L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> warehouseService.getById(55L)
        );

        assertEquals("Warehouse with id 55 not found", exception.getMessage());
    }
}
