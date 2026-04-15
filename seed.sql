-- 1. CLEAR EXISTING DATA & RESET IDs
-- (We intentionally do NOT delete from 'users' so you don't get locked out)
DELETE FROM invoice_items;
DELETE FROM invoices;
DELETE FROM pos_charges;
DELETE FROM housekeeping;
DELETE FROM activity_log;
DELETE FROM reservations;
DELETE FROM guests;
DELETE FROM rooms;
DELETE FROM staff;

-- Reset the Auto-Increment counters so IDs start at 1 again
DELETE FROM sqlite_sequence WHERE name IN ('invoice_items', 'invoices', 'pos_charges', 'housekeeping', 'activity_log', 'reservations', 'guests', 'rooms', 'staff');

-- ==========================================
-- 2. INSERT 25 ROOMS (5 Floors, 5 Types)
-- ==========================================
INSERT INTO rooms (room_number, floor, type, status, base_price, weekend_price, max_occupancy) VALUES
-- Floor 1: Singles
('101', 1, 'SINGLE', 'OCCUPIED', 100.00, 120.00, 1),
('102', 1, 'SINGLE', 'AVAILABLE', 100.00, 120.00, 1),
('103', 1, 'SINGLE', 'AVAILABLE', 100.00, 120.00, 1),
('104', 1, 'SINGLE', 'AVAILABLE', 100.00, 120.00, 1),
('105', 1, 'SINGLE', 'AVAILABLE', 100.00, 120.00, 1),
-- Floor 2: Doubles
('201', 2, 'DOUBLE', 'AVAILABLE', 150.00, 180.00, 2),
('202', 2, 'DOUBLE', 'OCCUPIED', 150.00, 180.00, 2),
('203', 2, 'DOUBLE', 'AVAILABLE', 150.00, 180.00, 2),
('204', 2, 'DOUBLE', 'AVAILABLE', 150.00, 180.00, 2),
('205', 2, 'DOUBLE', 'AVAILABLE', 150.00, 180.00, 2),
-- Floor 3: Deluxe
('301', 3, 'DELUXE', 'AVAILABLE', 200.00, 240.00, 3),
('302', 3, 'DELUXE', 'AVAILABLE', 200.00, 240.00, 3),
('303', 3, 'DELUXE', 'RESERVED', 200.00, 240.00, 3),
('304', 3, 'DELUXE', 'AVAILABLE', 200.00, 240.00, 3),
('305', 3, 'DELUXE', 'AVAILABLE', 200.00, 240.00, 3),
-- Floor 4: Suites
('401', 4, 'SUITE', 'AVAILABLE', 300.00, 350.00, 4),
('402', 4, 'SUITE', 'AVAILABLE', 300.00, 350.00, 4),
('403', 4, 'SUITE', 'AVAILABLE', 300.00, 350.00, 4),
('404', 4, 'SUITE', 'NEEDS_CLEANING', 300.00, 350.00, 4),
('405', 4, 'SUITE', 'AVAILABLE', 300.00, 350.00, 4),
-- Floor 5: Penthouses
('501', 5, 'PENTHOUSE', 'AVAILABLE', 500.00, 600.00, 4),
('502', 5, 'PENTHOUSE', 'AVAILABLE', 500.00, 600.00, 4),
('503', 5, 'PENTHOUSE', 'AVAILABLE', 500.00, 600.00, 4),
('504', 5, 'PENTHOUSE', 'AVAILABLE', 500.00, 600.00, 4),
('505', 5, 'PENTHOUSE', 'MAINTENANCE', 500.00, 600.00, 4);

-- ==========================================
-- 3. INSERT 15 GUESTS
-- ==========================================
INSERT INTO guests (first_name, last_name, email, phone, id_type, id_number, city, country, vip) VALUES 
('Aarav', 'Sharma', 'aarav@example.in', '+91-9876543210', 'PASSPORT', 'P1234567', 'Bengaluru', 'India', 1),
('Priya', 'Patel', 'priya@example.in', '+91-9123456789', 'AADHAAR', '123456789012', 'Mumbai', 'India', 0),
('James', 'Smith', 'jsmith@example.com', '+1-555-0101', 'PASSPORT', 'US987654', 'New York', 'USA', 0),
('Emma', 'Johnson', 'emma@example.co.uk', '+44-7700-900123', 'PASSPORT', 'UK123456', 'London', 'UK', 1),
('Yuki', 'Tanaka', 'yuki@example.jp', '+81-90-1234-5678', 'PASSPORT', 'JP456789', 'Tokyo', 'Japan', 0),
('Carlos', 'Garcia', 'carlos@example.es', '+34-600-123456', 'ID_CARD', 'ES112233', 'Madrid', 'Spain', 0),
('Marie', 'Dubois', 'marie@example.fr', '+33-6-12345678', 'PASSPORT', 'FR998877', 'Paris', 'France', 0),
('Lars', 'Jensen', 'lars@example.dk', '+45-20-123456', 'PASSPORT', 'DK554433', 'Copenhagen', 'Denmark', 0),
('Chen', 'Wei', 'chen@example.cn', '+86-139-12345678', 'PASSPORT', 'CN112233', 'Shanghai', 'China', 1),
('Sophia', 'Martinez', 'sophia@example.mx', '+52-55-12345678', 'ID_CARD', 'MX998877', 'Mexico City', 'Mexico', 0),
('Ali', 'Hassan', 'ali@example.ae', '+971-50-1234567', 'PASSPORT', 'AE334455', 'Dubai', 'UAE', 1),
('Olivia', 'Brown', 'olivia@example.ca', '+1-416-555-0199', 'PASSPORT', 'CA776655', 'Toronto', 'Canada', 0),
('Rohan', 'Desai', 'rohan@example.in', '+91-9988776655', 'AADHAAR', '987654321098', 'Pune', 'India', 0),
('Isabella', 'Rossi', 'isabella@example.it', '+39-333-1234567', 'ID_CARD', 'IT223344', 'Rome', 'Italy', 1),
('Liam', 'O''Connor', 'liam@example.ie', '+353-87-1234567', 'PASSPORT', 'IE556677', 'Dublin', 'Ireland', 0);

-- ==========================================
-- 4. INSERT HOUSE STAFF
-- ==========================================
INSERT INTO staff (name, role, phone, active) VALUES
('Maria', 'HOUSEKEEPER', '+1-555-0201', 1),
('David', 'MAINTENANCE', '+1-555-0202', 1);


-- ==========================================
-- 5. INSERT RESERVATIONS (Strictly linked to Room Status)
-- ==========================================
-- Res 1: Matches Room 101 (OCCUPIED)
INSERT INTO reservations (guest_id, room_id, check_in_date, check_out_date, actual_check_in, status, adults) VALUES 
(1, 1, '2026-04-08', '2026-04-12', '2026-04-08 14:30:00', 'CHECKED_IN', 1);

-- Res 2: Matches Room 202 (OCCUPIED)
INSERT INTO reservations (guest_id, room_id, check_in_date, check_out_date, actual_check_in, status, adults) VALUES 
(2, 7, '2026-04-10', '2026-04-15', '2026-04-10 09:15:00', 'CHECKED_IN', 2);

-- Res 3: Matches Room 303 (RESERVED)
INSERT INTO reservations (guest_id, room_id, check_in_date, check_out_date, status, adults) VALUES 
(3, 13, '2026-04-15', '2026-04-18', 'RESERVED', 2);

-- Res 4: Matches Room 404 (NEEDS_CLEANING) - Guest just checked out
INSERT INTO reservations (guest_id, room_id, check_in_date, check_out_date, actual_check_in, actual_check_out, status, adults) VALUES 
(4, 19, '2026-04-05', '2026-04-09', '2026-04-05 15:00:00', '2026-04-09 10:30:00', 'CHECKED_OUT', 2);

-- Res 5: Cancelled Reservation (Room 105 is safely AVAILABLE)
INSERT INTO reservations (guest_id, room_id, check_in_date, check_out_date, status, adults) VALUES 
(5, 5, '2026-04-01', '2026-04-03', 'CANCELLED', 1);

-- ==========================================
-- 6. INSERT POS CHARGES
-- ==========================================
-- Charges for active guest in Room 101
INSERT INTO pos_charges (reservation_id, category, description, amount, quantity) VALUES 
(1, 'RESTAURANT', 'Room Service - Breakfast', 25.50, 1),
(1, 'SPA', 'Swedish Massage', 80.00, 1);

-- Charges for checked out guest in Room 404
INSERT INTO pos_charges (reservation_id, category, description, amount, quantity) VALUES 
(4, 'MINIBAR', 'Water & Snacks', 15.00, 1),
(4, 'LAUNDRY', 'Dry Cleaning', 30.00, 1);

-- ==========================================
-- 7. INSERT HOUSEKEEPING TASKS
-- ==========================================
-- Task for Room 404 (NEEDS_CLEANING)
INSERT INTO housekeeping (room_id, assigned_to, task_type, priority, status, notes) VALUES 
(19, 'Maria', 'CLEANING', 'HIGH', 'PENDING', 'Guest checked out, deep clean required');

-- Task for Room 505 (MAINTENANCE)
INSERT INTO housekeeping (room_id, assigned_to, task_type, priority, status, notes) VALUES 
(25, 'David', 'MAINTENANCE', 'URGENT', 'IN_PROGRESS', 'AC unit leaking water onto carpet');

-- ==========================================
-- 8. GENERATE INVOICE (For the Checked-Out Guest)
-- ==========================================
-- Guest in Room 404 (Suite, $300/night for 4 nights = $1200 + $45 in POS charges)
INSERT INTO invoices (reservation_id, subtotal, tax_rate, tax_amount, total, payment_status, payment_method, paid_amount) VALUES 
(4, 1245.00, 0.18, 224.10, 1469.10, 'PAID', 'CREDIT_CARD', 1469.10);

INSERT INTO invoice_items (invoice_id, description, category, quantity, unit_price, total) VALUES 
(1, 'Accommodation - Suite (4 nights)', 'ROOM', 4, 300.00, 1200.00),
(1, 'Minibar - Water & Snacks', 'MINIBAR', 1, 15.00, 15.00),
(1, 'Laundry - Dry Cleaning', 'LAUNDRY', 1, 30.00, 30.00);