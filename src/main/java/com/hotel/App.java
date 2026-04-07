package com.hotel;

import com.hotel.util.DatabaseManager;
import com.hotel.view.LoginView;
import com.hotel.view.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    private static Stage primaryStage;

    @Override
    public void start(@SuppressWarnings("exports") Stage stage) {
        primaryStage = stage;
        DatabaseManager.initializeDatabase();
        showLogin();
    }

    public static void showLogin() {
        LoginView login = new LoginView();
        Scene scene = new Scene(login.getView(), 460, 520);
        applyCss(scene);
        primaryStage.setTitle("L'Horizon — Management Suite");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void showMain() {
        MainView main = new MainView();
        Scene scene = new Scene(main.getView(), 1340, 820);
        applyCss(scene);
        primaryStage.setTitle("L'Horizon — Management Suite");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();
    }

    private static void applyCss(Scene scene) {
        var css = App.class.getResource("/styles/atlanta.css");
        if (css != null)
            scene.getStylesheets().add(css.toExternalForm());
    }

    @SuppressWarnings("exports")
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
