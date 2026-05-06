package com.hikaro.warehouse.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hikaro.warehouse.dto.DispatchItemRequestDto;
import com.hikaro.warehouse.dto.DispatchRequestDto;
import com.hikaro.warehouse.entity.Dispatch;
import com.hikaro.warehouse.entity.DispatchItem;
import com.hikaro.warehouse.entity.DispatchStatus;
import com.hikaro.warehouse.entity.Product;
import com.hikaro.warehouse.entity.Recipient;
import com.hikaro.warehouse.entity.RecipientType;
import com.hikaro.warehouse.entity.Warehouse;
import com.hikaro.warehouse.exception.ResourceNotFoundException;
import com.hikaro.warehouse.index.ProductQueryIndex;
import com.hikaro.warehouse.repository.DispatchRepository;
import com.hikaro.warehouse.repository.ProductRepository;
import com.hikaro.warehouse.repository.RecipientRepository;
import com.hikaro.warehouse.repository.WarehouseRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DispatchServiceTest {

    @Mock
    private DispatchRepository dispatchRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private RecipientRepository recipientRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductQueryIndex productQueryIndex;

    @InjectMocks
    private DispatchService dispatchService;

    @Test
    void shouldCreateDraftDispatchWithoutReducingProductQuantities() {
        Warehouse warehouse = new Warehouse(1L, "North Hub", "Market Street");
        Recipient recipient = new Recipient(
                2L,
                "ООО ТехМаркет",
                RecipientType.COMPANY,
                "orders@example.com",
                "Commerce street"
        );
        Product laptop = buildProduct(10L, "SKU-10", "Laptop", 12, warehouse);
        Product phone = buildProduct(11L, "SKU-11", "Phone", 9, warehouse);
        DispatchRequestDto request = new DispatchRequestDto(
                "OUT-100",
                1L,
                2L,
                List.of(
                        new DispatchItemRequestDto(10L, 4),
                        new DispatchItemRequestDto(11L, 3)
                )
        );

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(recipientRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(productRepository.findAllById(List.of(10L, 11L)))
                .thenReturn(List.of(laptop, phone));
        when(dispatchRepository.save(any(Dispatch.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Dispatch created = dispatchService.create(request);

        assertEquals("OUT-100", created.getReferenceNumber());
        assertSame(warehouse, created.getWarehouse());
        assertSame(recipient, created.getRecipient());
        assertEquals(DispatchStatus.DRAFT, created.getStatus());
        assertEquals(2, created.getItems().size());
        assertEquals(12, laptop.getQuantity());
        assertEquals(9, phone.getQuantity());
    }

    @Test
    void shouldUpdateDraftDispatchWithoutChangingStock() {
        Warehouse warehouse = new Warehouse(1L, "North Hub", "Market Street");
        Recipient firstRecipient = new Recipient(
                2L,
                "ООО ТехМаркет",
                RecipientType.COMPANY,
                null,
                null
        );
        Recipient secondRecipient = new Recipient(
                3L,
                "Store 24",
                RecipientType.STORE,
                null,
                null
        );
        Product laptop = buildProduct(10L, "SKU-10", "Laptop", 8, warehouse);
        Product phone = buildProduct(11L, "SKU-11", "Phone", 6, warehouse);

        Dispatch existing = new Dispatch(50L, "OUT-050");
        existing.setWarehouse(warehouse);
        existing.setRecipient(firstRecipient);
        existing.setItems(new LinkedHashSet<>(List.of(
                buildDispatchItem(existing, laptop, 4),
                buildDispatchItem(existing, phone, 3)
        )));

        DispatchRequestDto request = new DispatchRequestDto(
                "OUT-051",
                1L,
                3L,
                List.of(
                        new DispatchItemRequestDto(10L, 2),
                        new DispatchItemRequestDto(11L, 5)
                )
        );

        when(dispatchRepository.findWithDetailsById(50L)).thenReturn(Optional.of(existing));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(recipientRepository.findById(3L)).thenReturn(Optional.of(secondRecipient));
        when(productRepository.findAllById(List.of(10L, 11L)))
                .thenReturn(List.of(laptop, phone));
        when(dispatchRepository.save(existing)).thenReturn(existing);

        Dispatch updated = dispatchService.update(50L, request);

        assertSame(existing, updated);
        assertEquals("OUT-051", updated.getReferenceNumber());
        assertSame(secondRecipient, updated.getRecipient());
        assertEquals(8, laptop.getQuantity());
        assertEquals(6, phone.getQuantity());
        assertEquals(2, updated.getItems().size());
    }

    @Test
    void shouldConfirmDraftDispatchAndReduceStock() {
        Warehouse warehouse = new Warehouse(1L, "North Hub", "Market Street");
        Product laptop = buildProduct(10L, "SKU-10", "Laptop", 5, warehouse);
        Product phone = buildProduct(11L, "SKU-11", "Phone", 6, warehouse);
        Dispatch existing = new Dispatch(50L, "OUT-050");
        existing.setWarehouse(warehouse);
        existing.setRecipient(new Recipient(3L, "Store 24", RecipientType.STORE, null, null));
        existing.setItems(new LinkedHashSet<>(List.of(
                buildDispatchItem(existing, laptop, 4),
                buildDispatchItem(existing, phone, 2)
        )));

        when(dispatchRepository.findWithDetailsById(50L)).thenReturn(Optional.of(existing));
        when(dispatchRepository.save(existing)).thenReturn(existing);

        Dispatch confirmed = dispatchService.confirm(50L);

        assertSame(existing, confirmed);
        assertEquals(DispatchStatus.CONFIRMED, confirmed.getStatus());
        assertEquals(1, laptop.getQuantity());
        assertEquals(4, phone.getQuantity());
        verify(productQueryIndex).invalidate();
    }

    @Test
    void shouldCancelDraftDispatchWithoutReducingStock() {
        Warehouse warehouse = new Warehouse(1L, "North Hub", "Market Street");
        Product laptop = buildProduct(10L, "SKU-10", "Laptop", 5, warehouse);
        Dispatch existing = new Dispatch(50L, "OUT-050");
        existing.setWarehouse(warehouse);
        existing.setRecipient(new Recipient(3L, "Store 24", RecipientType.STORE, null, null));
        existing.setItems(new LinkedHashSet<>(List.of(
                buildDispatchItem(existing, laptop, 4)
        )));

        when(dispatchRepository.findWithDetailsById(50L)).thenReturn(Optional.of(existing));
        when(dispatchRepository.save(existing)).thenReturn(existing);

        Dispatch cancelled = dispatchService.cancel(50L);

        assertSame(existing, cancelled);
        assertEquals(DispatchStatus.CANCELLED, cancelled.getStatus());
        assertEquals(5, laptop.getQuantity());
        verify(productQueryIndex, never()).invalidate();
    }

    @Test
    void shouldDeleteDraftDispatch() {
        Warehouse warehouse = new Warehouse(1L, "North Hub", "Market Street");
        Dispatch existing = new Dispatch(50L, "OUT-050");
        existing.setWarehouse(warehouse);
        existing.setRecipient(new Recipient(3L, "Store 24", RecipientType.STORE, null, null));

        when(dispatchRepository.findWithDetailsById(50L)).thenReturn(Optional.of(existing));

        dispatchService.delete(50L);

        verify(dispatchRepository).delete(existing);
    }

    @Test
    void shouldThrowWhenStockIsInsufficientDuringDraftCreation() {
        Warehouse warehouse = new Warehouse(1L, "North Hub", "Market Street");
        Recipient recipient = new Recipient(2L, "ООО ТехМаркет", RecipientType.COMPANY, null, null);
        Product laptop = buildProduct(10L, "SKU-10", "Laptop", 2, warehouse);
        DispatchRequestDto request = new DispatchRequestDto(
                "OUT-100",
                1L,
                2L,
                List.of(new DispatchItemRequestDto(10L, 4))
        );

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(recipientRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(productRepository.findAllById(List.of(10L))).thenReturn(List.of(laptop));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dispatchService.create(request)
        );

        assertEquals(
                "Not enough stock for product with id 10: available 2, requested 4",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowWhenStockIsInsufficientDuringConfirmation() {
        Warehouse warehouse = new Warehouse(1L, "North Hub", "Market Street");
        Product laptop = buildProduct(10L, "SKU-10", "Laptop", 2, warehouse);
        Dispatch existing = new Dispatch(50L, "OUT-050");
        existing.setWarehouse(warehouse);
        existing.setRecipient(new Recipient(3L, "Store 24", RecipientType.STORE, null, null));
        existing.setItems(new LinkedHashSet<>(List.of(
                buildDispatchItem(existing, laptop, 4)
        )));

        when(dispatchRepository.findWithDetailsById(50L)).thenReturn(Optional.of(existing));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dispatchService.confirm(50L)
        );

        assertEquals(
                "Not enough stock for product with id 10: available 2, requested 4",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowWhenProductBelongsToAnotherWarehouse() {
        Warehouse dispatchWarehouse = new Warehouse(1L, "North Hub", "Market Street");
        Warehouse productWarehouse = new Warehouse(3L, "West Hub", "Warehouse Ave");
        Recipient recipient = new Recipient(2L, "ООО ТехМаркет", RecipientType.COMPANY, null, null);
        Product laptop = buildProduct(10L, "SKU-10", "Laptop", 7, productWarehouse);
        DispatchRequestDto request = new DispatchRequestDto(
                "OUT-100",
                1L,
                2L,
                List.of(new DispatchItemRequestDto(10L, 1))
        );

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(dispatchWarehouse));
        when(recipientRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(productRepository.findAllById(List.of(10L))).thenReturn(List.of(laptop));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dispatchService.create(request)
        );

        assertEquals(
                "Product with id 10 does not belong to warehouse with id 1",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowWhenDispatchMissing() {
        when(dispatchRepository.findWithDetailsById(77L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> dispatchService.getById(77L)
        );

        assertEquals("Dispatch with id 77 not found", exception.getMessage());
    }

    @Test
    void shouldThrowWhenUpdatingConfirmedDispatch() {
        Dispatch dispatch = new Dispatch(77L, "OUT-077");
        dispatch.setStatus(DispatchStatus.CONFIRMED);

        when(dispatchRepository.findWithDetailsById(77L)).thenReturn(Optional.of(dispatch));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dispatchService.update(
                        77L,
                        new DispatchRequestDto("OUT-077", 1L, 2L, List.of())
                )
        );

        assertEquals("Only draft dispatches can be updated", exception.getMessage());
    }

    @Test
    void shouldThrowWhenDeletingConfirmedDispatch() {
        Dispatch dispatch = new Dispatch(77L, "OUT-077");
        dispatch.setStatus(DispatchStatus.CONFIRMED);

        when(dispatchRepository.findWithDetailsById(77L)).thenReturn(Optional.of(dispatch));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dispatchService.delete(77L)
        );

        assertEquals("Confirmed dispatches cannot be deleted", exception.getMessage());
    }

    @Test
    void shouldThrowWhenCancellingConfirmedDispatch() {
        Dispatch dispatch = new Dispatch(77L, "OUT-077");
        dispatch.setStatus(DispatchStatus.CONFIRMED);

        when(dispatchRepository.findWithDetailsById(77L)).thenReturn(Optional.of(dispatch));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dispatchService.cancel(77L)
        );

        assertEquals("Only draft dispatches can be cancelled", exception.getMessage());
    }

    private Product buildProduct(
            Long id,
            String sku,
            String name,
            Integer quantity,
            Warehouse warehouse
    ) {
        Product product = new Product(id, sku, name, quantity);
        product.setWarehouse(warehouse);
        return product;
    }

    private DispatchItem buildDispatchItem(
            Dispatch dispatch,
            Product product,
            Integer quantity
    ) {
        DispatchItem item = new DispatchItem();
        item.setDispatch(dispatch);
        item.setProduct(product);
        item.setQuantity(quantity);
        return item;
    }
}
