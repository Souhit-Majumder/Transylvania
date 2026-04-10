package com.hotel.view;

import com.hotel.dao.InvoiceDAO;
import com.hotel.model.Invoice;
import com.hotel.util.DialogHelper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class BillingView {
    private final VBox root;
    private final TableView<Invoice> table;
    private final InvoiceDAO dao = new InvoiceDAO();

    public BillingView() {
        root = new VBox(20);
        root.setPadding(new Insets(32));

        HBox header = new HBox();
        header.setAlignment(Pos.BOTTOM_LEFT);
        VBox headerText = new VBox(4);
        Label title = new Label("Guest Folio & Invoicing");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Billing • Settlement Controls");
        subtitle.getStyleClass().add("page-subtitle");
        headerText.getChildren().addAll(title, subtitle);
        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);

        HBox actions = new HBox(10);
        Button exportBtn = new Button("Export PDF");
        exportBtn.getStyleClass().add("btn-outline");
        Button confirmBtn = new Button("Confirm Payment");
        confirmBtn.getStyleClass().add("btn-primary");
        actions.getChildren().addAll(exportBtn, confirmBtn);
        header.getChildren().addAll(headerText, hSpacer, actions);

        // Revenue card
        HBox revCard = new HBox(16);
        revCard.getStyleClass().add("stat-card");
        revCard.setAlignment(Pos.CENTER_LEFT);
        double totalRev = dao.getTotalRevenue();
        Label revKicker = new Label("TOTAL REVENUE COLLECTED");
        revKicker.getStyleClass().add("stat-label");
        Label revValue = new Label(String.format("$%.2f", totalRev));
        revValue.getStyleClass().add("stat-value-primary");
        VBox revInfo = new VBox(4, revKicker, revValue);
        revCard.getChildren().add(revInfo);

        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String> filterBox = new ComboBox<>(
                FXCollections.observableArrayList("All", "PAID", "PENDING", "PARTIAL", "REFUNDED"));
        filterBox.setValue("All");
        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("btn-ghost");
        toolbar.getChildren().addAll(new Label("Status:"), filterBox, refreshBtn);

        table = new TableView<>();
        VBox.setVgrow(table, Priority.ALWAYS);

        table.setMaxWidth(Double.MAX_VALUE);
        table.setMaxHeight(Double.MAX_VALUE);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        setupColumns();
        loadData();

        refreshBtn.setOnAction(e -> loadData());
        filterBox.setOnAction(e -> {
            String f = filterBox.getValue();
            if ("All".equals(f))
                loadData();
            else
                table.getItems().setAll(dao.findAll().stream().filter(i -> i.getPaymentStatus().equals(f)).toList());
        });

        confirmBtn.setOnAction(e -> {
            Invoice inv = table.getSelectionModel().getSelectedItem();
            if (inv != null)
                showPaymentDialog(inv);
        });

        table.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Invoice inv = table.getSelectionModel().getSelectedItem();
                if (inv != null)
                    showPaymentDialog(inv);
            }
        });

        root.getChildren().addAll(header, revCard, toolbar, table);
    }

    @SuppressWarnings("unchecked")
    private void setupColumns() {
        TableColumn<Invoice, String> id = new TableColumn<>("Inv #");
        id.setCellValueFactory(c -> new SimpleStringProperty("#INV-" + c.getValue().getId()));
        id.setPrefWidth(90);
        TableColumn<Invoice, String> guest = new TableColumn<>("Guest");
        guest.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getGuestName()));
        guest.setPrefWidth(160);
        TableColumn<Invoice, String> room = new TableColumn<>("Room");
        room.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoomNumber()));
        TableColumn<Invoice, Number> total = new TableColumn<>("Total Due");
        total.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getTotal()));
        TableColumn<Invoice, Number> paid = new TableColumn<>("Paid");
        paid.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPaidAmount()));
        TableColumn<Invoice, String> status = new TableColumn<>("Status");
        status.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPaymentStatus()));
        TableColumn<Invoice, String> method = new TableColumn<>("Method");
        method.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPaymentMethod()));
        TableColumn<Invoice, String> date = new TableColumn<>("Date");
        date.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getGeneratedAt()));
        table.getColumns().addAll(id, guest, room, total, paid, status, method, date);
    }

    private void loadData() {
        table.getItems().setAll(dao.findAll());
    }

    private void showPaymentDialog(Invoice inv) {
        Dialog<Invoice> dialog = new Dialog<>();
        dialog.setTitle("Settlement — Invoice #INV-" + inv.getId());
        GridPane grid = DialogHelper.createFormGrid();

        double remaining = inv.getTotal() - inv.getPaidAmount();
        Label totalLabel = new Label(String.format("Total Due: $%.2f", inv.getTotal()));
        totalLabel.getStyleClass().add("section-title");
        Label paidLabel = new Label(String.format("Already Paid: $%.2f", inv.getPaidAmount()));
        paidLabel.getStyleClass().add("page-subtitle");
        Label remLabel = new Label(String.format("Remaining: $%.2f", remaining));
        remLabel.setStyle("-fx-font-weight: 700; -fx-text-fill: #A35C50; -fx-font-size: 13;");

        TextField amountField = new TextField(String.format("%.2f", remaining));
        ComboBox<String> methodBox = new ComboBox<>(FXCollections.observableArrayList(
                "Credit Card", "Corporate Billing", "Digital Wallet", "Cash / Direct Deposit"));
        methodBox.setValue(inv.getPaymentMethod() != null ? inv.getPaymentMethod() : "Credit Card");
        TextField discountField = new TextField(String.valueOf(inv.getDiscount()));

        grid.add(totalLabel, 0, 0, 2, 1);
        grid.add(paidLabel, 0, 1, 2, 1);
        grid.add(remLabel, 0, 2, 2, 1);
        grid.addRow(3, new Label("Payment Amount:"), amountField);
        grid.addRow(4, new Label("Method:"), methodBox);
        grid.addRow(5, new Label("Discount:"), discountField);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        DialogHelper.style(dialog);
        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    double amount = Double.parseDouble(amountField.getText());
                    double discount = Double.parseDouble(discountField.getText());
                    inv.setPaidAmount(inv.getPaidAmount() + amount);
                    inv.setDiscount(discount);
                    inv.setPaymentMethod(methodBox.getValue());
                    double effectiveTotal = inv.getTotal() - discount;
                    inv.setPaymentStatus(inv.getPaidAmount() >= effectiveTotal ? "PAID"
                            : inv.getPaidAmount() > 0 ? "PARTIAL" : "PENDING");
                    return inv;
                } catch (Exception ex) {
                    return null;
                }
            }
            return null;
        });
        dialog.showAndWait().ifPresent(i -> {
            dao.save(i);
            loadData();
        });
    }

    @SuppressWarnings("exports")
    public VBox getView() {
        return root;
    }
}
