package com.hikaro.warehouse.dto;

public class ProductResponseDto {

    private Long id;
    private String name;
    private Integer quantity;
    private String location;

    public ProductResponseDto() {
    }

    public ProductResponseDto(Long id, String name, Integer quantity, String location) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.location = location;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
