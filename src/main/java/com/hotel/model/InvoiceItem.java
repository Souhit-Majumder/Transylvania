package com.hotel.model;

public class InvoiceItem {
    private int id, invoiceId, quantity;
    private String description, category;
    private double unitPrice, total;

    public int getId() { return id; }
    public void setId(int v) { this.id = v; }
    public int getInvoiceId() { return invoiceId; }
    public void setInvoiceId(int v) { this.invoiceId = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public String getCategory() { return category; }
    public void setCategory(String v) { this.category = v; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int v) { this.quantity = v; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double v) { this.unitPrice = v; }
    public double getTotal() { return total; }
    public void setTotal(double v) { this.total = v; }
}
