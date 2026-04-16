# Transylvania — Hotel Management System
### Comprehensive Project Overview

---

## What The Project Is

**Transylvania** is a full-featured **Hotel Management desktop application** built entirely in Java. It manages every aspect of a hotel's daily operations:

| Module | What it does |
|---|---|
| **Reservations** | Book, check-in, check-out, cancel guests |
| **Rooms** | Inventory and status tracking |
| **Floor Plan** | Interactive visual layout of all rooms by floor |
| **Guests** | Guest profile management and search |
| **Billing** | Invoice generation and payment recording |
| **POS Charges** | In-stay charges (restaurant, spa, minibar, laundry) |
| **Housekeeping** | Task assignment and tracking |
| **House Staff** | Staff roster management |
| **Analytics** | Revenue charts, occupancy stats |
| **User Management** | Role-based access control (Admin/Manager/Receptionist) |
| **DB Backup** | Export database as `.db` file or full SQL dump |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| UI Framework | JavaFX 17.0.2 |
| Database | SQLite 3 (via `org.xerial:sqlite-jdbc:3.41.2.1`) |
| Build Tool | Apache Maven |
| Styling | Custom CSS (`atlanta.css`) |

---

## Project Architecture — 4-Layer MVC

```
com.hotel/
├── App.java              ← Entry point (extends Application)
├── model/                ← 9 POJOs: Room, Guest, Reservation,
│                              Invoice, InvoiceItem, POSCharge,
│                              HousekeepingTask, User, Staff
├── dao/                  ← 8 Data Access Objects (all DB logic)
├── util/                 ← DatabaseManager, Session, DialogHelper
└── view/                 ← 13 JavaFX screens
```

**Data flow:**
```
View (UI event)  →  DAO (SQL)  →  SQLite (hotel.db)
                 ←  Model      ←
```

---

## 1. Generics

Generics are used **heavily and consistently** throughout the project. Every collection, UI component, callback, and dialog is type-parameterised.

### Collections with type parameters
```java
// DAOs — every findAll/findByX method
List<Room> findAll()              // RoomDAO.java
List<Guest> findAll()             // GuestDAO.java
List<Reservation> findAll()       // ReservationDAO.java
List<Staff> findActive()          // StaffDAO.java

// Map of status → count (RoomDAO.java:78-79)
Map<String, Integer> getStatusCounts()
Map<String, Integer> map = new LinkedHashMap<>();

// Monthly revenue map (ReservationDAO)
Map<String, Double> getMonthlyRevenue()

// Floor-plan grouping (FloorPlanView.java:66-70)
Map<Integer, List<Room>> byFloor = new TreeMap<>();
Map<Integer, VBox>       floorSections = new LinkedHashMap<>();
```

### JavaFX generic UI components
```java
// Every screen has a typed TableView
TableView<Reservation>  table;   // ReservationView.java:17
TableView<Room>         table;   // RoomView.java:18
TableView<Guest>        table;   // GuestView.java:16
TableView<Staff>        table;   // StaffView.java:16
TableView<Invoice>      table;   // BillingView.java
TableView<HousekeepingTask> table; // HousekeepingView.java

// Typed columns for each
TableColumn<Reservation, String> guest = new TableColumn<>("Guest");
TableColumn<Room, Number>        price = new TableColumn<>("Base Price");

// Typed dialogs
Dialog<Guest>       dialog = new Dialog<>();   // GuestView.java:111
Dialog<POSCharge>   dialog = new Dialog<>();   // POSView.java
Dialog<Staff>       dialog = new Dialog<>();   // StaffView.java:121
Dialog<Room>        dialog = new Dialog<>();   // RoomView.java:113
Dialog<Invoice>     dialog = new Dialog<>();   // BillingView.java

// Typed combo boxes
ComboBox<String> filterType = new ComboBox<>(...);   // RoomView.java:41
ComboBox<String> catBox = new ComboBox<>(...);        // POSView.java:121
ChoiceDialog<String> dlg = new ChoiceDialog<>(...);   // FloorPlanView.java
```

### Generic cell value factories (lambda + generics)
```java
// Every column uses a Callback<CellDataFeatures<T,S>, ObservableValue<S>>
name.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
price.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getBasePrice()));
// SimpleStringProperty and SimpleDoubleProperty are themselves generic wrappers
```

### Generic model field
```java
// Invoice.java:11 — model holds a generic list
private List<InvoiceItem> items = new ArrayList<>();
public List<InvoiceItem> getItems() { return items; }
```

### XYChart generics (ReportsView.java)
```java
BarChart<String, Number>    revenueChart = new BarChart<>(xAxis, yAxis);
XYChart.Series<String, Number> revSeries = new XYChart.Series<>();
```

---

## 2. Java Collections Framework

Every major `java.util` collection type is put to real use:

| Collection | Where | Purpose |
|---|---|---|
| `ArrayList<>` | All 8 DAOs | Accumulates query results row by row |
| `LinkedHashMap<>` | `RoomDAO`, `ReportsView`, `FloorPlanView` | Ordered key-value maps (preserves insertion order) |
| `TreeMap<>` | `FloorPlanView.java:66` | Sorts floors numerically (1→5) automatically |
| `Map.getOrDefault()` | `DashboardView.java:30-33` | Safe count retrieval without null checks |
| `Map.computeIfAbsent()` | `FloorPlanView.java:67` | Groups rooms by floor lazily |
| `Map.merge()` | `ReportsView.java:70` | Aggregates room-type counts in one line |
| `Collections.reverse()` | `ReportsView.java:90` | Reverses month list for chronological chart order |
| `FXCollections.observableArrayList()` | Every View | Creates JavaFX-reactive lists for UI components |

### Key examples

```java
// FloorPlanView.java:66-67 — TreeMap auto-sorts floors + computeIfAbsent
Map<Integer, List<Room>> byFloor = new TreeMap<>();
for (Room r : rooms) byFloor.computeIfAbsent(r.getFloor(), k -> new ArrayList<>()).add(r);

// RoomDAO.java:78-79 — LinkedHashMap preserves display order
Map<String, Integer> map = new LinkedHashMap<>();

// ReportsView.java:39-40 — stream + mapToInt + getOrDefault
int totalRooms = roomStats.values().stream().mapToInt(i -> i).sum();
int available  = roomStats.getOrDefault("AVAILABLE", 0);

// ReportsView.java:70 — merge() aggregates counts
for (var room : roomDAO.findAll()) typeCounts.merge(room.getType(), 1, Integer::sum);
```

---

## 3. Functional Interfaces & Lambdas

The project makes extensive use of Java's functional programming features (Java 8+).

### Functional interface as a callback (`Consumer<String>`)
```java
// DashboardView.java — constructor accepts a navigator function
public DashboardView(Consumer<String> navigator) { ... }

// MainView.java — passes a method reference as the argument
new DashboardView(this::navigate)

// Used inside DashboardView
viewQueueBtn.setOnAction(e -> navigator.accept("HOUSEKEEPING"));
taskRow.setOnMouseClicked(e -> navigator.accept("FLOORPLAN"));
```

### `Runnable` as a first-class value
```java
// FloorPlanView.java — a Runnable stored as a field and passed around
private Runnable buildPlan;

buildPlan = () -> { ... }; // defined in constructor

// Passed into showRoomDetails as a refresh callback
roomBtn.setOnAction(e -> showRoomDetails(room, buildPlan));

private void showRoomDetails(Room room, Runnable refresh) {
    ...
    refresh.run(); // triggers floor plan redraw
}
```

### Stream API with lambdas
```java
// RoomView.java:106-109 — filter + toList pipeline
table.getItems().setAll(
    dao.findAll().stream()
        .filter(r -> "All Types".equals(type) || r.getType().equals(type))
        .filter(r -> "All Statuses".equals(status) || r.getStatus().equals(status))
        .toList());

// ReservationView.java:78
table.getItems().setAll(
    resDAO.findAll().stream().filter(r -> r.getStatus().equals(f)).toList());

// BillingView.java:79
table.getItems().setAll(
    dao.findAll().stream().filter(i -> i.getPaymentStatus().equals(f)).toList());

// ReportsView.java:39
int totalRooms = roomStats.values().stream().mapToInt(i -> i).sum();
```

### Method references
```java
// MainView.java
new DashboardView(this::navigate);   // instance method reference

// ReportsView.java:70
typeCounts.merge(room.getType(), 1, Integer::sum);  // static method reference

// StaffView.java:83
confirm.showAndWait().filter(b -> b == ButtonType.OK).ifPresent(b -> { ... });
```

### Lambda event handlers (everywhere)
```java
addBtn.setOnAction(e -> showGuestDialog(null));
searchField.setOnAction(e -> searchBtn.fire());
refreshBtn.setOnAction(e -> { searchField.clear(); loadData(); });
```

### `Optional` via `showAndWait().ifPresent()`
```java
// Pattern used across all view dialogs
dialog.showAndWait().ifPresent(g -> { dao.save(g); loadData(); });
dlg.showAndWait().ifPresent(s -> {
    dao.updateStatus(room.getId(), s);
    refresh.run();
});
```

---

## 4. Object-Oriented Programming Principles

### Encapsulation
All 9 model classes (`Room`, `Guest`, `Reservation`, `Invoice`, `InvoiceItem`, `POSCharge`, `HousekeepingTask`, `User`, `Staff`) follow strict encapsulation: every field is `private`, accessed only through public getters/setters.

```java
// Staff.java
private String name, role, phone;
private boolean active;
public String getName() { return name; }
public void setName(String v) { this.name = v; }
```

### Abstraction
The DAO layer completely abstracts all SQL logic from the views. A View never writes SQL — it only calls DAO methods:

```java
// ReservationView.java — the view doesn't know what SQL runs
List<Reservation> today = resDAO.findTodayCheckIns(today);
resDAO.checkIn(reservation.getId());
```

### Inheritance
- `App extends Application` — extends JavaFX's `Application` class, overriding `start(Stage)` to bootstrap the app
- All view classes use JavaFX's widget class hierarchy (`VBox`, `HBox`, `TableView`, `Dialog`, etc.)

### Polymorphism
```java
// Style method in DialogHelper accepts any Dialog subtype
public static <T> void style(Dialog<T> dialog) { ... }

// Different Dialog<T> at different call sites — same method handles all
Dialog<Guest>    → DialogHelper.style(dialog)
Dialog<POSCharge>→ DialogHelper.style(dialog)
Dialog<Invoice>  → DialogHelper.style(dialog)
```

---

## 5. Design Patterns

### Singleton Pattern
Two singletons manage shared global state:

```java
// DatabaseManager.java:7 — one connection shared across all DAOs
private static Connection connection;
public static Connection getConnection() {
    if (connection == null) connection = DriverManager.getConnection(DB_URL);
    return connection;
}

// Session.java:6 — single logged-in user for the session
private static User currentUser;
public static void setCurrentUser(User u) { currentUser = u; }
public static boolean isAdmin() { return "ADMIN".equals(currentUser.getRole()); }
```

### Data Access Object (DAO) Pattern
One DAO class per entity, each responsible solely for SQL interactions:

| DAO | Entity |
|---|---|
| `UserDAO` | `users` table |
| `RoomDAO` | `rooms` table |
| `GuestDAO` | `guests` table |
| `ReservationDAO` | `reservations` table |
| `InvoiceDAO` | `invoices` + `invoice_items` |
| `POSChargeDAO` | `pos_charges` table |
| `HousekeepingDAO` | `housekeeping` table |
| `StaffDAO` | `staff` table |

### Strategy / Callback Pattern
```java
// DashboardView accepts a navigation strategy at construction
public DashboardView(Consumer<String> navigator) { ... }
// FloorPlanView passes a refresh strategy into a helper method
private void showRoomDetails(Room room, Runnable refresh) { ... }
```

### Template Method Pattern
Every DAO's `mapRow(ResultSet rs)` method is an internal template for turning a DB row into a model object:
```java
private Room mapRow(ResultSet rs) throws SQLException {
    Room r = new Room();
    r.setId(rs.getInt("id"));
    r.setRoomNumber(rs.getString("room_number"));
    // ...
    return r;
}
```

### Observer Pattern (JavaFX built-in)
JavaFX `ObservableList` (returned by `FXCollections.observableArrayList()`) implements the Observer pattern — `TableView` automatically re-renders its rows whenever the backing list changes, without any manual UI refresh calls.

---

## 6. Exception Handling & JDBC Resource Management

### Try-with-resources (AutoCloseable)
Used in **every single** DAO method to guarantee that `Connection`, `Statement`, and `ResultSet` are closed even if an exception occurs:

```java
// RoomDAO.java:11 — Statement + ResultSet both auto-closed
try (Statement s = DatabaseManager.getConnection().createStatement();
     ResultSet rs = s.executeQuery("SELECT * FROM rooms ORDER BY room_number")) {
    while (rs.next()) list.add(mapRow(rs));
} catch (SQLException e) { e.printStackTrace(); }

// ReservationDAO.java:25 — PreparedStatement auto-closed
try (PreparedStatement ps = DatabaseManager.getConnection()
        .prepareStatement(SELECT_JOIN + " WHERE r.id=?")) {
    ps.setInt(1, id);
    ResultSet rs = ps.executeQuery();
    // ...
} catch (SQLException e) { e.printStackTrace(); }
```

### PreparedStatement (SQL Injection Prevention)
All INSERT/UPDATE/DELETE operations use `PreparedStatement` with `?` placeholders — never string concatenation:

```java
// GuestDAO example
PreparedStatement ps = conn.prepareStatement(
    "INSERT INTO guests (first_name,last_name,email,...) VALUES (?,?,?,...)");
ps.setString(1, g.getFirstName());
ps.setString(2, g.getLastName());
```

### RETURN_GENERATED_KEYS
Used in `ReservationDAO` and `InvoiceDAO` to retrieve auto-generated primary keys after INSERT:

```java
PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
ps.executeUpdate();
ResultSet keys = ps.getGeneratedKeys();
if (keys.next()) return keys.getInt(1); // returns the new row's ID
```

---

## 7. File I/O (BackupView.java)

Two types of file operations:

### Binary file copy (`java.nio.file`)
```java
// BackupView.java:54
Files.copy(Path.of("hotel.db"), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
```

### Text file writing (`java.io`)
```java
// BackupView.java:69-71 — SQL dump
try (Connection conn = DatabaseManager.getConnection();
     Statement  stmt = conn.createStatement();
     PrintWriter pw  = new PrintWriter(new FileWriter(dest))) {
    // Iterates sqlite_master, writes CREATE TABLE + INSERT statements
}
```

---

## 8. Multithreading — Honest Assessment

> [!IMPORTANT]
> The application does **not** use explicit multithreading (no `Thread`, `ExecutorService`, `Platform.runLater`, or JavaFX `Task<T>`). All database calls happen synchronously on the JavaFX Application Thread (JAT).

**However**, JavaFX itself is inherently multithreaded at the framework level:
- The **JavaFX Application Thread** (JAT) handles all UI events and rendering
- The **Pulse thread** drives animations and layout passes in the background
- The **`start(Stage stage)` method** in `App.java` is called by the JAT, not the main thread

The `Runnable` interface is used in `FloorPlanView.buildPlan` — but as a stored lambda (a callback), not as a thread target. This is functional programming, not multithreading.

**What could be added:** Database-heavy operations (e.g., loading all reservations, generating backups) should ideally run on a background thread using JavaFX's `Task<T>` and `Service<T>` classes, with `Platform.runLater()` to update the UI — this would be a natural next improvement.

---

## 9. Java Language Features Used

| Feature | Where |
|---|---|
| **Text Blocks** (`"""..."""`) | `DatabaseManager.java` — all SQL CREATE TABLE statements |
| **`var` keyword** | `FloorPlanView.java`, `ReportsView.java` — `for (var entry : map.entrySet())` |
| **`switch` expressions** | `MainView.java` — `navigate()` uses arrow-form switch |
| **`instanceof` pattern matching** | `FloorPlanView.java:118` — `if (node instanceof Button b)` |
| **Record-like models (`toString()`)** | `Room.toString()` → `"101 (SINGLE)"`, `Staff.toString()` → `"Maria (HOUSEKEEPER)"` |
| **Diamond operator `<>`** | Everywhere — `new ArrayList<>()`, `new Dialog<>()` |
| **`@SuppressWarnings`** | View `getView()` methods and `setupColumns()` |

---

## 10. Database Schema Summary (9 Tables)

```
users ←── reservations ──→ guests
              │   └──────→ rooms
              ↓
         pos_charges        housekeeping ──→ rooms
              ↓
           invoices ──→ invoice_items

         staff              (referenced by housekeeping.assigned_to as text)
         activity_log       (schema exists; logging not yet wired to UI)
```

**All foreign key constraints are enforced:**
```java
// DatabaseManager.java — runs on every connection
stmt.execute("PRAGMA foreign_keys = ON");
```

---

## 11. Package Structure (Full Class List)

```
com.hotel/
  App.java

  model/
    User.java, Room.java, Guest.java, Reservation.java
    Invoice.java, InvoiceItem.java, POSCharge.java
    HousekeepingTask.java, Staff.java

  dao/
    UserDAO.java, RoomDAO.java, GuestDAO.java, ReservationDAO.java
    InvoiceDAO.java, POSChargeDAO.java, HousekeepingDAO.java, StaffDAO.java

  util/
    DatabaseManager.java   ← Singleton connection + schema init
    Session.java           ← Singleton logged-in user
    DialogHelper.java      ← Generic dialog styler

  view/
    LoginView.java, MainView.java, DashboardView.java
    ReservationView.java, RoomView.java, FloorPlanView.java
    GuestView.java, BillingView.java, POSView.java
    HousekeepingView.java, StaffView.java, ReportsView.java
    UserManagementView.java, BackupView.java
```
