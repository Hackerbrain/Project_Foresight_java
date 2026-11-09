package com.foresight.inventory.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "stock_locations")
public class StockLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String type; // MAIN_WAREHOUSE, REGIONAL_HUB, RETAIL_STORE

    @Column(length = 255)
    private String address;

    @Column(nullable = false)
    private boolean active = true;

    public StockLocation() {}

    public StockLocation(String code, String name, String type, String address) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.address = address;
        this.active = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
