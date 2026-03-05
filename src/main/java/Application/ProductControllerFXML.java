package Application;

import Entite.Product;
import Services.ServiceProduct;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ProductControllerFXML implements Initializable {
    @FXML private TextField idField, nameField, priceField, quantityField;
    @FXML private Button addButton, updateButton, deleteButton, refreshButton;
    @FXML private Button exportPdfButton;
    @FXML private TableView<Product> tableView;
    @FXML private TableColumn<Product, Long> idCol;
    @FXML private TableColumn<Product, String> nameCol;
    @FXML private TableColumn<Product, Double> priceCol;
    @FXML private TableColumn<Product, Integer> quantityCol;
    @FXML private Label messageLabel;

    private final ServiceProduct serviceProduct = new ServiceProduct();
    private final ObservableList<Product> productsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        tableView.setItems(productsList);
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                idField.setText(String.valueOf(n.getId()));
                nameField.setText(n.getName());
                priceField.setText(String.valueOf(n.getPrice()));
                quantityField.setText(String.valueOf(n.getQuantity()));
            }
        });
        loadProducts();
    }

    @FXML private void handleAdd() {
        try {
            if (nameField.getText().isEmpty() || priceField.getText().isEmpty() || quantityField.getText().isEmpty()) { showMessage("Remplir tous les champs", "error"); return; }
            double price = Double.parseDouble(priceField.getText());
            int qty = Integer.parseInt(quantityField.getText());
            if (serviceProduct.ajouter(new Product(null, nameField.getText(), price, qty, null, null))) {
                showMessage("Produit ajouté!", "success"); clearFields(); loadProducts();
            }
        } catch (NumberFormatException e) { showMessage("Prix et quantité doivent être des nombres", "error"); }
        catch (SQLException ex) { showMessage("Erreur: " + ex.getMessage(), "error"); }
    }

    @FXML private void handleUpdate() {
        try {
            if (idField.getText().isEmpty()) { showMessage("Sélectionnez un produit", "error"); return; }
            double price = Double.parseDouble(priceField.getText());
            int qty = Integer.parseInt(quantityField.getText());
            if (serviceProduct.modifier(new Product(Long.parseLong(idField.getText()), nameField.getText(), price, qty, null, null))) {
                showMessage("Produit modifié!", "success"); clearFields(); loadProducts();
            }
        } catch (NumberFormatException e) { showMessage("Prix et quantité invalides", "error"); }
        catch (SQLException ex) { showMessage("Erreur: " + ex.getMessage(), "error"); }
    }

    @FXML private void handleDelete() {
        try {
            Product p = tableView.getSelectionModel().getSelectedItem();
            if (p != null && new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce produit?").showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                if (serviceProduct.supprimer(p)) { showMessage("Produit supprimé!", "success"); clearFields(); loadProducts(); }
            }
        } catch (SQLException ex) { showMessage("Erreur: " + ex.getMessage(), "error"); }
    }

    @FXML private void handleRefresh() { loadProducts(); }

    @FXML
    private void handleExportPdf() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter rapport produits (PDF)");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
            fileChooser.setInitialFileName("products-report.pdf");

            File target = fileChooser.showSaveDialog(tableView != null ? tableView.getScene().getWindow() : null);
            if (target == null) {
                return;
            }
            String path = target.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".pdf")) {
                target = new File(path + ".pdf");
            }
            ProductReportPdfGenerator.export(target);
            showMessage("Rapport produits exporté : " + target.getAbsolutePath(), "success");
        } catch (Exception e) {
            showMessage("Erreur export PDF : " + e.getMessage(), "error");
        }
    }

    private void loadProducts() {
        try { productsList.clear(); productsList.addAll(serviceProduct.afficher()); }
        catch (SQLException e) { showMessage("Erreur: " + e.getMessage(), "error"); }
    }
    private void clearFields() { idField.clear(); nameField.clear(); priceField.clear(); quantityField.clear(); }
    private void showMessage(String msg, String type) {
        messageLabel.setText(msg);
        messageLabel.getStyleClass().setAll("error".equals(type) ? "error-label" : "success-label");
    }
}
