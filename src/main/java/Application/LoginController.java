package Application;

import Entite.Customer;
import Entite.User;
import Services.ServiceCustomer;
import Services.ServiceUser;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private javafx.scene.control.Button loginButton;
    @FXML private javafx.scene.control.Hyperlink registerLink;
    @FXML private ComboBox<String> languageCombo;
    @FXML private ImageView logoView;

    private final ServiceUser serviceUser = new ServiceUser();
    private final ServiceCustomer serviceCustomer = new ServiceCustomer();

    @FXML
    private void initialize() {
        if (logoView != null) {
            try {
                java.io.InputStream is = getClass().getResourceAsStream("/images/logo.png");
                if (is != null) {
                    logoView.setImage(new Image(is));
                    is.close();
                } else {
                    logoView.setVisible(false);
                    logoView.setManaged(false);
                }
            } catch (Exception e) {
                if (logoView != null) {
                    logoView.setVisible(false);
                    logoView.setManaged(false);
                }
            }
        }
        if (languageCombo != null) {
            languageCombo.getItems().setAll("Français", "English", "العربية");
            Locale current = LanguageManager.getCurrentLocale();
            if (Locale.ENGLISH.getLanguage().equals(current.getLanguage())) languageCombo.setValue("English");
            else if ("ar".equals(current.getLanguage())) languageCombo.setValue("العربية");
            else languageCombo.setValue("Français");
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        if (username.isEmpty() || password.isEmpty()) {
            showError(LanguageManager.getString("login.errorEmpty"));
            return;
        }
        try {
            User user = serviceUser.authenticate(username, password);
            if (user != null) {
                SessionManager.getInstance().setCurrentUser(user);
                try { serviceUser.updateLastLogin(user.getId()); } catch (SQLException ignored) {}
                if ("CLIENT".equals(user.getRole())) {
                    try {
                        Customer c = serviceCustomer.findByUserId(user.getId());
                        if (c == null) c = serviceCustomer.findByEmail(username + "@mail.com");
                        if (c == null) c = serviceCustomer.findByEmail(username);
                        if (c == null) {
                            Customer newC = new Customer(null, "Client", user.getUsername(),
                                    username.contains("@") ? username : username + "@mail.com", user.getId());
                            if (serviceCustomer.ajouter(newC))
                                c = serviceCustomer.findByUserId(user.getId());
                        }
                        if (c != null) SessionManager.getInstance().setCurrentCustomer(c);
                    } catch (SQLException ignored) {}
                }
                redirectToDashboard(user.getRole());
            } else {
                showError(LanguageManager.getString("login.errorInvalid"));
            }
        } catch (SQLException e) {
            showError(LanguageManager.getString("login.errorConnection") + ": " + e.getMessage());
        }
    }

    @FXML
    private void handleLanguageChange() {
        if (languageCombo == null) return;
        String selected = languageCombo.getValue();
        Locale locale = "English".equals(selected) ? Locale.ENGLISH : "العربية".equals(selected) ? new Locale("ar") : Locale.FRENCH;
        LanguageManager.setCurrentLocale(locale);
        try {
            Stage stage = (Stage) languageCombo.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"), LanguageManager.getBundle());
            Scene scene = new Scene(loader.load(), stage.getScene().getWidth(), stage.getScene().getHeight());
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            stage.setScene(scene);
        } catch (IOException e) {
            showError(LanguageManager.getString("login.errorLoad") + ": " + e.getMessage());
        }
    }

    private void redirectToDashboard(String role) {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            FXMLLoader loader;
            if ("ADMIN".equals(role) || "MANAGER".equals(role)) {
                loader = new FXMLLoader(getClass().getResource("/main.fxml"), LanguageManager.getBundle());
                stage.setTitle(LanguageManager.getString("window.admin"));
            } else {
                loader = new FXMLLoader(getClass().getResource("/client.fxml"), LanguageManager.getBundle());
                stage.setTitle(LanguageManager.getString("window.client"));
            }
            Scene scene = new Scene(loader.load(), 1200, 700);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            ThemeManager.setScene(scene);
            stage.setScene(scene);
            stage.setMinWidth(1000);
            stage.setMinHeight(600);
        } catch (IOException e) {
            showError(LanguageManager.getString("login.errorLoad") + ": " + e.getMessage());
        }
    }

    @FXML
    private void handleGoToRegister() {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/register.fxml"), LanguageManager.getBundle());
            Scene scene = new Scene(loader.load(), 1000, 650);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle(LanguageManager.getString("window.register"));
        } catch (IOException e) {
            showError(LanguageManager.getString("login.errorLoad") + ": " + e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.getStyleClass().setAll("error-label");
    }
}
