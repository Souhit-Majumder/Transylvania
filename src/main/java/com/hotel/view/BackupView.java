package com.hotel.view;

import com.hotel.util.DatabaseManager;
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

        actionsCard.getChildren().addAll(backupFileBtn, backupSqlBtn);

        TextArea logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPromptText("Backup log will appear here...");
        VBox.setVgrow(logArea, Priority.ALWAYS);

        backupFileBtn.setOnAction(e -> {
            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle("Select Backup Destination");
            File dir = dc.showDialog(root.getScene().getWindow());
            if (dir != null) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                File dest = new File(dir, "hotel_backup_" + timestamp + ".db");
                try {
                    Files.copy(Path.of("hotel.db"), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    logArea.appendText("✓ Database backed up to: " + dest.getAbsolutePath() + "\n");
                } catch (IOException ex) {
                    logArea.appendText("✗ ERROR: " + ex.getMessage() + "\n");
                }
            }
        });

        backupSqlBtn.setOnAction(e -> {
            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle("Select Export Destination");
            File dir = dc.showDialog(root.getScene().getWindow());
            if (dir != null) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                File dest = new File(dir, "hotel_dump_" + timestamp + ".sql");
                try (Connection conn = DatabaseManager.getConnection();
                        Statement stmt = conn.createStatement();
                        PrintWriter pw = new PrintWriter(new FileWriter(dest))) {
                    ResultSet tables = stmt.executeQuery(
                            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'");
                    while (tables.next()) {
                        String tableName = tables.getString("name");
                        pw.println("-- Table: " + tableName);
                        ResultSet createStmt = conn.createStatement()
                                .executeQuery("SELECT sql FROM sqlite_master WHERE name='" + tableName + "'");
                        if (createStmt.next())
                            pw.println(createStmt.getString("sql") + ";\n");
                        ResultSet data = conn.createStatement().executeQuery("SELECT * FROM " + tableName);
                        ResultSetMetaData meta = data.getMetaData();
                        int cols = meta.getColumnCount();
                        while (data.next()) {
                            StringBuilder sb = new StringBuilder("INSERT INTO " + tableName + " VALUES (");
                            for (int i = 1; i <= cols; i++) {
                                if (i > 1)
                                    sb.append(", ");
                                Object val = data.getObject(i);
                                if (val == null)
                                    sb.append("NULL");
                                else if (val instanceof Number)
                                    sb.append(val);
                                else
                                    sb.append("'").append(val.toString().replace("'", "''")).append("'");
                            }
                            sb.append(");");
                            pw.println(sb);
                        }
                        pw.println();
                    }
                    logArea.appendText("✓ SQL dump exported to: " + dest.getAbsolutePath() + "\n");
                } catch (Exception ex) {
                    logArea.appendText("✗ ERROR: " + ex.getMessage() + "\n");
                }
            }
        });

        root.getChildren().addAll(headerText, actionsCard, logArea);
    }

    @SuppressWarnings("exports")
    public VBox getView() {
        return root;
    }
}
