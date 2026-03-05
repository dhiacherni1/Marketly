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

public class CategoryControllerClient implements Initializable {
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
        loadCategories();
    }

    private void loadCategories() {
        try {
            categoriesList.clear();
            categoriesList.addAll(serviceCategory.afficher());
        } catch (SQLException e) {
            messageLabel.setText("Erreur: " + e.getMessage());
            messageLabel.getStyleClass().setAll("error-label");
        }
    }
}
