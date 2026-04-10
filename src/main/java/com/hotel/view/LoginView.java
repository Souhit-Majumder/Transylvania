package com.hotel.view;

import com.hotel.App;
import com.hotel.dao.UserDAO;
import com.hotel.model.User;
import com.hotel.util.Session;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class LoginView {
    private final StackPane root;

    public LoginView() {
        root = new StackPane();
        root.getStyleClass().add("login-container");

        VBox card = new VBox(6);
        card.getStyleClass().add("login-card");
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(380);

        // Brand icon
        StackPane brandIcon = new StackPane();
        brandIcon.getStyleClass().add("login-brand-icon");
        Label brandLetter = new Label("Transylvania");
        brandIcon.getChildren().add(brandLetter);
        VBox.setMargin(brandIcon, new Insets(0, 0, 8, 0));

        Label title = new Label("Hotel Transylvania");
        title.getStyleClass().add("login-title");

        Label subtitle = new Label("Management Suite — Staff Login");
        subtitle.getStyleClass().add("login-subtitle");
        VBox.setMargin(subtitle, new Insets(0, 0, 16, 0));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(280);
        usernameField.setPrefHeight(42);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(280);
        passwordField.setPrefHeight(42);

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("login-error");
        errorLabel.setMinHeight(18);

        Button loginBtn = new Button("Sign In");
        loginBtn.getStyleClass().add("login-btn");
        loginBtn.setMaxWidth(280);
        loginBtn.setDefaultButton(true);
        VBox.setMargin(loginBtn, new Insets(6, 0, 0, 0));

        loginBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Please fill in all fields.");
                return;
            }
            User user = new UserDAO().authenticate(username, password);
            if (user != null) {
                Session.setCurrentUser(user);
                App.showMain();
            } else {
                errorLabel.setText("Invalid credentials or account disabled.");
                passwordField.clear();
            }
        });

        Label hint = new Label("Default credentials: admin / admin123");
        hint.getStyleClass().add("login-hint");
        VBox.setMargin(hint, new Insets(8, 0, 0, 0));

        card.getChildren().addAll(brandIcon, title, subtitle, usernameField, passwordField, errorLabel, loginBtn, hint);
        root.getChildren().add(card);
    }

    @SuppressWarnings("exports")
    public StackPane getView() { return root; }
}
