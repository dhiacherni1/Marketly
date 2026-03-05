package Application;

import Entite.Category;
import Entite.SubCategory;
import Services.ServiceCategory;
import Services.ServiceSubCategory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class SubCategoryControllerFXML implements Initializable {
    @FXML private TextField idField, nameField;
    @FXML private ComboBox<Category> categoryCombo;
    @FXML private Button addButton, updateButton, deleteButton, refreshButton;
    @FXML private TableView<SubCategory> tableView;
    @FXML private TableColumn<SubCategory, Long> idCol;
    @FXML private TableColumn<SubCategory, String> nameCol, categoryCol;
    @FXML private Label messageLabel;
    @FXML private ImageView bannerImageView;
    @FXML private Button uploadImageButton;

    private final ServiceSubCategory serviceSubCategory = new ServiceSubCategory();
    private final ServiceCategory serviceCategory = new ServiceCategory();
    private final ObservableList<SubCategory> subCategoriesList = FXCollections.observableArrayList();
    private final ObservableList<Category> categoriesList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue() != null && cell.getValue().getCategory() != null ? "Cat. " + cell.getValue().getCategory().getId() : ""));
        tableView.setItems(subCategoriesList);
        categoryCombo.setItems(categoriesList);
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                idField.setText(String.valueOf(n.getId()));
                nameField.setText(n.getName());
                categoryCombo.setValue(n.getCategory());
                updateBannerImage(n.getImagePath());
            }
        });
        if (bannerImageView != null) bannerImageView.setVisible(true);
        loadCategories();
        loadSubCategories();
    }

    @FXML private void handleAdd() {
        try {
            if (nameField.getText().isEmpty() || categoryCombo.getValue() == null) { showMessage("Remplir nom et catégorie", "error"); return; }
            if (serviceSubCategory.ajouter(new SubCategory(null, nameField.getText(), categoryCombo.getValue()))) {
                showMessage("Sous-catégorie ajoutée!", "success"); clearFields(); loadSubCategories();
            }
        } catch (SQLException ex) { showMessage("Erreur: " + ex.getMessage(), "error"); }
    }

    @FXML private void handleUpdate() {
        try {
            SubCategory selected = tableView.getSelectionModel().getSelectedItem();
            if (selected == null || idField.getText().isEmpty()) { showMessage("Sélectionnez une sous-catégorie", "error"); return; }
            selected.setName(nameField.getText());
            selected.setCategory(categoryCombo.getValue());
            if (serviceSubCategory.modifier(selected)) {
                showMessage("Sous-catégorie modifiée!", "success"); clearFields(); loadSubCategories();
            }
        } catch (SQLException ex) { showMessage("Erreur: " + ex.getMessage(), "error"); }
    }

    @FXML private void handleDelete() {
        try {
            SubCategory sc = tableView.getSelectionModel().getSelectedItem();
            if (sc != null && new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cette sous-catégorie?").showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                if (serviceSubCategory.supprimer(sc)) { showMessage("Sous-catégorie supprimée!", "success"); clearFields(); updateBannerImage(null); loadSubCategories(); }
            }
        } catch (SQLException ex) { showMessage("Erreur: " + ex.getMessage(), "error"); }
    }

    @FXML private void handleRefresh() { loadCategories(); loadSubCategories(); }

    @FXML private void handleUploadImage() {
        SubCategory selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) { showMessage("Sélectionnez une sous-catégorie", "error"); return; }
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une image");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
        java.io.File file = fc.showOpenDialog(uploadImageButton != null ? uploadImageButton.getScene().getWindow() : null);
        if (file == null) return;
        try {
            Path destDir = Paths.get(System.getProperty("user.home"), "subcategory_banners");
            Files.createDirectories(destDir);
            String ext = file.getName().contains(".") ? file.getName().substring(file.getName().lastIndexOf('.')) : ".png";
            Path dest = destDir.resolve("subcat_" + selected.getId() + ext);
            Files.copy(file.toPath(), dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            String savedPath = dest.toAbsolutePath().toString();
            selected.setImagePath(savedPath);
            if (serviceSubCategory.modifier(selected)) {
                updateBannerImage(savedPath);
                showMessage("Image enregistrée et affichée", "success");
                loadSubCategories();
            } else showMessage("Erreur lors de la mise à jour", "error");
        } catch (java.io.IOException e) { showMessage("Erreur copie image: " + e.getMessage(), "error"); }
        catch (SQLException e) { showMessage("Erreur base de données: " + e.getMessage(), "error"); }
    }

    private void updateBannerImage(String imagePath) {
        if (bannerImageView == null) return;
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            Path p = Paths.get(imagePath);
            if (Files.exists(p)) {
                try {
                    bannerImageView.setImage(new Image(p.toUri().toString()));
                    bannerImageView.setVisible(true);
                    return;
                } catch (Exception ignored) {}
            }
        }
        bannerImageView.setImage(null);
        bannerImageView.setVisible(true);
    }

    private void loadCategories() {
        try { categoriesList.clear(); categoriesList.addAll(serviceCategory.afficher()); }
        catch (SQLException e) { showMessage("Erreur catégories: " + e.getMessage(), "error"); }
    }
    private void loadSubCategories() {
        try { subCategoriesList.clear(); subCategoriesList.addAll(serviceSubCategory.afficher()); }
        catch (SQLException e) { showMessage("Erreur: " + e.getMessage(), "error"); }
    }
    private void clearFields() { idField.clear(); nameField.clear(); categoryCombo.setValue(null); }
    private void showMessage(String msg, String type) {
        messageLabel.setText(msg);
        messageLabel.getStyleClass().setAll("error".equals(type) ? "error-label" : "success-label");
    }
}
