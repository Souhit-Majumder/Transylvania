package com.hotel.dao;

import com.hotel.model.Room;
import com.hotel.util.DatabaseManager;
import java.sql.*;
import java.util.*;

public class RoomDAO {
    public List<Room> findAll() {
        List<Room> list = new ArrayList<>();
        try (Statement s = DatabaseManager.getConnection().createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM rooms ORDER BY room_number")) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Room findById(int id) {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement("SELECT * FROM rooms WHERE id=?")) {
            ps.setInt(1, id); ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Room> findAvailable(String checkIn, String checkOut, String type) {
        String sql = """
            SELECT * FROM rooms WHERE status IN ('AVAILABLE') 
            AND id NOT IN (
                SELECT room_id FROM reservations 
                WHERE status IN ('RESERVED','CHECKED_IN')
                AND check_in_date < ? AND check_out_date > ?
            )""" + (type != null && !type.isEmpty() ? " AND type=?" : "") + " ORDER BY room_number";
        List<Room> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, checkOut); ps.setString(2, checkIn);
            if (type != null && !type.isEmpty()) ps.setString(3, type);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void save(Room r) {
        try {
            if (r.getId() == 0) {
                PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                    "INSERT INTO rooms (room_number,floor,type,status,base_price,weekend_price,max_occupancy,description) VALUES (?,?,?,?,?,?,?,?)");
                ps.setString(1, r.getRoomNumber()); ps.setInt(2, r.getFloor());
                ps.setString(3, r.getType()); ps.setString(4, r.getStatus());
                ps.setDouble(5, r.getBasePrice()); ps.setDouble(6, r.getWeekendPrice());
                ps.setInt(7, r.getMaxOccupancy()); ps.setString(8, r.getDescription());
                ps.executeUpdate();
            } else {
                PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                    "UPDATE rooms SET room_number=?,floor=?,type=?,status=?,base_price=?,weekend_price=?,max_occupancy=?,description=? WHERE id=?");
                ps.setString(1, r.getRoomNumber()); ps.setInt(2, r.getFloor());
                ps.setString(3, r.getType()); ps.setString(4, r.getStatus());
                ps.setDouble(5, r.getBasePrice()); ps.setDouble(6, r.getWeekendPrice());
                ps.setInt(7, r.getMaxOccupancy()); ps.setString(8, r.getDescription());
                ps.setInt(9, r.getId()); ps.executeUpdate();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void updateStatus(int id, String status) {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement("UPDATE rooms SET status=? WHERE id=?")) {
            ps.setString(1, status); ps.setInt(2, id); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void delete(int id) {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement("DELETE FROM rooms WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public Map<String, Integer> getStatusCounts() {
        Map<String, Integer> map = new LinkedHashMap<>();
        try (Statement s = DatabaseManager.getConnection().createStatement();
             ResultSet rs = s.executeQuery("SELECT status, COUNT(*) as cnt FROM rooms GROUP BY status")) {
            while (rs.next()) map.put(rs.getString("status"), rs.getInt("cnt"));
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    private Room mapRow(ResultSet rs) throws SQLException {
        Room r = new Room();
        r.setId(rs.getInt("id")); r.setRoomNumber(rs.getString("room_number"));
        r.setFloor(rs.getInt("floor")); r.setType(rs.getString("type"));
        r.setStatus(rs.getString("status")); r.setBasePrice(rs.getDouble("base_price"));
        r.setWeekendPrice(rs.getDouble("weekend_price")); r.setMaxOccupancy(rs.getInt("max_occupancy"));
        r.setDescription(rs.getString("description"));
        return r;
    }
}
