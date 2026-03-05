package Application;

import Services.ServiceOrder;
import Services.ServiceProduct;
import Services.ServiceReport;
import Services.ServiceUser;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DashboardControllerFXML implements Initializable {

    @FXML private Label totalUsersLabel;
    @FXML private Label clientsLabel;
    @FXML private Label adminsLabel;
    @FXML private Label newThisMonthLabel;
    @FXML private Label activeTodayLabel;
    @FXML private Label totalOrdersLabel;
    @FXML private Label pendingOrdersLabel;
    @FXML private Label totalProductsLabel;
    @FXML private Label totalRevenueLabel;

    private final ServiceUser serviceUser = new ServiceUser();
    private final ServiceOrder serviceOrder = new ServiceOrder();
    private final ServiceProduct serviceProduct = new ServiceProduct();
    private final ServiceReport serviceReport = new ServiceReport();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadDashboard();
    }

    /** Recharge les indicateurs du tableau de bord (appelé à l’affichage ou au rafraîchissement). */
    public void refresh() {
        loadDashboard();
    }

    private void loadDashboard() {
        try {
            totalUsersLabel.setText(String.valueOf(serviceUser.getTotalUsers()));
            clientsLabel.setText(String.valueOf(serviceUser.getCountByRole("CLIENT")));
            int admins = serviceUser.getCountByRole("ADMIN") + serviceUser.getCountByRole("MANAGER");
            adminsLabel.setText(String.valueOf(admins));
            newThisMonthLabel.setText(String.valueOf(serviceUser.getNewInscritsThisMonth()));
            activeTodayLabel.setText(String.valueOf(serviceUser.getActiveToday()));
        } catch (SQLException e) {
            totalUsersLabel.setText("-");
            clientsLabel.setText("-");
            adminsLabel.setText("-");
            newThisMonthLabel.setText("-");
            activeTodayLabel.setText("-");
        }
        try {
            totalOrdersLabel.setText(String.valueOf(serviceOrder.getTotalOrdersCount()));
            pendingOrdersLabel.setText(String.valueOf(serviceOrder.getPendingOrdersCount()));
            totalProductsLabel.setText(String.valueOf(serviceProduct.getTotalProductsCount()));
            double revenue = serviceReport.getTotalRevenue();
            totalRevenueLabel.setText(String.format("%.2f", revenue) + " DT");
        } catch (SQLException e) {
            if (totalOrdersLabel != null) totalOrdersLabel.setText("-");
            if (pendingOrdersLabel != null) pendingOrdersLabel.setText("-");
            if (totalProductsLabel != null) totalProductsLabel.setText("-");
            if (totalRevenueLabel != null) totalRevenueLabel.setText("-");
        }
    }
}
