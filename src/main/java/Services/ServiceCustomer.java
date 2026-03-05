package Services;

import Entite.Customer;
import Utlis.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCustomer implements IService<Customer> {
    private Connection getCon() { return DataSource.getInstance().getCon(); }

    @Override
    public boolean ajouter(Customer c) throws SQLException {
        try (PreparedStatement ps = getCon().prepareStatement("INSERT INTO customers(prenom, nom, email, user_id) VALUES (?,?,?,?)")) {
            ps.setString(1, c.getPrenom());
            ps.setString(2, c.getNom());
            ps.setString(3, c.getEmail());
            ps.setObject(4, c.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean supprimer(Customer c) throws SQLException {
        try (PreparedStatement ps = getCon().prepareStatement("DELETE FROM customers WHERE id=?")) {
            ps.setLong(1, c.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean modifier(Customer c) throws SQLException {
        try (PreparedStatement ps = getCon().prepareStatement("UPDATE customers SET prenom=?, nom=?, email=?, user_id=? WHERE id=?")) {
            ps.setString(1, c.getPrenom());
            ps.setString(2, c.getNom());
            ps.setString(3, c.getEmail());
            ps.setObject(4, c.getUserId());
            ps.setLong(5, c.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Customer> afficher() throws SQLException {
        List<Customer> list = new ArrayList<>();
        try (Statement st = getCon().createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM customers")) {
            while (rs.next()) list.add(mapCustomer(rs));
        }
        return list;
    }

    @Override
    public Customer findById(int id) throws SQLException {
        try (PreparedStatement ps = getCon().prepareStatement("SELECT * FROM customers WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapCustomer(rs);
        }
        return null;
    }

    public Customer findByEmail(String email) throws SQLException {
        if (email == null) return null;
        try (PreparedStatement ps = getCon().prepareStatement("SELECT * FROM customers WHERE email=?")) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapCustomer(rs);
        }
        return null;
    }

    /** Trouve le client lié à un utilisateur (pour la session panier à la connexion). */
    public Customer findByUserId(long userId) throws SQLException {
        try (PreparedStatement ps = getCon().prepareStatement("SELECT * FROM customers WHERE user_id=?")) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapCustomer(rs);
        }
        return null;
    }

    private static Customer mapCustomer(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String prenom = rs.getString("prenom");
        String nom = rs.getString("nom");
        String email = rs.getString("email");
        Long userId = null;
        try { if (rs.getObject("user_id") != null) userId = rs.getLong("user_id"); } catch (SQLException ignored) {}
        return new Customer(id, prenom, nom, email, userId);
    }
}
