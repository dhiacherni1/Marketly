package Application;

import Entite.Customer;
import Entite.User;
import Services.ServiceCustomer;
import Services.ServiceUser;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;
    @FXML private javafx.scene.control.Button registerButton;
    @FXML private javafx.scene.control.Hyperlink loginLink;

    private final ServiceUser serviceUser = new ServiceUser();
    private final ServiceCustomer serviceCustomer = new ServiceCustomer();

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirm = confirmPasswordField.getText().trim();
        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showError(LanguageManager.getString("register.errorEmpty"));
            return;
        }
        if (username.length() < 3) {
            showError(LanguageManager.getString("register.errorUsernameLength"));
            return;
        }
        if (password.length() < 4) {
            showError(LanguageManager.getString("register.errorPasswordLength"));
            return;
        }
        if (!password.equals(confirm)) {
            showError(LanguageManager.getString("register.errorPasswordMatch"));
            return;
        }
        try {
            if (serviceUser.findByUsername(username) != null) {
                showError(LanguageManager.getString("register.errorUsernameTaken"));
                return;
            }
            User newUser = new User(null, username, password, "CLIENT");
            if (serviceUser.ajouter(newUser)) {
                User created = serviceUser.findByUsername(username);
                Long userId = created != null ? created.getId() : null;
                Customer c = new Customer(null, username, username, username + "@mail.com", userId);
                try {
                    serviceCustomer.ajouter(c);
                } catch (SQLException ignored) {}
                showSuccess(LanguageManager.getString("register.success"));
                new Thread(() -> {
                    try {
                        Thread.sleep(1200);
                        javafx.application.Platform.runLater(this::handleGoToLogin);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            } else {
                showError("Erreur lors de l'inscription");
            }
        } catch (SQLException e) {
            showError("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void handleGoToLogin() {
        try {
            Stage stage = (Stage) registerButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"), LanguageManager.getBundle());
            Scene scene = new Scene(loader.load(), 1000, 650);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("E-Commerce Unifié - Connexion");
        } catch (IOException e) {
            showError(LanguageManager.getString("register.errorLoad") + ": " + e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.getStyleClass().setAll("error-label");
    }
    private void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.getStyleClass().setAll("success-label");
    }
}
