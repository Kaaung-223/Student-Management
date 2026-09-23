package com.example.student_management_system.Controller.Util;

public class GenerateHash {

    public static void main(String[] args) {

        // 👇 CHANGE THIS to whatever plaintext you want to hash
        String plain = "kK2386mm$";

        String hash = PasswordHasher.hash(plain);

        System.out.println("Plain     : " + plain);
        System.out.println("BCrypt    : " + hash);
        System.out.println("Verify OK : " + PasswordHasher.verify(plain, hash));
        System.out.println();
        System.out.println("--- paste this into your SQL file ---");
        System.out.println("'" + hash + "'");
    }
}