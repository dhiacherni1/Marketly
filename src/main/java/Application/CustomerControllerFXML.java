package Application;

import Entite.Customer;
import Services.ServiceCustomer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class CustomerControllerFXML implements Initializable {
    @FXML private TextField idField, prenomField, nomField, emailField;
    @FXML private Button addButton, updateButton, deleteButton, refreshButton;
    @FXML private TableView<Customer> tableView;
    @FXML private TableColumn<Customer, Long> idCol;
    @FXML private TableColumn<Customer, String> prenomCol, nomCol, emailCol;
    @FXML private Label messageLabel;

    private final ServiceCustomer serviceCustomer = new ServiceCustomer();
    private final ObservableList<Customer> customersList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        prenomCol.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        tableView.setItems(customersList);
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                idField.setText(String.valueOf(n.getId()));
                prenomField.setText(n.getPrenom() != null ? n.getPrenom() : "");
                nomField.setText(n.getNom() != null ? n.getNom() : "");
                emailField.setText(n.getEmail() != null ? n.getEmail() : "");
            }
        });
        loadCustomers();
    }

    @FXML private void handleAdd() {
        try {
            if (nomField.getText().isEmpty() || emailField.getText().isEmpty()) { showMessage("Remplir nom et email", "error"); return; }
            String prenom = prenomField.getText().trim().isEmpty() ? "" : prenomField.getText().trim();
            if (serviceCustomer.ajouter(new Customer(null, prenom, nomField.getText(), emailField.getText()))) {
                showMessage("Client ajouté!", "success"); clearFields(); loadCustomers();
            }
        } catch (SQLException ex) { showMessage("Erreur: " + ex.getMessage(), "error"); }
    }

    @FXML private void handleUpdate() {
        try {
            if (idField.getText().isEmpty()) { showMessage("Sélectionnez un client", "error"); return; }
            if (serviceCustomer.modifier(new Customer(Long.parseLong(idField.getText()), prenomField.getText(), nomField.getText(), emailField.getText()))) {
                showMessage("Client modifié!", "success"); clearFields(); loadCustomers();
            }
        } catch (SQLException ex) { showMessage("Erreur: " + ex.getMessage(), "error"); }
    }

    @FXML private void handleDelete() {
        try {
            Customer c = tableView.getSelectionModel().getSelectedItem();
            if (c != null && new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce client?").showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                if (serviceCustomer.supprimer(c)) { showMessage("Client supprimé!", "success"); clearFields(); loadCustomers(); }
            }
        } catch (SQLException ex) { showMessage("Erreur: " + ex.getMessage(), "error"); }
    }

    @FXML private void handleRefresh() { loadCustomers(); }

    private void loadCustomers() {
        try { customersList.clear(); customersList.addAll(serviceCustomer.afficher()); }
        catch (SQLException e) { showMessage("Erreur: " + e.getMessage(), "error"); }
    }
    private void clearFields() { idField.clear(); prenomField.clear(); nomField.clear(); emailField.clear(); }
    private void showMessage(String msg, String type) {
        messageLabel.setText(msg);
        messageLabel.getStyleClass().setAll("error".equals(type) ? "error-label" : "success-label");
    }
}
