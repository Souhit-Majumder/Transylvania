package com.hotel.model;

public class HousekeepingTask {
    private int id, roomId;
    private String assignedTo, taskType, priority, status, notes, createdAt, completedAt;
    private String roomNumber;

    public int getId() { return id; }
    public void setId(int v) { this.id = v; }
    public int getRoomId() { return roomId; }
    public void setRoomId(int v) { this.roomId = v; }
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String v) { this.assignedTo = v; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String v) { this.taskType = v; }
    public String getPriority() { return priority; }
    public void setPriority(String v) { this.priority = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public String getNotes() { return notes; }
    public void setNotes(String v) { this.notes = v; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String v) { this.createdAt = v; }
    public String getCompletedAt() { return completedAt; }
    public void setCompletedAt(String v) { this.completedAt = v; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String v) { this.roomNumber = v; }
}
