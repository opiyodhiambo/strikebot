package Botlogger.database;

import Botlogger.model.Appeal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import static java.sql.DriverManager.getConnection;

public class AppealDatabase {
    private static final String DB_URL = System.getenv("DATABASE_PATH") != null
            ? System.getenv("DATABASE_PATH")
            : "jdbc:sqlite:opiyodhiambo";

    public AppealDatabase() {
        initAppealTables();
    }

    private void initAppealTables() {
        try (Connection conn = getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            String createAppealsTable = """
                CREATE TABLE IF NOT EXISTS appeals (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id TEXT NOT NULL,
                    strike_id INTEGER NOT NULL,
                    reason TEXT NOT NULL,
                    status TEXT DEFAULT 'PENDING',
                    reviewer_id TEXT,
                    review_reason TEXT,
                    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    review_date TIMESTAMP,
                    FOREIGN KEY (strike_id) REFERENCES strikes(id)
                );
                """;
            stmt.executeUpdate(createAppealsTable);

        } catch (SQLException e) {
            System.err.println("Error initializing appeal tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean createAppeal(String userId, int strikeId, String reason) {
        if (hasEverSubmittedAppeal(userId)) {
            return false;
        }

        String sql = "INSERT INTO appeals (user_id, strike_id, reason) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setInt(2, strikeId);
            pstmt.setString(3, reason);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating appeal: " + e.getMessage());
            return false;
        }
    }

    public boolean hasEverSubmittedAppeal(String userId) {
        String sql = "SELECT COUNT(*) FROM appeals WHERE user_id = ?";
        try (Connection conn = getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Error checking if user has submitted appeal: " + e.getMessage());
            return false;
        }
    }

    public List<Appeal> getAllPendingAppeals() {
        List<Appeal> appeals = new ArrayList<>();
        String sql = "SELECT * FROM appeals WHERE status = 'PENDING' ORDER BY date ASC";
        try (Connection conn = getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                appeals.add(new Appeal(
                        rs.getInt("id"),
                        rs.getString("user_id"),
                        rs.getInt("strike_id"),
                        rs.getString("reason"),
                        rs.getString("status"),
                        rs.getString("reviewer_id"),
                        rs.getString("review_reason"),
                        rs.getTimestamp("date"),
                        rs.getTimestamp("review_date")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting pending appeals: " + e.getMessage());
        }
        return appeals;
    }

    public List<Appeal> getUserAppeals(String userId) {
        List<Appeal> appeals = new ArrayList<>();
        String sql = "SELECT * FROM appeals WHERE user_id = ? ORDER BY date DESC";
        try (Connection conn = getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                appeals.add(new Appeal(
                        rs.getInt("id"),
                        rs.getString("user_id"),
                        rs.getInt("strike_id"),
                        rs.getString("reason"),
                        rs.getString("status"),
                        rs.getString("reviewer_id"),
                        rs.getString("review_reason"),
                        rs.getTimestamp("date"),
                        rs.getTimestamp("review_date")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting user appeals: " + e.getMessage());
        }
        return appeals;
    }

    public Appeal getAppealById(int appealId) {
        String sql = "SELECT * FROM appeals WHERE id = ?";
        try (Connection conn = getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, appealId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Appeal(
                        rs.getInt("id"),
                        rs.getString("user_id"),
                        rs.getInt("strike_id"),
                        rs.getString("reason"),
                        rs.getString("status"),
                        rs.getString("reviewer_id"),
                        rs.getString("review_reason"),
                        rs.getTimestamp("date"),
                        rs.getTimestamp("review_date")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting appeal by ID: " + e.getMessage());
        }
        return null;
    }

    public boolean reviewAppeal(int appealId, String reviewerId, String status, String reviewReason) {
        String sql = "UPDATE appeals SET status = ?, reviewer_id = ?, review_reason = ?, review_date = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setString(2, reviewerId);
            pstmt.setString(3, reviewReason);
            pstmt.setInt(4, appealId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error reviewing appeal: " + e.getMessage());
            return false;
        }
    }

    public boolean resetUserAppeals(String userId) {
        String sql = "DELETE FROM appeals WHERE user_id = ?";
        try (Connection conn = getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error resetting user appeals: " + e.getMessage());
            return false;
        }
    }
}