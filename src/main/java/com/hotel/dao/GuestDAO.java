package com.hotel.dao;

import com.hotel.model.Guest;
import com.hotel.util.DatabaseManager;
import java.sql.*;
import java.util.*;

public class GuestDAO {
    public List<Guest> findAll() {
        List<Guest> list = new ArrayList<>();
        try (Statement s = DatabaseManager.getConnection().createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM guests ORDER BY last_name, first_name")) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Guest findById(int id) {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement("SELECT * FROM guests WHERE id=?")) {
            ps.setInt(1, id); ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Guest> search(String query) {
        List<Guest> list = new ArrayList<>();
        String sql = "SELECT * FROM guests WHERE first_name LIKE ? OR last_name LIKE ? OR email LIKE ? OR phone LIKE ? ORDER BY last_name";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            String q = "%" + query + "%";
            ps.setString(1, q); ps.setString(2, q); ps.setString(3, q); ps.setString(4, q);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public int save(Guest g) {
        try {
            if (g.getId() == 0) {
                PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                    "INSERT INTO guests (first_name,last_name,email,phone,id_type,id_number,address,city,country,notes,vip) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
                setParams(ps, g); ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) { g.setId(keys.getInt(1)); return g.getId(); }
            } else {
                PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                    "UPDATE guests SET first_name=?,last_name=?,email=?,phone=?,id_type=?,id_number=?,address=?,city=?,country=?,notes=?,vip=? WHERE id=?");
                setParams(ps, g); ps.setInt(12, g.getId()); ps.executeUpdate();
                return g.getId();
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    private void setParams(PreparedStatement ps, Guest g) throws SQLException {
        ps.setString(1, g.getFirstName()); ps.setString(2, g.getLastName());
        ps.setString(3, g.getEmail()); ps.setString(4, g.getPhone());
        ps.setString(5, g.getIdType()); ps.setString(6, g.getIdNumber());
        ps.setString(7, g.getAddress()); ps.setString(8, g.getCity());
        ps.setString(9, g.getCountry()); ps.setString(10, g.getNotes());
        ps.setInt(11, g.isVip() ? 1 : 0);
    }

    public void delete(int id) {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement("DELETE FROM guests WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private Guest mapRow(ResultSet rs) throws SQLException {
        Guest g = new Guest();
        g.setId(rs.getInt("id")); g.setFirstName(rs.getString("first_name"));
        g.setLastName(rs.getString("last_name")); g.setEmail(rs.getString("email"));
        g.setPhone(rs.getString("phone")); g.setIdType(rs.getString("id_type"));
        g.setIdNumber(rs.getString("id_number")); g.setAddress(rs.getString("address"));
        g.setCity(rs.getString("city")); g.setCountry(rs.getString("country"));
        g.setNotes(rs.getString("notes")); g.setVip(rs.getInt("vip") == 1);
        return g;
    }
}
