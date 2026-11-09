package com.weintern.inventory.domain;

public enum Role {
    ADMIN("Administrator"),
    INVENTORY_STAFF("Inventory staff"),
    PURCHASING_STAFF("Purchasing staff");

    private final String label;

    Role(String label) { this.label = label; }

    public String getLabel() { return label; }
}
