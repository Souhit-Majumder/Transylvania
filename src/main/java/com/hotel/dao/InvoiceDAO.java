package com.hotel.dao;

import com.hotel.model.*;
import com.hotel.util.DatabaseManager;
import java.sql.*;
import java.util.*;

public class InvoiceDAO {
    public Invoice findByReservation(int reservationId) {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                "SELECT i.*, g.first_name || ' ' || g.last_name AS guest_name, rm.room_number " +
                        "FROM invoices i " +
                        "JOIN reservations r ON i.reservation_id = r.id " +
                        "JOIN guests g ON r.guest_id = g.id " +
                        "JOIN rooms rm ON r.room_id = rm.id " +
                        "WHERE i.reservation_id=?")) {
            ps.setInt(1, reservationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Invoice inv = mapRow(rs);
                inv.setItems(findItems(inv.getId()));
                return inv;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Invoice> findAll() {
        List<Invoice> list = new ArrayList<>();
        try (Statement s = DatabaseManager.getConnection().createStatement();
                ResultSet rs = s.executeQuery(
                        "SELECT i.*, g.first_name || ' ' || g.last_name AS guest_name, rm.room_number " +
                                "FROM invoices i " +
                                "JOIN reservations r ON i.reservation_id = r.id " +
                                "JOIN guests g ON r.guest_id = g.id " +
                                "JOIN rooms rm ON r.room_id = rm.id " +
                                "ORDER BY i.generated_at DESC")) {
            while (rs.next())
                list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int save(Invoice inv) {
        try {
            if (inv.getId() == 0) {
                PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                        "INSERT INTO invoices (reservation_id,subtotal,tax_rate,tax_amount,total,discount,payment_status,payment_method,paid_amount) VALUES (?,?,?,?,?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, inv.getReservationId());
                ps.setDouble(2, inv.getSubtotal());
                ps.setDouble(3, inv.getTaxRate());
                ps.setDouble(4, inv.getTaxAmount());
                ps.setDouble(5, inv.getTotal());
                ps.setDouble(6, inv.getDiscount());
                ps.setString(7, inv.getPaymentStatus());
                ps.setString(8, inv.getPaymentMethod());
                ps.setDouble(9, inv.getPaidAmount());
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next())
                    return keys.getInt(1);
            } else {
                PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                        "UPDATE invoices SET subtotal=?,tax_rate=?,tax_amount=?,total=?,discount=?,payment_status=?,payment_method=?,paid_amount=? WHERE id=?");
                ps.setDouble(1, inv.getSubtotal());
                ps.setDouble(2, inv.getTaxRate());
                ps.setDouble(3, inv.getTaxAmount());
                ps.setDouble(4, inv.getTotal());
                ps.setDouble(5, inv.getDiscount());
                ps.setString(6, inv.getPaymentStatus());
                ps.setString(7, inv.getPaymentMethod());
                ps.setDouble(8, inv.getPaidAmount());
                ps.setInt(9, inv.getId());
                ps.executeUpdate();
                return inv.getId();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void addItem(InvoiceItem item) {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                "INSERT INTO invoice_items (invoice_id,description,category,quantity,unit_price,total) VALUES (?,?,?,?,?,?)")) {
            ps.setInt(1, item.getInvoiceId());
            ps.setString(2, item.getDescription());
            ps.setString(3, item.getCategory());
            ps.setInt(4, item.getQuantity());
            ps.setDouble(5, item.getUnitPrice());
            ps.setDouble(6, item.getTotal());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<InvoiceItem> findItems(int invoiceId) {
        List<InvoiceItem> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                "SELECT * FROM invoice_items WHERE invoice_id=?")) {
            ps.setInt(1, invoiceId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                InvoiceItem item = new InvoiceItem();
                item.setId(rs.getInt("id"));
                item.setInvoiceId(rs.getInt("invoice_id"));
                item.setDescription(rs.getString("description"));
                item.setCategory(rs.getString("category"));
                item.setQuantity(rs.getInt("quantity"));
                item.setUnitPrice(rs.getDouble("unit_price"));
                item.setTotal(rs.getDouble("total"));
                list.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public double getTotalRevenue() {
        try (Statement s = DatabaseManager.getConnection().createStatement();
                ResultSet rs = s.executeQuery(
                        "SELECT COALESCE(SUM(paid_amount),0) FROM invoices WHERE payment_status IN ('PAID','PARTIAL')")) {
            if (rs.next())
                return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Invoice mapRow(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice();
        inv.setId(rs.getInt("id"));
        inv.setReservationId(rs.getInt("reservation_id"));
        inv.setSubtotal(rs.getDouble("subtotal"));
        inv.setTaxRate(rs.getDouble("tax_rate"));
        inv.setTaxAmount(rs.getDouble("tax_amount"));
        inv.setTotal(rs.getDouble("total"));
        inv.setDiscount(rs.getDouble("discount"));
        inv.setPaidAmount(rs.getDouble("paid_amount"));
        inv.setPaymentStatus(rs.getString("payment_status"));
        inv.setPaymentMethod(rs.getString("payment_method"));
        inv.setGeneratedAt(rs.getString("generated_at"));
        inv.setGuestName(rs.getString("guest_name"));
        inv.setRoomNumber(rs.getString("room_number"));
        return inv;
    }
}
