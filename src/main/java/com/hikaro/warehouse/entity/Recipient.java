package com.hikaro.warehouse.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "recipients")
public class Recipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecipientType type;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(length = 255)
    private String address;

    @OneToMany(
            mappedBy = "recipient",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    private Set<Dispatch> dispatches = new LinkedHashSet<>();

    public Recipient() {
    }

    public Recipient(
            Long id,
            String name,
            RecipientType type,
            String contactEmail,
            String address
    ) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.contactEmail = contactEmail;
        this.address = address;
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

    public RecipientType getType() {
        return type;
    }

    public void setType(RecipientType type) {
        this.type = type;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Set<Dispatch> getDispatches() {
        return dispatches;
    }

    public void setDispatches(Set<Dispatch> dispatches) {
        this.dispatches = dispatches;
    }
}
