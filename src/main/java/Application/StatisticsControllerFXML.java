package Application;

import Entite.RevenueByMonthStat;
import Entite.TopCustomerStat;
import Entite.TopProductStat;
import Services.ServiceReport;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class StatisticsControllerFXML implements Initializable {
    @FXML private ComboBox<Integer> yearCombo, limitCombo;
    @FXML private Label totalRevenueLabel;
    @FXML private TableView<TopProductStat> topProductsTable;
    @FXML private TableColumn<TopProductStat, String> prodNameCol;
    @FXML private TableColumn<TopProductStat, Long> prodQtyCol;
    @FXML private TableColumn<TopProductStat, Double> prodRevenueCol;
    @FXML private TableView<TopCustomerStat> topCustomersTable;
    @FXML private TableColumn<TopCustomerStat, String> custNameCol;
    @FXML private TableColumn<TopCustomerStat, Long> custOrdersCol;
    @FXML private TableColumn<TopCustomerStat, Double> custSpentCol;
    @FXML private TableView<RevenueByMonthStat> revenueByMonthTable;
    @FXML private TableColumn<RevenueByMonthStat, String> periodCol;
    @FXML private TableColumn<RevenueByMonthStat, Double> revenueCol;
    @FXML private LineChart<String, Number> revenueLineChart;
    @FXML private BarChart<String, Number> productBarChart;
    @FXML private BarChart<String, Number> customerBarChart;

    private final ServiceReport serviceReport = new ServiceReport();
    private final ObservableList<TopProductStat> topProductsList = FXCollections.observableArrayList();
    private final ObservableList<TopCustomerStat> topCustomersList = FXCollections.observableArrayList();
    private final ObservableList<RevenueByMonthStat> revenueByMonthList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            if (prodNameCol != null) prodNameCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("productName"));
            if (prodQtyCol != null) prodQtyCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("totalQuantity"));
            if (prodRevenueCol != null) prodRevenueCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("totalRevenue"));
            if (topProductsTable != null) topProductsTable.setItems(topProductsList);
            if (custNameCol != null) custNameCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("customerName"));
            if (custOrdersCol != null) custOrdersCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("totalOrders"));
            if (custSpentCol != null) custSpentCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("totalSpent"));
            if (topCustomersTable != null) topCustomersTable.setItems(topCustomersList);
            if (periodCol != null) periodCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("periodLabel"));
            if (revenueCol != null) revenueCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("totalRevenue"));
            if (revenueByMonthTable != null) revenueByMonthTable.setItems(revenueByMonthList);
            if (yearCombo != null) {
                int y = LocalDate.now().getYear();
                yearCombo.setItems(FXCollections.observableArrayList(y - 1, y, y + 1));
                yearCombo.setValue(y);
            }
            if (limitCombo != null) {
                limitCombo.setItems(FXCollections.observableArrayList(5, 10, 20));
                limitCombo.setValue(5);
            }
            refreshAll();
        } catch (Exception e) {
            if (totalRevenueLabel != null) totalRevenueLabel.setText("Erreur: " + e.getMessage());
        }
    }

    @FXML private void handleRefreshAll() { refreshAll(); }

    private void refreshAll() {
        int year = yearCombo.getValue() != null ? yearCombo.getValue() : LocalDate.now().getYear();
        int limit = limitCombo.getValue() != null ? limitCombo.getValue() : 5;
        try {
            topProductsList.clear();
            topProductsList.addAll(serviceReport.getTopProducts(limit));
            topCustomersList.clear();
            topCustomersList.addAll(serviceReport.getTopCustomers(limit));
            revenueByMonthList.clear();
            revenueByMonthList.addAll(serviceReport.getRevenueByMonth(year));
            totalRevenueLabel.setText(String.format("%.2f DT", serviceReport.getTotalRevenue()));
            for (RevenueByMonthStat r : revenueByMonthList) {
                r.setPeriodLabel(formatPeriodLabel(r.getPeriodLabel()));
            }
            if (revenueLineChart != null) {
                revenueLineChart.getData().clear();
                XYChart.Series<String, Number> s = new XYChart.Series<>();
                for (RevenueByMonthStat r : revenueByMonthList) s.getData().add(new XYChart.Data<>(r.getPeriodLabel(), r.getTotalRevenue()));
                revenueLineChart.getData().add(s);
            }
            if (productBarChart != null) {
                productBarChart.getData().clear();
                XYChart.Series<String, Number> s = new XYChart.Series<>();
                for (TopProductStat p : topProductsList) s.getData().add(new XYChart.Data<>(p.getProductName(), p.getTotalQuantity()));
                productBarChart.getData().add(s);
            }
            if (customerBarChart != null) {
                customerBarChart.getData().clear();
                XYChart.Series<String, Number> s = new XYChart.Series<>();
                for (TopCustomerStat c : topCustomersList) s.getData().add(new XYChart.Data<>(c.getCustomerName(), c.getTotalSpent()));
                customerBarChart.getData().add(s);
            }
        } catch (SQLException e) {
            totalRevenueLabel.setText("Erreur: " + e.getMessage());
        }
    }

    /** Formate une période "yyyy-MM" en "MMM yyyy" (ex: 2024-03 -> "Mars 2024"). */
    private static String formatPeriodLabel(String period) {
        if (period == null || period.length() < 7) return period;
        try {
            YearMonth ym = YearMonth.parse(period);
            return ym.format(DateTimeFormatter.ofPattern("MMM yyyy", java.util.Locale.FRENCH));
        } catch (Exception e) {
            return period;
        }
    }
}
