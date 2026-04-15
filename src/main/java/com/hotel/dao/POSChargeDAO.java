package com.hotel.dao;

import com.hotel.model.POSCharge;
import com.hotel.util.DatabaseManager;
import java.sql.*;
import java.util.*;

public class POSChargeDAO {
    public List<POSCharge> findByReservation(int reservationId) {
        List<POSCharge> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                "SELECT * FROM pos_charges WHERE reservation_id=? ORDER BY charged_at DESC")) {
            ps.setInt(1, reservationId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<POSCharge> findAll() {
        List<POSCharge> list = new ArrayList<>();
        try (Statement s = DatabaseManager.getConnection().createStatement();
             ResultSet rs = s.executeQuery("""
                SELECT p.*, g.first_name || ' ' || g.last_name AS guest_name, rm.room_number
                FROM pos_charges p
                JOIN reservations r ON p.reservation_id = r.id
                JOIN guests g ON r.guest_id = g.id
                JOIN rooms rm ON r.room_id = rm.id
                ORDER BY p.charged_at DESC""")) {
            while (rs.next()) {
                POSCharge c = mapRow(rs);
                c.setGuestName(rs.getString("guest_name"));
                c.setRoomNumber(rs.getString("room_number"));
                list.add(c);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void save(POSCharge c) {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                "INSERT INTO pos_charges (reservation_id,category,description,amount,quantity,charged_by) VALUES (?,?,?,?,?,?)")) {
            ps.setInt(1, c.getReservationId()); ps.setString(2, c.getCategory());
            ps.setString(3, c.getDescription()); ps.setDouble(4, c.getAmount());
            ps.setInt(5, c.getQuantity()); ps.setInt(6, c.getChargedBy());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void update(POSCharge c) {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                "UPDATE pos_charges SET category=?, description=?, amount=?, quantity=? WHERE id=?")) {
            ps.setString(1, c.getCategory()); ps.setString(2, c.getDescription());
            ps.setDouble(3, c.getAmount()); ps.setInt(4, c.getQuantity());
            ps.setInt(5, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void delete(int id) {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                "DELETE FROM pos_charges WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public double getTotalByReservation(int reservationId) {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                "SELECT COALESCE(SUM(amount * quantity), 0) FROM pos_charges WHERE reservation_id=?")) {
            ps.setInt(1, reservationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private POSCharge mapRow(ResultSet rs) throws SQLException {
        POSCharge c = new POSCharge();
        c.setId(rs.getInt("id")); c.setReservationId(rs.getInt("reservation_id"));
        c.setCategory(rs.getString("category")); c.setDescription(rs.getString("description"));
        c.setAmount(rs.getDouble("amount")); c.setQuantity(rs.getInt("quantity"));
        c.setChargedAt(rs.getString("charged_at")); c.setChargedBy(rs.getInt("charged_by"));
        return c;
    }
}
