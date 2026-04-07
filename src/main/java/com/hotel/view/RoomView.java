package com.hotel.view;

import com.hotel.dao.RoomDAO;
import com.hotel.model.Room;
import com.hotel.util.Session;
import com.hotel.util.DialogHelper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class RoomView {
    private final VBox root;
    private final TableView<Room> table;
    private final RoomDAO dao = new RoomDAO();

    public RoomView() {
        root = new VBox(20);
        root.setPadding(new Insets(32));

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.BOTTOM_LEFT);
        VBox headerText = new VBox(4);
        Label title = new Label("Room Inventory");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Manage all rooms, pricing, and statuses");
        subtitle.getStyleClass().add("page-subtitle");
        headerText.getChildren().addAll(title, subtitle);
        header.getChildren().add(headerText);

        // Toolbar
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        Button addBtn = new Button("+ Add Room");
        addBtn.getStyleClass().add("btn-primary");
        ComboBox<String> filterType = new ComboBox<>(FXCollections.observableArrayList("All Types", "SINGLE", "DOUBLE", "SUITE", "DELUXE", "PENTHOUSE"));
        filterType.setValue("All Types");
        ComboBox<String> filterStatus = new ComboBox<>(FXCollections.observableArrayList("All Statuses", "AVAILABLE", "OCCUPIED", "RESERVED", "MAINTENANCE", "NEEDS_CLEANING"));
        filterStatus.setValue("All Statuses");
        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("btn-outline");
        toolbar.getChildren().addAll(addBtn, filterType, filterStatus, refreshBtn);

        // Table
        table = new TableView<>();
        VBox.setVgrow(table, Priority.ALWAYS);
        setupColumns();
        loadData();

        addBtn.setOnAction(e -> showRoomDialog(null));
        refreshBtn.setOnAction(e -> loadData());
        filterType.setOnAction(e -> applyFilter(filterType.getValue(), filterStatus.getValue()));
        filterStatus.setOnAction(e -> applyFilter(filterType.getValue(), filterStatus.getValue()));

        ContextMenu ctx = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit Room");
        MenuItem deleteItem = new MenuItem("Delete Room");
        MenuItem statusMenu = new MenuItem("Change Status...");
        editItem.setOnAction(e -> { Room r = table.getSelectionModel().getSelectedItem(); if (r != null) showRoomDialog(r); });
        deleteItem.setOnAction(e -> {
            Room r = table.getSelectionModel().getSelectedItem();
            if (r != null && Session.isAdmin()) { dao.delete(r.getId()); loadData(); }
        });
        statusMenu.setOnAction(e -> {
            Room r = table.getSelectionModel().getSelectedItem();
            if (r != null) showStatusDialog(r);
        });
        ctx.getItems().addAll(editItem, statusMenu, new SeparatorMenuItem(), deleteItem);
        table.setContextMenu(ctx);

        root.getChildren().addAll(header, toolbar, table);
    }

    @SuppressWarnings("unchecked")
    private void setupColumns() {
        TableColumn<Room, String> num = new TableColumn<>("Room #");
        num.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoomNumber()));
        TableColumn<Room, Number> floor = new TableColumn<>("Floor");
        floor.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getFloor()));
        TableColumn<Room, String> type = new TableColumn<>("Type");
        type.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType()));
        TableColumn<Room, String> status = new TableColumn<>("Status");
        status.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        TableColumn<Room, Number> price = new TableColumn<>("Base Price");
        price.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getBasePrice()));
        TableColumn<Room, Number> wknd = new TableColumn<>("Weekend");
        wknd.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getWeekendPrice()));
        TableColumn<Room, Number> occ = new TableColumn<>("Max Occ.");
        occ.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getMaxOccupancy()));
        table.getColumns().addAll(num, floor, type, status, price, wknd, occ);
    }

    private void loadData() { table.getItems().setAll(dao.findAll()); }

    private void applyFilter(String type, String status) {
        table.getItems().setAll(dao.findAll().stream()
            .filter(r -> "All Types".equals(type) || r.getType().equals(type))
            .filter(r -> "All Statuses".equals(status) || r.getStatus().equals(status))
            .toList());
    }

    private void showRoomDialog(Room existing) {
        Dialog<Room> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Room" : "Edit Room");
        GridPane grid = DialogHelper.createFormGrid();

        TextField numField = new TextField(existing != null ? existing.getRoomNumber() : "");
        Spinner<Integer> floorSpin = new Spinner<>(1, 50, existing != null ? existing.getFloor() : 1);
        ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList("SINGLE", "DOUBLE", "SUITE", "DELUXE", "PENTHOUSE"));
        typeBox.setValue(existing != null ? existing.getType() : "SINGLE");
        TextField priceField = new TextField(existing != null ? String.valueOf(existing.getBasePrice()) : "100");
        TextField wkndField = new TextField(existing != null ? String.valueOf(existing.getWeekendPrice()) : "130");
        Spinner<Integer> occSpin = new Spinner<>(1, 10, existing != null ? existing.getMaxOccupancy() : 2);
        TextArea descArea = new TextArea(existing != null ? existing.getDescription() : "");
        descArea.setPrefRowCount(2);

        grid.addRow(0, new Label("Room Number:"), numField);
        grid.addRow(1, new Label("Floor:"), floorSpin);
        grid.addRow(2, new Label("Type:"), typeBox);
        grid.addRow(3, new Label("Base Price:"), priceField);
        grid.addRow(4, new Label("Weekend Price:"), wkndField);
        grid.addRow(5, new Label("Max Occupancy:"), occSpin);
        grid.addRow(6, new Label("Description:"), descArea);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        DialogHelper.style(dialog);
        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                Room r = existing != null ? existing : new Room();
                r.setRoomNumber(numField.getText().trim());
                r.setFloor(floorSpin.getValue());
                r.setType(typeBox.getValue());
                if (existing == null) r.setStatus("AVAILABLE");
                try { r.setBasePrice(Double.parseDouble(priceField.getText())); } catch (Exception ex) { r.setBasePrice(100); }
                try { r.setWeekendPrice(Double.parseDouble(wkndField.getText())); } catch (Exception ex) { r.setWeekendPrice(130); }
                r.setMaxOccupancy(occSpin.getValue());
                r.setDescription(descArea.getText());
                return r;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(r -> { dao.save(r); loadData(); });
    }

    private void showStatusDialog(Room room) {
        ChoiceDialog<String> dlg = new ChoiceDialog<>(room.getStatus(),
            "AVAILABLE", "OCCUPIED", "RESERVED", "MAINTENANCE", "NEEDS_CLEANING");
        dlg.setTitle("Change Status");
        dlg.setHeaderText("Room " + room.getRoomNumber());
        DialogHelper.style(dlg);
        dlg.showAndWait().ifPresent(s -> { dao.updateStatus(room.getId(), s); loadData(); });
    }

    @SuppressWarnings("exports")
    public VBox getView() { return root; }
}
