package com.example.demo.security.jwt;

import java.util.Base64;
import java.security.SecureRandom;

public class JwtKeyGenerator {
    public static void main(String[] args) {
        byte[] key = new byte[64]; // 512bit
        new SecureRandom().nextBytes(key);
        String encoded = Base64.getEncoder().encodeToString(key);
        System.out.println(encoded);
    }
}