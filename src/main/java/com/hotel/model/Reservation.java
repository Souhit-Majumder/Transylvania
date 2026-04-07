package com.hotel.model;

public class Reservation {
    private int id, guestId, roomId, adults, children, createdBy;
    private String checkInDate, checkOutDate, actualCheckIn, actualCheckOut, status, specialRequests, createdAt;
    // joined fields
    private String guestName, roomNumber;

    public int getId() { return id; }
    public void setId(int v) { this.id = v; }
    public int getGuestId() { return guestId; }
    public void setGuestId(int v) { this.guestId = v; }
    public int getRoomId() { return roomId; }
    public void setRoomId(int v) { this.roomId = v; }
    public String getCheckInDate() { return checkInDate; }
    public void setCheckInDate(String v) { this.checkInDate = v; }
    public String getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(String v) { this.checkOutDate = v; }
    public String getActualCheckIn() { return actualCheckIn; }
    public void setActualCheckIn(String v) { this.actualCheckIn = v; }
    public String getActualCheckOut() { return actualCheckOut; }
    public void setActualCheckOut(String v) { this.actualCheckOut = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public int getAdults() { return adults; }
    public void setAdults(int v) { this.adults = v; }
    public int getChildren() { return children; }
    public void setChildren(int v) { this.children = v; }
    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String v) { this.specialRequests = v; }
    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int v) { this.createdBy = v; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String v) { this.createdAt = v; }
    public String getGuestName() { return guestName; }
    public void setGuestName(String v) { this.guestName = v; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String v) { this.roomNumber = v; }
}
