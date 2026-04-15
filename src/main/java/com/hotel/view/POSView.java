package com.hotel.view;

import com.hotel.dao.*;
import com.hotel.model.*;
import com.hotel.util.DialogHelper;
import com.hotel.util.Session;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;

public class POSView {
    private final VBox root;
    private final TableView<POSCharge> table;
    private final POSChargeDAO dao = new POSChargeDAO();
    private final ReservationDAO resDAO = new ReservationDAO();

    public POSView() {
        root = new VBox(20);
        root.setPadding(new Insets(32));

        VBox headerText = new VBox(4);
        Label title = new Label("Point of Sale");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Add restaurant, minibar, spa, and laundry charges to guest folios");
        subtitle.getStyleClass().add("page-subtitle");
        headerText.getChildren().addAll(title, subtitle);

        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        Button addChargeBtn = new Button("+ Add Charge");
        addChargeBtn.getStyleClass().add("btn-primary");
        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("btn-outline");
        toolbar.getChildren().addAll(addChargeBtn, refreshBtn);

        table = new TableView<>();
        VBox.setVgrow(table, Priority.ALWAYS);

        table.setMaxWidth(Double.MAX_VALUE);
        table.setMaxHeight(Double.MAX_VALUE);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        setupColumns();
        loadData();

        addChargeBtn.setOnAction(e -> showChargeDialog(null));
        refreshBtn.setOnAction(e -> loadData());

        // Right-click context menu
        ContextMenu ctx = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit Charge");
        MenuItem deleteItem = new MenuItem("Delete Charge");
        editItem.setOnAction(e -> {
            POSCharge c = table.getSelectionModel().getSelectedItem();
            if (c != null) showChargeDialog(c);
        });
        deleteItem.setOnAction(e -> {
            POSCharge c = table.getSelectionModel().getSelectedItem();
            if (c != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete this charge? This cannot be undone.", ButtonType.OK, ButtonType.CANCEL);
                confirm.setTitle("Delete POS Charge");
                DialogHelper.style(confirm);
                confirm.showAndWait().filter(b -> b == ButtonType.OK)
                    .ifPresent(b -> { dao.delete(c.getId()); loadData(); });
            }
        });
        ctx.getItems().addAll(editItem, new SeparatorMenuItem(), deleteItem);
        table.setContextMenu(ctx);

        root.getChildren().addAll(headerText, toolbar, table);
    }

    @SuppressWarnings("unchecked")
    private void setupColumns() {
        TableColumn<POSCharge, String> guest = new TableColumn<>("Guest");
        guest.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getGuestName()));
        guest.setPrefWidth(150);
        TableColumn<POSCharge, String> room = new TableColumn<>("Room");
        room.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoomNumber()));
        TableColumn<POSCharge, String> cat = new TableColumn<>("Category");
        cat.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));
        TableColumn<POSCharge, String> desc = new TableColumn<>("Description");
        desc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescription()));
        desc.setPrefWidth(220);
        TableColumn<POSCharge, Number> amt = new TableColumn<>("Amount");
        amt.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getAmount()));
        TableColumn<POSCharge, String> qty = new TableColumn<>("Qty");
        qty.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getQuantity())));
        TableColumn<POSCharge, String> date = new TableColumn<>("Charged At");
        date.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getChargedAt()));
        table.getColumns().addAll(guest, room, cat, desc, amt, qty, date);
    }

    private void loadData() { table.getItems().setAll(dao.findAll()); }

    private void showChargeDialog(POSCharge existing) {
        boolean isEdit = existing != null;
        Dialog<POSCharge> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit POS Charge" : "Add POS Charge");
        GridPane grid = DialogHelper.createFormGrid();

        List<Reservation> active = resDAO.findCheckedIn();
        ComboBox<String> resBox = new ComboBox<>();
        for (Reservation r : active)
            resBox.getItems().add(r.getId() + " — " + r.getGuestName() + " (Room " + r.getRoomNumber() + ")");

        if (isEdit) {
            // Show which reservation this charge belongs to, but lock it
            resBox.setPromptText("Reservation #" + existing.getReservationId() + " (locked)");
            resBox.setDisable(true);
        } else {
            resBox.setPromptText("Select active reservation");
        }

        ComboBox<String> catBox = new ComboBox<>(FXCollections.observableArrayList("RESTAURANT", "MINIBAR", "SPA", "LAUNDRY", "OTHER"));
        catBox.setValue(isEdit ? existing.getCategory() : "RESTAURANT");
        TextField descField = new TextField(isEdit ? existing.getDescription() : "");
        descField.setPromptText("e.g., Room service dinner");
        TextField amountField = new TextField(isEdit ? String.valueOf(existing.getAmount()) : "");
        amountField.setPromptText("Amount");
        Spinner<Integer> qtySpin = new Spinner<>(1, 100, isEdit ? existing.getQuantity() : 1);

        int row = 0;
        if (!isEdit) grid.addRow(row++, new Label("Reservation:"), resBox);
        grid.addRow(row++, new Label("Category:"), catBox);
        grid.addRow(row++, new Label("Description:"), descField);
        grid.addRow(row++, new Label("Amount:"), amountField);
        grid.addRow(row++, new Label("Quantity:"), qtySpin);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        DialogHelper.style(dialog);
        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    if (isEdit) {
                        existing.setCategory(catBox.getValue());
                        existing.setDescription(descField.getText().trim());
                        existing.setAmount(Double.parseDouble(amountField.getText().trim()));
                        existing.setQuantity(qtySpin.getValue());
                        return existing;
                    } else if (resBox.getValue() != null) {
                        int resId = active.get(resBox.getSelectionModel().getSelectedIndex()).getId();
                        POSCharge c = new POSCharge();
                        c.setReservationId(resId); c.setCategory(catBox.getValue());
                        c.setDescription(descField.getText().trim());
                        c.setAmount(Double.parseDouble(amountField.getText().trim()));
                        c.setQuantity(qtySpin.getValue()); c.setChargedBy(Session.getCurrentUser().getId());
                        return c;
                    }
                } catch (Exception ex) { return null; }
            }
            return null;
        });
        dialog.showAndWait().ifPresent(c -> {
            if (isEdit) dao.update(c);
            else dao.save(c);
            loadData();
        });
    }

    @SuppressWarnings("exports")
    public VBox getView() { return root; }
}
