package Services;

import Entite.Category;
import Entite.Product;
import Entite.SubCategory;
import Utlis.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceProduct implements IService<Product> {
    private Connection getCon() { return DataSource.getInstance().getCon(); }

    private static void setLongOrNull(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value != null) ps.setLong(index, value);
        else ps.setNull(index, java.sql.Types.BIGINT);
    }

    @Override
    public boolean ajouter(Product p) throws SQLException {
        Connection con = getCon();
        if (p.getCategory() != null && p.getCategory().getId() != null) {
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO products(name, price, quantity, category_id) VALUES (?,?,?,?)")) {
                ps.setString(1, p.getName());
                ps.setDouble(2, p.getPrice());
                ps.setInt(3, p.getQuantity());
                ps.setLong(4, p.getCategory().getId());
                return ps.executeUpdate() > 0;
            } catch (SQLException ignored) {}
        }
        if (p.getProvider() != null && p.getProvider().getId() != null) {
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO products(name, price, quantity, provider_id) VALUES (?,?,?,?)")) {
                ps.setString(1, p.getName());
                ps.setDouble(2, p.getPrice());
                ps.setInt(3, p.getQuantity());
                ps.setLong(4, p.getProvider().getId());
                return ps.executeUpdate() > 0;
            } catch (SQLException ignored) {}
        }
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO products(name, price, quantity) VALUES (?,?,?)")) {
            ps.setString(1, p.getName());
            ps.setDouble(2, p.getPrice());
            ps.setInt(3, p.getQuantity());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean supprimer(Product p) throws SQLException {
        try (PreparedStatement ps = getCon().prepareStatement("DELETE FROM products WHERE id=?")) {
            ps.setLong(1, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean modifier(Product p) throws SQLException {
        Connection con = getCon();
        try (PreparedStatement ps = con.prepareStatement("UPDATE products SET name=?, price=?, quantity=?, category_id=?, subcategory_id=?, provider_id=? WHERE id=?")) {
            ps.setString(1, p.getName());
            ps.setDouble(2, p.getPrice());
            ps.setInt(3, p.getQuantity());
            setLongOrNull(ps, 4, p.getCategory() != null ? p.getCategory().getId() : null);
            setLongOrNull(ps, 5, p.getSubCategory() != null ? p.getSubCategory().getId() : null);
            setLongOrNull(ps, 6, p.getProvider() != null ? p.getProvider().getId() : null);
            ps.setLong(7, p.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE products SET name=?, price=?, quantity=? WHERE id=?")) {
                ps.setString(1, p.getName());
                ps.setDouble(2, p.getPrice());
                ps.setInt(3, p.getQuantity());
                ps.setLong(4, p.getId());
                return ps.executeUpdate() > 0;
            }
        }
    }

    @Override
    public List<Product> afficher() throws SQLException {
        List<Product> list = new ArrayList<>();
        Connection con = getCon();
        try {
            String sql = "SELECT p.id, p.name, p.price, p.quantity, p.category_id, c.name AS category_name, p.subcategory_id, sc.name AS subcategory_name FROM products p LEFT JOIN categories c ON p.category_id = c.id LEFT JOIN subcategories sc ON p.subcategory_id = sc.id";
            try (ResultSet rs = con.createStatement().executeQuery(sql)) {
                while (rs.next()) list.add(mapProductWithCategory(rs, true));
            }
        } catch (SQLException e) {
            try {
                String sql = "SELECT p.id, p.name, p.price, p.quantity, p.category_id, c.name AS category_name FROM products p LEFT JOIN categories c ON p.category_id = c.id";
                try (ResultSet rs = con.createStatement().executeQuery(sql)) {
                    while (rs.next()) list.add(mapProductWithCategory(rs, false));
                }
            } catch (SQLException e2) {
                try (ResultSet rs = con.createStatement().executeQuery("SELECT id, name, price, quantity FROM products")) {
                    while (rs.next()) list.add(new Product(rs.getLong("id"), rs.getString("name"), rs.getDouble("price"), rs.getInt("quantity"), null, null));
                }
            }
        }
        return list;
    }

    @Override
    public Product findById(int id) throws SQLException {
        Connection con = getCon();
        try (PreparedStatement ps = con.prepareStatement("SELECT p.id, p.name, p.price, p.quantity, p.category_id, c.name AS category_name, p.subcategory_id, sc.name AS subcategory_name FROM products p LEFT JOIN categories c ON p.category_id = c.id LEFT JOIN subcategories sc ON p.subcategory_id = sc.id WHERE p.id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapProductWithCategory(rs, true);
        } catch (SQLException e) {
            try (PreparedStatement ps = con.prepareStatement("SELECT p.id, p.name, p.price, p.quantity, p.category_id, c.name AS category_name FROM products p LEFT JOIN categories c ON p.category_id = c.id WHERE p.id=?")) {
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return mapProductWithCategory(rs, false);
            } catch (SQLException e2) {
                try (PreparedStatement ps = con.prepareStatement("SELECT id, name, price, quantity FROM products WHERE id=?")) {
                    ps.setInt(1, id);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) return new Product(rs.getLong("id"), rs.getString("name"), rs.getDouble("price"), rs.getInt("quantity"), null, null);
                }
            }
        }
        return null;
    }

    private Product mapProductWithCategory(ResultSet rs, boolean hasSubCategory) throws SQLException {
        Long categoryId = rs.getObject("category_id") == null ? null : rs.getLong("category_id");
        Category category = categoryId != null ? new Category(categoryId, rs.getString("category_name"), null) : null;
        Product p = new Product(rs.getLong("id"), rs.getString("name"), rs.getDouble("price"), rs.getInt("quantity"), category, null);
        if (hasSubCategory) {
            try {
                Object subIdObj = rs.getObject("subcategory_id");
                if (subIdObj != null) {
                    SubCategory sub = new SubCategory(rs.getLong("subcategory_id"), rs.getString("subcategory_name"), category);
                    p.setSubCategory(sub);
                }
            } catch (SQLException ignored) {}
        }
        return p;
    }

    /** Nombre total de produits. */
    public int getTotalProductsCount() throws SQLException {
        try (Statement st = getCon().createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM products")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
