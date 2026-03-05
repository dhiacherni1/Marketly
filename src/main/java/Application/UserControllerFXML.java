package Application;

import Entite.User;
import Services.ServiceUser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class UserControllerFXML implements Initializable {
    @FXML private TextField idField, usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Button addButton, updateButton, deleteButton, refreshButton;
    @FXML private TableView<User> tableView;
    @FXML private TableColumn<User, Long> idCol;
    @FXML private TableColumn<User, String> usernameCol, passwordCol, roleCol;
    @FXML private Label messageLabel;

    private final ServiceUser serviceUser = new ServiceUser();
    private final ObservableList<User> usersList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        passwordCol.setCellValueFactory(new PropertyValueFactory<>("password"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        tableView.setItems(usersList);
        roleCombo.setItems(FXCollections.observableArrayList("ADMIN", "MANAGER", "CLIENT"));
        roleCombo.setValue("CLIENT");
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                idField.setText(String.valueOf(n.getId()));
                usernameField.setText(n.getUsername());
                passwordField.setText(n.getPassword());
                roleCombo.setValue(n.getRole());
            }
        });
        loadUsers();
    }

    @FXML private void handleAdd() {
        try {
            if (usernameField.getText().isEmpty() || passwordField.getText().isEmpty()) { showMessage("Remplir tous les champs", "error"); return; }
            if (serviceUser.ajouter(new User(null, usernameField.getText(), passwordField.getText(), roleCombo.getValue()))) {
                showMessage("Utilisateur ajouté!", "success"); clearFields(); loadUsers();
            }
        } catch (SQLException ex) { showMessage("Erreur: " + ex.getMessage(), "error"); }
    }

    @FXML private void handleUpdate() {
        try {
            if (idField.getText().isEmpty()) { showMessage("Sélectionnez un utilisateur", "error"); return; }
            if (serviceUser.modifier(new User(Long.parseLong(idField.getText()), usernameField.getText(), passwordField.getText(), roleCombo.getValue()))) {
                showMessage("Utilisateur modifié!", "success"); clearFields(); loadUsers();
            }
        } catch (SQLException ex) { showMessage("Erreur: " + ex.getMessage(), "error"); }
    }

    @FXML private void handleDelete() {
        try {
            User u = tableView.getSelectionModel().getSelectedItem();
            if (u != null && new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cet utilisateur?").showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                if (serviceUser.supprimer(u)) { showMessage("Utilisateur supprimé!", "success"); clearFields(); loadUsers(); }
            }
        } catch (SQLException ex) { showMessage("Erreur: " + ex.getMessage(), "error"); }
    }

    @FXML private void handleRefresh() { loadUsers(); }

    private void loadUsers() {
        try { usersList.clear(); usersList.addAll(serviceUser.afficher()); }
        catch (SQLException e) { showMessage("Erreur: " + e.getMessage(), "error"); }
    }
    private void clearFields() { idField.clear(); usernameField.clear(); passwordField.clear(); roleCombo.setValue("CLIENT"); }
    private void showMessage(String msg, String type) {
        messageLabel.setText(msg);
        messageLabel.getStyleClass().setAll("error".equals(type) ? "error-label" : "success-label");
    }
}
