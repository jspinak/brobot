package io.github.jspinak.brobot.app.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Used with persistent databases, not with H2
 */
public class DatabaseChecker {

    private static final String JDBC_URL = "jdbc:h2:~/test"; // Update with your database URL
    private static final String JDBC_USER = "sa"; // Update with your database username
    private static final String JDBC_PASSWORD = ""; // Update with your database password

    public static void main(String[] args) {
        String query = "SELECT scene_id, COUNT(*) FROM state_scenes GROUP BY scene_id HAVING COUNT(*) > 1";

        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                long sceneId = resultSet.getLong("scene_id");
                int count = resultSet.getInt("COUNT(*)");
                System.out.println("Duplicate scene_id: " + sceneId + ", Count: " + count);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

