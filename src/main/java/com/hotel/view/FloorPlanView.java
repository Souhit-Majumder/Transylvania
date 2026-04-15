package com.hotel.view;

import com.hotel.dao.HousekeepingDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.model.HousekeepingTask;
import com.hotel.model.Room;
import com.hotel.util.DialogHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.*;

public class FloorPlanView {
    private final VBox root;
    private final RoomDAO dao = new RoomDAO();
    private final HousekeepingDAO hkDAO = new HousekeepingDAO();
    private Runnable buildPlan;

    public FloorPlanView() {
        root = new VBox(20);
        root.setPadding(new Insets(32));

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.BOTTOM_LEFT);
        VBox headerText = new VBox(4);
        Label title = new Label("Interactive Floor Layout");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Spatial architectural view — click any room for details");
        subtitle.getStyleClass().add("page-subtitle");
        headerText.getChildren().addAll(title, subtitle);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Legend
        HBox legend = new HBox(16);
        legend.setAlignment(Pos.CENTER);
        legend.getStyleClass().add("live-indicator");
        legend.getChildren().addAll(
            legendItem("#C3E8DD", "Available"),
            legendItem("#F9D1C9", "Occupied"),
            legendItem("#cbd5e1", "Reserved"),
            legendItem("#FCE9C2", "Cleaning"),
            legendItem("#e2e8f0", "Service")
        );
        header.getChildren().addAll(headerText, spacer, legend);

        // Floor selector
        HBox floorSelector = new HBox(2);
        floorSelector.getStyleClass().add("segment-group");
        floorSelector.setMaxWidth(Region.USE_PREF_SIZE);

        ScrollPane scroll = new ScrollPane();
        scroll.getStyleClass().add("content-scroll");
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox floorsContainer = new VBox(28);
        floorsContainer.setPadding(new Insets(12));

        buildPlan = () -> {
            floorsContainer.getChildren().clear();
            floorSelector.getChildren().clear();
            List<Room> rooms = dao.findAll();
            Map<Integer, List<Room>> byFloor = new TreeMap<>();
            for (Room r : rooms) byFloor.computeIfAbsent(r.getFloor(), k -> new ArrayList<>()).add(r);

            // Build floor sections first so buttons can reference them
            Map<Integer, VBox> floorSections = new java.util.LinkedHashMap<>();
            for (var entry : byFloor.entrySet()) {
                VBox floorSection = new VBox(12);
                floorSection.getStyleClass().add("card");

                Label floorLabel = new Label("Floor " + entry.getKey());
                floorLabel.getStyleClass().add("section-title");

                FlowPane roomsPane = new FlowPane(8, 8);
                roomsPane.setPadding(new Insets(8, 0, 0, 0));

                for (Room room : entry.getValue()) {
                    Button roomBtn = new Button(room.getRoomNumber() + "\n" + room.getType());
                    roomBtn.getStyleClass().add("room-node");
                    roomBtn.getStyleClass().add(getStatusClass(room.getStatus()));
                    roomBtn.setPrefSize(100, 64);
                    roomBtn.setMinSize(100, 64);

                    Tooltip tip = new Tooltip(
                        "Room: " + room.getRoomNumber() +
                        "\nType: " + room.getType() +
                        "\nStatus: " + room.getStatus() +
                        "\nPrice: $" + room.getBasePrice() +
                        "\nMax Occupancy: " + room.getMaxOccupancy());
                    Tooltip.install(roomBtn, tip);

                    roomBtn.setOnAction(e -> showRoomDetails(room, buildPlan));
                    roomsPane.getChildren().add(roomBtn);
                }
                floorSection.getChildren().addAll(floorLabel, roomsPane);
                floorsContainer.getChildren().add(floorSection);
                floorSections.put(entry.getKey(), floorSection);
            }

            // Floor selector buttons: toggle active style + scroll to section
            boolean first = true;
            for (var entry : byFloor.entrySet()) {
                final int floorNum = entry.getKey();
                Button floorBtn = new Button("Floor " + floorNum);
                floorBtn.getStyleClass().add(first ? "segment-btn-active" : "segment-btn");
                floorBtn.setOnAction(e -> {
                    // Toggle highlight
                    for (var node : floorSelector.getChildren()) {
                        if (node instanceof Button b) {
                            b.getStyleClass().setAll(
                                b.getText().equals("Floor " + floorNum)
                                    ? "segment-btn-active" : "segment-btn");
                        }
                    }
                    // Scroll to the target floor section
                    VBox section = floorSections.get(floorNum);
                    if (section != null) {
                        double contentH  = floorsContainer.getBoundsInLocal().getHeight();
                        double viewportH = scroll.getViewportBounds().getHeight();
                        double scrollable = contentH - viewportH;
                        double sectionY  = section.getBoundsInParent().getMinY();
                        if (scrollable > 0) scroll.setVvalue(sectionY / scrollable);
                    }
                });
                floorSelector.getChildren().add(floorBtn);
                first = false;
            }
        };

        buildPlan.run();
        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("btn-outline");
        refreshBtn.setOnAction(e -> buildPlan.run());

        HBox toolbarRow = new HBox(16);
        toolbarRow.setAlignment(Pos.CENTER_LEFT);
        toolbarRow.getChildren().addAll(floorSelector, refreshBtn);

        scroll.setContent(floorsContainer);
        root.getChildren().addAll(header, toolbarRow, scroll);
    }

    private HBox legendItem(String color, String label) {
        HBox box = new HBox(6);
        box.setAlignment(Pos.CENTER_LEFT);
        Region dot = new Region();
        dot.getStyleClass().add("legend-dot");
        dot.setStyle("-fx-background-color: " + color + ";");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 9.5; -fx-font-weight: 700; -fx-text-fill: #64748b;");
        box.getChildren().addAll(dot, lbl);
        return box;
    }

    private String getStatusClass(String status) {
        return switch (status) {
            case "AVAILABLE" -> "room-available";
            case "OCCUPIED" -> "room-occupied";
            case "RESERVED" -> "room-reserved";
            case "MAINTENANCE" -> "room-maintenance";
            case "NEEDS_CLEANING" -> "room-cleaning";
            default -> "room-maintenance";
        };
    }

    private void showRoomDetails(Room room, Runnable refresh) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Room " + room.getRoomNumber());
        alert.setHeaderText(room.getType() + " — " + room.getStatus());
        alert.setContentText(
            "Floor: " + room.getFloor() +
            "\nBase Price: $" + room.getBasePrice() +
            "\nWeekend Price: $" + room.getWeekendPrice() +
            "\nMax Occupancy: " + room.getMaxOccupancy() +
            (room.getDescription() != null ? "\n\n" + room.getDescription() : ""));

        ButtonType changeStatus = new ButtonType("Change Status", ButtonBar.ButtonData.LEFT);
        alert.getButtonTypes().add(changeStatus);
        DialogHelper.style(alert);
        alert.showAndWait().ifPresent(bt -> {
            if (bt == changeStatus) {
                ChoiceDialog<String> dlg = new ChoiceDialog<>(room.getStatus(),
                    "AVAILABLE", "OCCUPIED", "RESERVED", "MAINTENANCE", "NEEDS_CLEANING");
                dlg.setTitle("Change Status");
                DialogHelper.style(dlg);
                dlg.showAndWait().ifPresent(s -> {
                    dao.updateStatus(room.getId(), s);
                    autoCreateHousekeepingTask(room, s);
                    refresh.run();
                });
            }
        });
    }

    private void autoCreateHousekeepingTask(Room room, String newStatus) {
        if (!newStatus.equals("NEEDS_CLEANING") && !newStatus.equals("MAINTENANCE")) return;

        HousekeepingTask task = new HousekeepingTask();
        task.setRoomId(room.getId());
        task.setStatus("PENDING");

        if (newStatus.equals("NEEDS_CLEANING")) {
            task.setTaskType("CLEANING");
            task.setPriority("HIGH");
            task.setNotes("Room marked for cleaning from floor plan.");
        } else {
            task.setTaskType("MAINTENANCE");
            task.setPriority("URGENT");
            task.setNotes("Room flagged for maintenance from floor plan.");
        }

        hkDAO.save(task);
    }

    @SuppressWarnings("exports")
    public VBox getView() { return root; }
}
