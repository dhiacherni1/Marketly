package Services;

import Entite.User;
import Utlis.DataSource;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceUser implements IService<User> {
    private static final String HASH_ALGO = "SHA-256";

    private Connection getCon() { return DataSource.getInstance().getCon(); }

    public static String hashPassword(String plainPassword) {
        if (plainPassword == null) return null;
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGO);
            byte[] hash = md.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return plainPassword;
        }
    }

    private static boolean looksLikeHash(String stored) {
        if (stored == null || stored.length() != 64) return false;
        return stored.matches("[0-9a-fA-F]{64}");
    }

    @Override
    public boolean ajouter(User u) throws SQLException {
        try (PreparedStatement ps = getCon().prepareStatement("INSERT INTO users(username, password, role) VALUES (?,?,?)")) {
            ps.setString(1, u.getUsername());
            ps.setString(2, hashPassword(u.getPassword()));
            ps.setString(3, u.getRole() != null ? u.getRole() : "CLIENT");
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean supprimer(User u) throws SQLException {
        try (PreparedStatement ps = getCon().prepareStatement("DELETE FROM users WHERE id=?")) {
            ps.setLong(1, u.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean modifier(User u) throws SQLException {
        try (PreparedStatement ps = getCon().prepareStatement("UPDATE users SET username=?, password=?, role=? WHERE id=?")) {
            ps.setString(1, u.getUsername());
            ps.setString(2, hashPassword(u.getPassword()));
            ps.setString(3, u.getRole());
            ps.setLong(4, u.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<User> afficher() throws SQLException {
        List<User> list = new ArrayList<>();
        try (Statement st = getCon().createStatement(); ResultSet rs = st.executeQuery("SELECT id, username, password, role FROM users")) {
            while (rs.next()) list.add(mapUser(rs));
        }
        return list;
    }

    @Override
    public User findById(int id) throws SQLException {
        try (PreparedStatement ps = getCon().prepareStatement("SELECT id, username, password, role FROM users WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapUser(rs);
        }
        return null;
    }

    public User authenticate(String username, String password) throws SQLException {
        if (username == null || password == null) return null;
        User user = findByUsername(username);
        if (user == null) return null;
        String stored = user.getPassword();
        boolean match = looksLikeHash(stored) ? hashPassword(password).equals(stored) : password.equals(stored);
        return match ? user : null;
    }

    public User findByUsername(String username) throws SQLException {
        if (username == null) return null;
        try (PreparedStatement ps = getCon().prepareStatement("SELECT id, username, password, role FROM users WHERE username=?")) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapUser(rs);
        }
        return null;
    }

    private static User mapUser(ResultSet rs) throws SQLException {
        return new User(rs.getLong("id"), rs.getString("username"), rs.getString("password"), rs.getString("role"));
    }

    /** Met à jour last_login pour un utilisateur (appelé à la connexion). */
    public void updateLastLogin(long userId) throws SQLException {
        try (PreparedStatement ps = getCon().prepareStatement("UPDATE users SET last_login = NOW() WHERE id = ?")) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage() == null || !e.getMessage().contains("last_login")) throw e;
        }
    }

    /** Nombre total d'utilisateurs. */
    public int getTotalUsers() throws SQLException {
        try (Statement st = getCon().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Nombre d'utilisateurs par rôle (CLIENT, ADMIN, MANAGER). */
    public int getCountByRole(String role) throws SQLException {
        try (PreparedStatement ps = getCon().prepareStatement("SELECT COUNT(*) FROM users WHERE role = ?")) {
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Nouveaux inscrits ce mois (nécessite colonne created_at). */
    public int getNewInscritsThisMonth() throws SQLException {
        try {
            try (Statement st = getCon().createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users WHERE MONTH(created_at) = MONTH(CURDATE()) AND YEAR(created_at) = YEAR(CURDATE())")) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("created_at")) return 0;
            throw e;
        }
    }

    /** Utilisateurs actifs aujourd'hui (nécessite colonne last_login). */
    public int getActiveToday() throws SQLException {
        try {
            try (Statement st = getCon().createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users WHERE DATE(last_login) = CURDATE()")) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("last_login")) return 0;
            throw e;
        }
    }
}
