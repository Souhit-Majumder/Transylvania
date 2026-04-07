package com.hotel.view;

import com.hotel.dao.UserDAO;
import com.hotel.model.User;
import com.hotel.util.DialogHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class UserManagementView {
    private final VBox root;
    private final TableView<User> table;
    private final UserDAO dao = new UserDAO();

    public UserManagementView() {
        root = new VBox(20);
        root.setPadding(new Insets(32));

        VBox headerText = new VBox(4);
        Label title = new Label("User Management");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Admin-only — manage staff accounts and roles");
        subtitle.getStyleClass().add("page-subtitle");
        headerText.getChildren().addAll(title, subtitle);

        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        Button addBtn = new Button("+ Add User");
        addBtn.getStyleClass().add("btn-primary");
        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("btn-outline");
        toolbar.getChildren().addAll(addBtn, refreshBtn);

        table = new TableView<>();
        VBox.setVgrow(table, Priority.ALWAYS);
        setupColumns();
        loadData();

        addBtn.setOnAction(e -> showUserDialog(null));
        refreshBtn.setOnAction(e -> loadData());

        ContextMenu ctx = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit");
        MenuItem deleteItem = new MenuItem("Delete");
        editItem.setOnAction(e -> { User u = table.getSelectionModel().getSelectedItem(); if (u != null) showUserDialog(u); });
        deleteItem.setOnAction(e -> {
            User u = table.getSelectionModel().getSelectedItem();
            if (u != null && u.getId() != 1) { dao.delete(u.getId()); loadData(); }
        });
        ctx.getItems().addAll(editItem, new SeparatorMenuItem(), deleteItem);
        table.setContextMenu(ctx);

        root.getChildren().addAll(headerText, toolbar, table);
    }

    @SuppressWarnings("unchecked")
    private void setupColumns() {
        TableColumn<User, String> id = new TableColumn<>("ID");
        id.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        id.setPrefWidth(50);
        TableColumn<User, String> user = new TableColumn<>("Username");
        user.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsername()));
        user.setPrefWidth(140);
        TableColumn<User, String> name = new TableColumn<>("Full Name");
        name.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        name.setPrefWidth(200);
        TableColumn<User, String> role = new TableColumn<>("Role");
        role.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRole()));
        role.setPrefWidth(130);
        TableColumn<User, String> active = new TableColumn<>("Status");
        active.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActive() ? "Active" : "Disabled"));
        table.getColumns().addAll(id, user, name, role, active);
    }

    private void loadData() { table.getItems().setAll(dao.findAll()); }

    private void showUserDialog(User existing) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add User" : "Edit User");
        GridPane grid = DialogHelper.createFormGrid();

        TextField userField = new TextField(existing != null ? existing.getUsername() : "");
        PasswordField passField = new PasswordField();
        passField.setPromptText(existing != null ? "(leave blank to keep)" : "Password");
        TextField nameField = new TextField(existing != null ? existing.getFullName() : "");
        ComboBox<String> roleBox = new ComboBox<>(FXCollections.observableArrayList("ADMIN", "MANAGER", "RECEPTIONIST"));
        roleBox.setValue(existing != null ? existing.getRole() : "RECEPTIONIST");
        CheckBox activeCheck = new CheckBox("Active");
        activeCheck.setSelected(existing == null || existing.isActive());

        grid.addRow(0, new Label("Username:"), userField);
        grid.addRow(1, new Label("Password:"), passField);
        grid.addRow(2, new Label("Full Name:"), nameField);
        grid.addRow(3, new Label("Role:"), roleBox);
        grid.add(activeCheck, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        DialogHelper.style(dialog);
        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                User u = existing != null ? existing : new User();
                u.setUsername(userField.getText().trim());
                if (!passField.getText().isEmpty()) u.setPassword(passField.getText());
                else if (existing != null) u.setPassword(existing.getPassword());
                u.setFullName(nameField.getText().trim());
                u.setRole(roleBox.getValue()); u.setActive(activeCheck.isSelected());
                return u;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(u -> { dao.save(u); loadData(); });
    }

    @SuppressWarnings("exports")
    public VBox getView() { return root; }
}
