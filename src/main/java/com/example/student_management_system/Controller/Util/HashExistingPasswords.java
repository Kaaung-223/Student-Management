package com.example.student_management_system.Controller.Util;

import com.example.student_management_system.Controller.DAO.DBConnention;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Migration: hashes every plaintext password in users (ALL roles).
 * Safe to re-run — already-hashed rows are skipped.
 *
 * Right-click this file → Run 'HashExistingPasswords.main()'.
 */
public class HashExistingPasswords {

    public static void main(String[] args) throws Exception {

        String selectSql = "SELECT user_id, username, role, password FROM users";
        String updateSql = "UPDATE users SET password = ? WHERE user_id = ?";

        try (Connection con = DBConnention.getConnection();
             PreparedStatement sel = con.prepareStatement(selectSql);
             ResultSet rs = sel.executeQuery();
             PreparedStatement upd = con.prepareStatement(updateSql)) {

            int hashed = 0, skipped = 0;

            while (rs.next()) {
                int    id       = rs.getInt("user_id");
                String username = rs.getString("username");
                String role     = rs.getString("role");
                String stored   = rs.getString("password");

                if (PasswordHasher.isHashed(stored)) {
                    System.out.printf("SKIP   [%s] %-15s%n", role, username);
                    skipped++;
                    continue;
                }

                String hash = PasswordHasher.hash(stored);
                upd.setString(1, hash);
                upd.setInt(2, id);
                upd.executeUpdate();

                System.out.printf("HASHED [%s] %-15s  %s%n", role, username, hash);
                hashed++;
            }

            System.out.println("--------------------------------------------");
            System.out.println("Done. Hashed = " + hashed + ", Skipped = " + skipped);
        }
    }
}