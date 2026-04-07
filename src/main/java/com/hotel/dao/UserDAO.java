package com.hotel.dao;

import com.hotel.model.User;
import com.hotel.util.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    public User authenticate(String username, String password) {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                "SELECT * FROM users WHERE username=? AND password=? AND active=1")) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        try (Statement s = DatabaseManager.getConnection().createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM users ORDER BY full_name")) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void save(User u) {
        try {
            if (u.getId() == 0) {
                PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                    "INSERT INTO users (username,password,role,full_name) VALUES (?,?,?,?)");
                ps.setString(1, u.getUsername()); ps.setString(2, u.getPassword());
                ps.setString(3, u.getRole()); ps.setString(4, u.getFullName());
                ps.executeUpdate();
            } else {
                PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                    "UPDATE users SET username=?,password=?,role=?,full_name=?,active=? WHERE id=?");
                ps.setString(1, u.getUsername()); ps.setString(2, u.getPassword());
                ps.setString(3, u.getRole()); ps.setString(4, u.getFullName());
                ps.setInt(5, u.isActive() ? 1 : 0); ps.setInt(6, u.getId());
                ps.executeUpdate();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void delete(int id) {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement("DELETE FROM users WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id")); u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password")); u.setRole(rs.getString("role"));
        u.setFullName(rs.getString("full_name")); u.setActive(rs.getInt("active") == 1);
        return u;
    }
}
