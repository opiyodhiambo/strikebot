package Botlogger.database;

import Botlogger.model.Strike;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.SQLException;
import static java.sql.DriverManager.getConnection;

public class StrikeDatabase {
    private static final String DB_URL = System.getenv("DATABASE_PATH") != null
            ? System.getenv("DATABASE_PATH")
            : "jdbc:sqlite:zrestaffstrikes";

    public StrikeDatabase() {
        initDatabase();
    }

    private void initDatabase() {
        try (Connection conn = getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            String createStrikesTable = """
                CREATE TABLE IF NOT EXISTS strikes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id TEXT NOT NULL,
                    reason TEXT NOT NULL,
                    moderator_id TEXT NOT NULL,
                    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """;
            stmt.executeUpdate(createStrikesTable);

            String createMetadataTable = """
                CREATE TABLE IF NOT EXISTS bot_metadata (
                    key TEXT PRIMARY KEY,
                    value TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """;
            stmt.executeUpdate(createMetadataTable);

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void addStrike(String userId, String reason, String moderatorId, Timestamp date) {
        String sql = "INSERT INTO strikes (user_id, reason, moderator_id, date) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, reason);
            pstmt.setString(3, moderatorId);
            pstmt.setTimestamp(4, date);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding strike: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Strike> getStrikes(String userId) {
        List<Strike> strikes = new ArrayList<>();
        String sql = "SELECT id, user_id, reason, moderator_id, date FROM strikes WHERE user_id = ?";
        try (Connection conn = getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                strikes.add(new Strike(
                        rs.getInt("id"),
                        rs.getString("user_id"),
                        rs.getString("reason"),
                        rs.getString("moderator_id"),
                        rs.getTimestamp("date")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting strikes: " + e.getMessage());
            e.printStackTrace();
        }
        return strikes;
    }

    public void clearStrikes(String userId) {
        String sql = "DELETE FROM strikes WHERE user_id = ?";
        try (Connection conn = getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error clearing strikes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void removeStrike(String userId, int strikeNumber) {
        List<Strike> strikes = getStrikes(userId);
        if (strikeNumber < 1 || strikeNumber > strikes.size()) {
            return;
        }

        Strike strikeToRemove = strikes.get(strikeNumber - 1);
        String sql = "DELETE FROM strikes WHERE id = ?";
        try (Connection conn = getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, strikeToRemove.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error removing strike: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void editStrike(String userId, int strikeNumber, String newReason) {
        List<Strike> strikes = getStrikes(userId);

        if (strikeNumber < 1 || strikeNumber > strikes.size()) {
            throw new IllegalArgumentException("Invalid strike number");
        }

        Strike strikeToEdit = strikes.get(strikeNumber - 1);

        String sql = "UPDATE strikes SET reason = ? WHERE id = ?";

        try (Connection conn = getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newReason);
            pstmt.setInt(2, strikeToEdit.getId());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected == 0) {
                System.err.println("Warning: No strike was updated for user " + userId);
            }

        } catch (SQLException e) {
            System.err.println("Error editing strike for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean hasBeenInitialized() {
        String sql = "SELECT value FROM bot_metadata WHERE key = 'strikes_imported'";
        try (Connection conn = getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && "true".equals(rs.getString("value"));
        } catch (SQLException e) {
            System.err.println("Error checking initialization status: " + e.getMessage());
            return false;
        }
    }

    public void markAsInitialized() {
        String sql = "INSERT OR REPLACE INTO bot_metadata (key, value) VALUES ('strikes_imported', 'true')";
        try (Connection conn = getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
            System.out.println("✅ Database marked as initialized - strikes will not be re-imported");
        } catch (SQLException e) {
            System.err.println("Error marking database as initialized: " + e.getMessage());
        }
    }

    public int getTotalStrikeCount() {
        String sql = "SELECT COUNT(*) as count FROM strikes";
        try (Connection conn = getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error getting total strike count: " + e.getMessage());
        }
        return 0;
    }
    public List<String> getAllUsersWithStrikes() {
        List<String> userIds = new ArrayList<>();
        String sql = "SELECT DISTINCT user_id FROM strikes";

        try (Connection conn = getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                userIds.add(rs.getString("user_id"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all users with strikes: " + e.getMessage());
            e.printStackTrace();
        }

        return userIds;
    }
}