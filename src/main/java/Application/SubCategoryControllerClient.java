package Application;

import Entite.SubCategory;
import Services.ServiceSubCategory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class SubCategoryControllerClient implements Initializable {
    @FXML private TableView<SubCategory> tableView;
    @FXML private TableColumn<SubCategory, Long> idCol;
    @FXML private TableColumn<SubCategory, String> nameCol;
    @FXML private TableColumn<SubCategory, String> categoryCol;
    @FXML private Label messageLabel;
    @FXML private ImageView bannerImageView;

    private final ServiceSubCategory serviceSubCategory = new ServiceSubCategory();
    private final ObservableList<SubCategory> subCategoriesList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue() != null && cellData.getValue().getCategory() != null ? cellData.getValue().getCategory().getName() : ""));
        tableView.setItems(subCategoriesList);
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> updateBannerImage(n != null ? n.getImagePath() : null));
        if (bannerImageView != null) bannerImageView.setVisible(true);
        loadSubCategories();
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

    private void loadSubCategories() {
        try {
            subCategoriesList.clear();
            subCategoriesList.addAll(serviceSubCategory.afficher());
        } catch (SQLException e) {
            messageLabel.setText("Erreur: " + e.getMessage());
            messageLabel.getStyleClass().setAll("error-label");
        }
    }
}
