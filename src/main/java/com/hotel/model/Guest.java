package com.hotel.model;

public class Guest {
    private int id;
    private String firstName, lastName, email, phone, idType, idNumber, address, city, country, notes;
    private boolean vip;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String v) { this.firstName = v; }
    public String getLastName() { return lastName; }
    public void setLastName(String v) { this.lastName = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getPhone() { return phone; }
    public void setPhone(String v) { this.phone = v; }
    public String getIdType() { return idType; }
    public void setIdType(String v) { this.idType = v; }
    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String v) { this.idNumber = v; }
    public String getAddress() { return address; }
    public void setAddress(String v) { this.address = v; }
    public String getCity() { return city; }
    public void setCity(String v) { this.city = v; }
    public String getCountry() { return country; }
    public void setCountry(String v) { this.country = v; }
    public String getNotes() { return notes; }
    public void setNotes(String v) { this.notes = v; }
    public boolean isVip() { return vip; }
    public void setVip(boolean v) { this.vip = v; }
    public String getFullName() { return firstName + " " + lastName; }
    @Override public String toString() { return getFullName(); }
}
