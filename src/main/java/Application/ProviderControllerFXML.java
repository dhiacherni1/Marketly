package Application;

import Entite.Provider;
import Services.ServiceProvider;
import Services.EmailService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

public class ProviderControllerFXML implements Initializable {
    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField addressField;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> searchTypeCombo;
    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button refreshButton;
    @FXML private TableView<Provider> tableView;
    @FXML private TableColumn<Provider, Long> idCol;
    @FXML private TableColumn<Provider, String> nameCol;
    @FXML private TableColumn<Provider, String> phoneCol;
    @FXML private TableColumn<Provider, String> emailCol;
    @FXML private TableColumn<Provider, String> addressCol;
    @FXML private Label messageLabel;

    private final ServiceProvider serviceProvider = new ServiceProvider();
    private final ObservableList<Provider> allProviders = FXCollections.observableArrayList();
    private final ObservableList<Provider> providersList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        tableView.setItems(providersList);
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                idField.setText(String.valueOf(n.getId()));
                nameField.setText(n.getName());
                phoneField.setText(n.getPhone() != null ? n.getPhone() : "");
                emailField.setText(n.getEmail() != null ? n.getEmail() : "");
                addressField.setText(n.getAddress() != null ? n.getAddress() : "");
            }
        });

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldV, newV) -> applyFilter(newV));
        }
        if (searchTypeCombo != null) {
            searchTypeCombo.getItems().setAll("Tous", "Nom", "Téléphone", "Email", "Adresse");
            searchTypeCombo.setValue("Tous");
            searchTypeCombo.valueProperty().addListener((obs, oldV, newV) -> applyFilter(searchField != null ? searchField.getText() : ""));
        }

        loadProviders();
    }

    @FXML private void handleAdd() {
        try {
            if (nameField.getText().isEmpty()) { showMessage("Remplir le nom", "error"); return; }
            Provider provider = new Provider(null, nameField.getText(), phoneField.getText(), addressField.getText(), emailField.getText());
            if (serviceProvider.ajouter(provider)) {
                showMessage("Fournisseur ajouté!", "success");

                if (provider.getEmail() != null && !provider.getEmail().trim().isEmpty()) {
                    String subject = "Bienvenue comme fournisseur sur MARKTLY";
                    String body = "Bonjour " + (provider.getName() != null ? provider.getName() : "") + ",\n\n"
                            + "Votre compte fournisseur a été créé avec succès sur la plateforme MARKTLY.\n"
                            + "Vous pouvez désormais collaborer avec nous pour la gestion et la vente de vos produits.\n\n"
                            + "Cordialement,\nL'équipe MARKTLY";
                    String err = EmailService.sendWithAttachment(provider.getEmail(), subject, body, null);
                    if (err != null) {
                        showMessage("Fournisseur ajouté, mais e-mail non envoyé : " + err, "error");
                    }
                }

                clearFields();
                loadProviders();
            }
        } catch (SQLException ex) { showMessage("Erreur: " + ex.getMessage(), "error"); }
    }

    @FXML private void handleUpdate() {
        try {
            if (idField.getText().isEmpty()) { showMessage("Sélectionnez un fournisseur", "error"); return; }
            if (serviceProvider.modifier(new Provider(Long.parseLong(idField.getText()), nameField.getText(), phoneField.getText(), addressField.getText(), emailField.getText()))) {
                showMessage("Fournisseur modifié!", "success"); clearFields(); loadProviders();
            }
        } catch (SQLException ex) { showMessage("Erreur: " + ex.getMessage(), "error"); }
    }

    @FXML private void handleDelete() {
        try {
            Provider p = tableView.getSelectionModel().getSelectedItem();
            if (p != null && new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce fournisseur?").showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                if (serviceProvider.supprimer(p)) { showMessage("Fournisseur supprimé!", "success"); clearFields(); loadProviders(); }
            }
        } catch (SQLException ex) { showMessage("Erreur: " + ex.getMessage(), "error"); }
    }

    @FXML private void handleRefresh() { loadProviders(); }

    @FXML
    private void handleSearch() {
        if (searchField != null) {
            applyFilter(searchField.getText());
        }
    }

    private void loadProviders() {
        try {
            allProviders.clear();
            allProviders.addAll(serviceProvider.afficher());
            applyFilter(searchField != null ? searchField.getText() : "");
        } catch (SQLException e) {
            showMessage("Erreur: " + e.getMessage(), "error");
        }
    }
    private void clearFields() { idField.clear(); nameField.clear(); phoneField.clear(); emailField.clear(); addressField.clear(); }
    private void showMessage(String msg, String type) {
        messageLabel.setText(msg);
        messageLabel.getStyleClass().setAll("error".equals(type) ? "error-label" : "success-label");
    }

    private void applyFilter(String term) {
        String t = term == null ? "" : term.trim().toLowerCase(Locale.ROOT);
        String mode = searchTypeCombo != null && searchTypeCombo.getValue() != null
                ? searchTypeCombo.getValue()
                : "Tous";

        providersList.clear();
        if (t.isEmpty()) {
            providersList.addAll(allProviders);
            return;
        }

        for (Provider p : allProviders) {
            String name = p.getName() != null ? p.getName().toLowerCase(Locale.ROOT) : "";
            String phone = p.getPhone() != null ? p.getPhone().toLowerCase(Locale.ROOT) : "";
            String email = p.getEmail() != null ? p.getEmail().toLowerCase(Locale.ROOT) : "";
            String address = p.getAddress() != null ? p.getAddress().toLowerCase(Locale.ROOT) : "";

            boolean match;
            switch (mode) {
                case "Nom":
                    match = name.contains(t);
                    break;
                case "Téléphone":
                    match = phone.contains(t);
                    break;
                case "Email":
                    match = email.contains(t);
                    break;
                case "Adresse":
                    match = address.contains(t);
                    break;
                default:
                    match = name.contains(t) || phone.contains(t) || email.contains(t) || address.contains(t);
            }

            if (match) {
                providersList.add(p);
            }
        }
    }
}
