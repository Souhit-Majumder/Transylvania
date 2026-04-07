package com.hotel.view;

import com.hotel.dao.*;
import com.hotel.model.*;
import com.hotel.util.DialogHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;

public class HousekeepingView {
    private final VBox root;
    private final TableView<HousekeepingTask> table;
    private final HousekeepingDAO dao = new HousekeepingDAO();
    private final RoomDAO roomDAO = new RoomDAO();

    public HousekeepingView() {
        root = new VBox(20);
        root.setPadding(new Insets(32));

        HBox header = new HBox();
        header.setAlignment(Pos.BOTTOM_LEFT);
        VBox headerText = new VBox(4);
        Label title = new Label("Housekeeping");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Task management and room turnover tracking");
        subtitle.getStyleClass().add("page-subtitle");
        headerText.getChildren().addAll(title, subtitle);
        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);
        Label activeBadge = new Label(dao.findPending().size() + " Active");
        activeBadge.getStyleClass().add("section-badge");
        header.getChildren().addAll(headerText, hSpacer, activeBadge);

        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        Button addBtn = new Button("+ New Task");
        addBtn.getStyleClass().add("btn-primary");

        HBox segGroup = new HBox(2);
        segGroup.getStyleClass().add("segment-group");
        Button pendingBtn = new Button("Pending");
        pendingBtn.getStyleClass().add("segment-btn-active");
        Button allBtn = new Button("All Tasks");
        allBtn.getStyleClass().add("segment-btn");
        segGroup.getChildren().addAll(pendingBtn, allBtn);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("btn-ghost");
        toolbar.getChildren().addAll(addBtn, segGroup, refreshBtn);

        table = new TableView<>();
        VBox.setVgrow(table, Priority.ALWAYS);
        setupColumns();
        loadPending();

        addBtn.setOnAction(e -> showTaskDialog());
        pendingBtn.setOnAction(e -> { loadPending(); pendingBtn.getStyleClass().setAll("segment-btn-active"); allBtn.getStyleClass().setAll("segment-btn"); });
        allBtn.setOnAction(e -> { table.getItems().setAll(dao.findAll()); allBtn.getStyleClass().setAll("segment-btn-active"); pendingBtn.getStyleClass().setAll("segment-btn"); });
        refreshBtn.setOnAction(e -> loadPending());

        ContextMenu ctx = new ContextMenu();
        MenuItem startItem = new MenuItem("Start (In Progress)");
        MenuItem completeItem = new MenuItem("Mark Complete");
        MenuItem makeAvailable = new MenuItem("Complete & Set Room Available");
        startItem.setOnAction(e -> {
            HousekeepingTask t = table.getSelectionModel().getSelectedItem();
            if (t != null) { t.setStatus("IN_PROGRESS"); dao.save(t); loadPending(); }
        });
        completeItem.setOnAction(e -> {
            HousekeepingTask t = table.getSelectionModel().getSelectedItem();
            if (t != null) { dao.markComplete(t.getId()); loadPending(); }
        });
        makeAvailable.setOnAction(e -> {
            HousekeepingTask t = table.getSelectionModel().getSelectedItem();
            if (t != null) { dao.markComplete(t.getId()); roomDAO.updateStatus(t.getRoomId(), "AVAILABLE"); loadPending(); }
        });
        ctx.getItems().addAll(startItem, completeItem, new SeparatorMenuItem(), makeAvailable);
        table.setContextMenu(ctx);

        root.getChildren().addAll(header, toolbar, table);
    }

    @SuppressWarnings("unchecked")
    private void setupColumns() {
        TableColumn<HousekeepingTask, String> room = new TableColumn<>("Room");
        room.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoomNumber()));
        room.setPrefWidth(80);
        TableColumn<HousekeepingTask, String> type = new TableColumn<>("Task");
        type.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTaskType()));
        TableColumn<HousekeepingTask, String> assigned = new TableColumn<>("Assigned To");
        assigned.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAssignedTo()));
        assigned.setPrefWidth(140);
        TableColumn<HousekeepingTask, String> priority = new TableColumn<>("Priority");
        priority.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPriority()));
        TableColumn<HousekeepingTask, String> status = new TableColumn<>("Status");
        status.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        TableColumn<HousekeepingTask, String> notes = new TableColumn<>("Notes");
        notes.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNotes()));
        notes.setPrefWidth(200);
        TableColumn<HousekeepingTask, String> created = new TableColumn<>("Created");
        created.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCreatedAt()));
        table.getColumns().addAll(room, type, assigned, priority, status, notes, created);
    }

    private void loadPending() { table.getItems().setAll(dao.findPending()); }

    private void showTaskDialog() {
        Dialog<HousekeepingTask> dialog = new Dialog<>();
        dialog.setTitle("New Housekeeping Task");
        GridPane grid = DialogHelper.createFormGrid();

        List<Room> rooms = roomDAO.findAll();
        ComboBox<String> roomBox = new ComboBox<>();
        for (Room r : rooms) roomBox.getItems().add(r.getRoomNumber() + " (" + r.getStatus() + ")");
        roomBox.setPromptText("Select Room");

        ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList("CLEANING", "MAINTENANCE", "INSPECTION", "DEEP_CLEAN"));
        typeBox.setValue("CLEANING");
        ComboBox<String> priorityBox = new ComboBox<>(FXCollections.observableArrayList("LOW", "NORMAL", "HIGH", "URGENT"));
        priorityBox.setValue("NORMAL");
        TextField assignField = new TextField();
        assignField.setPromptText("Staff name");
        TextArea notesArea = new TextArea();
        notesArea.setPrefRowCount(2);

        grid.addRow(0, new Label("Room:"), roomBox);
        grid.addRow(1, new Label("Task Type:"), typeBox);
        grid.addRow(2, new Label("Priority:"), priorityBox);
        grid.addRow(3, new Label("Assign To:"), assignField);
        grid.addRow(4, new Label("Notes:"), notesArea);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        DialogHelper.style(dialog);
        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK && roomBox.getSelectionModel().getSelectedIndex() >= 0) {
                HousekeepingTask t = new HousekeepingTask();
                t.setRoomId(rooms.get(roomBox.getSelectionModel().getSelectedIndex()).getId());
                t.setTaskType(typeBox.getValue()); t.setPriority(priorityBox.getValue());
                t.setAssignedTo(assignField.getText().trim()); t.setStatus("PENDING");
                t.setNotes(notesArea.getText());
                return t;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(t -> { dao.save(t); loadPending(); });
    }

    @SuppressWarnings("exports")
    public VBox getView() { return root; }
}
