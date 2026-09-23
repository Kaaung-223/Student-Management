package com.example.student_management_system.Controller.Util;

import com.example.student_management_system.Controller.DAO.DBConnention;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Auto-migrates every plaintext password in users → BCrypt.
 * Idempotent — safe to run on every application startup.
 *
 * Called once from Main.start().
 */
public final class AutoHashOnStartup {

    private AutoHashOnStartup() {}

    public static void run() {
        String selectSql = "SELECT user_id, username, role, password FROM users";
        String updateSql = "UPDATE users SET password = ? WHERE user_id = ?";

        int hashed = 0, skipped = 0, failed = 0;

        try (Connection con = DBConnention.getConnection();
             PreparedStatement sel = con.prepareStatement(selectSql);
             ResultSet rs = sel.executeQuery();
             PreparedStatement upd = con.prepareStatement(updateSql)) {

            while (rs.next()) {
                int    id       = rs.getInt("user_id");
                String username = rs.getString("username");
                String role     = rs.getString("role");
                String stored   = rs.getString("password");

                if (stored == null || stored.isEmpty()) {
                    skipped++;
                    continue;
                }

                if (PasswordHasher.isHashed(stored)) {
                    skipped++;
                    continue;
                }

                try {
                    String hash = PasswordHasher.hash(stored);
                    upd.setString(1, hash);
                    upd.setInt(2, id);
                    upd.executeUpdate();
                    System.out.printf("HASHED [%s] %-15s%n", role, username);
                    hashed++;
                } catch (Exception ex) {
                    System.err.printf("FAILED [%s] %-15s : %s%n",
                            role, username, ex.getMessage());
                    failed++;
                }
            }

            System.out.println("─────────────────────────────────────────────");
            System.out.printf("AutoHashOnStartup → hashed=%d, skipped=%d, failed=%d%n",
                    hashed, skipped, failed);

        } catch (Exception e) {
            System.err.println("AutoHashOnStartup could not run: " + e.getMessage());
            e.printStackTrace();
        }
    }
}