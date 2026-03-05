package Application;

import Entite.Order;
import Entite.OrderItem;
import Services.EmailService;
import Services.ServiceOrder;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class OrderControllerFXML implements Initializable {

    @FXML private TableView<Order> tableView;
    @FXML private TableColumn<Order, Long> idCol;
    @FXML private TableColumn<Order, String> customerCol;
    @FXML private TableColumn<Order, Double> totalCol;
    @FXML private TableColumn<Order, String> dateCol;
    @FXML private TableColumn<Order, String> statusCol;
    @FXML private Button acceptButton;
    @FXML private Label messageLabel;

    private ServiceOrder serviceOrder = new ServiceOrder();
    private ResourceBundle bundle;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.bundle = resources != null ? resources : LanguageManager.getBundle();
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("orderDateFormatted"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        loadOrders();
    }

    private void loadOrders() {
        try {
            List<Order> list = serviceOrder.findAllWithCustomer();
            tableView.getItems().setAll(list);
            messageLabel.setText("");
        } catch (SQLException e) {
            messageLabel.setText(bundle.getString("cart.error") + " " + e.getMessage());
            messageLabel.getStyleClass().setAll("error-label");
        }
    }

    @FXML
    private void handleRefresh() {
        loadOrders();
    }

    @FXML
    private void handleAcceptOrder() {
        Order selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText(bundle.getString("admin.order.select"));
            messageLabel.getStyleClass().setAll("error-label");
            return;
        }
        if ("ACCEPTED".equals(selected.getStatus())) {
            messageLabel.setText(bundle.getString("admin.order.alreadyAccepted"));
            messageLabel.getStyleClass().setAll("error-label");
            return;
        }
        try {
            try {
                serviceOrder.updateStatus(selected.getId(), "ACCEPTED");
            } catch (SQLException ignored) { /* colonne status peut être absente */ }
            Order full = serviceOrder.findByIdWithItems(selected.getId());
            if (full == null) {
                messageLabel.setText(bundle.getString("cart.error"));
                messageLabel.getStyleClass().setAll("error-label");
                return;
            }
            showInvoiceAndSendEmail(full);
            messageLabel.setText(bundle.getString("admin.order.acceptedAndSent"));
            messageLabel.getStyleClass().setAll("success-label");
            loadOrders();
        } catch (SQLException e) {
            messageLabel.setText(bundle.getString("cart.error") + " " + e.getMessage());
            messageLabel.getStyleClass().setAll("error-label");
        }
    }

    private void showInvoiceAndSendEmail(Order order) {
        Stage stage = (Stage) tableView.getScene().getWindow();
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(bundle.getString("admin.order.invoice") + " n° " + order.getId());
        dialog.initOwner(stage);

        TableView<OrderItem> table = new TableView<>();
        TableColumn<OrderItem, String> prodCol = new TableColumn<>(bundle.getString("admin.order.product"));
        prodCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        TableColumn<OrderItem, Integer> qtyCol = new TableColumn<>(bundle.getString("admin.order.quantity"));
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        TableColumn<OrderItem, Double> unitCol = new TableColumn<>(bundle.getString("admin.order.unitPrice"));
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        TableColumn<OrderItem, Double> lineCol = new TableColumn<>(bundle.getString("admin.order.lineTotal"));
        lineCol.setCellValueFactory(new PropertyValueFactory<>("lineTotal"));
        table.getColumns().addAll(prodCol, qtyCol, unitCol, lineCol);
        table.getItems().setAll(order.getItems());

        GridPane info = new GridPane();
        info.setHgap(10);
        info.setVgap(5);
        info.addRow(0, new Label(bundle.getString("admin.order.customer") + " :"), new Label(order.getCustomerName()));
        info.addRow(1, new Label(bundle.getString("checkout.address") + " :"), new Label(order.getAddress() != null ? order.getAddress() : ""));
        info.addRow(2, new Label(bundle.getString("checkout.phone") + " :"), new Label(order.getPhone() != null ? order.getPhone() : ""));
        info.addRow(3, new Label(bundle.getString("admin.order.total") + " :"), new Label(String.format("%.2f", order.getTotal())));

        VBox content = new VBox(10, info, table);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        ButtonType exportPdfType = new ButtonType(bundle.getString("admin.order.exportPdf"), ButtonBar.ButtonData.LEFT);
        ButtonType sendEmailType = new ButtonType(bundle.getString("admin.order.sendEmail"), ButtonBar.ButtonData.LEFT);
        dialog.getDialogPane().getButtonTypes().addAll(exportPdfType, sendEmailType, ButtonType.CLOSE);

        dialog.setResultConverter(button -> button);
        dialog.showAndWait().ifPresent(button -> {
            if (button == exportPdfType) {
                exportInvoiceToPdf(order, stage);
            } else if (button == sendEmailType) {
                sendInvoiceByEmail(order);
            }
        });
    }

    private void exportInvoiceToPdf(Order order, Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(bundle.getString("admin.order.saveInvoice"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        chooser.setInitialFileName("facture-" + order.getId() + ".pdf");
        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getAbsolutePath() + ".pdf");
            }
            try {
                InvoicePdfGenerator.export(order, file);
                showInfo(bundle.getString("admin.order.pdfSaved"));
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
            } catch (Exception e) {
                showInfo(bundle.getString("admin.order.pdfError") + " " + e.getMessage());
            }
        }
    }

    private void sendInvoiceByEmail(Order order) {
        String initialEmail = order.getCustomer() != null ? order.getCustomer().getEmail() : "";
        if (initialEmail == null) initialEmail = "";
        String toEmail = askEmailAddress(initialEmail.trim());
        if (toEmail == null || toEmail.isEmpty()) {
            if (toEmail != null) showInfo(bundle.getString("admin.order.enterEmail"));
            return;
        }

        File pdfFile = null;
        try {
            pdfFile = File.createTempFile("facture-" + order.getId(), ".pdf");
            InvoicePdfGenerator.export(order, pdfFile);
            String subject = bundle.getString("admin.order.invoice") + " n° " + order.getId();
            String body = bundle.getString("admin.order.emailBody");
            String err = EmailService.sendWithAttachment(toEmail, subject, body, pdfFile);
            if (err == null) {
                showInfo(bundle.getString("admin.order.emailSent") + " " + toEmail);
            } else {
                showInfo(err);
            }
        } catch (Exception e) {
            showInfo(bundle.getString("admin.order.pdfError") + " " + e.getMessage());
        } finally {
            if (pdfFile != null && pdfFile.exists()) pdfFile.delete();
        }
    }

    /** Demande au client de saisir son e-mail pour recevoir la facture. */
    private String askEmailAddress(String initialEmail) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(bundle.getString("admin.order.invoiceEmailTitle"));
        dialog.setHeaderText(bundle.getString("admin.order.invoiceEmailPrompt"));

        TextField emailField = new TextField(initialEmail);
        emailField.setPromptText(bundle.getString("admin.order.emailPlaceholder"));
        emailField.setPrefWidth(320);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label(bundle.getString("admin.order.customerEmail") + " :"), 0, 0);
        grid.add(emailField, 1, 0);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(button -> ButtonType.OK.equals(button) ? emailField.getText().trim() : null);
        return dialog.showAndWait().orElse(null);
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(msg);
        a.showAndWait();
    }
}
