package com.hotel.dao;

import com.hotel.model.HousekeepingTask;
import com.hotel.util.DatabaseManager;
import java.sql.*;
import java.util.*;

public class HousekeepingDAO {
    public List<HousekeepingTask> findAll() {
        List<HousekeepingTask> list = new ArrayList<>();
        try (Statement s = DatabaseManager.getConnection().createStatement();
             ResultSet rs = s.executeQuery("""
                SELECT h.*, r.room_number FROM housekeeping h
                JOIN rooms r ON h.room_id = r.id
                ORDER BY CASE h.priority WHEN 'URGENT' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'NORMAL' THEN 3 ELSE 4 END, h.created_at DESC""")) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<HousekeepingTask> findPending() {
        List<HousekeepingTask> list = new ArrayList<>();
        try (Statement s = DatabaseManager.getConnection().createStatement();
             ResultSet rs = s.executeQuery("""
                SELECT h.*, r.room_number FROM housekeeping h
                JOIN rooms r ON h.room_id = r.id
                WHERE h.status IN ('PENDING','IN_PROGRESS')
                ORDER BY CASE h.priority WHEN 'URGENT' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'NORMAL' THEN 3 ELSE 4 END""")) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void save(HousekeepingTask t) {
        try {
            if (t.getId() == 0) {
                PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                    "INSERT INTO housekeeping (room_id,assigned_to,task_type,priority,status,notes) VALUES (?,?,?,?,?,?)");
                ps.setInt(1, t.getRoomId()); ps.setString(2, t.getAssignedTo());
                ps.setString(3, t.getTaskType()); ps.setString(4, t.getPriority());
                ps.setString(5, t.getStatus()); ps.setString(6, t.getNotes());
                ps.executeUpdate();
            } else {
                PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                    "UPDATE housekeeping SET room_id=?,assigned_to=?,task_type=?,priority=?,status=?,notes=?,completed_at=? WHERE id=?");
                ps.setInt(1, t.getRoomId()); ps.setString(2, t.getAssignedTo());
                ps.setString(3, t.getTaskType()); ps.setString(4, t.getPriority());
                ps.setString(5, t.getStatus()); ps.setString(6, t.getNotes());
                ps.setString(7, t.getCompletedAt()); ps.setInt(8, t.getId());
                ps.executeUpdate();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void markComplete(int id) {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                "UPDATE housekeeping SET status='COMPLETED', completed_at=datetime('now') WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /**
     * Marks all PENDING or IN_PROGRESS tasks for a specific room as COMPLETED.
     * Called when a room's status is manually cleared (e.g. NEEDS_CLEANING → AVAILABLE)
     * so the housekeeping queue stays in sync with the floor plan.
     */
    public void markCompleteByRoom(int roomId) {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                "UPDATE housekeeping SET status='COMPLETED', completed_at=datetime('now') " +
                "WHERE room_id=? AND status IN ('PENDING','IN_PROGRESS')")) {
            ps.setInt(1, roomId); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private HousekeepingTask mapRow(ResultSet rs) throws SQLException {
        HousekeepingTask t = new HousekeepingTask();
        t.setId(rs.getInt("id")); t.setRoomId(rs.getInt("room_id"));
        t.setAssignedTo(rs.getString("assigned_to")); t.setTaskType(rs.getString("task_type"));
        t.setPriority(rs.getString("priority")); t.setStatus(rs.getString("status"));
        t.setNotes(rs.getString("notes")); t.setCreatedAt(rs.getString("created_at"));
        t.setCompletedAt(rs.getString("completed_at")); t.setRoomNumber(rs.getString("room_number"));
        return t;
    }
}
