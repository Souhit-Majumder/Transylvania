package com.hotel.model;

public class Staff {
    private int id;
    private String name, role, phone;
    private boolean active;

    public int getId() { return id; }
    public void setId(int v) { this.id = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getRole() { return role; }
    public void setRole(String v) { this.role = v; }
    public String getPhone() { return phone; }
    public void setPhone(String v) { this.phone = v; }
    public boolean isActive() { return active; }
    public void setActive(boolean v) { this.active = v; }

    @Override public String toString() { return name + " (" + role + ")"; }
}
