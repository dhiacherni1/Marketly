package Services;

import Entite.Provider;
import Utlis.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceProvider implements IService<Provider> {
    private Connection getCon() { return DataSource.getInstance().getCon(); }

    @Override
    public boolean ajouter(Provider p) throws SQLException {
        try (PreparedStatement ps = getCon().prepareStatement("INSERT INTO providers(name, phone, address, email) VALUES (?,?,?,?)")) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getPhone());
            ps.setString(3, p.getAddress());
            ps.setString(4, p.getEmail());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean supprimer(Provider p) throws SQLException {
        try (PreparedStatement ps = getCon().prepareStatement("DELETE FROM providers WHERE id=?")) {
            ps.setLong(1, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean modifier(Provider p) throws SQLException {
        try (PreparedStatement ps = getCon().prepareStatement("UPDATE providers SET name=?, phone=?, address=?, email=? WHERE id=?")) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getPhone());
            ps.setString(3, p.getAddress());
            ps.setString(4, p.getEmail());
            ps.setLong(5, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Provider> afficher() throws SQLException {
        List<Provider> list = new ArrayList<>();
        try (Statement st = getCon().createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM providers")) {
            while (rs.next()) {
                list.add(new Provider(rs.getLong("id"), rs.getString("name"), rs.getString("phone"), rs.getString("address"), getStringOrNull(rs, "email")));
            }
        }
        return list;
    }

    @Override
    public Provider findById(int id) throws SQLException {
        try (PreparedStatement ps = getCon().prepareStatement("SELECT * FROM providers WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new Provider(rs.getLong("id"), rs.getString("name"), rs.getString("phone"), rs.getString("address"), getStringOrNull(rs, "email"));
        }
        return null;
    }

    private static String getStringOrNull(ResultSet rs, String column) throws SQLException {
        try {
            return rs.getString(column);
        } catch (SQLException e) {
            return null;
        }
    }
}
