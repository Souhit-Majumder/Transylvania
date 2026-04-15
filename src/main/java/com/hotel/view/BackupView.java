package com.hotel.view;

import com.hotel.util.DatabaseManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BackupView {
    private final VBox root;

    public BackupView() {
        root = new VBox(20);
        root.setPadding(new Insets(32));

        VBox headerText = new VBox(4);
        Label title = new Label("Database Backup");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Admin-only — export the current SQLite database for safekeeping");
        subtitle.getStyleClass().add("page-subtitle");
        headerText.getChildren().addAll(title, subtitle);

        VBox actionsCard = new VBox(16);
        actionsCard.getStyleClass().add("card");

        Button backupFileBtn = new Button("Backup Database File (.db)");
        backupFileBtn.getStyleClass().add("btn-primary");
        backupFileBtn.setMaxWidth(320);

        Button backupSqlBtn = new Button("Export as SQL Dump (.sql)");
        backupSqlBtn.getStyleClass().add("btn-outline");
        backupSqlBtn.setMaxWidth(320);

        // ── Progress area ──────────────────────────────────────────
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(10);
        progressBar.setVisible(false);

        Label progressLabel = new Label();
        progressLabel.getStyleClass().add("page-subtitle");
        progressLabel.setVisible(false);

        VBox progressBox = new VBox(6, progressBar, progressLabel);
        progressBox.setFillWidth(true);

        actionsCard.getChildren().addAll(backupFileBtn, backupSqlBtn, progressBox);

        TextArea logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPromptText("Backup log will appear here...");
        VBox.setVgrow(logArea, Priority.ALWAYS);

        // ── File backup handler ────────────────────────────────────
        backupFileBtn.setOnAction(e -> {
            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle("Select Backup Destination");
            File dir = dc.showDialog(root.getScene().getWindow());
            if (dir == null) return;

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File dest = new File(dir, "hotel_backup_" + timestamp + ".db");

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    updateMessage("Copying database file…");
                    updateProgress(0, 1);

                    Files.copy(Path.of("hotel.db"), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    updateProgress(1, 1);
                    updateMessage("✓ Backup saved to: " + dest.getAbsolutePath());
                    return null;
                }
            };

            bindTask(task, progressBar, progressLabel);
            task.setOnSucceeded(evt -> {
                Platform.runLater(() -> {
                    logArea.appendText(task.getMessage() + "\n");
                    resetProgress(progressBar, progressLabel);
                    backupFileBtn.setDisable(false);
                    backupSqlBtn.setDisable(false);
                });
            });
            task.setOnFailed(evt -> Platform.runLater(() -> {
                logArea.appendText("✗ ERROR: " + task.getException().getMessage() + "\n");
                resetProgress(progressBar, progressLabel);
                backupFileBtn.setDisable(false);
                backupSqlBtn.setDisable(false);
            }));

            backupFileBtn.setDisable(true);
            backupSqlBtn.setDisable(true);
            Thread t = new Thread(task, "db-file-backup");
            t.setDaemon(true); // thread dies automatically when the app closes
            t.start();
        });

        // ── SQL dump handler ───────────────────────────────────────
        backupSqlBtn.setOnAction(e -> {
            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle("Select Export Destination");
            File dir = dc.showDialog(root.getScene().getWindow());
            if (dir == null) return;

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File dest = new File(dir, "hotel_dump_" + timestamp + ".sql");

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    try (Connection conn = DatabaseManager.getConnection();
                         Statement stmt = conn.createStatement();
                         PrintWriter pw = new PrintWriter(new FileWriter(dest))) {

                        // Count tables first so we can report accurate progress
                        ResultSet countRs = conn.createStatement().executeQuery(
                            "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'");
                        int totalTables = countRs.next() ? countRs.getInt(1) : 1;
                        int processed = 0;

                        ResultSet tables = stmt.executeQuery(
                            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'");

                        while (tables.next()) {
                            String tableName = tables.getString("name");
                            processed++;
                            updateProgress(processed, totalTables);
                            updateMessage("Exporting table: " + tableName
                                    + " (" + processed + " / " + totalTables + ")");

                            // DDL
                            pw.println("-- Table: " + tableName);
                            ResultSet ddl = conn.createStatement()
                                .executeQuery("SELECT sql FROM sqlite_master WHERE name='" + tableName + "'");
                            if (ddl.next()) pw.println(ddl.getString("sql") + ";\n");

                            // Data
                            ResultSet data = conn.createStatement()
                                .executeQuery("SELECT * FROM " + tableName);
                            ResultSetMetaData meta = data.getMetaData();
                            int cols = meta.getColumnCount();
                            while (data.next()) {
                                StringBuilder sb = new StringBuilder(
                                    "INSERT INTO " + tableName + " VALUES (");
                                for (int i = 1; i <= cols; i++) {
                                    if (i > 1) sb.append(", ");
                                    Object val = data.getObject(i);
                                    if (val == null)        sb.append("NULL");
                                    else if (val instanceof Number) sb.append(val);
                                    else sb.append("'")
                                            .append(val.toString().replace("'", "''"))
                                            .append("'");
                                }
                                sb.append(");");
                                pw.println(sb);
                            }
                            pw.println();
                        }
                        updateMessage("✓ SQL dump exported to: " + dest.getAbsolutePath());
                    }
                    return null;
                }
            };

            bindTask(task, progressBar, progressLabel);
            task.setOnSucceeded(evt -> Platform.runLater(() -> {
                logArea.appendText(task.getMessage() + "\n");
                resetProgress(progressBar, progressLabel);
                backupFileBtn.setDisable(false);
                backupSqlBtn.setDisable(false);
            }));
            task.setOnFailed(evt -> Platform.runLater(() -> {
                logArea.appendText("✗ ERROR: " + task.getException().getMessage() + "\n");
                resetProgress(progressBar, progressLabel);
                backupFileBtn.setDisable(false);
                backupSqlBtn.setDisable(false);
            }));

            backupFileBtn.setDisable(true);
            backupSqlBtn.setDisable(true);
            Thread t = new Thread(task, "db-sql-dump");
            t.setDaemon(true);
            t.start();
        });

        root.getChildren().addAll(headerText, actionsCard, logArea);
    }

    /** Binds the progress bar and label to a running task's properties. */
    private void bindTask(Task<?> task, ProgressBar bar, Label label) {
        bar.progressProperty().bind(task.progressProperty());
        label.textProperty().bind(task.messageProperty());
        bar.setVisible(true);
        label.setVisible(true);
    }

    /** Unbinds and hides the progress controls after a task finishes. */
    private void resetProgress(ProgressBar bar, Label label) {
        bar.progressProperty().unbind();
        label.textProperty().unbind();
        bar.setProgress(0);
        bar.setVisible(false);
        label.setVisible(false);
    }

    @SuppressWarnings("exports")
    public VBox getView() { return root; }
}
