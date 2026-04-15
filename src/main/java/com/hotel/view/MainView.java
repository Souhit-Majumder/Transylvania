package com.hotel.view;

import com.hotel.App;
import com.hotel.util.Session;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class MainView {
    private final BorderPane root;
    private final StackPane contentArea;
    private VBox navContainer;
    @SuppressWarnings("unused")
    private String activeTarget = "";

    public MainView() {
        root = new BorderPane();
        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");

        // ── Sidebar ──
        VBox sidebar = new VBox(4);
        sidebar.getStyleClass().add("sidebar");

        // Brand header
        HBox brandRow = new HBox(12);
        brandRow.setAlignment(Pos.CENTER_LEFT);
        StackPane brandIcon = new StackPane();
        brandIcon.getStyleClass().add("sidebar-brand-icon");
        Label brandLetter = new Label("🕸️");
        brandIcon.getChildren().add(brandLetter);

        VBox brandText = new VBox(2);
        Label brandTitle = new Label("Transylvania");
        brandTitle.getStyleClass().add("sidebar-title");
        Label brandSub = new Label("MANAGEMENT SUITE");
        brandSub.getStyleClass().add("sidebar-subtitle");
        brandText.getChildren().addAll(brandTitle, brandSub);
        brandRow.getChildren().addAll(brandIcon, brandText);
        VBox.setMargin(brandRow, new Insets(0, 0, 12, 0));

        // User info
        Label userLabel = new Label(Session.getCurrentUser().getFullName());
        userLabel.getStyleClass().add("sidebar-user-name");
        Label roleLabel = new Label(Session.getCurrentUser().getRole());
        roleLabel.getStyleClass().add("sidebar-user-role");

        // New Reservation button
        Button newResBtn = new Button("+ New Reservation");
        newResBtn.getStyleClass().add("sidebar-new-btn");
        newResBtn.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(newResBtn, new Insets(4, 0, 8, 0));
        newResBtn.setOnAction(e -> navigate("RESERVATIONS"));

        Separator sep1 = new Separator();
        sep1.getStyleClass().add("sidebar-separator");

        // Navigation items
        navContainer = new VBox(2);
        String[][] menuItems = {
            {"Overview",       "dashboard",        "DASHBOARD"},
            {"Bookings",       "calendar_today",   "RESERVATIONS"},
            {"Rooms",          "bed",              "ROOMS"},
            {"Floor Plan",     "map",              "FLOORPLAN"},
            {"Concierge",      "room_service",     "GUESTS"},
            {"Billing",        "receipt_long",     "BILLING"},
            {"POS Charges",    "point_of_sale",    "POS"},
            {"Housekeeping",   "cleaning_services","HOUSEKEEPING"},
            {"House Staff",    "badge",            "STAFF"},
            {"Analytics",      "analytics",        "REPORTS"},
        };
        for (String[] item : menuItems) {
            navContainer.getChildren().add(createNavButton(item[0], item[2]));
        }

        // Admin section
        VBox adminSection = new VBox(2);
        if (Session.isAdmin()) {
            Separator sep2 = new Separator();
            sep2.getStyleClass().add("sidebar-separator");
            adminSection.getChildren().add(sep2);
            adminSection.getChildren().add(createNavButton("User Mgmt", "USERS"));
            adminSection.getChildren().add(createNavButton("DB Backup", "BACKUP"));
        }

        // Spacer + bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Separator sep3 = new Separator();
        sep3.getStyleClass().add("sidebar-separator");

        Button logoutBtn = new Button("Sign Out");
        logoutBtn.getStyleClass().add("sidebar-logout-btn");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> { Session.logout(); App.showLogin(); });

        sidebar.getChildren().addAll(
            brandRow, userLabel, roleLabel, newResBtn, sep1,
            navContainer, adminSection, spacer, sep3, logoutBtn
        );

        // Wrap content in ScrollPane
        ScrollPane contentScroll = new ScrollPane();
        contentScroll.getStyleClass().add("content-scroll");
        contentScroll.setFitToWidth(true);
        contentScroll.setFitToHeight(true);
        contentScroll.setContent(contentArea);
        contentScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        root.setLeft(sidebar);
        root.setCenter(contentScroll);

        navigate("DASHBOARD");
    }

    private Button createNavButton(String text, String target) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-item");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> navigate(target));
        return btn;
    }

    private void navigate(String target) {
        activeTarget = target;
        // Update nav active states
        for (var node : navContainer.getChildren()) {
            if (node instanceof Button b) {
                b.getStyleClass().removeAll("nav-item-active", "nav-item");
                String btnTarget = getTargetForButton(b.getText());
                b.getStyleClass().add(target.equals(btnTarget) ? "nav-item-active" : "nav-item");
            }
        }

        contentArea.getChildren().clear();
        switch (target) {
            case "DASHBOARD"    -> contentArea.getChildren().add(new DashboardView(this::navigate).getView());
            case "RESERVATIONS" -> contentArea.getChildren().add(new ReservationView().getView());
            case "ROOMS"        -> contentArea.getChildren().add(new RoomView().getView());
            case "FLOORPLAN"    -> contentArea.getChildren().add(new FloorPlanView().getView());
            case "GUESTS"       -> contentArea.getChildren().add(new GuestView().getView());
            case "BILLING"      -> contentArea.getChildren().add(new BillingView().getView());
            case "POS"          -> contentArea.getChildren().add(new POSView().getView());
            case "HOUSEKEEPING" -> contentArea.getChildren().add(new HousekeepingView().getView());
            case "STAFF"        -> contentArea.getChildren().add(new StaffView().getView());
            case "REPORTS"      -> contentArea.getChildren().add(new ReportsView().getView());
            case "USERS"        -> contentArea.getChildren().add(new UserManagementView().getView());
            case "BACKUP"       -> contentArea.getChildren().add(new BackupView().getView());
        }
    }

    private String getTargetForButton(String text) {
        return switch (text) {
            case "Overview" -> "DASHBOARD";
            case "Bookings" -> "RESERVATIONS";
            case "Rooms" -> "ROOMS";
            case "Floor Plan" -> "FLOORPLAN";
            case "Concierge" -> "GUESTS";
            case "Billing" -> "BILLING";
            case "POS Charges" -> "POS";
            case "Housekeeping" -> "HOUSEKEEPING";
            case "House Staff" -> "STAFF";
            case "Analytics" -> "REPORTS";
            case "User Mgmt" -> "USERS";
            case "DB Backup" -> "BACKUP";
            default -> "";
        };
    }

    @SuppressWarnings("exports")
    public BorderPane getView() { return root; }
}
