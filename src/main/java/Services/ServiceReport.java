package Services;

import Entite.RevenueByMonthStat;
import Entite.TopCustomerStat;
import Entite.TopProductStat;
import Utlis.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceReport {
    private Connection getCon() { return DataSource.getInstance().getCon(); }

    public List<TopProductStat> getTopProducts(int limit) throws SQLException {
        String sql = "SELECT p.name AS product_name, SUM(c.quantity) AS total_quantity, SUM(c.quantity * p.price) AS total_revenue FROM carts c JOIN products p ON c.product_id = p.id GROUP BY p.id, p.name ORDER BY total_quantity DESC LIMIT ?";
        List<TopProductStat> list = new ArrayList<>();
        try (PreparedStatement ps = getCon().prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(new TopProductStat(rs.getString("product_name"), rs.getLong("total_quantity"), rs.getDouble("total_revenue")));
        }
        return list;
    }

    public List<TopCustomerStat> getTopCustomers(int limit) throws SQLException {
        String sql = "SELECT CONCAT(c.prenom, ' ', c.nom) AS customer_name, COUNT(*) AS total_orders, SUM(ca.quantity * p.price) AS total_spent FROM carts ca JOIN customers c ON ca.customer_id = c.id JOIN products p ON ca.product_id = p.id GROUP BY c.id, c.prenom, c.nom ORDER BY total_spent DESC LIMIT ?";
        List<TopCustomerStat> list = new ArrayList<>();
        try (PreparedStatement ps = getCon().prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(new TopCustomerStat(rs.getString("customer_name"), rs.getLong("total_orders"), rs.getDouble("total_spent")));
        }
        return list;
    }

    public List<RevenueByMonthStat> getRevenueByMonth(int year) throws SQLException {
        List<RevenueByMonthStat> list = new ArrayList<>();
        try {
            String sql = "SELECT DATE_FORMAT(created_at, '%Y-%m') AS period, SUM(c.quantity * p.price) AS total_revenue FROM carts c JOIN products p ON c.product_id = p.id WHERE YEAR(created_at) = ? GROUP BY DATE_FORMAT(created_at, '%Y-%m') ORDER BY period";
            try (PreparedStatement ps = getCon().prepareStatement(sql)) {
                ps.setInt(1, year);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) list.add(new RevenueByMonthStat(rs.getString("period"), rs.getDouble("total_revenue")));
            }
        } catch (SQLException e) {
            // Si la table carts n'a pas created_at, retourner liste vide
        }
        return list;
    }

    public double getTotalRevenue() throws SQLException {
        String sql = "SELECT SUM(c.quantity * p.price) AS total_revenue FROM carts c JOIN products p ON c.product_id = p.id";
        try (PreparedStatement ps = getCon().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble("total_revenue");
        }
        return 0.0;
    }
}
