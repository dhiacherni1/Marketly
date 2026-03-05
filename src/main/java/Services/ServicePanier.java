package Services;

import Entite.Customer;
import Entite.Panier;
import Entite.Product;
import Utlis.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicePanier implements IService<Panier> {
    private Connection getCon() { return DataSource.getInstance().getCon(); }
    private final ServiceCustomer serviceCustomer = new ServiceCustomer();
    private final ServiceProduct serviceProduct = new ServiceProduct();

    @Override
    public boolean ajouter(Panier p) throws SQLException {
        Connection con = getCon();
        String checkSql = "SELECT id, quantity FROM carts WHERE customer_id=? AND product_id=?";
        try (PreparedStatement check = con.prepareStatement(checkSql)) {
            check.setLong(1, p.getCustomer().getId());
            check.setLong(2, p.getProduct().getId());
            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                long id = rs.getLong("id");
                int newQty = rs.getInt("quantity") + p.getQuantity();
                try (PreparedStatement ups = con.prepareStatement("UPDATE carts SET quantity=? WHERE id=?")) {
                    ups.setInt(1, newQty);
                    ups.setLong(2, id);
                    return ups.executeUpdate() > 0;
                }
            }
        }
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO carts(customer_id, product_id, quantity) VALUES (?,?,?)")) {
            ps.setLong(1, p.getCustomer().getId());
            ps.setLong(2, p.getProduct().getId());
            ps.setInt(3, p.getQuantity());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean supprimer(Panier p) throws SQLException {
        try (PreparedStatement ps = getCon().prepareStatement("DELETE FROM carts WHERE id=?")) {
            ps.setLong(1, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean modifier(Panier p) throws SQLException {
        try (PreparedStatement ps = getCon().prepareStatement("UPDATE carts SET customer_id=?, product_id=?, quantity=? WHERE id=?")) {
            ps.setLong(1, p.getCustomer().getId());
            ps.setLong(2, p.getProduct().getId());
            ps.setInt(3, p.getQuantity());
            ps.setLong(4, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Panier> afficher() throws SQLException {
        List<Panier> list = new ArrayList<>();
        try (Statement st = getCon().createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM carts")) {
            while (rs.next()) {
                Customer customer = serviceCustomer.findById(rs.getInt("customer_id"));
                Product product = serviceProduct.findById(rs.getInt("product_id"));
                list.add(new Panier(rs.getLong("id"), customer, product, rs.getInt("quantity")));
            }
        }
        return list;
    }

    public List<Panier> findByCustomerId(long customerId) throws SQLException {
        List<Panier> list = new ArrayList<>();
        try (PreparedStatement ps = getCon().prepareStatement("SELECT * FROM carts WHERE customer_id=?")) {
            ps.setLong(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Customer customer = serviceCustomer.findById(rs.getInt("customer_id"));
                Product product = serviceProduct.findById(rs.getInt("product_id"));
                list.add(new Panier(rs.getLong("id"), customer, product, rs.getInt("quantity")));
            }
        }
        return list;
    }

    @Override
    public Panier findById(int id) throws SQLException {
        try (PreparedStatement ps = getCon().prepareStatement("SELECT * FROM carts WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Customer customer = serviceCustomer.findById(rs.getInt("customer_id"));
                Product product = serviceProduct.findById(rs.getInt("product_id"));
                return new Panier(rs.getLong("id"), customer, product, rs.getInt("quantity"));
            }
        }
        return null;
    }
}
