# Hotel Management System (JavaFX + SQLite)

A desktop Property Management System (PMS) for hotel staff built with JavaFX 17 and SQLite.

## Features

### Core
- **Login & Role-Based Access** — Admin, Manager, Receptionist roles with scoped permissions
- **Dashboard** — Live snapshot: occupied rooms, today's check-ins/outs, pending housekeeping
- **Reservation System** — Book rooms, check-in, check-out, cancel, mark no-show
- **Room Management** — CRUD with status tracking (Available/Occupied/Reserved/Maintenance/Needs Cleaning)
- **Guest Management** — Guest profiles with search, ID proof, VIP flag, contact details
- **Billing & Invoicing** — Auto-generated itemized invoices, tax calculation, payment recording (Cash/Card/UPI)

### Advanced
- **Interactive Floor Plan** — Color-coded room grid by floor, click to view/change status
- **POS (Point of Sale)** — Charge restaurant, minibar, spa, laundry to guest's room bill
- **Housekeeping Module** — Task assignment with priority levels, completion workflow
- **Analytics & Reports** — PieChart (room status/types), BarChart (monthly revenue)
- **User Management** — Admin-only user CRUD
- **Database Backup** — Export `.db` file or `.sql` dump via DirectoryChooser
- **Activity Logging** — Built-in audit trail table

## Prerequisites
- Java 17+
- Maven 3.8+

## Run
```bash
cd hotel-management-system
mvn javafx:run
```

## Default Credentials
| Username    | Password   | Role         |
|-------------|------------|--------------|
| admin       | admin123   | ADMIN        |
| reception   | pass123    | RECEPTIONIST |

## Seed Data
- 24 rooms across 4 floors are auto-generated on first run
- Room types: Single ($80), Double ($120), Suite ($200), Deluxe ($300)

## Tech Stack
- JavaFX 17 (programmatic UI, no FXML)
- SQLite via JDBC (sqlite-jdbc)
- Maven build system
- No external CSS (barebones as requested)

## Project Structure
```
src/main/java/com/hotel/
├── App.java                  # Entry point
├── model/                    # POJOs: User, Room, Guest, Reservation, Invoice, etc.
├── dao/                      # Data access layer (raw SQL)
├── util/                     # DatabaseManager, Session
└── view/                     # All UI views (programmatic JavaFX)
    ├── LoginView.java
    ├── MainView.java         # Sidebar navigation shell
    ├── DashboardView.java
    ├── ReservationView.java
    ├── RoomView.java
    ├── FloorPlanView.java
    ├── GuestView.java
    ├── BillingView.java
    ├── POSView.java
    ├── HousekeepingView.java
    ├── ReportsView.java
    ├── UserManagementView.java
    └── BackupView.java
```
