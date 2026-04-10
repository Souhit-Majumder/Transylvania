package com.hotel.view;

import com.hotel.dao.*;
import com.hotel.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DashboardView {
    private final VBox root;

    public DashboardView() {
        root = new VBox(24);
        root.setPadding(new Insets(32));

        RoomDAO roomDAO = new RoomDAO();
        ReservationDAO resDAO = new ReservationDAO();
        HousekeepingDAO hkDAO = new HousekeepingDAO();
        String today = LocalDate.now().toString();
        String todayFormatted = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));

        Map<String, Integer> roomStats = roomDAO.getStatusCounts();
        int totalRooms = roomStats.values().stream().mapToInt(i -> i).sum();
        int occupied = roomStats.getOrDefault("OCCUPIED", 0);
        int available = roomStats.getOrDefault("AVAILABLE", 0);
        int maintenance = roomStats.getOrDefault("MAINTENANCE", 0);
        int needsCleaning = roomStats.getOrDefault("NEEDS_CLEANING", 0);
        int occupancyPct = totalRooms > 0 ? (occupied * 100 / totalRooms) : 0;

        List<Reservation> todayCheckIns = resDAO.findTodayCheckIns(today);
        List<Reservation> todayCheckOuts = resDAO.findTodayCheckOuts(today);
        List<HousekeepingTask> pendingTasks = hkDAO.findPending();

        // ── Header ──
        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.BOTTOM_LEFT);
        VBox headerText = new VBox(4);
        Label title = new Label("Daily Pulse");
        title.getStyleClass().add("page-title");
        Label dateLine = new Label(todayFormatted);
        dateLine.getStyleClass().add("page-subtitle");
        headerText.getChildren().addAll(title, dateLine);
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        HBox liveIndicator = new HBox(8);
        liveIndicator.setAlignment(Pos.CENTER);
        liveIndicator.getStyleClass().add("live-indicator");
        Region liveDot = new Region();
        liveDot.getStyleClass().add("live-dot");
        Label liveText = new Label("SYSTEM LIVE");
        liveText.getStyleClass().add("live-text");
        liveIndicator.getChildren().addAll(liveDot, liveText);

        headerRow.getChildren().addAll(headerText, headerSpacer, liveIndicator);

        // ── Bento Stats Grid ──
        HBox statsRow = new HBox(16);

        // Occupancy card (wide)
        VBox occCard = new VBox(8);
        occCard.getStyleClass().add("stat-card");
        HBox.setHgrow(occCard, Priority.ALWAYS);
        occCard.setMinWidth(260);
        Label occKicker = new Label("OCCUPANCY TODAY");
        occKicker.getStyleClass().add("stat-label");
        Label occValue = new Label(occupancyPct + "%");
        occValue.getStyleClass().add("stat-value-primary");
        ProgressBar occBar = new ProgressBar(occupancyPct / 100.0);
        occBar.setMaxWidth(Double.MAX_VALUE);
        occBar.setPrefHeight(10);
        Label occDetail = new Label("+" + (totalRooms > 0 ? occupied : 0) + " of " + totalRooms + " rooms filled");
        occDetail.getStyleClass().add("stat-detail");
        occCard.getChildren().addAll(occKicker, occValue, occBar, occDetail);

        // Check-ins card
        VBox ciCard = createStatCard("CHECK-INS", String.valueOf(todayCheckIns.size()),
                todayCheckIns.size() + " remaining today");
        ciCard.getStyleClass().add("stat-card-alt");
        HBox.setHgrow(ciCard, Priority.ALWAYS);

        // Check-outs card
        VBox coCard = createStatCard("CHECK-OUTS", String.valueOf(todayCheckOuts.size()),
                "pending inspections");
        coCard.getStyleClass().add("stat-card-alt");
        HBox.setHgrow(coCard, Priority.ALWAYS);

        statsRow.getChildren().addAll(occCard, ciCard, coCard);

        // ── Second stats row ──
        HBox statsRow2 = new HBox(16);
        VBox avCard = createStatCard("AVAILABLE", String.valueOf(available), "rooms ready");
        avCard.getStyleClass().add("stat-card-alt");
        HBox.setHgrow(avCard, Priority.ALWAYS);
        VBox mtCard = createStatCard("MAINTENANCE", String.valueOf(maintenance), "rooms offline");
        mtCard.getStyleClass().add("stat-card-alt");
        HBox.setHgrow(mtCard, Priority.ALWAYS);
        VBox clCard = createStatCard("NEEDS CLEANING", String.valueOf(needsCleaning), "rooms pending");
        clCard.getStyleClass().add("stat-card-alt");
        HBox.setHgrow(clCard, Priority.ALWAYS);
        VBox hkCard = createStatCard("HK TASKS", String.valueOf(pendingTasks.size()), "active tasks");
        hkCard.getStyleClass().add("stat-card-alt");
        HBox.setHgrow(hkCard, Priority.ALWAYS);
        statsRow2.getChildren().addAll(avCard, mtCard, clCard, hkCard);

        // ── Main content grid ──
        HBox mainGrid = new HBox(24);
        VBox.setVgrow(mainGrid, Priority.ALWAYS);

        // Left: tables
        VBox leftCol = new VBox(24);
        HBox.setHgrow(leftCol, Priority.ALWAYS);

        // Check-ins table
        VBox ciSection = new VBox(12);
        ciSection.getStyleClass().add("card");
        HBox ciHeader = new HBox();
        ciHeader.setAlignment(Pos.CENTER_LEFT);
        Label ciTitle = new Label("Expected Check-Ins Today");
        ciTitle.getStyleClass().add("section-title");
        Region ciSpacer = new Region();
        HBox.setHgrow(ciSpacer, Priority.ALWAYS);
        Label ciBadge = new Label(todayCheckIns.size() + " Pending");
        ciBadge.getStyleClass().add("section-badge");
        ciHeader.getChildren().addAll(ciTitle, ciSpacer, ciBadge);
        TableView<Reservation> ciTable = buildResTable();
        ciTable.getItems().addAll(todayCheckIns);
        ciTable.setPrefHeight(180);
        ciSection.getChildren().addAll(ciHeader, ciTable);

        // Check-outs table
        VBox coSection = new VBox(12);
        coSection.getStyleClass().add("card");
        HBox coHeader = new HBox();
        coHeader.setAlignment(Pos.CENTER_LEFT);
        Label coTitle = new Label("Expected Check-Outs Today");
        coTitle.getStyleClass().add("section-title");
        Region coSpacer = new Region();
        HBox.setHgrow(coSpacer, Priority.ALWAYS);
        Label coBadge = new Label(todayCheckOuts.size() + " Pending");
        coBadge.getStyleClass().add("section-badge");
        coHeader.getChildren().addAll(coTitle, coSpacer, coBadge);
        TableView<Reservation> coTable = buildResTable();
        coTable.getItems().addAll(todayCheckOuts);
        coTable.setPrefHeight(180);
        coSection.getChildren().addAll(coHeader, coTable);

        leftCol.getChildren().addAll(ciSection, coSection);

        // Right: Housekeeping tasks
        VBox rightCol = new VBox(12);
        rightCol.getStyleClass().add("card");
        rightCol.setPrefWidth(340);
        rightCol.setMinWidth(300);

        HBox hkHeader = new HBox();
        hkHeader.setAlignment(Pos.CENTER_LEFT);
        Label hkTitle = new Label("Housekeeping");
        hkTitle.getStyleClass().add("section-title");
        Region hkSpacer = new Region();
        HBox.setHgrow(hkSpacer, Priority.ALWAYS);
        Label hkBadge = new Label(pendingTasks.size() + " Active");
        hkBadge.getStyleClass().add("section-badge");
        hkHeader.getChildren().addAll(hkTitle, hkSpacer, hkBadge);
        rightCol.getChildren().add(hkHeader);

        for (int i = 0; i < Math.min(pendingTasks.size(), 6); i++) {
            HousekeepingTask task = pendingTasks.get(i);
            HBox taskRow = new HBox(14);
            taskRow.getStyleClass().add("task-row");
            taskRow.setAlignment(Pos.CENTER_LEFT);

            StackPane taskIcon = new StackPane();
            taskIcon.getStyleClass().add("task-icon");
            Label iconLabel = new Label(task.getTaskType().equals("CLEANING") ? "🧹" : "🔧");
            taskIcon.getChildren().add(iconLabel);

            VBox taskInfo = new VBox(2);
            HBox.setHgrow(taskInfo, Priority.ALWAYS);
            Label taskName = new Label("Room " + task.getRoomNumber() + " — " + task.getTaskType());
            taskName.setStyle("-fx-font-size: 12; -fx-font-weight: 700; -fx-text-fill: #334155;");
            Label taskDetail = new Label("Priority: " + task.getPriority() +
                    (task.getAssignedTo() != null ? " • " + task.getAssignedTo() : ""));
            taskDetail.getStyleClass().add("kicker");
            taskInfo.getChildren().addAll(taskName, taskDetail);

            Label chevron = new Label("›");
            chevron.setStyle("-fx-font-size: 18; -fx-text-fill: #cbd5e1;");

            taskRow.getChildren().addAll(taskIcon, taskInfo, chevron);
            rightCol.getChildren().add(taskRow);
        }

        if (pendingTasks.isEmpty()) {
            Label noTasks = new Label("All clear — no pending tasks");
            noTasks.getStyleClass().add("stat-detail");
            noTasks.setPadding(new Insets(20, 0, 0, 0));
            rightCol.getChildren().add(noTasks);
        }

        Button viewQueueBtn = new Button("View Full Queue");
        viewQueueBtn.setOnAction(null);
        viewQueueBtn.getStyleClass().addAll("btn-outline", "btn-small");
        viewQueueBtn.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(viewQueueBtn, new Insets(12, 0, 0, 0));
        rightCol.getChildren().add(viewQueueBtn);

        mainGrid.getChildren().addAll(leftCol, rightCol);

        root.getChildren().addAll(headerRow, statsRow, statsRow2, mainGrid);
    }

    private VBox createStatCard(String label, String value, String detail) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(24));
        Label lbl = new Label(label);
        lbl.getStyleClass().add("stat-label");
        Label val = new Label(value);
        val.getStyleClass().add("stat-value");
        Label det = new Label(detail);
        det.getStyleClass().add("stat-detail");
        card.getChildren().addAll(lbl, val, det);
        return card;
    }

    @SuppressWarnings("unchecked")
    private TableView<Reservation> buildResTable() {
        TableView<Reservation> table = new TableView<>();
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
        status.setPrefWidth(100);
        table.getColumns().addAll(guest, room, in, out, status);
        table.setPlaceholder(new Label("No entries for today"));
        return table;
    }

    @SuppressWarnings("exports")
    public VBox getView() {
        return root;
    }
}
