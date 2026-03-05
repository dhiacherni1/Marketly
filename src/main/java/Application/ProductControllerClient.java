package Application;

import Entite.Customer;
import Entite.Panier;
import Entite.Product;
import Entite.User;
import Services.ServiceCustomer;
import Services.ServicePanier;
import Services.ServiceProduct;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ProductControllerClient implements Initializable {

    @FXML private TableView<Product> tableView;
    @FXML private TableColumn<Product, Long> idCol;
    @FXML private TableColumn<Product, String> nameCol;
    @FXML private TableColumn<Product, String> categoryCol;
    @FXML private TableColumn<Product, String> subCategoryCol;
    @FXML private TableColumn<Product, Double> priceCol;
    @FXML private TableColumn<Product, Integer> quantityCol;
    @FXML private Label messageLabel;
    @FXML private ScrollPane chatScroll;
    @FXML private VBox chatMessages;
    @FXML private TextField chatInput;
    @FXML private TextField quantityField;

    private ServiceProduct serviceProduct = new ServiceProduct();
    private ServicePanier servicePanier = new ServicePanier();
    private ServiceCustomer serviceCustomer = new ServiceCustomer();
    private ObservableList<Product> productsList = FXCollections.observableArrayList();
    private ResourceBundle bundle;
    private boolean waitingCategoryClarification = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.bundle = resources;

        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        subCategoryCol.setCellValueFactory(new PropertyValueFactory<>("subCategoryName"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        tableView.setItems(productsList);
        loadProducts();

        if (chatMessages != null && bundle != null) {
            appendBotMessage(bundle.getString("chat.welcome"));
        }
        if (quantityField != null) {
            quantityField.setText("1");
        }
    }

    @FXML
    private void handleAddToCart() {
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
        Product product = tableView.getSelectionModel().getSelectedItem();
        if (product == null) {
            messageLabel.setText(bundle.getString("cart.selectProduct"));
            messageLabel.getStyleClass().setAll("error-label");
            return;
        }
        int qty = 1;
        try {
            String t = quantityField != null ? quantityField.getText().trim() : "";
            if (!t.isEmpty()) qty = Integer.parseInt(t);
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
        if (product.getQuantity() < qty) {
            messageLabel.setText(bundle.getString("cart.notEnoughStock"));
            messageLabel.getStyleClass().setAll("error-label");
            return;
        }
        try {
            Panier panier = new Panier(null, customer, product, qty);
            if (servicePanier.ajouter(panier)) {
                messageLabel.setText(bundle.getString("cart.added"));
                messageLabel.getStyleClass().setAll("success-label");
                if (quantityField != null) {
                    quantityField.clear();
                }
                // Mettre à jour la quantité affichée pour refléter le stock restant
                product.setQuantity(product.getQuantity() - qty);
                try {
                    serviceProduct.modifier(product);
                } catch (SQLException ignored) { }
                loadProducts();
            } else {
                messageLabel.setText(bundle.getString("cart.error"));
                messageLabel.getStyleClass().setAll("error-label");
            }
        } catch (SQLException e) {
            messageLabel.setText(bundle.getString("cart.error") + " " + e.getMessage());
            messageLabel.getStyleClass().setAll("error-label");
        }
    }

    private void loadProducts() {
        try {
            productsList.clear();
            productsList.addAll(serviceProduct.afficher());
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors du chargement: " + e.getMessage());
            messageLabel.getStyleClass().setAll("error-label");
        }
    }

    @FXML
    private void handleSendMessage() {
        if (chatInput == null || chatMessages == null) {
            return;
        }

        String question = chatInput.getText();
        if (question == null || question.trim().isEmpty()) {
            return;
        }

        String q = question.trim();
        chatInput.clear();

        // S'assurer que la liste est à jour avant de répondre
        loadProducts();

        appendUserMessage(q);

        String answer = buildAnswer(q);

        appendBotMessage(answer);
    }

    private String buildAnswer(String question) {
        if (bundle == null) {
            bundle = LanguageManager.getBundle();
        }

        String lower = question.toLowerCase();

        // Petites conversations (salut, merci, ...).
        String smallTalk = handleSmallTalk(lower);
        if (smallTalk != null) {
            return smallTalk;
        }

        if (productsList.isEmpty()) {
            return bundle.getString("chat.noProducts");
        }

        // Si on attend une précision de catégorie, la réponse suivante est interprétée comme un filtre.
        if (waitingCategoryClarification) {
            waitingCategoryClarification = false;
            StringBuilder sb = new StringBuilder(bundle.getString("chat.allProducts")).append("\n");
            for (Product p : productsList) {
                sb.append("- ")
                        .append(p.getName())
                        .append(" (")
                        .append(bundle.getString("chat.productPrice"))
                        .append(" ")
                        .append(p.getPrice())
                        .append(", ")
                        .append(bundle.getString("chat.productQuantity"))
                        .append(" ")
                        .append(p.getQuantity())
                        .append(")\n");
            }
            return sb.toString();
        }

        // Si l'utilisateur demande les produits en général.
        if ( (lower.contains("produit") || lower.contains("produits") || lower.contains("product") || lower.contains("products"))
                && (lower.contains("disponible") || lower.contains("disponibles") || lower.contains("liste") || lower.contains("list") ) ) {
            waitingCategoryClarification = true;
            return bundle.getString("chat.askCategory");
        }

        // Try to match by product name contained in the question
        for (Product p : productsList) {
            if (p.getName() != null && lower.contains(p.getName().toLowerCase())) {
                StringBuilder sb = new StringBuilder(bundle.getString("chat.productDetailsPrefix")).append("\n");
                sb.append(bundle.getString("chat.productName"))
                        .append(" ")
                        .append(p.getName())
                        .append("\n");
                sb.append(bundle.getString("chat.productPrice"))
                        .append(" ")
                        .append(p.getPrice())
                        .append("\n");
                sb.append(bundle.getString("chat.productQuantity"))
                        .append(" ")
                        .append(p.getQuantity())
                        .append("\n");

                if (p.getCategory() != null) {
                    sb.append(bundle.getString("chat.productCategory"))
                            .append(" ")
                            .append(p.getCategory().getName());
                }

                return sb.toString();
            }
        }

        return bundle.getString("chat.notFound");
    }

    private String handleSmallTalk(String lower) {
        if (lower.contains("salut") || lower.contains("bonjour") || lower.contains("bonsoir")
                || lower.contains("hello") || lower.contains("hi")) {
            return bundle.getString("chat.hello");
        }

        if (lower.contains("merci") || lower.contains("thanks") || lower.contains("thank you")) {
            return bundle.getString("chat.thanks");
        }

        if (lower.contains("aurevoir") || lower.contains("au revoir") || lower.contains("bye")) {
            return bundle.getString("chat.bye");
        }

        return null;
    }

    private void appendUserMessage(String text) {
        if (chatMessages == null) return;

        Label label = new Label(text);
        label.getStyleClass().add("chat-bubble-user");
        label.setWrapText(true);
        label.setTextAlignment(TextAlignment.RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox container = new HBox(8, spacer, label);
        container.setAlignment(Pos.CENTER_RIGHT);
        container.setPadding(new Insets(4, 0, 4, 0));

        chatMessages.getChildren().add(container);
        scrollToBottom();
    }

    private void appendBotMessage(String text) {
        if (chatMessages == null) return;

        Label label = new Label(text);
        label.getStyleClass().add("chat-bubble-bot");
        label.setWrapText(true);
        label.setTextAlignment(TextAlignment.LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox container = new HBox(8, label, spacer);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(4, 0, 4, 0));

        chatMessages.getChildren().add(container);
        scrollToBottom();
    }

    private void scrollToBottom() {
        if (chatScroll != null) {
            chatScroll.layout();
            chatScroll.setVvalue(1.0);
        }
    }
}
