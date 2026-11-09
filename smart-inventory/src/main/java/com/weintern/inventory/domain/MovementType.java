package com.weintern.inventory.domain;

public enum MovementType {
    STOCK_IN("Stock in", "in"),
    STOCK_OUT("Stock out", "out"),
    ADJUSTMENT("Adjustment", "adj"),
    PO_RECEIPT("PO receipt", "in");

    private final String label;
    private final String css;

    MovementType(String label, String css) { this.label = label; this.css = css; }

    public String getLabel() { return label; }
    public String getCss() { return css; }
}
