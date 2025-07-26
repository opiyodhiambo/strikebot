package Botlogger.service;

import Botlogger.database.AppealDatabase;
import Botlogger.database.StrikeDatabase;
import Botlogger.model.Appeal;
import Botlogger.model.Strike;
import java.util.List;

public class AppealService {
    private final AppealDatabase appealDatabase = new AppealDatabase();
    private final StrikeDatabase strikeDatabase = new StrikeDatabase();

    public boolean createAppeal(String userId, int strikeNumber, String reason) {
        if (appealDatabase.hasEverSubmittedAppeal(userId)) {
            return false;
        }

        List<Strike> strikes = strikeDatabase.getStrikes(userId);

        if (strikeNumber < 1 || strikeNumber > strikes.size()) {
            return false;
        }

        Strike strike = strikes.get(strikeNumber - 1);
        return appealDatabase.createAppeal(userId, strike.getId(), reason);
    }

    public boolean hasEverSubmittedAppeal(String userId) {
        return appealDatabase.hasEverSubmittedAppeal(userId);
    }

    public List<Appeal> getAllPendingAppeals() {
        return appealDatabase.getAllPendingAppeals();
    }

    public List<Appeal> getUserAppeals(String userId) {
        return appealDatabase.getUserAppeals(userId);
    }

    public Appeal getAppealById(int appealId) {
        return appealDatabase.getAppealById(appealId);
    }

    public boolean approveAppeal(int appealId, String reviewerId, String reviewReason) {
        Appeal appeal = appealDatabase.getAppealById(appealId);
        if (appeal == null || !appeal.getStatus().equals("PENDING")) {
            return false;
        }

        boolean updated = appealDatabase.reviewAppeal(appealId, reviewerId, "APPROVED", reviewReason);

        if (updated) {
            removeStrikeById(appeal.getStrikeId());
        }

        return updated;
    }

    public boolean denyAppeal(int appealId, String reviewerId, String reviewReason) {
        return appealDatabase.reviewAppeal(appealId, reviewerId, "DENIED", reviewReason);
    }

    public boolean resetUserAppeals(String userId) {
        return appealDatabase.resetUserAppeals(userId);
    }

    private void removeStrikeById(int strikeId) {
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
                System.getenv("DATABASE_PATH") != null ? System.getenv("DATABASE_PATH") : "jdbc:sqlite:zrestaffstrikes")) {
            String sql = "DELETE FROM strikes WHERE id = ?";
            try (java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, strikeId);
                pstmt.executeUpdate();
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error removing strike by ID: " + e.getMessage());
        }
    }

    public Strike getStrikeById(int strikeId) {
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
                System.getenv("DATABASE_PATH") != null ? System.getenv("DATABASE_PATH") : "jdbc:sqlite:zrestaffstrikes")) {
            String sql = "SELECT * FROM strikes WHERE id = ?";
            try (java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, strikeId);
                java.sql.ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return new Strike(
                            rs.getInt("id"),
                            rs.getString("user_id"),
                            rs.getString("reason"),
                            rs.getString("moderator_id"),
                            rs.getTimestamp("date")
                    );
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error getting strike by ID: " + e.getMessage());
        }
        return null;
    }
}