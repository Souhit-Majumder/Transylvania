package com.hotel.util;

import java.sql.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:hotel.db";
    private static Connection connection;

    @SuppressWarnings("exports")
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            connection.createStatement().execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    role TEXT NOT NULL CHECK(role IN ('ADMIN','MANAGER','RECEPTIONIST')),
                    full_name TEXT NOT NULL,
                    active INTEGER DEFAULT 1,
                    created_at TEXT DEFAULT (datetime('now'))
                )""");

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS rooms (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    room_number TEXT UNIQUE NOT NULL,
                    floor INTEGER NOT NULL,
                    type TEXT NOT NULL CHECK(type IN ('SINGLE','DOUBLE','SUITE','DELUXE','PENTHOUSE')),
                    status TEXT NOT NULL DEFAULT 'AVAILABLE'
                        CHECK(status IN ('AVAILABLE','OCCUPIED','RESERVED','MAINTENANCE','NEEDS_CLEANING')),
                    base_price REAL NOT NULL,
                    weekend_price REAL,
                    max_occupancy INTEGER DEFAULT 2,
                    description TEXT
                )""");

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS guests (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    first_name TEXT NOT NULL,
                    last_name TEXT NOT NULL,
                    email TEXT,
                    phone TEXT,
                    id_type TEXT,
                    id_number TEXT,
                    address TEXT,
                    city TEXT,
                    country TEXT,
                    notes TEXT,
                    vip INTEGER DEFAULT 0,
                    created_at TEXT DEFAULT (datetime('now'))
                )""");

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS reservations (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    guest_id INTEGER NOT NULL,
                    room_id INTEGER NOT NULL,
                    check_in_date TEXT NOT NULL,
                    check_out_date TEXT NOT NULL,
                    actual_check_in TEXT,
                    actual_check_out TEXT,
                    status TEXT NOT NULL DEFAULT 'RESERVED'
                        CHECK(status IN ('RESERVED','CHECKED_IN','CHECKED_OUT','CANCELLED','NO_SHOW')),
                    adults INTEGER DEFAULT 1,
                    children INTEGER DEFAULT 0,
                    special_requests TEXT,
                    created_by INTEGER,
                    created_at TEXT DEFAULT (datetime('now')),
                    FOREIGN KEY (guest_id) REFERENCES guests(id),
                    FOREIGN KEY (room_id) REFERENCES rooms(id),
                    FOREIGN KEY (created_by) REFERENCES users(id)
                )""");

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS invoices (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    reservation_id INTEGER NOT NULL,
                    subtotal REAL DEFAULT 0,
                    tax_rate REAL DEFAULT 0.18,
                    tax_amount REAL DEFAULT 0,
                    total REAL DEFAULT 0,
                    discount REAL DEFAULT 0,
                    payment_status TEXT DEFAULT 'PENDING'
                        CHECK(payment_status IN ('PAID','PENDING','PARTIAL','REFUNDED')),
                    payment_method TEXT,
                    paid_amount REAL DEFAULT 0,
                    generated_at TEXT DEFAULT (datetime('now')),
                    FOREIGN KEY (reservation_id) REFERENCES reservations(id)
                )""");

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS invoice_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    invoice_id INTEGER NOT NULL,
                    description TEXT NOT NULL,
                    category TEXT DEFAULT 'ROOM',
                    quantity INTEGER DEFAULT 1,
                    unit_price REAL NOT NULL,
                    total REAL NOT NULL,
                    FOREIGN KEY (invoice_id) REFERENCES invoices(id)
                )""");

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS pos_charges (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    reservation_id INTEGER NOT NULL,
                    category TEXT NOT NULL CHECK(category IN ('RESTAURANT','MINIBAR','SPA','LAUNDRY','OTHER')),
                    description TEXT NOT NULL,
                    amount REAL NOT NULL,
                    quantity INTEGER DEFAULT 1,
                    charged_at TEXT DEFAULT (datetime('now')),
                    charged_by INTEGER,
                    FOREIGN KEY (reservation_id) REFERENCES reservations(id),
                    FOREIGN KEY (charged_by) REFERENCES users(id)
                )""");

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS housekeeping (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    room_id INTEGER NOT NULL,
                    assigned_to TEXT,
                    task_type TEXT DEFAULT 'CLEANING' CHECK(task_type IN ('CLEANING','MAINTENANCE','INSPECTION','DEEP_CLEAN')),
                    priority TEXT DEFAULT 'NORMAL' CHECK(priority IN ('LOW','NORMAL','HIGH','URGENT')),
                    status TEXT DEFAULT 'PENDING' CHECK(status IN ('PENDING','IN_PROGRESS','COMPLETED')),
                    notes TEXT,
                    created_at TEXT DEFAULT (datetime('now')),
                    completed_at TEXT,
                    FOREIGN KEY (room_id) REFERENCES rooms(id)
                )""");

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS activity_log (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER,
                    action TEXT NOT NULL,
                    entity_type TEXT,
                    entity_id INTEGER,
                    details TEXT,
                    timestamp TEXT DEFAULT (datetime('now')),
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )""");

            // Seed default admin
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.executeUpdate("""
                    INSERT INTO users (username, password, role, full_name)
                    VALUES ('admin', 'admin123', 'ADMIN', 'System Administrator')""");
                stmt.executeUpdate("""
                    INSERT INTO users (username, password, role, full_name)
                    VALUES ('reception', 'pass123', 'RECEPTIONIST', 'Front Desk')""");
            }

            // Seed sample rooms if empty
            rs = stmt.executeQuery("SELECT COUNT(*) FROM rooms");
            if (rs.next() && rs.getInt(1) == 0) {
                String[] types = {"SINGLE", "DOUBLE", "SUITE", "DELUXE"};
                double[] prices = {80, 120, 200, 300};
                int roomNum = 100;
                for (int floor = 1; floor <= 4; floor++) {
                    for (int r = 1; r <= 6; r++) {
                        roomNum++;
                        int ti = (floor - 1) % 4;
                        stmt.executeUpdate(String.format(
                            "INSERT INTO rooms (room_number, floor, type, base_price, weekend_price, max_occupancy) " +
                            "VALUES ('%d', %d, '%s', %.2f, %.2f, %d)",
                            roomNum, floor, types[ti], prices[ti], prices[ti] * 1.3, ti + 1));
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void logActivity(int userId, String action, String entityType, int entityId, String details) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "INSERT INTO activity_log (user_id, action, entity_type, entity_id, details) VALUES (?,?,?,?,?)")) {
            ps.setInt(1, userId);
            ps.setString(2, action);
            ps.setString(3, entityType);
            ps.setInt(4, entityId);
            ps.setString(5, details);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
