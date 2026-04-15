package com.hotel.view;

import com.hotel.dao.StaffDAO;
import com.hotel.model.Staff;
import com.hotel.util.DialogHelper;
import com.hotel.util.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class StaffView {
    private final VBox root;
    private final TableView<Staff> table;
    private final StaffDAO dao = new StaffDAO();

    public StaffView() {
        root = new VBox(20);
        root.setPadding(new Insets(32));

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.BOTTOM_LEFT);
        VBox headerText = new VBox(4);
        Label title = new Label("House Staff");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Manage housekeeping and maintenance personnel");
        subtitle.getStyleClass().add("page-subtitle");
        headerText.getChildren().addAll(title, subtitle);
        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);
        header.getChildren().addAll(headerText, hSpacer);

        // Toolbar
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        Button addBtn = new Button("+ Add Staff");
        addBtn.getStyleClass().add("btn-primary");
        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("btn-ghost");
        toolbar.getChildren().addAll(addBtn, refreshBtn);

        // Table
        table = new TableView<>();
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setMaxWidth(Double.MAX_VALUE);
        table.setMaxHeight(Double.MAX_VALUE);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupColumns();
        loadData();

        addBtn.setOnAction(e -> showStaffDialog(null));
        refreshBtn.setOnAction(e -> loadData());

        // Context menu
        ContextMenu ctx = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit");
        MenuItem toggleItem = new MenuItem("Toggle Active / Inactive");
        MenuItem deleteItem = new MenuItem("Delete");

        editItem.setOnAction(e -> {
            Staff s = table.getSelectionModel().getSelectedItem();
            if (s != null) showStaffDialog(s);
        });
        toggleItem.setOnAction(e -> {
            Staff s = table.getSelectionModel().getSelectedItem();
            if (s != null) {
                s.setActive(!s.isActive());
                dao.save(s);
                loadData();
            }
        });
        deleteItem.setOnAction(e -> {
            Staff s = table.getSelectionModel().getSelectedItem();
            if (s != null && Session.isAdmin()) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete " + s.getName() + "? This cannot be undone.",
                    ButtonType.OK, ButtonType.CANCEL);
                confirm.setTitle("Delete Staff Member");
                DialogHelper.style(confirm);
                confirm.showAndWait().filter(b -> b == ButtonType.OK)
                    .ifPresent(b -> { dao.delete(s.getId()); loadData(); });
            }
        });
        ctx.getItems().addAll(editItem, toggleItem, new SeparatorMenuItem(), deleteItem);
        table.setContextMenu(ctx);

        root.getChildren().addAll(header, toolbar, table);
    }

    @SuppressWarnings("unchecked")
    private void setupColumns() {
        TableColumn<Staff, String> name = new TableColumn<>("Name");
        name.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        name.setPrefWidth(200);

        TableColumn<Staff, String> role = new TableColumn<>("Role");
        role.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRole()));
        role.setPrefWidth(150);

        TableColumn<Staff, String> phone = new TableColumn<>("Phone");
        phone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPhone()));
        phone.setPrefWidth(160);

        TableColumn<Staff, String> status = new TableColumn<>("Status");
        status.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().isActive() ? "Active" : "Inactive"));
        status.setPrefWidth(100);

        table.getColumns().addAll(name, role, phone, status);
    }

    private void loadData() {
        table.getItems().setAll(dao.findAll());
    }

    void showStaffDialog(Staff existing) {
        boolean isEdit = existing != null;
        Dialog<Staff> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Staff" : "Add Staff Member");
        GridPane grid = DialogHelper.createFormGrid();

        TextField nameField = new TextField(isEdit ? existing.getName() : "");
        nameField.setPromptText("Full name");

        ComboBox<String> roleBox = new ComboBox<>(FXCollections.observableArrayList(
            "HOUSEKEEPER", "MAINTENANCE", "SUPERVISOR", "INSPECTOR"));
        roleBox.setValue(isEdit ? existing.getRole() : "HOUSEKEEPER");

        TextField phoneField = new TextField(isEdit && existing.getPhone() != null ? existing.getPhone() : "");
        phoneField.setPromptText("Phone number");

        CheckBox activeCheck = new CheckBox("Active");
        activeCheck.setSelected(isEdit ? existing.isActive() : true);

        grid.addRow(0, new Label("Name:"), nameField);
        grid.addRow(1, new Label("Role:"), roleBox);
        grid.addRow(2, new Label("Phone:"), phoneField);
        grid.add(activeCheck, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        DialogHelper.style(dialog);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK && !nameField.getText().isBlank()) {
                Staff s = isEdit ? existing : new Staff();
                s.setName(nameField.getText().trim());
                s.setRole(roleBox.getValue());
                s.setPhone(phoneField.getText().trim());
                s.setActive(activeCheck.isSelected());
                return s;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(s -> { dao.save(s); loadData(); });
    }

    @SuppressWarnings("exports")
    public VBox getView() { return root; }
}
