package com.hotel.model;

public class POSCharge {
    private int id, reservationId, quantity, chargedBy;
    private String category, description, chargedAt;
    private double amount;
    private String guestName, roomNumber;

    public int getId() { return id; }
    public void setId(int v) { this.id = v; }
    public int getReservationId() { return reservationId; }
    public void setReservationId(int v) { this.reservationId = v; }
    public String getCategory() { return category; }
    public void setCategory(String v) { this.category = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public double getAmount() { return amount; }
    public void setAmount(double v) { this.amount = v; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int v) { this.quantity = v; }
    public String getChargedAt() { return chargedAt; }
    public void setChargedAt(String v) { this.chargedAt = v; }
    public int getChargedBy() { return chargedBy; }
    public void setChargedBy(int v) { this.chargedBy = v; }
    public String getGuestName() { return guestName; }
    public void setGuestName(String v) { this.guestName = v; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String v) { this.roomNumber = v; }
}
