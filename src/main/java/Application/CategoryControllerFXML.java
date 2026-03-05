package Application;

import Entite.Category;
import Services.ServiceCategory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class CategoryControllerFXML implements Initializable {
    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;
    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button refreshButton;
    @FXML private TableView<Category> tableView;
    @FXML private TableColumn<Category, Long> idCol;
    @FXML private TableColumn<Category, String> nameCol;
    @FXML private TableColumn<Category, String> descCol;
    @FXML private Label messageLabel;

    private final ServiceCategory serviceCategory = new ServiceCategory();
    private final ObservableList<Category> categoriesList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        tableView.setItems(categoriesList);
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                idField.setText(String.valueOf(n.getId()));
                nameField.setText(n.getName());
                descriptionArea.setText(n.getDescription());
            }
        });
        loadCategories();
    }

    @FXML private void handleAdd() {
        try {
            if (nameField.getText().isEmpty()) { showMessage("Veuillez remplir le nom", "error"); return; }
            if (serviceCategory.ajouter(new Category(null, nameField.getText(), descriptionArea.getText()))) {
                showMessage("Catégorie ajoutée avec succès!", "success"); clearFields(); loadCategories();
            }
        } catch (SQLException ex) { showMessage("Erreur: " + ex.getMessage(), "error"); }
    }

    @FXML private void handleUpdate() {
        try {
            if (idField.getText().isEmpty()) { showMessage("Sélectionnez une catégorie à modifier", "error"); return; }
            if (serviceCategory.modifier(new Category(Long.parseLong(idField.getText()), nameField.getText(), descriptionArea.getText()))) {
                showMessage("Catégorie modifiée avec succès!", "success"); clearFields(); loadCategories();
            }
        } catch (SQLException ex) { showMessage("Erreur: " + ex.getMessage(), "error"); }
    }

    @FXML private void handleDelete() {
        try {
            if (idField.getText().isEmpty()) { showMessage("Sélectionnez une catégorie à supprimer", "error"); return; }
            Category c = tableView.getSelectionModel().getSelectedItem();
            if (c != null && new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cette catégorie?").showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                if (serviceCategory.supprimer(c)) { showMessage("Catégorie supprimée!", "success"); clearFields(); loadCategories(); }
            }
        } catch (SQLException ex) { showMessage("Erreur: " + ex.getMessage(), "error"); }
    }

    @FXML private void handleRefresh() { loadCategories(); }

    private void loadCategories() {
        try { categoriesList.clear(); categoriesList.addAll(serviceCategory.afficher()); }
        catch (SQLException e) { showMessage("Erreur chargement: " + e.getMessage(), "error"); }
    }
    private void clearFields() { idField.clear(); nameField.clear(); descriptionArea.clear(); }
    private void showMessage(String msg, String type) {
        messageLabel.setText(msg);
        messageLabel.getStyleClass().setAll("error".equals(type) ? "error-label" : "success-label");
    }
}
