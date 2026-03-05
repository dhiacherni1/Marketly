package Services;

import Entite.Category;
import Entite.SubCategory;
import Utlis.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceSubCategory implements IService<SubCategory> {
    private Connection getCon() { return DataSource.getInstance().getCon(); }

    private static String getStringOrNull(ResultSet rs, String column) {
        try { return rs.getString(column); } catch (SQLException e) { return null; }
    }

    @Override
    public boolean ajouter(SubCategory sc) throws SQLException {
        try (PreparedStatement ps = getCon().prepareStatement("INSERT INTO subcategories(name, category_id, image_path) VALUES (?,?,?)")) {
            ps.setString(1, sc.getName());
            ps.setLong(2, sc.getCategory().getId());
            ps.setString(3, sc.getImagePath());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("image_path")) {
                try (PreparedStatement ps = getCon().prepareStatement("INSERT INTO subcategories(name, category_id) VALUES (?,?)")) {
                    ps.setString(1, sc.getName());
                    ps.setLong(2, sc.getCategory().getId());
                    return ps.executeUpdate() > 0;
                }
            }
            throw e;
        }
    }

    @Override
    public boolean supprimer(SubCategory sc) throws SQLException {
        try (PreparedStatement ps = getCon().prepareStatement("DELETE FROM subcategories WHERE id=?")) {
            ps.setLong(1, sc.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean modifier(SubCategory sc) throws SQLException {
        try (PreparedStatement ps = getCon().prepareStatement("UPDATE subcategories SET name=?, category_id=?, image_path=? WHERE id=?")) {
            ps.setString(1, sc.getName());
            ps.setLong(2, sc.getCategory().getId());
            ps.setString(3, sc.getImagePath());
            ps.setLong(4, sc.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("image_path")) {
                try (PreparedStatement ps = getCon().prepareStatement("UPDATE subcategories SET name=?, category_id=? WHERE id=?")) {
                    ps.setString(1, sc.getName());
                    ps.setLong(2, sc.getCategory().getId());
                    ps.setLong(3, sc.getId());
                    return ps.executeUpdate() > 0;
                }
            }
            throw e;
        }
    }

    @Override
    public List<SubCategory> afficher() throws SQLException {
        List<SubCategory> list = new ArrayList<>();
        try (Statement st = getCon().createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM subcategories")) {
            while (rs.next()) {
                Category c = new Category(rs.getLong("category_id"), null, null);
                String imagePath = getStringOrNull(rs, "image_path");
                list.add(new SubCategory(rs.getLong("id"), rs.getString("name"), c, imagePath));
            }
        }
        return list;
    }

    @Override
    public SubCategory findById(int id) throws SQLException {
        try (PreparedStatement ps = getCon().prepareStatement("SELECT * FROM subcategories WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Category c = new Category(rs.getLong("category_id"), null, null);
                String imagePath = getStringOrNull(rs, "image_path");
                return new SubCategory(rs.getLong("id"), rs.getString("name"), c, imagePath);
            }
        }
        return null;
    }
}
