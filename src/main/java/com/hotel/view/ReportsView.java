package com.hotel.view;

import com.hotel.dao.*;
import com.hotel.model.Room;
import com.hotel.util.AsyncLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.*;

public class ReportsView {
    private final VBox root;

    /**
     * Holds all data fetched from the DB off the JavaFX Application Thread.
     * Using a record so it is immutable and thread-safe by nature.
     */
    private record ReportData(
        double totalRev,
        int totalRes,
        int checkedIn,
        Map<String, Integer> roomStats,
        List<Room> allRooms,
        Map<String, Double> monthlyRevenue
    ) {}

    public ReportsView() {
        root = new VBox(24);
        root.setPadding(new Insets(32));

        // ── Header (always visible immediately) ───────────────────
        VBox headerText = new VBox(4);
        Label title = new Label("Occupancy Analytics");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Revenue trends, room demand, and operational insights");
        subtitle.getStyleClass().add("page-subtitle");
        headerText.getChildren().addAll(title, subtitle);

        // ── Loading spinner (shown while Task runs) ───────────────
        VBox loadingBox = new VBox(16);
        loadingBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(loadingBox, Priority.ALWAYS);
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(56, 56);
        Label loadingLabel = new Label("Loading analytics…");
        loadingLabel.getStyleClass().add("page-subtitle");
        loadingBox.getChildren().addAll(spinner, loadingLabel);

        root.getChildren().addAll(headerText, loadingBox);

        // ── AsyncLoader: all DB queries run off the JAT ───────────
        RoomDAO roomDAO = new RoomDAO();
        ReservationDAO resDAO = new ReservationDAO();
        InvoiceDAO invDAO = new InvoiceDAO();

        AsyncLoader<ReportData> loader = new AsyncLoader<>(() -> new ReportData(
            invDAO.getTotalRevenue(),
            resDAO.findAll().size(),
            resDAO.countByStatus("CHECKED_IN"),
            roomDAO.getStatusCounts(),
            roomDAO.findAll(),
            resDAO.getMonthlyRevenue()
        ));

        loader.setOnSucceeded(e -> {
            root.getChildren().remove(loadingBox);
            buildContent(loader.getValue());
        });

        loader.setOnFailed(e -> {
            loadingLabel.setText("Failed to load data: "
                + (loader.getException() != null ? loader.getException().getMessage() : "unknown error"));
            spinner.setVisible(false);
        });

        loader.start();
    }

    /**
     * Builds and appends all stat cards and charts.
     * Always called on the JavaFX Application Thread via {@code onSucceeded}.
     */
    private void buildContent(ReportData data) {
        // ── Summary stat cards ────────────────────────────────────
        HBox statsRow = new HBox(16);

        Map<String, Integer> roomStats = data.roomStats();
        int totalRooms = roomStats.values().stream().mapToInt(i -> i).sum();
        int available  = roomStats.getOrDefault("AVAILABLE", 0);
        int avPct      = totalRooms > 0 ? (available * 100 / totalRooms) : 0;

        statsRow.getChildren().addAll(
            createStat("REVENUE",     String.format("$%.0f", data.totalRev()),  "+5.4%",               true),
            createStat("BOOKINGS",    String.valueOf(data.totalRes()),           "+12%",                false),
            createStat("CHECKED IN",  String.valueOf(data.checkedIn()),          "active now",          false),
            createStat("AVAILABILITY",avPct + "%",                              available + " rooms left", false)
        );

        // ── Charts row ────────────────────────────────────────────
        HBox chartsRow = new HBox(20);
        VBox.setVgrow(chartsRow, Priority.ALWAYS);

        // Pie: Room Status
        VBox pieCard = new VBox(8);
        pieCard.getStyleClass().add("card");
        HBox.setHgrow(pieCard, Priority.ALWAYS);
        PieChart statusChart = new PieChart();
        statusChart.setTitle("Room Status");
        statusChart.setPrefHeight(280);
        statusChart.setLabelsVisible(true);
        for (var entry : roomStats.entrySet())
            statusChart.getData().add(
                new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
        pieCard.getChildren().add(statusChart);

        // Pie: Room Types
        VBox typeCard = new VBox(8);
        typeCard.getStyleClass().add("card");
        HBox.setHgrow(typeCard, Priority.ALWAYS);
        PieChart typeChart = new PieChart();
        typeChart.setTitle("Room Types");
        typeChart.setPrefHeight(280);
        Map<String, Integer> typeCounts = new LinkedHashMap<>();
        for (var room : data.allRooms()) typeCounts.merge(room.getType(), 1, Integer::sum);
        for (var entry : typeCounts.entrySet())
            typeChart.getData().add(
                new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
        typeCard.getChildren().add(typeChart);

        chartsRow.getChildren().addAll(pieCard, typeCard);

        // Bar: Monthly Revenue
        VBox barCard = new VBox(8);
        barCard.getStyleClass().add("card");
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis   yAxis = new NumberAxis();
        xAxis.setLabel("Month"); yAxis.setLabel("Revenue ($)");
        BarChart<String, Number> revenueChart = new BarChart<>(xAxis, yAxis);
        revenueChart.setTitle("Monthly Revenue");
        revenueChart.setPrefHeight(300);
        revenueChart.setLegendVisible(false);
        XYChart.Series<String, Number> revSeries = new XYChart.Series<>();
        revSeries.setName("Revenue");
        var months = new ArrayList<>(data.monthlyRevenue().entrySet());
        Collections.reverse(months);
        for (var entry : months)
            revSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        revenueChart.getData().add(revSeries);
        barCard.getChildren().add(revenueChart);

        // ── Assemble into scroll ───────────────────────────────────
        VBox headerText = (VBox) root.getChildren().get(0); // preserve existing header
        ScrollPane scroll = new ScrollPane(new VBox(20, statsRow, chartsRow, barCard));
        scroll.getStyleClass().add("content-scroll");
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        root.getChildren().add(scroll);
    }

    private VBox createStat(String label, String value, String detail, boolean highlight) {
        VBox card = new VBox(4);
        card.getStyleClass().add(highlight ? "stat-card" : "stat-card-alt");
        card.setPadding(new Insets(20));
        HBox.setHgrow(card, Priority.ALWAYS);
        Label lbl = new Label(label); lbl.getStyleClass().add("stat-label");
        Label val = new Label(value); val.getStyleClass().add("stat-value");
        Label det = new Label(detail);
        det.setStyle("-fx-font-size: 11; -fx-font-weight: 600; -fx-text-fill: #4A7A6B;");
        card.getChildren().addAll(lbl, val, det);
        return card;
    }

    @SuppressWarnings("exports")
    public VBox getView() { return root; }
}
