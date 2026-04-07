package com.hotel.util;

import com.hotel.App;
import javafx.scene.Node;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.GridPane;

/**
 * Applies the Atlanta Pastel CSS theme to any Dialog.
 * Call DialogHelper.style(dialog) right after creating a dialog
 * and before calling showAndWait().
 */
public class DialogHelper {

    public static <T> void style(Dialog<T> dialog) {
        DialogPane pane = dialog.getDialogPane();

        // Apply the stylesheet
        var css = App.class.getResource("/styles/atlanta.css");
        if (css != null) {
            pane.getStylesheets().add(css.toExternalForm());
        }

        // Ensure the owning stage also gets styled (for ChoiceDialog, Alert, etc.)
        dialog.initOwner(App.getPrimaryStage());

        // Apply min width for good form layout
        pane.setMinWidth(420);

        // If content is a GridPane, style it as a form
        Node content = pane.getContent();
        if (content instanceof GridPane grid) {
            grid.getStyleClass().add("dialog-form-grid");
        }
    }

    /**
     * Creates a pre-styled GridPane for use inside dialogs.
     */
    public static GridPane createFormGrid() {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("dialog-form-grid");
        grid.setHgap(14);
        grid.setVgap(14);
        grid.setStyle("-fx-padding: 8;");
        return grid;
    }
}
