package com.weintern.inventory.domain;

public enum PoStatus {
    DRAFT("Draft", "draft"),
    ORDERED("Ordered", "ordered"),
    PARTIALLY_RECEIVED("Partially received", "partial"),
    RECEIVED("Received", "done"),
    CANCELLED("Cancelled", "cancelled");

    private final String label;
    private final String css;

    PoStatus(String label, String css) { this.label = label; this.css = css; }

    public String getLabel() { return label; }
    public String getCss() { return css; }

    public boolean isOpen() { return this == ORDERED || this == PARTIALLY_RECEIVED; }
}
