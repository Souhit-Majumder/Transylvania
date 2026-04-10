package com.hotel.view;

import com.hotel.dao.*;
import com.hotel.model.*;
import com.hotel.util.DialogHelper;
import com.hotel.util.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalDate;

public class ReservationView {
    private final VBox root;
    private final TableView<Reservation> table;
    private final ReservationDAO resDAO = new ReservationDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final GuestDAO guestDAO = new GuestDAO();

    public ReservationView() {
        root = new VBox(20);
        root.setPadding(new Insets(32));

        HBox header = new HBox();
        header.setAlignment(Pos.BOTTOM_LEFT);
        VBox headerText = new VBox(4);
        Label title = new Label("Booking Calendar");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Manage reservations, check-ins, and check-outs");
        subtitle.getStyleClass().add("page-subtitle");
        headerText.getChildren().addAll(title, subtitle);
        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);
        header.getChildren().addAll(headerText, hSpacer);

        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        Button newBookingBtn = new Button("+ New Booking");
        newBookingBtn.getStyleClass().add("btn-primary");

        HBox filterGroup = new HBox(2);
        filterGroup.getStyleClass().add("segment-group");
        String[] statuses = { "All", "RESERVED", "CHECKED_IN", "CHECKED_OUT", "CANCELLED" };
        ComboBox<String> filterBox = new ComboBox<>(FXCollections.observableArrayList(statuses));
        filterBox.setValue("All");

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("btn-outline");
        toolbar.getChildren().addAll(newBookingBtn, new Label("Status:"), filterBox, refreshBtn);

        // Legend
        HBox legend = new HBox(20);
        legend.setPadding(new Insets(0, 0, 0, 4));
        legend.getChildren().addAll(
                legendDot("#A4B5E0", "Confirmed"),
                legendDot("#7D86A3", "Checked-In"),
                legendDot("#FCE9C2", "Pending"));

        table = new TableView<>();
        VBox.setVgrow(table, Priority.ALWAYS);

        table.setMaxWidth(Double.MAX_VALUE);
        table.setMaxHeight(Double.MAX_VALUE);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        setupColumns();
        loadData();

        newBookingBtn.setOnAction(e -> showBookingDialog());
        refreshBtn.setOnAction(e -> loadData());
        filterBox.setOnAction(e -> {
            String f = filterBox.getValue();
            if ("All".equals(f))
                loadData();
            else
                table.getItems().setAll(resDAO.findAll().stream().filter(r -> r.getStatus().equals(f)).toList());
        });

        ContextMenu ctx = new ContextMenu();
        MenuItem checkInItem = new MenuItem("Check In");
        MenuItem checkOutItem = new MenuItem("Check Out");
        MenuItem cancelItem = new MenuItem("Cancel Reservation");
        MenuItem noShowItem = new MenuItem("Mark No-Show");
        MenuItem viewInvoice = new MenuItem("View / Generate Invoice");

        checkInItem.setOnAction(e -> {
            Reservation r = table.getSelectionModel().getSelectedItem();
            if (r != null && "RESERVED".equals(r.getStatus())) {
                resDAO.checkIn(r.getId());
                roomDAO.updateStatus(r.getRoomId(), "OCCUPIED");
                loadData();
            }
        });
        checkOutItem.setOnAction(e -> {
            Reservation r = table.getSelectionModel().getSelectedItem();
            if (r != null && "CHECKED_IN".equals(r.getStatus())) {
                resDAO.checkOut(r.getId());
                roomDAO.updateStatus(r.getRoomId(), "NEEDS_CLEANING");
                loadData();
            }
        });
        cancelItem.setOnAction(e -> {
            Reservation r = table.getSelectionModel().getSelectedItem();
            if (r != null && "RESERVED".equals(r.getStatus())) {
                resDAO.updateStatus(r.getId(), "CANCELLED");
                loadData();
            }
        });
        noShowItem.setOnAction(e -> {
            Reservation r = table.getSelectionModel().getSelectedItem();
            if (r != null && "RESERVED".equals(r.getStatus())) {
                resDAO.updateStatus(r.getId(), "NO_SHOW");
                loadData();
            }
        });
        viewInvoice.setOnAction(e -> {
            Reservation r = table.getSelectionModel().getSelectedItem();
            if (r != null)
                showInvoiceDialog(r);
        });

        ctx.getItems().addAll(checkInItem, checkOutItem, new SeparatorMenuItem(), cancelItem, noShowItem,
                new SeparatorMenuItem(), viewInvoice);
        table.setContextMenu(ctx);

        root.getChildren().addAll(header, toolbar, legend, table);
    }

    private HBox legendDot(String color, String text) {
        HBox h = new HBox(6);
        h.setAlignment(Pos.CENTER_LEFT);
        Region dot = new Region();
        dot.getStyleClass().add("legend-dot");
        dot.setStyle("-fx-background-color: " + color + ";");
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 11; -fx-font-weight: 600; -fx-text-fill: #64748b;");
        h.getChildren().addAll(dot, lbl);
        return h;
    }

    @SuppressWarnings("unchecked")
    private void setupColumns() {
        TableColumn<Reservation, String> id = new TableColumn<>("ID");
        id.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        id.setPrefWidth(50);
        TableColumn<Reservation, String> guest = new TableColumn<>("Guest");
        guest.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getGuestName()));
        guest.setPrefWidth(160);
        TableColumn<Reservation, String> room = new TableColumn<>("Room");
        room.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoomNumber()));
        room.setPrefWidth(80);
        TableColumn<Reservation, String> in = new TableColumn<>("Check-In");
        in.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCheckInDate()));
        in.setPrefWidth(110);
        TableColumn<Reservation, String> out = new TableColumn<>("Check-Out");
        out.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCheckOutDate()));
        out.setPrefWidth(110);
        TableColumn<Reservation, String> status = new TableColumn<>("Status");
        status.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        status.setPrefWidth(110);
        TableColumn<Reservation, String> adults = new TableColumn<>("Adults");
        adults.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getAdults())));
        adults.setPrefWidth(60);
        table.getColumns().addAll(id, guest, room, in, out, status, adults);
    }

    private void loadData() {
        table.getItems().setAll(resDAO.findAll());
    }

    private void showBookingDialog() {
        Dialog<Reservation> dialog = new Dialog<>();
        dialog.setTitle("New Booking");
        GridPane grid = DialogHelper.createFormGrid();

        ComboBox<Guest> guestBox = new ComboBox<>();
        guestBox.getItems().addAll(guestDAO.findAll());
        guestBox.setPromptText("Select Guest");
        Button newGuestBtn = new Button("+");
        newGuestBtn.getStyleClass().addAll("btn-outline", "btn-small");
        HBox guestRow = new HBox(8, guestBox, newGuestBtn);

        DatePicker checkInPicker = new DatePicker(LocalDate.now());
        DatePicker checkOutPicker = new DatePicker(LocalDate.now().plusDays(1));

        ComboBox<String> typeFilter = new ComboBox<>(
                FXCollections.observableArrayList("", "SINGLE", "DOUBLE", "SUITE", "DELUXE", "PENTHOUSE"));
        typeFilter.setPromptText("Any Type");

        ComboBox<Room> roomBox = new ComboBox<>();
        roomBox.setPromptText("Select Room");

        Runnable updateRooms = () -> {
            if (checkInPicker.getValue() != null && checkOutPicker.getValue() != null) {
                roomBox.getItems().setAll(roomDAO.findAvailable(
                        checkInPicker.getValue().toString(), checkOutPicker.getValue().toString(),
                        typeFilter.getValue()));
            }
        };
        checkInPicker.setOnAction(e -> updateRooms.run());
        checkOutPicker.setOnAction(e -> updateRooms.run());
        typeFilter.setOnAction(e -> updateRooms.run());
        updateRooms.run();

        Spinner<Integer> adultsSpin = new Spinner<>(1, 10, 1);
        Spinner<Integer> childrenSpin = new Spinner<>(0, 10, 0);
        TextArea requestsArea = new TextArea();
        requestsArea.setPrefRowCount(2);
        requestsArea.setPromptText("Special requests...");

        newGuestBtn.setOnAction(e -> {
            new GuestView().showGuestDialog(null);
            guestBox.getItems().setAll(guestDAO.findAll());
        });

        int row = 0;
        grid.addRow(row++, new Label("Guest:"), guestRow);
        grid.addRow(row++, new Label("Check-In:"), checkInPicker);
        grid.addRow(row++, new Label("Check-Out:"), checkOutPicker);
        grid.addRow(row++, new Label("Room Type:"), typeFilter);
        grid.addRow(row++, new Label("Room:"), roomBox);
        grid.addRow(row++, new Label("Adults:"), adultsSpin);
        grid.addRow(row++, new Label("Children:"), childrenSpin);
        grid.addRow(row++, new Label("Requests:"), requestsArea);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        DialogHelper.style(dialog);
        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK && guestBox.getValue() != null && roomBox.getValue() != null) {
                Reservation r = new Reservation();
                r.setGuestId(guestBox.getValue().getId());
                r.setRoomId(roomBox.getValue().getId());
                r.setCheckInDate(checkInPicker.getValue().toString());
                r.setCheckOutDate(checkOutPicker.getValue().toString());
                r.setStatus("RESERVED");
                r.setAdults(adultsSpin.getValue());
                r.setChildren(childrenSpin.getValue());
                r.setSpecialRequests(requestsArea.getText());
                r.setCreatedBy(Session.getCurrentUser().getId());
                return r;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(r -> {
            int id = resDAO.save(r);
            if (id > 0) {
                roomDAO.updateStatus(r.getRoomId(), "RESERVED");
                loadData();
            }
        });
    }

    private void showInvoiceDialog(Reservation res) {
        InvoiceDAO invDAO = new InvoiceDAO();
        POSChargeDAO posDAO = new POSChargeDAO();
        Invoice invoice = invDAO.findByReservation(res.getId());

        if (invoice == null) {
            Room room = roomDAO.findById(res.getRoomId());
            long nights = java.time.temporal.ChronoUnit.DAYS.between(
                    LocalDate.parse(res.getCheckInDate()), LocalDate.parse(res.getCheckOutDate()));
            if (nights < 1)
                nights = 1;
            double roomTotal = room.getBasePrice() * nights;
            double posTotal = posDAO.getTotalByReservation(res.getId());
            double subtotal = roomTotal + posTotal;
            double taxRate = 0.125;
            double tax = subtotal * taxRate;
            double total = subtotal + tax;

            invoice = new Invoice();
            invoice.setReservationId(res.getId());
            invoice.setSubtotal(subtotal);
            invoice.setTaxRate(taxRate);
            invoice.setTaxAmount(tax);
            invoice.setTotal(total);
            invoice.setPaymentStatus("PENDING");
            int invId = invDAO.save(invoice);
            invoice.setId(invId);

            InvoiceItem roomItem = new InvoiceItem();
            roomItem.setInvoiceId(invId);
            roomItem.setDescription("Accommodation — Room " + room.getRoomNumber() + " (" + room.getType() + ")");
            roomItem.setCategory("ROOM");
            roomItem.setQuantity((int) nights);
            roomItem.setUnitPrice(room.getBasePrice());
            roomItem.setTotal(roomTotal);
            invDAO.addItem(roomItem);

            for (POSCharge charge : posDAO.findByReservation(res.getId())) {
                InvoiceItem posItem = new InvoiceItem();
                posItem.setInvoiceId(invId);
                posItem.setDescription("Extra Services — " + charge.getDescription());
                posItem.setCategory(charge.getCategory());
                posItem.setQuantity(charge.getQuantity());
                posItem.setUnitPrice(charge.getAmount());
                posItem.setTotal(charge.getAmount() * charge.getQuantity());
                invDAO.addItem(posItem);
            }
            invoice = invDAO.findByReservation(res.getId());
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Invoice #INV-" + invoice.getId());
        alert.setHeaderText("Guest: " + res.getGuestName() + " • Room: " + res.getRoomNumber());
        StringBuilder sb = new StringBuilder();
        sb.append("─── LINE ITEMS ───\n\n");
        for (InvoiceItem item : invoice.getItems()) {
            sb.append(String.format("  %s\n  Qty: %d × $%.2f = $%.2f\n\n",
                    item.getDescription(), item.getQuantity(), item.getUnitPrice(), item.getTotal()));
        }
        sb.append(String.format("───────────────────\n"));
        sb.append(String.format("Subtotal:      $%.2f\n", invoice.getSubtotal()));
        sb.append(String.format("Tax (%.1f%%):    $%.2f\n", invoice.getTaxRate() * 100, invoice.getTaxAmount()));
        sb.append(String.format("Discount:      $%.2f\n", invoice.getDiscount()));
        sb.append(String.format("───────────────────\n"));
        sb.append(String.format("TOTAL DUE:     $%.2f\n", invoice.getTotal()));
        sb.append(String.format("Paid:          $%.2f\n", invoice.getPaidAmount()));
        sb.append(String.format("Status:        %s", invoice.getPaymentStatus()));

        TextArea content = new TextArea(sb.toString());
        content.setEditable(false);
        content.setPrefWidth(500);
        content.setPrefHeight(350);
        alert.getDialogPane().setContent(content);
        DialogHelper.style(alert);
        alert.showAndWait();
    }

    @SuppressWarnings("exports")
    public VBox getView() {
        return root;
    }
}
