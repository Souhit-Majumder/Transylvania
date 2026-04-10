package com.hotel.view;

import com.hotel.dao.GuestDAO;
import com.hotel.model.Guest;
import com.hotel.util.DialogHelper;
import com.hotel.util.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class GuestView {
    private final VBox root;
    private final TableView<Guest> table;
    private final GuestDAO dao = new GuestDAO();

    public GuestView() {
        root = new VBox(20);
        root.setPadding(new Insets(32));

        HBox header = new HBox();
        header.setAlignment(Pos.BOTTOM_LEFT);
        VBox headerText = new VBox(4);
        Label title = new Label("Guest Relations");
        title.getStyleClass().add("page-title");
        HBox statsLine = new HBox(20);
        Label totalGuests = new Label("● " + dao.findAll().size() + " Total Guests");
        totalGuests.getStyleClass().add("page-subtitle");
        statsLine.getChildren().add(totalGuests);
        headerText.getChildren().addAll(title, statsLine);
        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);

        HBox actions = new HBox(10);
        Button exportBtn = new Button("Export .CSV");
        exportBtn.getStyleClass().add("btn-outline");
        actions.getChildren().add(exportBtn);
        header.getChildren().addAll(headerText, hSpacer, actions);

        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        TextField searchField = new TextField();
        searchField.setPromptText("Search guests by name, email, phone...");
        searchField.setPrefWidth(350);
        searchField.setPrefHeight(38);
        Button searchBtn = new Button("Search");
        searchBtn.getStyleClass().add("btn-outline");
        Button addBtn = new Button("+ Add Guest");
        addBtn.getStyleClass().add("btn-primary");
        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("btn-ghost");
        toolbar.getChildren().addAll(searchField, searchBtn, addBtn, refreshBtn);

        table = new TableView<>();
        VBox.setVgrow(table, Priority.ALWAYS);

        table.setMaxWidth(Double.MAX_VALUE);
        table.setMaxHeight(Double.MAX_VALUE);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        setupColumns();
        loadData();

        searchBtn.setOnAction(e -> {
            String q = searchField.getText().trim();
            table.getItems().setAll(q.isEmpty() ? dao.findAll() : dao.search(q));
        });
        searchField.setOnAction(e -> searchBtn.fire());
        addBtn.setOnAction(e -> showGuestDialog(null));
        refreshBtn.setOnAction(e -> { searchField.clear(); loadData(); });

        ContextMenu ctx = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit Profile");
        MenuItem deleteItem = new MenuItem("Delete Guest");
        editItem.setOnAction(e -> { Guest g = table.getSelectionModel().getSelectedItem(); if (g != null) showGuestDialog(g); });
        deleteItem.setOnAction(e -> {
            Guest g = table.getSelectionModel().getSelectedItem();
            if (g != null && Session.isAdmin()) { dao.delete(g.getId()); loadData(); }
        });
        ctx.getItems().addAll(editItem, new SeparatorMenuItem(), deleteItem);
        table.setContextMenu(ctx);

        root.getChildren().addAll(header, toolbar, table);
    }

    @SuppressWarnings("unchecked")
    private void setupColumns() {
        TableColumn<Guest, String> name = new TableColumn<>("Guest Name");
        name.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        name.setPrefWidth(170);
        TableColumn<Guest, String> email = new TableColumn<>("Contact Information");
        email.setCellValueFactory(c -> new SimpleStringProperty(
            (c.getValue().getEmail() != null ? c.getValue().getEmail() : "") +
            (c.getValue().getPhone() != null ? "\n" + c.getValue().getPhone() : "")));
        email.setPrefWidth(200);
        TableColumn<Guest, String> idType = new TableColumn<>("ID Type");
        idType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getIdType()));
        TableColumn<Guest, String> city = new TableColumn<>("City");
        city.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCity()));
        TableColumn<Guest, String> vip = new TableColumn<>("Loyalty");
        vip.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isVip() ? "VIP" : "Member"));
        vip.setPrefWidth(80);
        table.getColumns().addAll(name, email, idType, city, vip);
    }

    private void loadData() { table.getItems().setAll(dao.findAll()); }

    void showGuestDialog(Guest existing) {
        Dialog<Guest> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Guest" : "Edit Guest");
        GridPane grid = DialogHelper.createFormGrid();

        TextField firstField = new TextField(existing != null ? existing.getFirstName() : "");
        TextField lastField = new TextField(existing != null ? existing.getLastName() : "");
        TextField emailField = new TextField(existing != null ? existing.getEmail() : "");
        TextField phoneField = new TextField(existing != null ? existing.getPhone() : "");
        ComboBox<String> idTypeBox = new ComboBox<>(FXCollections.observableArrayList("Passport", "Driver License", "National ID", "Aadhaar", "Other"));
        idTypeBox.setValue(existing != null && existing.getIdType() != null ? existing.getIdType() : "Passport");
        TextField idNumField = new TextField(existing != null ? existing.getIdNumber() : "");
        TextField addressField = new TextField(existing != null ? existing.getAddress() : "");
        TextField cityField = new TextField(existing != null ? existing.getCity() : "");
        TextField countryField = new TextField(existing != null ? existing.getCountry() : "");
        TextArea notesArea = new TextArea(existing != null ? existing.getNotes() : "");
        notesArea.setPrefRowCount(2);
        CheckBox vipCheck = new CheckBox("VIP Guest");
        if (existing != null) vipCheck.setSelected(existing.isVip());

        int row = 0;
        grid.addRow(row++, new Label("First Name:"), firstField);
        grid.addRow(row++, new Label("Last Name:"), lastField);
        grid.addRow(row++, new Label("Email:"), emailField);
        grid.addRow(row++, new Label("Phone:"), phoneField);
        grid.addRow(row++, new Label("ID Type:"), idTypeBox);
        grid.addRow(row++, new Label("ID Number:"), idNumField);
        grid.addRow(row++, new Label("Address:"), addressField);
        grid.addRow(row++, new Label("City:"), cityField);
        grid.addRow(row++, new Label("Country:"), countryField);
        grid.addRow(row++, new Label("Notes:"), notesArea);
        grid.add(vipCheck, 1, row);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        DialogHelper.style(dialog);
        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                Guest g = existing != null ? existing : new Guest();
                g.setFirstName(firstField.getText().trim()); g.setLastName(lastField.getText().trim());
                g.setEmail(emailField.getText().trim()); g.setPhone(phoneField.getText().trim());
                g.setIdType(idTypeBox.getValue()); g.setIdNumber(idNumField.getText().trim());
                g.setAddress(addressField.getText().trim()); g.setCity(cityField.getText().trim());
                g.setCountry(countryField.getText().trim()); g.setNotes(notesArea.getText());
                g.setVip(vipCheck.isSelected());
                return g;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(g -> { dao.save(g); loadData(); });
    }

    @SuppressWarnings("exports")
    public VBox getView() { return root; }
}
