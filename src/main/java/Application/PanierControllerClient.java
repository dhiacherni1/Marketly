package Application;

import Entite.Customer;
import Entite.Order;
import Entite.Panier;
import Entite.User;
import Services.EmailService;
import Services.ServiceCustomer;
import Services.ServiceOrder;
import Services.ServicePanier;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class PanierControllerClient implements Initializable {

    @FXML private TableView<Panier> tableView;
    @FXML private TableColumn<Panier, String> productCol;
    @FXML private TableColumn<Panier, Integer> quantityCol;
    @FXML private TableColumn<Panier, Double> priceCol;
    @FXML private TextField editQuantityField;
    @FXML private Label totalLabel;
    @FXML private Label messageLabel;

    private ServicePanier servicePanier = new ServicePanier();
    private ServiceOrder serviceOrder = new ServiceOrder();
    private ServiceCustomer serviceCustomer = new ServiceCustomer();
    private ObservableList<Panier> panierList = FXCollections.observableArrayList();
    private ResourceBundle bundle;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.bundle = resources;
        productCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("productPrice"));
        tableView.setItems(panierList);
        loadPanier();
    }

    public void refresh() {
        loadPanier();
    }

    private void loadPanier() {
        if (bundle == null) bundle = LanguageManager.getBundle();
        Customer customer = SessionManager.getInstance().getCurrentCustomer();
        if (customer == null) {
            try {
                User user = SessionManager.getInstance().getCurrentUser();
                if (user != null && "CLIENT".equals(user.getRole())) {
                    customer = serviceCustomer.findByUserId(user.getId());
                    if (customer == null) customer = serviceCustomer.findByEmail(user.getUsername() + "@mail.com");
                    if (customer == null) customer = serviceCustomer.findByEmail(user.getUsername());
                    if (customer != null) SessionManager.getInstance().setCurrentCustomer(customer);
                }
            } catch (SQLException ignored) {}
        }
        if (customer == null) {
            messageLabel.setText(bundle.getString("cart.noCustomer"));
            messageLabel.getStyleClass().setAll("error-label");
            return;
        }
        try {
            panierList.clear();
            panierList.addAll(servicePanier.findByCustomerId(customer.getId()));
            updateTotal();
        } catch (SQLException e) {
            messageLabel.setText(bundle.getString("cart.error") + " " + e.getMessage());
            messageLabel.getStyleClass().setAll("error-label");
        }
    }

    @FXML
    private void handleRemove() {
        if (bundle == null) bundle = LanguageManager.getBundle();
        Panier selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText(bundle.getString("cart.selectProduct"));
            messageLabel.getStyleClass().setAll("error-label");
            return;
        }
        try {
            if (servicePanier.supprimer(selected)) {
                messageLabel.setText(bundle.getString("cart.removed"));
                messageLabel.getStyleClass().setAll("success-label");
                loadPanier();
            } else {
                messageLabel.setText(bundle.getString("cart.error"));
                messageLabel.getStyleClass().setAll("error-label");
            }
        } catch (SQLException e) {
            messageLabel.setText(bundle.getString("cart.error") + " " + e.getMessage());
            messageLabel.getStyleClass().setAll("error-label");
        }
    }

    @FXML
    private void handleUpdateQuantity() {
        if (bundle == null) bundle = LanguageManager.getBundle();
        Panier selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText(bundle.getString("cart.selectProduct"));
            messageLabel.getStyleClass().setAll("error-label");
            return;
        }
        int qty;
        try {
            String t = editQuantityField != null ? editQuantityField.getText().trim() : "";
            qty = Integer.parseInt(t);
        } catch (NumberFormatException e) {
            messageLabel.setText(bundle.getString("cart.invalidQuantity"));
            messageLabel.getStyleClass().setAll("error-label");
            return;
        }
        if (qty < 1) {
            messageLabel.setText(bundle.getString("cart.invalidQuantity"));
            messageLabel.getStyleClass().setAll("error-label");
            return;
        }
        selected.setQuantity(qty);
        try {
            if (servicePanier.modifier(selected)) {
                messageLabel.setText(bundle.getString("cart.added"));
                messageLabel.getStyleClass().setAll("success-label");
                loadPanier();
            } else {
                messageLabel.setText(bundle.getString("cart.error"));
                messageLabel.getStyleClass().setAll("error-label");
            }
        } catch (SQLException e) {
            messageLabel.setText(bundle.getString("cart.error") + " " + e.getMessage());
            messageLabel.getStyleClass().setAll("error-label");
        }
    }

    @FXML
    private void handleCheckout() {
        if (bundle == null) bundle = LanguageManager.getBundle();
        Customer customer = SessionManager.getInstance().getCurrentCustomer();
        if (customer == null) {
            try {
                User user = SessionManager.getInstance().getCurrentUser();
                if (user != null && "CLIENT".equals(user.getRole())) {
                    customer = serviceCustomer.findByUserId(user.getId());
                    if (customer == null) customer = serviceCustomer.findByEmail(user.getUsername() + "@mail.com");
                    if (customer == null) customer = serviceCustomer.findByEmail(user.getUsername());
                    if (customer == null) {
                        String email = user.getUsername().contains("@") ? user.getUsername() : user.getUsername() + "@mail.com";
                        Customer newC = new Customer(null, "Client", user.getUsername(), email, user.getId());
                        if (serviceCustomer.ajouter(newC))
                            customer = serviceCustomer.findByUserId(user.getId());
                    }
                    if (customer != null) SessionManager.getInstance().setCurrentCustomer(customer);
                }
            } catch (SQLException ignored) {}
        }
        if (customer == null) {
            messageLabel.setText(bundle.getString("cart.noCustomer"));
            messageLabel.getStyleClass().setAll("error-label");
            return;
        }
        final Customer checkoutCustomer = customer;
        Stage owner = (Stage) tableView.getScene().getWindow();
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(bundle.getString("checkout.title"));
        stage.setMaximized(true);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(30));

        Label title = new Label(bundle.getString("checkout.title"));
        title.getStyleClass().add("form-title");
        root.setTop(title);
        BorderPane.setAlignment(title, Pos.CENTER_LEFT);

        TextField firstNameField = new TextField(checkoutCustomer.getPrenom());
        TextField lastNameField = new TextField(checkoutCustomer.getNom());
        TextField phoneField = new TextField();
        TextField addressField = new TextField();
        ComboBox<String> paymentBox = new ComboBox<>();
        paymentBox.getItems().addAll(
                bundle.getString("payment.cash"),
                bundle.getString("payment.card")
        );
        paymentBox.getSelectionModel().selectFirst();

        TextField cardHolderField = new TextField();
        TextField cardNumberField = new TextField();
        TextField cardExpiryField = new TextField();
        PasswordField cardCvvField = new PasswordField();

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.addRow(0, new Label(bundle.getString("checkout.firstname")), firstNameField);
        grid.addRow(1, new Label(bundle.getString("checkout.lastname")), lastNameField);
        grid.addRow(2, new Label(bundle.getString("checkout.phone")), phoneField);
        grid.addRow(3, new Label(bundle.getString("checkout.address")), addressField);
        grid.addRow(4, new Label(bundle.getString("checkout.payment")), paymentBox);

        GridPane cardGrid = new GridPane();
        cardGrid.setHgap(20);
        cardGrid.setVgap(15);
        cardGrid.addRow(0, new Label(bundle.getString("card.holder")), cardHolderField);
        cardGrid.addRow(1, new Label(bundle.getString("card.number")), cardNumberField);
        cardGrid.addRow(2, new Label(bundle.getString("card.expiry")), cardExpiryField);
        cardGrid.addRow(3, new Label(bundle.getString("card.cvv")), cardCvvField);

        VBox centerBox = new VBox(20, grid, cardGrid);
        centerBox.getStyleClass().add("form-container");
        root.setCenter(centerBox);

        cardGrid.visibleProperty().bind(paymentBox.valueProperty().isEqualTo(bundle.getString("payment.card")));
        cardGrid.managedProperty().bind(cardGrid.visibleProperty());

        Label feedbackLabel = new Label();
        feedbackLabel.setWrapText(true);
        feedbackLabel.setMaxWidth(Double.MAX_VALUE);

        Button cancelButton = new Button(bundle.getString("button.cancel"));
        cancelButton.getStyleClass().add("btn-secondary");
        cancelButton.setOnAction(e -> stage.close());

        Button confirmButton = new Button(bundle.getString("button.confirm"));
        confirmButton.getStyleClass().add("btn-primary");
        confirmButton.setOnAction(e -> {
            if (bundle == null) bundle = LanguageManager.getBundle();
            feedbackLabel.setText("");
            String payment = paymentBox.getValue();
            if (payment == null) payment = paymentBox.getItems().isEmpty() ? "" : paymentBox.getItems().get(0);
            if (bundle.getString("payment.card").equals(payment)) {
                if (cardHolderField.getText().trim().isEmpty()
                        || cardNumberField.getText().trim().isEmpty()
                        || cardExpiryField.getText().trim().isEmpty()
                        || cardCvvField.getText().trim().isEmpty()) {
                    feedbackLabel.setText(bundle.getString("card.missing"));
                    feedbackLabel.getStyleClass().setAll("error-label");
                    return;
                }
            }
            try {
                Order order = serviceOrder.createOrderFromCart(
                        checkoutCustomer,
                        firstNameField.getText().trim(),
                        lastNameField.getText().trim(),
                        phoneField.getText().trim(),
                        addressField.getText().trim(),
                        payment
                );
                if (order != null) {
                    messageLabel.setText(bundle.getString("checkout.confirmation"));
                    messageLabel.getStyleClass().setAll("success-label");
                    loadPanier();
                    stage.close();
                    showInvoiceWindow(order);
                } else {
                    feedbackLabel.setText(bundle.getString("cart.noProducts"));
                    feedbackLabel.getStyleClass().setAll("error-label");
                }
            } catch (SQLException ex) {
                String err = bundle.getString("cart.error") + " " + ex.getMessage();
                feedbackLabel.setText(err);
                feedbackLabel.getStyleClass().setAll("error-label");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(stage);
                alert.setTitle(bundle.getString("cart.error"));
                alert.setHeaderText(null);
                String msg = ex.getMessage();
                if (msg != null && msg.contains("Unknown column ")) {
                    msg = "Colonne(s) manquante(s) dans la table orders.\n\n"
                            + "Exécutez le script fix_orders_table.sql dans MySQL (base ecommerce_db),\n"
                            + "ou exécutez les commandes ALTER TABLE du fichier.\n\n"
                            + "Puis fermez cette fenêtre et réessayez de confirmer.";
                } else if (msg != null && msg.contains("doesn't have a default value")) {
                    msg = "Un champ obligatoire de la table orders n'a pas de valeur par défaut.\n\n"
                            + "Exécutez dans MySQL (base ecommerce_db) :\n\n"
                            + "ALTER TABLE orders MODIFY COLUMN order_number VARCHAR(50) NULL;\n"
                            + "ALTER TABLE orders MODIFY COLUMN total_amount DOUBLE NULL;\n\n"
                            + "Puis fermez cette fenêtre et réessayez de confirmer.";
                }
                alert.setContentText(msg);
                alert.showAndWait();
            }
        });

        VBox bottomBox = new VBox(10, feedbackLabel, new HBox(10, cancelButton, confirmButton));
        bottomBox.setAlignment(Pos.CENTER_RIGHT);
        bottomBox.setPadding(new Insets(20, 0, 0, 0));
        ((HBox) bottomBox.getChildren().get(1)).setAlignment(Pos.CENTER_RIGHT);
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, owner.getWidth(), owner.getHeight());
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void updateTotal() {
        if (totalLabel == null) return;
        double total = 0.0;
        for (Panier p : panierList) {
            total += p.getProductPrice() * p.getQuantity();
        }
        totalLabel.setText(String.format("%.2f", total));
    }

    private void showInvoiceWindow(Order order) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Facture n° " + order.getId());

        TableView<Entite.OrderItem> table = new TableView<>();
        TableColumn<Entite.OrderItem, String> prodCol = new TableColumn<>("Produit");
        prodCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        TableColumn<Entite.OrderItem, Integer> qtyCol = new TableColumn<>("Quantité");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        TableColumn<Entite.OrderItem, Double> unitCol = new TableColumn<>("Prix unit.");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        TableColumn<Entite.OrderItem, Double> lineCol = new TableColumn<>("Total ligne");
        lineCol.setCellValueFactory(new PropertyValueFactory<>("lineTotal"));
        table.getColumns().addAll(prodCol, qtyCol, unitCol, lineCol);
        table.getItems().setAll(order.getItems());

        String clientLabel = order.getCustomer() != null
                ? (order.getCustomer().getPrenom() + " " + order.getCustomer().getNom()).trim()
                : "-";
        VBox content = new VBox(10,
                new Label("Client : " + clientLabel),
                new Label("Adresse : " + (order.getAddress() != null ? order.getAddress() : "-")),
                new Label("Téléphone : " + (order.getPhone() != null ? order.getPhone() : "-")),
                table,
                new Label("Total : " + order.getTotal())
        );

        dialog.getDialogPane().setContent(content);
        ButtonType exportType = new ButtonType("Exporter PDF", ButtonBar.ButtonData.LEFT);
        ButtonType sendEmailType = new ButtonType("Envoyer par e-mail", ButtonBar.ButtonData.LEFT);
        dialog.getDialogPane().getButtonTypes().addAll(exportType, sendEmailType, ButtonType.CLOSE);

        dialog.setResultConverter(button -> button);
        javafx.stage.Stage ownerStage = (javafx.stage.Stage) tableView.getScene().getWindow();
        dialog.showAndWait().ifPresent(button -> {
            if (button == exportType) {
                exportInvoiceToPdf(order, ownerStage);
            } else if (button == sendEmailType) {
                sendInvoiceByEmail(order);
            }
        });
    }

    private void exportInvoiceToPdf(Order order, javafx.stage.Stage ownerStage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Enregistrer la facture");
        chooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF", "*.pdf"));
        chooser.setInitialFileName("facture-" + order.getId() + ".pdf");
        File file = chooser.showSaveDialog(ownerStage);
        if (file != null) {
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getAbsolutePath() + ".pdf");
            }
            try {
                InvoicePdfGenerator.export(order, file);
                messageLabel.setText("Facture exportée en PDF.");
                messageLabel.getStyleClass().setAll("success-label");
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
            } catch (IOException io) {
                messageLabel.setText("PDF enregistré mais impossible de l'ouvrir: " + io.getMessage());
                messageLabel.getStyleClass().setAll("error-label");
            } catch (Exception e) {
                messageLabel.setText("Erreur lors de la génération du PDF: " + e.getMessage());
                messageLabel.getStyleClass().setAll("error-label");
            }
        }
    }

    private void sendInvoiceByEmail(Order order) {
        String initialEmail = order.getCustomer() != null ? order.getCustomer().getEmail() : "";
        if (initialEmail == null) initialEmail = "";
        String toEmail = askEmailForInvoice(initialEmail.trim());
        if (toEmail == null || toEmail.isEmpty()) {
            if (toEmail != null) messageLabel.setText("Veuillez saisir une adresse e-mail.");
            if (toEmail != null) messageLabel.getStyleClass().setAll("error-label");
            return;
        }

        File pdfFile = null;
        try {
            pdfFile = File.createTempFile("facture-" + order.getId(), ".pdf");
            InvoicePdfGenerator.export(order, pdfFile);
            String subject = "Facture n° " + order.getId();
            String body = "Veuillez trouver votre facture en pièce jointe.";
            String err = EmailService.sendWithAttachment(toEmail, subject, body, pdfFile);
            if (err == null) {
                messageLabel.setText("Facture envoyée par e-mail à " + toEmail);
                messageLabel.getStyleClass().setAll("success-label");
            } else {
                messageLabel.setText(err);
                messageLabel.getStyleClass().setAll("error-label");
            }
        } catch (Exception e) {
            messageLabel.setText("Erreur : " + e.getMessage());
            messageLabel.getStyleClass().setAll("error-label");
        } finally {
            if (pdfFile != null && pdfFile.exists()) pdfFile.delete();
        }
    }

    /** Demande au client de saisir son e-mail pour recevoir la facture. */
    private String askEmailForInvoice(String initialEmail) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Recevoir la facture par e-mail");
        dialog.setHeaderText("Saisissez l'adresse e-mail à laquelle envoyer la facture :");

        javafx.scene.control.TextField emailField = new javafx.scene.control.TextField(initialEmail);
        emailField.setPromptText("exemple@email.com");
        emailField.setPrefWidth(320);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("E-mail :"), 0, 0);
        grid.add(emailField, 1, 0);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(button -> ButtonType.OK.equals(button) ? emailField.getText().trim() : null);
        return dialog.showAndWait().orElse(null);
    }
}
