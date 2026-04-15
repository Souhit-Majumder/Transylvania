package com.hotel.dao;

import com.hotel.model.Staff;
import com.hotel.util.DatabaseManager;
import java.sql.*;
import java.util.*;

public class StaffDAO {

    public List<Staff> findAll() {
        List<Staff> list = new ArrayList<>();
        try (Statement s = DatabaseManager.getConnection().createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM staff ORDER BY name")) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Staff> findActive() {
        List<Staff> list = new ArrayList<>();
        try (Statement s = DatabaseManager.getConnection().createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM staff WHERE active=1 ORDER BY name")) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void save(Staff st) {
        try {
            if (st.getId() == 0) {
                PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                    "INSERT INTO staff (name, role, phone, active) VALUES (?,?,?,?)");
                ps.setString(1, st.getName()); ps.setString(2, st.getRole());
                ps.setString(3, st.getPhone()); ps.setInt(4, st.isActive() ? 1 : 0);
                ps.executeUpdate();
            } else {
                PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                    "UPDATE staff SET name=?, role=?, phone=?, active=? WHERE id=?");
                ps.setString(1, st.getName()); ps.setString(2, st.getRole());
                ps.setString(3, st.getPhone()); ps.setInt(4, st.isActive() ? 1 : 0);
                ps.setInt(5, st.getId()); ps.executeUpdate();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void delete(int id) {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                "DELETE FROM staff WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private Staff mapRow(ResultSet rs) throws SQLException {
        Staff st = new Staff();
        st.setId(rs.getInt("id")); st.setName(rs.getString("name"));
        st.setRole(rs.getString("role")); st.setPhone(rs.getString("phone"));
        st.setActive(rs.getInt("active") == 1);
        return st;
    }
}
