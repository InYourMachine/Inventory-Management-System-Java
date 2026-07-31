import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Utility class for obtaining a MySQL database connection.

public class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/inventory_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "This_2389";

    private DBConnection() {
        // Prevent instantiation
    }


    // Returns a new connection to the MySQL database.

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found.", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
