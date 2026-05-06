package com.hikaro.warehouse.service;

import com.hikaro.warehouse.dto.DispatchItemRequestDto;
import com.hikaro.warehouse.dto.DispatchRequestDto;
import com.hikaro.warehouse.entity.Dispatch;
import com.hikaro.warehouse.entity.DispatchItem;
import com.hikaro.warehouse.entity.DispatchStatus;
import com.hikaro.warehouse.entity.Product;
import com.hikaro.warehouse.entity.Recipient;
import com.hikaro.warehouse.entity.Warehouse;
import com.hikaro.warehouse.exception.ResourceNotFoundException;
import com.hikaro.warehouse.index.ProductQueryIndex;
import com.hikaro.warehouse.repository.DispatchRepository;
import com.hikaro.warehouse.repository.ProductRepository;
import com.hikaro.warehouse.repository.RecipientRepository;
import com.hikaro.warehouse.repository.WarehouseRepository;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DispatchService {

    private final DispatchRepository dispatchRepository;
    private final WarehouseRepository warehouseRepository;
    private final RecipientRepository recipientRepository;
    private final ProductRepository productRepository;
    private final ProductQueryIndex productQueryIndex;

    public DispatchService(
            DispatchRepository dispatchRepository,
            WarehouseRepository warehouseRepository,
            RecipientRepository recipientRepository,
            ProductRepository productRepository,
            ProductQueryIndex productQueryIndex
    ) {
        this.dispatchRepository = dispatchRepository;
        this.warehouseRepository = warehouseRepository;
        this.recipientRepository = recipientRepository;
        this.productRepository = productRepository;
        this.productQueryIndex = productQueryIndex;
    }

    @Transactional(readOnly = true)
    public List<Dispatch> findAll() {
        return dispatchRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Dispatch getById(Long id) {
        return findByIdOrThrow(id);
    }

    @Transactional
    public Dispatch create(DispatchRequestDto request) {
        Dispatch dispatch = new Dispatch();
        applyRequest(dispatch, request);
        dispatch.setStatus(DispatchStatus.DRAFT);
        return dispatchRepository.save(dispatch);
    }

    @Transactional
    public Dispatch update(Long id, DispatchRequestDto request) {
        Dispatch dispatch = findByIdOrThrow(id);
        requireDraft(dispatch, "updated");
        dispatch.getItems().clear();
        applyRequest(dispatch, request);
        return dispatchRepository.save(dispatch);
    }

    @Transactional
    public void delete(Long id) {
        Dispatch dispatch = findByIdOrThrow(id);
        if (dispatch.getStatus() == DispatchStatus.CONFIRMED) {
            throw new IllegalArgumentException("Confirmed dispatches cannot be deleted");
        }
        dispatchRepository.delete(dispatch);
    }

    @Transactional
    public Dispatch confirm(Long id) {
        Dispatch dispatch = findByIdOrThrow(id);
        requireDraft(dispatch, "confirmed");
        validateDispatchItems(dispatch);
        deductProductQuantities(dispatch);
        dispatch.setStatus(DispatchStatus.CONFIRMED);
        Dispatch confirmedDispatch = dispatchRepository.save(dispatch);
        productQueryIndex.invalidate();
        return confirmedDispatch;
    }

    @Transactional
    public Dispatch cancel(Long id) {
        Dispatch dispatch = findByIdOrThrow(id);
        requireDraft(dispatch, "cancelled");
        dispatch.setStatus(DispatchStatus.CANCELLED);
        return dispatchRepository.save(dispatch);
    }

    private Dispatch findByIdOrThrow(Long id) {
        return dispatchRepository.findWithDetailsById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Dispatch with id " + id + " not found"
                        )
                );
    }

    private void applyRequest(Dispatch dispatch, DispatchRequestDto request) {
        Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Warehouse with id " + request.warehouseId() + " not found"
                        )
                );
        Recipient recipient = recipientRepository.findById(request.recipientId())
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Recipient with id " + request.recipientId() + " not found"
                        )
                );

        validateUniqueProducts(request.items());
        Map<Long, Product> productsById = loadProducts(request.items());
        Set<DispatchItem> items = new LinkedHashSet<>();

        for (DispatchItemRequestDto itemRequest : request.items()) {
            Product product = productsById.get(itemRequest.productId());
            validateProductWarehouse(product, warehouse);
            validateAvailableQuantity(product, itemRequest.quantity());

            DispatchItem item = new DispatchItem();
            item.setDispatch(dispatch);
            item.setProduct(product);
            item.setQuantity(itemRequest.quantity());
            items.add(item);
        }

        dispatch.setReferenceNumber(request.referenceNumber());
        dispatch.setWarehouse(warehouse);
        dispatch.setRecipient(recipient);
        dispatch.setItems(items);
    }

    private void deductProductQuantities(Dispatch dispatch) {
        dispatch.getItems().forEach(item -> {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() - item.getQuantity());
        });
    }

    private void validateDispatchItems(Dispatch dispatch) {
        dispatch.getItems().forEach(item -> {
            validateProductWarehouse(item.getProduct(), dispatch.getWarehouse());
            validateAvailableQuantity(item.getProduct(), item.getQuantity());
        });
    }

    private void validateUniqueProducts(List<DispatchItemRequestDto> items) {
        Set<Long> productIds = new LinkedHashSet<>();
        items.forEach(item -> {
            if (!productIds.add(item.productId())) {
                throw new IllegalArgumentException("Dispatch products must be unique");
            }
        });
    }

    private Map<Long, Product> loadProducts(List<DispatchItemRequestDto> items) {
        List<Long> productIds = items.stream()
                .map(DispatchItemRequestDto::productId)
                .toList();
        List<Product> products = productRepository.findAllById(productIds);
        if (products.size() != productIds.size()) {
            throw new ResourceNotFoundException("One or more products not found");
        }

        Map<Long, Product> productsById = new LinkedHashMap<>();
        products.forEach(product -> productsById.put(product.getId(), product));
        return productsById;
    }

    private void validateProductWarehouse(Product product, Warehouse warehouse) {
        if (!product.getWarehouse().getId().equals(warehouse.getId())) {
            throw new IllegalArgumentException(
                    "Product with id "
                            + product.getId()
                            + " does not belong to warehouse with id "
                            + warehouse.getId()
            );
        }
    }

    private void validateAvailableQuantity(Product product, Integer requestedQuantity) {
        if (product.getQuantity() < requestedQuantity) {
            throw new IllegalArgumentException(
                    "Not enough stock for product with id "
                            + product.getId()
                            + ": available "
                            + product.getQuantity()
                            + ", requested "
                            + requestedQuantity
            );
        }
    }

    private void requireDraft(Dispatch dispatch, String action) {
        if (dispatch.getStatus() != DispatchStatus.DRAFT) {
            throw new IllegalArgumentException(
                    "Only draft dispatches can be " + action
            );
        }
    }
}
