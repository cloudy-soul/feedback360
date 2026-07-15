package com.feedback.feedback360;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptEncodeTest {

    @Test
    void printBcryptHash() {
        String raw = "Admin123!";
        String hash = new BCryptPasswordEncoder().encode(raw);
        System.out.println("BCrypt(Admin123!): " + hash);
    }
}
