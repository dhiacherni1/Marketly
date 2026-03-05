package Services;

import Entite.Category;
import Utlis.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCategory implements IService<Category> {
    private Connection getCon() { return DataSource.getInstance().getCon(); }

    @Override
    public boolean ajouter(Category c) throws SQLException {
        String sql = "INSERT INTO categories(name, description) VALUES (?,?)";
        try (PreparedStatement ps = getCon().prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getDescription());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean supprimer(Category c) throws SQLException {
        try (PreparedStatement ps = getCon().prepareStatement("DELETE FROM categories WHERE id=?")) {
            ps.setLong(1, c.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean modifier(Category c) throws SQLException {
        String sql = "UPDATE categories SET name=?, description=? WHERE id=?";
        try (PreparedStatement ps = getCon().prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getDescription());
            ps.setLong(3, c.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Category> afficher() throws SQLException {
        List<Category> list = new ArrayList<>();
        try (Statement st = getCon().createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM categories")) {
            while (rs.next()) {
                list.add(new Category(rs.getLong("id"), rs.getString("name"), rs.getString("description")));
            }
        }
        return list;
    }

    @Override
    public Category findById(int id) throws SQLException {
        try (PreparedStatement ps = getCon().prepareStatement("SELECT * FROM categories WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new Category(rs.getLong("id"), rs.getString("name"), rs.getString("description"));
        }
        return null;
    }
}
