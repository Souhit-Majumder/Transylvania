package com.hotel.model;

public class Room {
    private int id, floor, maxOccupancy;
    private String roomNumber, type, status, description;
    private double basePrice, weekendPrice;

    public Room() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }
    public double getWeekendPrice() { return weekendPrice; }
    public void setWeekendPrice(double weekendPrice) { this.weekendPrice = weekendPrice; }
    public int getMaxOccupancy() { return maxOccupancy; }
    public void setMaxOccupancy(int maxOccupancy) { this.maxOccupancy = maxOccupancy; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    @Override public String toString() { return roomNumber + " (" + type + ")"; }
}
