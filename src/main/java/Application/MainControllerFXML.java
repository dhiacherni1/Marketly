package Application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;

public class MainControllerFXML {
    @FXML private Button logoutButton;
    @FXML private HBox topBar;
    @FXML private ComboBox<String> languageCombo;

    @FXML
    private void initialize() {
        if (topBar != null && !topBar.getChildren().contains(ThemeManager.createThemeToggleButton())) {
            topBar.getChildren().add(0, ThemeManager.createThemeToggleButton());
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
    private void handleLanguageChange() {
        if (languageCombo == null) return;
        String selected = languageCombo.getValue();
        Locale locale = "English".equals(selected) ? Locale.ENGLISH : "العربية".equals(selected) ? new Locale("ar") : Locale.FRENCH;
        LanguageManager.setCurrentLocale(locale);
        try {
            Stage stage = (Stage) languageCombo.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"), LanguageManager.getBundle());
            Scene scene = new Scene(loader.load(), stage.getScene().getWidth(), stage.getScene().getHeight());
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            ThemeManager.setScene(scene);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            SessionManager.getInstance().logout();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"), LanguageManager.getBundle());
            Scene scene = new Scene(loader.load(), 1000, 650);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle(LanguageManager.getString("window.login"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
