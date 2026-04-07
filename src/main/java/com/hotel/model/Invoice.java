package com.hotel.model;

import java.util.ArrayList;
import java.util.List;

public class Invoice {
    private int id, reservationId;
    private double subtotal, taxRate, taxAmount, total, discount, paidAmount;
    private String paymentStatus, paymentMethod, generatedAt;
    private String guestName, roomNumber;
    private List<InvoiceItem> items = new ArrayList<>();

    public int getId() { return id; }
    public void setId(int v) { this.id = v; }
    public int getReservationId() { return reservationId; }
    public void setReservationId(int v) { this.reservationId = v; }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double v) { this.subtotal = v; }
    public double getTaxRate() { return taxRate; }
    public void setTaxRate(double v) { this.taxRate = v; }
    public double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(double v) { this.taxAmount = v; }
    public double getTotal() { return total; }
    public void setTotal(double v) { this.total = v; }
    public double getDiscount() { return discount; }
    public void setDiscount(double v) { this.discount = v; }
    public double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(double v) { this.paidAmount = v; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String v) { this.paymentStatus = v; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String v) { this.paymentMethod = v; }
    public String getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(String v) { this.generatedAt = v; }
    public String getGuestName() { return guestName; }
    public void setGuestName(String v) { this.guestName = v; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String v) { this.roomNumber = v; }
    public List<InvoiceItem> getItems() { return items; }
    public void setItems(List<InvoiceItem> v) { this.items = v; }
}
