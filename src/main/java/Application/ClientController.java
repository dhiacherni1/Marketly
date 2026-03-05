package Application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;

public class ClientController {
    @FXML private Button logoutButton;
    @FXML private ComboBox<String> languageCombo;
    @FXML private TabPane clientTabPane;
    @FXML private Tab cartTab;
    @FXML private PanierControllerClient panierIncludeController;

    @FXML
    private void initialize() {
        if (languageCombo != null) {
            languageCombo.getItems().setAll("Français", "English", "العربية");
            Locale current = LanguageManager.getCurrentLocale();
            if (Locale.ENGLISH.getLanguage().equals(current.getLanguage())) languageCombo.setValue("English");
            else if ("ar".equals(current.getLanguage())) languageCombo.setValue("العربية");
            else languageCombo.setValue("Français");
        }
        if (clientTabPane != null && cartTab != null && panierIncludeController != null) {
            clientTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                if (newTab == cartTab) panierIncludeController.refresh();
            });
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client.fxml"), LanguageManager.getBundle());
            Scene scene = new Scene(loader.load(), stage.getScene().getWidth(), stage.getScene().getHeight());
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
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
