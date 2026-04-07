package com.hotel.view;

import com.hotel.dao.*;
import javafx.geometry.Insets;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.Map;

public class ReportsView {
    private final VBox root;

    public ReportsView() {
        root = new VBox(24);
        root.setPadding(new Insets(32));

        VBox headerText = new VBox(4);
        Label title = new Label("Occupancy Analytics");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Revenue trends, room demand, and operational insights");
        subtitle.getStyleClass().add("page-subtitle");
        headerText.getChildren().addAll(title, subtitle);

        RoomDAO roomDAO = new RoomDAO();
        ReservationDAO resDAO = new ReservationDAO();
        InvoiceDAO invDAO = new InvoiceDAO();

        // Summary stats row
        HBox statsRow = new HBox(16);
        double totalRev = invDAO.getTotalRevenue();
        int totalRes = resDAO.findAll().size();
        int checkedIn = resDAO.countByStatus("CHECKED_IN");

        VBox revStat = createStat("REVENUE", String.format("$%.0f", totalRev), "+5.4%", true);
        VBox resStat = createStat("BOOKINGS", String.valueOf(totalRes), "+12%", false);
        VBox ciStat = createStat("CHECKED IN", String.valueOf(checkedIn), "active now", false);

        Map<String, Integer> roomStats = roomDAO.getStatusCounts();
        int totalRooms = roomStats.values().stream().mapToInt(i -> i).sum();
        int available = roomStats.getOrDefault("AVAILABLE", 0);
        int avPct = totalRooms > 0 ? (available * 100 / totalRooms) : 0;
        VBox avStat = createStat("AVAILABILITY", avPct + "%", available + " rooms left", false);

        statsRow.getChildren().addAll(revStat, resStat, ciStat, avStat);

        // Charts row
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
            statusChart.getData().add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
        pieCard.getChildren().add(statusChart);

        // Pie: Room Types
        VBox typeCard = new VBox(8);
        typeCard.getStyleClass().add("card");
        HBox.setHgrow(typeCard, Priority.ALWAYS);
        PieChart typeChart = new PieChart();
        typeChart.setTitle("Room Types");
        typeChart.setPrefHeight(280);
        Map<String, Integer> typeCounts = new java.util.LinkedHashMap<>();
        for (var room : roomDAO.findAll()) typeCounts.merge(room.getType(), 1, Integer::sum);
        for (var entry : typeCounts.entrySet())
            typeChart.getData().add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
        typeCard.getChildren().add(typeChart);

        chartsRow.getChildren().addAll(pieCard, typeCard);

        // Bar: Monthly Revenue
        VBox barCard = new VBox(8);
        barCard.getStyleClass().add("card");
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Month"); yAxis.setLabel("Revenue ($)");
        BarChart<String, Number> revenueChart = new BarChart<>(xAxis, yAxis);
        revenueChart.setTitle("Monthly Revenue");
        revenueChart.setPrefHeight(300);
        revenueChart.setLegendVisible(false);
        XYChart.Series<String, Number> revSeries = new XYChart.Series<>();
        revSeries.setName("Revenue");
        var months = new java.util.ArrayList<>(resDAO.getMonthlyRevenue().entrySet());
        java.util.Collections.reverse(months);
        for (var entry : months)
            revSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        revenueChart.getData().add(revSeries);
        barCard.getChildren().add(revenueChart);

        ScrollPane scroll = new ScrollPane(new VBox(20, headerText, statsRow, chartsRow, barCard));
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
        Label lbl = new Label(label);
        lbl.getStyleClass().add("stat-label");
        Label val = new Label(value);
        val.getStyleClass().add("stat-value");
        Label det = new Label(detail);
        det.setStyle("-fx-font-size: 11; -fx-font-weight: 600; -fx-text-fill: #4A7A6B;");
        card.getChildren().addAll(lbl, val, det);
        return card;
    }

    @SuppressWarnings("exports")
    public VBox getView() { return root; }
}
