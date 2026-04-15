-- ============================================================
--  Hotel Transylvania — Complete Database Seed
--  Scenario Date: April 16, 2026
--  25 rooms · 20 guests · 8 staff · 13 reservations
-- ============================================================

-- ── 1. CLEAR ALL DATA ─────────────────────────────────────
DELETE FROM invoice_items;
DELETE FROM invoices;
DELETE FROM pos_charges;
DELETE FROM housekeeping;
DELETE FROM activity_log;
DELETE FROM reservations;
DELETE FROM guests;
DELETE FROM rooms;
DELETE FROM staff;

DELETE FROM sqlite_sequence WHERE name IN (
    'invoice_items','invoices','pos_charges','housekeeping',
    'activity_log','reservations','guests','rooms','staff'
);

-- ── 2. ROOMS (25 rooms, 5 floors × 5 types) ───────────────
-- Room IDs map: floor 1→1-5, floor 2→6-10, floor 3→11-15, floor 4→16-20, floor 5→21-25
INSERT INTO rooms (room_number, floor, type, status, base_price, weekend_price, max_occupancy, description) VALUES
-- Floor 1 — Singles
('101', 1, 'SINGLE', 'OCCUPIED',      100, 120, 1, 'Cosy single with garden view. Queen bed, work desk, and ensuite.'),
('102', 1, 'SINGLE', 'RESERVED',      100, 120, 1, 'Single room with city view. Queen bed and complimentary Wi-Fi.'),
('103', 1, 'SINGLE', 'AVAILABLE',     100, 120, 1, 'Single room, ground floor access. Ideal for business travellers.'),
('104', 1, 'SINGLE', 'AVAILABLE',     100, 120, 1, 'Bright corner single with twin beds and lounge chair.'),
('105', 1, 'SINGLE', 'AVAILABLE',     100, 120, 1, 'Accessible single room with roll-in shower and wider doorways.'),

-- Floor 2 — Doubles
('201', 2, 'DOUBLE', 'OCCUPIED',      150, 180, 2, 'Spacious double with king bed, sofa, and pool-facing balcony.'),
('202', 2, 'DOUBLE', 'AVAILABLE',     150, 180, 2, 'Double room with twin beds. Ideal for colleagues or siblings.'),
('203', 2, 'DOUBLE', 'AVAILABLE',     150, 180, 2, 'Double with king bed, walk-in closet, and marble bathroom.'),
('204', 2, 'DOUBLE', 'OCCUPIED',      150, 180, 2, 'Double with mountain-view balcony, king bed, and rain shower.'),
('205', 2, 'DOUBLE', 'AVAILABLE',     150, 180, 2, 'Double corner room with panoramic windows and plush furnishings.'),

-- Floor 3 — Deluxes
('301', 3, 'DELUXE', 'AVAILABLE',     200, 240, 3, 'Deluxe with separate living area, kitchenette, and city views.'),
('302', 3, 'DELUXE', 'OCCUPIED',      200, 240, 3, 'Deluxe with king bed, private terrace, and espresso bar.'),
('303', 3, 'DELUXE', 'NEEDS_CLEANING',200, 240, 3, 'Deluxe with king bed, bay window, and slate bathroom.'),
('304', 3, 'DELUXE', 'AVAILABLE',     200, 240, 3, 'Deluxe with two queen beds, lounge zone, and outdoor seating.'),
('305', 3, 'DELUXE', 'AVAILABLE',     200, 240, 3, 'Deluxe with studio layout, private balcony, and wine cooler.'),

-- Floor 4 — Suites
('401', 4, 'SUITE',  'OCCUPIED',      300, 350, 4, 'Executive suite with separate bedroom, dining area, and butler pantry.'),
('402', 4, 'SUITE',  'AVAILABLE',     300, 350, 4, 'Grand suite with panoramic skyline views and private jacuzzi.'),
('403', 4, 'SUITE',  'AVAILABLE',     300, 350, 4, 'Heritage suite with period furniture and four-poster king bed.'),
('404', 4, 'SUITE',  'RESERVED',      300, 350, 4, 'Corner suite with double-sided fireplace and wraparound terrace.'),
('405', 4, 'SUITE',  'MAINTENANCE',   300, 350, 4, 'Presidential suite. Currently under HVAC maintenance.'),

-- Floor 5 — Penthouses
('501', 5, 'PENTHOUSE', 'OCCUPIED',   500, 600, 4, 'North Penthouse. Rooftop infinity pool access, home theatre, chef kitchen.'),
('502', 5, 'PENTHOUSE', 'AVAILABLE',  500, 600, 4, 'East Penthouse. 270° sunrise views, private gym, butler service.'),
('503', 5, 'PENTHOUSE', 'AVAILABLE',  500, 600, 4, 'West Penthouse. Sunset terrace, wine cellar, grand piano.'),
('504', 5, 'PENTHOUSE', 'AVAILABLE',  500, 600, 4, 'South Penthouse. Sky garden, private pool, Michelin-starred dining option.'),
('505', 5, 'PENTHOUSE', 'NEEDS_CLEANING', 500, 600, 4, 'Central Penthouse. Needs post-checkout cleaning. Terrace and private spa.');

-- ── 3. GUESTS (20 international guests) ───────────────────
INSERT INTO guests (first_name, last_name, email, phone, id_type, id_number, city, country, vip, notes) VALUES
-- Active / Checked-In
('Aarav',     'Sharma',      'aarav.sharma@gmail.com',       '+91-98100-11223', 'PASSPORT',  'IN8821001', 'Mumbai',      'India',        1, 'Prefers high floor. Gluten-free dietary requirement.'),
('Emma',      'Johnson',     'emma.j@outlook.co.uk',         '+44-77700-12345', 'PASSPORT',  'GB1122334', 'London',      'United Kingdom',1,'Travelling with spouse. Anniversary stay — leave welcome amenity.'),
('Carlos',    'Garcia',      'carlos.garcia@correo.es',       '+34-612-345678',  'ID_CARD',   'ES9988776', 'Barcelona',   'Spain',        0, 'Business guest. Needs early morning wake-up at 06:30.'),
('Chen',      'Wei',         'chen.wei@enterprise.cn',        '+86-138-0013-8899','PASSPORT', 'CN5566778', 'Shanghai',    'China',        1, 'VIP corporate account. Champagne on arrival. Chinese newspaper.'),
('Isabella',  'Rossi',       'isabella.rossi@fashionhq.it',  '+39-02-1234567',  'ID_CARD',   'IT2233445', 'Milan',       'Italy',        1, 'Returning VIP. Daily fresh orchids in room. Late checkout requested.'),
-- Upcoming / Reserved
('Yuki',      'Tanaka',      'yuki.tanaka@jobmail.jp',        '+81-3-1234-5678', 'PASSPORT',  'JP4455667', 'Tokyo',       'Japan',        0, 'First visit. Vegetarian meals preferred.'),
-- Checked-Out
('James',     'Smith',       'james.smith@usatech.com',       '+1-415-555-0102', 'PASSPORT',  'US1234567', 'San Francisco','USA',         0, 'Business travel. Invoiced on departure — payment pending.'),
('Sophia',    'Martinez',    'sophia.m@correo.mx',            '+52-55-1234-5678','ID_CARD',   'MX8877665', 'Mexico City', 'Mexico',       0, 'Extended leisure stay. Paid by credit card on departure.'),
('Lars',      'Jensen',      'lars.jensen@nNordic.dk',         '+45-20-123456',   'PASSPORT',  'DK3344556', 'Copenhagen',  'Denmark',      0, 'Reservation cancelled 3 days prior — full refund processed.'),
('Marie',     'Dubois',      'marie.dubois@courrier.fr',      '+33-6-12-34-56-78','PASSPORT', 'FR6677889', 'Paris',       'France',       0, 'No-show. Did not contact hotel. Standard no-show policy applied.'),
-- More Active
('Rohan',     'Desai',       'rohan.desai@techbridge.in',     '+91-70001-22334', 'AADHAAR',   '4455 6677 8899','Pune',   'India',        0, 'Tech conference attendee. Extends stay possible — monitor.'),
('Ali',       'Hassan',      'ali.hassan@gulfpartners.ae',    '+971-50-1234567', 'PASSPORT',  'AE5566778', 'Dubai',       'UAE',          1, 'VIP arrival Apr 18. Private car transfer confirmed. Suite upgrade.'),
('Olivia',    'Brown',       'olivia.b@mediahouse.ca',        '+1-416-555-0189', 'PASSPORT',  'CA3344556', 'Toronto',     'Canada',       0, 'Short leisure stay. Paid in full. Positive feedback on spa.'),
-- Guest profiles (no active reservation)
('Liam',      'O''Connor',   'liam.oc@enterprise.ie',         '+353-87-1234567', 'PASSPORT',  'IE9988001', 'Dublin',      'Ireland',      0, 'Loyalty member. Last visited Q1 2025.'),
('Stefan',    'Müller',      'stefan.mueller@techag.de',      '+49-30-12345678', 'ID_CARD',   'DE7766554', 'Berlin',      'Germany',      1, 'Frequent business guest — 8th stay. Always prefers Room 402 or 403.'),
('Fatima',    'Al-Rashid',   'fatima.rashid@alinvest.sa',     '+966-50-1234567', 'PASSPORT',  'SA4455991', 'Riyadh',      'Saudi Arabia', 1, 'Ultra-VIP. Requires female staff only for housekeeping.'),
('Hiroshi',   'Yamamoto',    'hiroshi.y@jplogistics.jp',      '+81-6-1234-5678', 'PASSPORT',  'JP8877665', 'Osaka',       'Japan',        0, 'Corporate account. Standard room preferred.'),
('Amelia',    'Thompson',    'amelia.t@auspublish.au',        '+61-2-9876-5432', 'PASSPORT',  'AU3344221', 'Sydney',      'Australia',    0, 'Travel writer. May request media rate — flag for manager approval.'),
('Diego',     'Hernandez',   'diego.h@saocolors.br',          '+55-11-98765-4321','PASSPORT', 'BR6677112', 'São Paulo',   'Brazil',       0, NULL),
('Priya',     'Patel',       'priya.patel@indolink.in',       '+91-98200-33445', 'AADHAAR',   '8899 1122 3344','Ahmedabad','India',       0, 'Honeymoon inquiry — directed to Suite packages.');

-- ── 4. HOUSE STAFF (8 personnel) ──────────────────────────
INSERT INTO staff (name, role, phone, active) VALUES
('Maria Santos',    'HOUSEKEEPER', '+1-555-0201', 1),
('David Chen',      'MAINTENANCE', '+1-555-0202', 1),
('Priya Sharma',    'HOUSEKEEPER', '+1-555-0203', 1),
('James Wilson',    'MAINTENANCE', '+1-555-0204', 1),
('Sofia Rodriguez', 'SUPERVISOR',  '+1-555-0205', 1),
('Ahmed Hassan',    'INSPECTOR',   '+1-555-0206', 1),
('Elena Petrov',    'HOUSEKEEPER', '+1-555-0207', 1),
('Lucas Müller',    'MAINTENANCE', '+1-555-0208', 1);

-- ── 5. RESERVATIONS ───────────────────────────────────────
-- Rooms: 101=1, 102=2, 103=3, 201=6, 204=9, 302=12, 303=13, 401=16, 402=17, 404=19, 501=21, 503=23, 505=25

-- [CURRENTLY CHECKED IN] ──────────────────────────────────
-- Res 1 · Aarav Sharma · Room 101 SINGLE · 4 nights (Apr 14–18)
INSERT INTO reservations (guest_id,room_id,check_in_date,check_out_date,actual_check_in,status,adults,special_requests,created_by) VALUES
(1, 1, '2026-04-14','2026-04-18','2026-04-14 14:30:00','CHECKED_IN',1,'Gluten-free minibar options. High floor preferred.',1);

-- Res 2 · Emma Johnson · Room 201 DOUBLE · 5 nights (Apr 15–20) — Anniversary stay
INSERT INTO reservations (guest_id,room_id,check_in_date,check_out_date,actual_check_in,status,adults,special_requests,created_by) VALUES
(2, 6, '2026-04-15','2026-04-20','2026-04-15 15:10:00','CHECKED_IN',2,'Anniversary. Please place rose petals and champagne on arrival. Late checkout if possible.',1);

-- Res 3 · Carlos Garcia · Room 302 DELUXE · 5 nights (Apr 12–17) — Business guest
INSERT INTO reservations (guest_id,room_id,check_in_date,check_out_date,actual_check_in,status,adults,special_requests,created_by) VALUES
(3, 12, '2026-04-12','2026-04-17','2026-04-12 13:45:00','CHECKED_IN',1,'Wake-up call at 06:30 daily. Business newspaper. Express laundry.',1);

-- Res 4 · Chen Wei · Room 401 SUITE · 3 nights (Apr 16–19) — Checked in this morning
INSERT INTO reservations (guest_id,room_id,check_in_date,check_out_date,actual_check_in,status,adults,special_requests,created_by) VALUES
(4, 16, '2026-04-16','2026-04-19','2026-04-16 11:00:00','CHECKED_IN',2,'Champagne and Chinese fruit basket on arrival. Corporate VIP — butler service.',1);

-- Res 5 · Isabella Rossi · Room 501 PENTHOUSE · 7 nights (Apr 14–21) — VIP
INSERT INTO reservations (guest_id,room_id,check_in_date,check_out_date,actual_check_in,status,adults,special_requests,created_by) VALUES
(5, 21, '2026-04-14','2026-04-21','2026-04-14 16:00:00','CHECKED_IN',2,'Fresh orchids daily. Late checkout 14:00. Press suite before each day. Dietary: no seafood.',1);

-- Res 11 · Rohan Desai · Room 204 DOUBLE · 5 nights (Apr 13–18) — Conference guest
INSERT INTO reservations (guest_id,room_id,check_in_date,check_out_date,actual_check_in,status,adults,special_requests,created_by) VALUES
(11, 9, '2026-04-13','2026-04-18','2026-04-13 14:30:00','CHECKED_IN',1,'Tech conference. May extend by 1–2 nights. Early breakfast (07:00) required.',1);

-- [UPCOMING / RESERVED] ───────────────────────────────────
-- Res 6 · Yuki Tanaka · Room 102 SINGLE · 3 nights (Apr 17–20)
INSERT INTO reservations (guest_id,room_id,check_in_date,check_out_date,status,adults,special_requests,created_by) VALUES
(6, 2, '2026-04-17','2026-04-20','RESERVED',1,'Vegetarian meal plan. Non-smoking room.',1);

-- Res 12 · Ali Hassan · Room 404 SUITE · 4 nights (Apr 18–22) — VIP arrival
INSERT INTO reservations (guest_id,room_id,check_in_date,check_out_date,status,adults,special_requests,created_by) VALUES
(12, 19, '2026-04-18','2026-04-22','RESERVED',2,'Private car transfer from airport at 18:00. Pre-chill suite to 22°C. Arabic dates and coffee.',1);

-- [CHECKED OUT] ───────────────────────────────────────────
-- Res 7 · James Smith · Room 303 DELUXE · 6 nights (Apr 10–16) — checked out this morning, invoice PENDING
INSERT INTO reservations (guest_id,room_id,check_in_date,check_out_date,actual_check_in,actual_check_out,status,adults,special_requests,created_by) VALUES
(7, 13, '2026-04-10','2026-04-16','2026-04-10 12:00:00','2026-04-16 10:30:00','CHECKED_OUT',1,'Business guest. Express checkout. Invoice to company email.',1);

-- Res 8 · Sophia Martinez · Room 402 SUITE · 5 nights (Apr 8–13) — paid, room recleaned
INSERT INTO reservations (guest_id,room_id,check_in_date,check_out_date,actual_check_in,actual_check_out,status,adults,special_requests,created_by) VALUES
(8, 17, '2026-04-08','2026-04-13','2026-04-08 14:00:00','2026-04-13 11:00:00','CHECKED_OUT',2,'Leisure stay. Requested daily turndown service and pillow menu.',1);

-- Res 13 · Olivia Brown · Room 505 PENTHOUSE · 2 nights (Apr 12–14) — paid, room needs cleaning
INSERT INTO reservations (guest_id,room_id,check_in_date,check_out_date,actual_check_in,actual_check_out,status,adults,special_requests,created_by) VALUES
(13, 25, '2026-04-12','2026-04-14','2026-04-12 15:30:00','2026-04-14 10:00:00','CHECKED_OUT',1,'Media stay. Photography in suite permitted.',1);

-- [CANCELLED] ─────────────────────────────────────────────
-- Res 9 · Lars Jensen · Room 503 PENTHOUSE — cancelled 3 days prior
INSERT INTO reservations (guest_id,room_id,check_in_date,check_out_date,status,adults,created_by) VALUES
(9, 23, '2026-04-16','2026-04-19','CANCELLED',2,1);

-- [NO SHOW] ───────────────────────────────────────────────
-- Res 10 · Marie Dubois · Room 103 SINGLE — was due Apr 15, never arrived
INSERT INTO reservations (guest_id,room_id,check_in_date,check_out_date,status,adults,created_by) VALUES
(10, 3, '2026-04-15','2026-04-17','NO_SHOW',1,1);

-- ── 6. POS CHARGES ────────────────────────────────────────
-- Charges for ACTIVE reservations (not yet invoiced)

-- Res 1 — Aarav Sharma (Room 101, Day 2-3 of stay)
INSERT INTO pos_charges (reservation_id,category,description,amount,quantity,charged_by,charged_at) VALUES
(1,'SPA',        'Swedish Relaxation Massage (60 min)',   120.00,1,1,'2026-04-15 10:30:00'),
(1,'RESTAURANT', 'Room Service — Premium Dinner Set',      65.00,1,1,'2026-04-15 20:15:00');

-- Res 2 — Emma Johnson (Room 201, Anniversary stay)
INSERT INTO pos_charges (reservation_id,category,description,amount,quantity,charged_by,charged_at) VALUES
(2,'RESTAURANT', 'Continental Breakfast Buffet (per person)',45.00,2,1,'2026-04-16 08:30:00'),
(2,'MINIBAR',    'Premium Wine Selection — Merlot 2021',    55.00,1,1,'2026-04-15 22:00:00');

-- Res 3 — Carlos Garcia (Room 302, checking out tomorrow)
INSERT INTO pos_charges (reservation_id,category,description,amount,quantity,charged_by,charged_at) VALUES
(3,'RESTAURANT', 'Business Dinner — 3 Courses',             80.00,1,1,'2026-04-14 19:45:00'),
(3,'MINIBAR',    'Craft Beer Pack & Gourmet Snacks',         25.00,2,1,'2026-04-13 23:00:00'),
(3,'LAUNDRY',    'Suit Dry Cleaning & Pressing (2 items)',   35.00,1,1,'2026-04-13 09:00:00');

-- Res 4 — Chen Wei (Room 401, checked in this morning)
INSERT INTO pos_charges (reservation_id,category,description,amount,quantity,charged_by,charged_at) VALUES
(4,'RESTAURANT', 'Welcome Dinner — Private Dining Room',    95.00,1,1,'2026-04-16 19:30:00');

-- Res 5 — Isabella Rossi (Room 501, VIP Penthouse)
INSERT INTO pos_charges (reservation_id,category,description,amount,quantity,charged_by,charged_at) VALUES
(5,'SPA',        'Luxury Full-Body Treatment (90 min)',     250.00,1,1,'2026-04-15 14:00:00'),
(5,'RESTAURANT', 'Private Penthouse Dining Experience',    200.00,1,1,'2026-04-15 20:00:00'),
(5,'MINIBAR',    'Dom Pérignon Champagne + Canapés',       180.00,1,1,'2026-04-14 21:00:00');

-- Res 11 — Rohan Desai (Room 204, conference guest)
INSERT INTO pos_charges (reservation_id,category,description,amount,quantity,charged_by,charged_at) VALUES
(11,'RESTAURANT','Poolside Lunch',                          40.00,1,1,'2026-04-14 13:00:00'),
(11,'SPA',       'Couple''s Deep Tissue Massage (60 min)', 180.00,1,1,'2026-04-15 17:00:00');

-- Charges for CHECKED-OUT reservations (billed into invoices below)
-- Res 7 — James Smith (Room 303)
INSERT INTO pos_charges (reservation_id,category,description,amount,quantity,charged_by,charged_at) VALUES
(7,'RESTAURANT', 'Three-Course Business Dinner',            80.00,1,1,'2026-04-14 20:00:00'),
(7,'MINIBAR',    'Minibar Beverages & Snacks',              50.00,1,1,'2026-04-12 23:30:00'),
(7,'SPA',        'Relaxation Massage (45 min)',             120.00,1,1,'2026-04-13 15:30:00');

-- Res 8 — Sophia Martinez (Room 402)
INSERT INTO pos_charges (reservation_id,category,description,amount,quantity,charged_by,charged_at) VALUES
(8,'RESTAURANT', 'Room Service — Various Meals',            95.00,1,1,'2026-04-10 19:30:00'),
(8,'MINIBAR',    'Minibar Snacks & Non-Alcoholic Drinks',   45.00,1,1,'2026-04-11 16:00:00'),
(8,'LAUNDRY',    'Evening Dress Dry Cleaning',              60.00,1,1,'2026-04-09 10:00:00');

-- Res 13 — Olivia Brown (Room 505)
INSERT INTO pos_charges (reservation_id,category,description,amount,quantity,charged_by,charged_at) VALUES
(13,'RESTAURANT','Private Fine Dining — 5 Courses',        150.00,1,1,'2026-04-12 20:30:00'),
(13,'MINIBAR',   'Premium Minibar — Spirits & Wines',       90.00,1,1,'2026-04-13 22:00:00'),
(13,'SPA',       'Signature Luxury Spa Package (120 min)', 200.00,1,1,'2026-04-13 11:00:00');

-- ── 7. HOUSEKEEPING TASKS ──────────────────────────────────
-- room_ids: 303=13, 405=20, 505=25, 202=7
INSERT INTO housekeeping (room_id,assigned_to,task_type,priority,status,notes) VALUES
-- Room 303: James Smith checked out this morning — needs a thorough clean
(13,'Priya Sharma',  'CLEANING',    'HIGH',   'PENDING',      'Full post-checkout clean. 6-night stay. Pay attention to bathroom and desk area.'),
-- Room 405: HVAC system failure — heating/cooling not working
(20,'James Wilson',  'MAINTENANCE', 'URGENT', 'IN_PROGRESS',  'HVAC unit fault. Fan motor bearings worn. Parts ordered — ETA 4 hours.'),
-- Room 505: Olivia Brown checked out Apr 14 — still pending
(25,'Maria Santos',  'CLEANING',    'NORMAL', 'PENDING',      'Standard post-checkout clean. Note: photography equipment was in suite — double check all wardrobes.'),
-- Room 202: Scheduled quarterly deep clean (vacant room)
( 7,'Elena Petrov',  'DEEP_CLEAN',  'LOW',    'PENDING',      'Quarterly deep clean. Steam-clean carpets, sanitise AC vents, replenish all amenities.');

-- ── 8. INVOICES WITH LINE ITEMS ────────────────────────────

-- INVOICE 1 — Res 7 — James Smith — PENDING (just checked out)
-- Room 303 DELUXE: 6 nights × $200 = $1,200
-- POS: Dinner $80 + Minibar $50 + Spa $120 = $250
-- Subtotal $1,450 · Tax 18% = $261 · Total = $1,711
INSERT INTO invoices (reservation_id,subtotal,tax_rate,tax_amount,total,discount,payment_status,payment_method,paid_amount) VALUES
(7, 1450.00, 0.18, 261.00, 1711.00, 0.00, 'PENDING', NULL, 0.00);

INSERT INTO invoice_items (invoice_id,description,category,quantity,unit_price,total) VALUES
(1,'6 Nights — Deluxe Room 303 (Apr 10–16)','ROOM',      6,  200.00, 1200.00),
(1,'Three-Course Business Dinner',           'RESTAURANT',1,  80.00,   80.00),
(1,'Minibar Beverages & Snacks',             'MINIBAR',   1,  50.00,   50.00),
(1,'Relaxation Massage (45 min)',            'SPA',       1, 120.00,  120.00);

-- INVOICE 2 — Res 8 — Sophia Martinez — PAID (credit card)
-- Room 402 SUITE: 5 nights × $300 = $1,500
-- POS: Room Service $95 + Minibar $45 + Laundry $60 = $200
-- Subtotal $1,700 · Tax 18% = $306 · Total = $2,006
INSERT INTO invoices (reservation_id,subtotal,tax_rate,tax_amount,total,discount,payment_status,payment_method,paid_amount) VALUES
(8, 1700.00, 0.18, 306.00, 2006.00, 0.00, 'PAID', 'CREDIT_CARD', 2006.00);

INSERT INTO invoice_items (invoice_id,description,category,quantity,unit_price,total) VALUES
(2,'5 Nights — Suite Room 402 (Apr 8–13)',  'ROOM',       5, 300.00, 1500.00),
(2,'Room Service — Various Meals',          'RESTAURANT', 1,  95.00,   95.00),
(2,'Minibar Snacks & Non-Alcoholic Drinks', 'MINIBAR',    1,  45.00,   45.00),
(2,'Evening Dress Dry Cleaning',            'LAUNDRY',    1,  60.00,   60.00);

-- INVOICE 3 — Res 13 — Olivia Brown — PAID (credit card)
-- Room 505 PENTHOUSE: 2 nights × $500 = $1,000
-- POS: Dining $150 + Minibar $90 + Spa $200 = $440
-- Subtotal $1,440 · Tax 18% = $259.20 · Total = $1,699.20
INSERT INTO invoices (reservation_id,subtotal,tax_rate,tax_amount,total,discount,payment_status,payment_method,paid_amount) VALUES
(13, 1440.00, 0.18, 259.20, 1699.20, 0.00, 'PAID', 'CREDIT_CARD', 1699.20);

INSERT INTO invoice_items (invoice_id,description,category,quantity,unit_price,total) VALUES
(3,'2 Nights — Penthouse Room 505 (Apr 12–14)', 'ROOM',       2, 500.00, 1000.00),
(3,'Private Fine Dining — 5 Courses',           'RESTAURANT', 1, 150.00,  150.00),
(3,'Premium Minibar — Spirits & Wines',         'MINIBAR',    1,  90.00,   90.00),
(3,'Signature Luxury Spa Package (120 min)',     'SPA',        1, 200.00,  200.00);