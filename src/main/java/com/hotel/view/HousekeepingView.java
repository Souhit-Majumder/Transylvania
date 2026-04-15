package com.hotel.view;

import com.hotel.dao.*;
import com.hotel.model.*;
import com.hotel.util.DialogHelper;
import com.hotel.dao.StaffDAO;
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
    private final StaffDAO staffDAO = new StaffDAO();

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

        table.setMaxWidth(Double.MAX_VALUE);
        table.setMaxHeight(Double.MAX_VALUE);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        setupColumns();
        loadPending();

        addBtn.setOnAction(e -> showTaskDialog());
        pendingBtn.setOnAction(e -> { loadPending(); pendingBtn.getStyleClass().setAll("segment-btn-active"); allBtn.getStyleClass().setAll("segment-btn"); });
        allBtn.setOnAction(e -> { table.getItems().setAll(dao.findAll()); allBtn.getStyleClass().setAll("segment-btn-active"); pendingBtn.getStyleClass().setAll("segment-btn"); });
        refreshBtn.setOnAction(e -> loadPending());

        ContextMenu ctx = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit Task");
        MenuItem startItem = new MenuItem("Start (In Progress)");
        MenuItem completeItem = new MenuItem("Mark Complete");
        MenuItem makeAvailable = new MenuItem("Complete & Set Room Available");
        editItem.setOnAction(e -> {
            HousekeepingTask t = table.getSelectionModel().getSelectedItem();
            if (t != null) showEditTaskDialog(t);
        });
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
        ctx.getItems().addAll(editItem, new SeparatorMenuItem(), startItem, completeItem, new SeparatorMenuItem(), makeAvailable);
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

        // Assignee from active staff list; editable to allow free-text fallback
        List<com.hotel.model.Staff> staffList = staffDAO.findActive();
        ComboBox<String> assignBox = new ComboBox<>();
        assignBox.setEditable(true);
        for (com.hotel.model.Staff s : staffList) assignBox.getItems().add(s.getName());
        assignBox.setPromptText("Select or type staff name");

        TextArea notesArea = new TextArea();
        notesArea.setPrefRowCount(2);

        grid.addRow(0, new Label("Room:"), roomBox);
        grid.addRow(1, new Label("Task Type:"), typeBox);
        grid.addRow(2, new Label("Priority:"), priorityBox);
        grid.addRow(3, new Label("Assign To:"), assignBox);
        grid.addRow(4, new Label("Notes:"), notesArea);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        DialogHelper.style(dialog);
        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK && roomBox.getSelectionModel().getSelectedIndex() >= 0) {
                HousekeepingTask t = new HousekeepingTask();
                t.setRoomId(rooms.get(roomBox.getSelectionModel().getSelectedIndex()).getId());
                t.setTaskType(typeBox.getValue()); t.setPriority(priorityBox.getValue());
                t.setAssignedTo(assignBox.getValue()); t.setStatus("PENDING");
                t.setNotes(notesArea.getText());
                return t;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(t -> { dao.save(t); loadPending(); });
    }

    private void showEditTaskDialog(HousekeepingTask t) {
        Dialog<HousekeepingTask> dialog = new Dialog<>();
        dialog.setTitle("Edit Task — Room " + t.getRoomNumber());
        GridPane grid = DialogHelper.createFormGrid();

        // Assignee: dropdown from active staff, plus a free-text fallback
        List<com.hotel.model.Staff> staffList = staffDAO.findActive();
        ComboBox<String> assignBox = new ComboBox<>();
        assignBox.setEditable(true); // allow free text if staff list is empty
        for (com.hotel.model.Staff s : staffList) assignBox.getItems().add(s.getName());
        assignBox.setValue(t.getAssignedTo());
        assignBox.setPromptText("Select or type staff name");

        ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList(
            "CLEANING", "MAINTENANCE", "INSPECTION", "DEEP_CLEAN"));
        typeBox.setValue(t.getTaskType());

        ComboBox<String> priorityBox = new ComboBox<>(FXCollections.observableArrayList(
            "LOW", "NORMAL", "HIGH", "URGENT"));
        priorityBox.setValue(t.getPriority());

        ComboBox<String> statusBox = new ComboBox<>(FXCollections.observableArrayList(
            "PENDING", "IN_PROGRESS", "COMPLETED"));
        statusBox.setValue(t.getStatus());

        TextArea notesArea = new TextArea(t.getNotes() != null ? t.getNotes() : "");
        notesArea.setPrefRowCount(3);

        grid.addRow(0, new Label("Assigned To:"), assignBox);
        grid.addRow(1, new Label("Task Type:"), typeBox);
        grid.addRow(2, new Label("Priority:"), priorityBox);
        grid.addRow(3, new Label("Status:"), statusBox);
        grid.addRow(4, new Label("Notes:"), notesArea);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        DialogHelper.style(dialog);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                t.setAssignedTo(assignBox.getValue());
                t.setTaskType(typeBox.getValue());
                t.setPriority(priorityBox.getValue());
                t.setStatus(statusBox.getValue());
                t.setNotes(notesArea.getText());
                if ("COMPLETED".equals(t.getStatus()) && t.getCompletedAt() == null) {
                    t.setCompletedAt(java.time.LocalDateTime.now().toString());
                }
                return t;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(task -> { dao.save(task); loadPending(); });
    }

    @SuppressWarnings("exports")
    public VBox getView() { return root; }
}
