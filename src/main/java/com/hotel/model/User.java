package com.hotel.model;

public class User {
    private int id;
    private String username, password, role, fullName;
    private boolean active;

    public User() {}
    public User(int id, String username, String role, String fullName, boolean active) {
        this.id = id; this.username = username; this.role = role; this.fullName = fullName; this.active = active;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    @Override public String toString() { return fullName + " (" + role + ")"; }
}
