package Utlis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataSource {
    private static DataSource dataSource;
    private Connection con;
    private final String url = "jdbc:mysql://localhost:3306/ecommerce_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private final String username = "root";
    private final String password = "";

    private DataSource() {
        con = createConnection();
    }

    private Connection createConnection() {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de connexion à la base de données", e);
        }
    }

    public Connection getCon() {
        try {
            if (con == null || con.isClosed()) {
                con = createConnection();
            }
        } catch (SQLException e) {
            con = createConnection();
        }
        return con;
    }

    public static DataSource getInstance() {
        if (dataSource == null) {
            synchronized (DataSource.class) {
                if (dataSource == null) {
                    dataSource = new DataSource();
                }
            }
        }
        return dataSource;
    }
}
