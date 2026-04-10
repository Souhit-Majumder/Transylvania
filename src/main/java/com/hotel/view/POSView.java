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

        addChargeBtn.setOnAction(e -> showChargeDialog());
        refreshBtn.setOnAction(e -> loadData());

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

    private void showChargeDialog() {
        Dialog<POSCharge> dialog = new Dialog<>();
        dialog.setTitle("Add POS Charge");
        GridPane grid = DialogHelper.createFormGrid();

        List<Reservation> active = resDAO.findCheckedIn();
        ComboBox<String> resBox = new ComboBox<>();
        for (Reservation r : active)
            resBox.getItems().add(r.getId() + " — " + r.getGuestName() + " (Room " + r.getRoomNumber() + ")");
        resBox.setPromptText("Select active reservation");

        ComboBox<String> catBox = new ComboBox<>(FXCollections.observableArrayList("RESTAURANT", "MINIBAR", "SPA", "LAUNDRY", "OTHER"));
        catBox.setValue("RESTAURANT");
        TextField descField = new TextField();
        descField.setPromptText("e.g., Room service dinner");
        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        Spinner<Integer> qtySpin = new Spinner<>(1, 100, 1);

        grid.addRow(0, new Label("Reservation:"), resBox);
        grid.addRow(1, new Label("Category:"), catBox);
        grid.addRow(2, new Label("Description:"), descField);
        grid.addRow(3, new Label("Amount:"), amountField);
        grid.addRow(4, new Label("Quantity:"), qtySpin);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        DialogHelper.style(dialog);
        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK && resBox.getValue() != null) {
                try {
                    int resId = active.get(resBox.getSelectionModel().getSelectedIndex()).getId();
                    POSCharge c = new POSCharge();
                    c.setReservationId(resId); c.setCategory(catBox.getValue());
                    c.setDescription(descField.getText().trim());
                    c.setAmount(Double.parseDouble(amountField.getText().trim()));
                    c.setQuantity(qtySpin.getValue()); c.setChargedBy(Session.getCurrentUser().getId());
                    return c;
                } catch (Exception ex) { return null; }
            }
            return null;
        });
        dialog.showAndWait().ifPresent(c -> { dao.save(c); loadData(); });
    }

    @SuppressWarnings("exports")
    public VBox getView() { return root; }
}
