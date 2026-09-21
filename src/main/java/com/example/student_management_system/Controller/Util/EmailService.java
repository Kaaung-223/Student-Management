package com.example.student_management_system.Controller.Util;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

/**
 * Sends OTP emails via Gmail SMTP.
 *
 * Credentials are hardcoded below — no external properties file needed.
 * Replace GMAIL_APP_PASSWORD with your 16-character Gmail App Password.
 */
public class EmailService {

    // ============================================================
    //  👇👇👇  EDIT ONLY THESE TWO LINES  👇👇👇
    // ============================================================
    private static final String GMAIL_USER         = "kk8264483@gmail.com";
    private static final String GMAIL_APP_PASSWORD = "";
    // ============================================================
    //  👆👆👆  EDIT ONLY THESE TWO LINES  👆👆👆
    // ============================================================

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String FROM_NAME = "StudentHub";

    public static void sendOtpEmail(String toEmail,
                                    String recipientName,
                                    String otp,
                                    int validSeconds) throws MessagingException {

        System.out.println("[EmailService] Preparing to send OTP to " + toEmail);
        System.out.println("[EmailService] From: " + GMAIL_USER);
        System.out.println("[EmailService] Password length: " + GMAIL_APP_PASSWORD.length());

        // ---- Validation (clear error messages) ----
        if (GMAIL_USER == null || GMAIL_USER.isBlank()) {
            throw new MessagingException("GMAIL_USER is empty. Edit EmailService.java.");
        }
        if (GMAIL_APP_PASSWORD == null || GMAIL_APP_PASSWORD.isBlank()
                || GMAIL_APP_PASSWORD.equals("PASTE_YOUR_16_CHAR_APP_PASSWORD_HERE")) {
            throw new MessagingException(
                    "You did NOT paste your Gmail App Password. " +
                            "Open EmailService.java, find GMAIL_APP_PASSWORD, " +
                            "and paste the 16-character code from " +
                            "https://myaccount.google.com/apppasswords");
        }
        if (GMAIL_APP_PASSWORD.contains(" ")) {
            throw new MessagingException(
                    "GMAIL_APP_PASSWORD contains spaces. " +
                            "Remove all spaces. 'abcd efgh ijkl mnop' -> 'abcdefghijklmnop'");
        }
        if (GMAIL_APP_PASSWORD.length() != 16) {
            throw new MessagingException(
                    "GMAIL_APP_PASSWORD must be 16 characters. " +
                            "Yours is " + GMAIL_APP_PASSWORD.length() + ". " +
                            "If you used your normal Gmail password, it will NOT work. " +
                            "Create an App Password at https://myaccount.google.com/apppasswords");
        }

        // ---- SMTP setup ----
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.trust", SMTP_HOST);
        props.put("mail.smtp.connectiontimeout", "20000");
        props.put("mail.smtp.timeout", "20000");
        props.put("mail.smtp.writetimeout", "20000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(GMAIL_USER, GMAIL_APP_PASSWORD);
            }
        });

        // ---- Build & send ----
        MimeMessage msg = new MimeMessage(session);
        try {
            msg.setFrom(new InternetAddress(GMAIL_USER, FROM_NAME));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            msg.setSubject("StudentHub - Your Password Reset Code");
            msg.setContent(buildHtml(recipientName, otp, validSeconds),
                    "text/html; charset=utf-8");

            System.out.println("[EmailService] Connecting to " + SMTP_HOST + ":" + SMTP_PORT + " ...");
            Transport.send(msg);
            System.out.println("[EmailService] ✅ SUCCESS — email sent to " + toEmail);

        } catch (MessagingException me) {
            System.err.println("[EmailService] ❌ FAILED to send to " + toEmail);
            System.err.println("[EmailService] Reason: " + me.getMessage());
            Throwable cause = me.getCause();
            if (cause != null) {
                System.err.println("[EmailService] Cause : " + cause.getMessage());
            }
            throw me;
        } catch (Exception e) {
            System.err.println("[EmailService] ❌ Unexpected error: " + e.getMessage());
            throw new MessagingException("Failed to send: " + e.getMessage(), e);
        }
    }

    private static String buildHtml(String name, String otp, int validSeconds) {
        String safeName = (name == null || name.isBlank()) ? "there" : escape(name);
        return "<div style=\"font-family:Segoe UI,Arial,sans-serif;background:#f8fafc;padding:28px;\">"
                + "<div style=\"max-width:520px;margin:0 auto;background:#ffffff;border-radius:14px;"
                + "border:1px solid #e2e8f0;padding:28px;\">"
                +   "<h2 style=\"color:#0f172a;margin:0 0 6px;\">StudentHub Password Reset</h2>"
                +   "<p style=\"color:#64748b;margin:0 0 18px;\">Hello " + safeName + ",</p>"
                +   "<p style=\"color:#334155;margin:0 0 18px;\">Use the code below to reset your "
                +   "password. This code expires in <b>" + validSeconds + " seconds</b>.</p>"
                +   "<div style=\"text-align:center;margin:22px 0;\">"
                +     "<div style=\"display:inline-block;background:#eef2ff;color:#4338ca;"
                +       "font-size:34px;font-weight:bold;letter-spacing:12px;padding:14px 26px;"
                +       "border-radius:12px;\">" + otp + "</div>"
                +   "</div>"
                +   "<p style=\"color:#94a3b8;font-size:12px;margin:22px 0 0;\">"
                +   "If you did not request this, you can safely ignore this email.</p>"
                + "</div></div>";
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ---- Standalone test — right-click this file → Run ----
    public static void main(String[] args) {
        try {
            sendOtpEmail("kk8264483@gmail.com", "Kaung", "123456", 30);
            System.out.println(">>> TEST PASSED — check inbox (and Spam).");
        } catch (Exception e) {
            System.out.println(">>> TEST FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}