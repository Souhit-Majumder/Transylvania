package com.hotel.dao;

import com.hotel.model.Reservation;
import com.hotel.util.DatabaseManager;
import java.sql.*;
import java.util.*;

public class ReservationDAO {
    private static final String SELECT_JOIN = """
        SELECT r.*, g.first_name || ' ' || g.last_name AS guest_name, rm.room_number
        FROM reservations r
        JOIN guests g ON r.guest_id = g.id
        JOIN rooms rm ON r.room_id = rm.id""";

    public List<Reservation> findAll() {
        List<Reservation> list = new ArrayList<>();
        try (Statement s = DatabaseManager.getConnection().createStatement();
             ResultSet rs = s.executeQuery(SELECT_JOIN + " ORDER BY r.check_in_date DESC")) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Reservation findById(int id) {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(SELECT_JOIN + " WHERE r.id=?")) {
            ps.setInt(1, id); ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Reservation> findByDate(String date) {
        List<Reservation> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                SELECT_JOIN + " WHERE r.check_in_date <= ? AND r.check_out_date > ? AND r.status IN ('RESERVED','CHECKED_IN') ORDER BY r.check_in_date")) {
            ps.setString(1, date); ps.setString(2, date);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Reservation> findTodayCheckIns(String today) {
        List<Reservation> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                SELECT_JOIN + " WHERE r.check_in_date = ? AND r.status = 'RESERVED'")) {
            ps.setString(1, today); ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Reservation> findTodayCheckOuts(String today) {
        List<Reservation> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                SELECT_JOIN + " WHERE r.check_out_date = ? AND r.status = 'CHECKED_IN'")) {
            ps.setString(1, today); ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Reservation> findCheckedIn() {
        List<Reservation> list = new ArrayList<>();
        try (Statement s = DatabaseManager.getConnection().createStatement();
             ResultSet rs = s.executeQuery(SELECT_JOIN + " WHERE r.status = 'CHECKED_IN' ORDER BY r.check_out_date")) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public int save(Reservation r) {
        try {
            if (r.getId() == 0) {
                PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                    "INSERT INTO reservations (guest_id,room_id,check_in_date,check_out_date,status,adults,children,special_requests,created_by) VALUES (?,?,?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, r.getGuestId()); ps.setInt(2, r.getRoomId());
                ps.setString(3, r.getCheckInDate()); ps.setString(4, r.getCheckOutDate());
                ps.setString(5, r.getStatus()); ps.setInt(6, r.getAdults());
                ps.setInt(7, r.getChildren()); ps.setString(8, r.getSpecialRequests());
                ps.setInt(9, r.getCreatedBy()); ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) return keys.getInt(1);
            } else {
                PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                    "UPDATE reservations SET guest_id=?,room_id=?,check_in_date=?,check_out_date=?,status=?,adults=?,children=?,special_requests=?,actual_check_in=?,actual_check_out=? WHERE id=?");
                ps.setInt(1, r.getGuestId()); ps.setInt(2, r.getRoomId());
                ps.setString(3, r.getCheckInDate()); ps.setString(4, r.getCheckOutDate());
                ps.setString(5, r.getStatus()); ps.setInt(6, r.getAdults());
                ps.setInt(7, r.getChildren()); ps.setString(8, r.getSpecialRequests());
                ps.setString(9, r.getActualCheckIn()); ps.setString(10, r.getActualCheckOut());
                ps.setInt(11, r.getId()); ps.executeUpdate();
                return r.getId();
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public void updateStatus(int id, String status) {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement("UPDATE reservations SET status=? WHERE id=?")) {
            ps.setString(1, status); ps.setInt(2, id); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void checkIn(int id) {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                "UPDATE reservations SET status='CHECKED_IN', actual_check_in=datetime('now') WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void checkOut(int id) {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                "UPDATE reservations SET status='CHECKED_OUT', actual_check_out=datetime('now') WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public int countByStatus(String status) {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                "SELECT COUNT(*) FROM reservations WHERE status=?")) {
            ps.setString(1, status); ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public Map<String, Double> getMonthlyRevenue() {
        Map<String, Double> map = new LinkedHashMap<>();
        try (Statement s = DatabaseManager.getConnection().createStatement();
             ResultSet rs = s.executeQuery("""
                SELECT strftime('%Y-%m', i.generated_at) as month, SUM(i.total) as revenue
                FROM invoices i WHERE i.payment_status IN ('PAID','PARTIAL')
                GROUP BY month ORDER BY month DESC LIMIT 12""")) {
            while (rs.next()) map.put(rs.getString("month"), rs.getDouble("revenue"));
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    private Reservation mapRow(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setId(rs.getInt("id")); r.setGuestId(rs.getInt("guest_id"));
        r.setRoomId(rs.getInt("room_id")); r.setCheckInDate(rs.getString("check_in_date"));
        r.setCheckOutDate(rs.getString("check_out_date"));
        r.setActualCheckIn(rs.getString("actual_check_in"));
        r.setActualCheckOut(rs.getString("actual_check_out"));
        r.setStatus(rs.getString("status")); r.setAdults(rs.getInt("adults"));
        r.setChildren(rs.getInt("children")); r.setSpecialRequests(rs.getString("special_requests"));
        r.setCreatedBy(rs.getInt("created_by")); r.setCreatedAt(rs.getString("created_at"));
        r.setGuestName(rs.getString("guest_name")); r.setRoomNumber(rs.getString("room_number"));
        return r;
    }
}
