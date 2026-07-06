package com.feedback.feedback360;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

@SpringBootApplication
@EnableScheduling
public class Feedback360Application {

    public static void main(String[] args) {
        SpringApplication.run(Feedback360Application.class, args);
    }

    @PostConstruct
    public void checkMailConfig() {

        System.out.println("========== MAIL DEBUG ==========");

        System.out.println("APP_MAIL_IMPL = " + System.getenv("APP_MAIL_IMPL"));
        System.out.println("SMTP_USERNAME = " + System.getenv("SMTP_USERNAME"));

        String password = System.getenv("SMTP_PASSWORD");

        System.out.println("SMTP_PASSWORD present = " + (password != null));
        System.out.println("SMTP_PASSWORD length = "
                + (password == null ? 0 : password.length()));

        System.out.println("================================");
    }
}

@Component
class EnvCheck {

    @PostConstruct
    public void init() {

        System.out.println("MAIL_IMPL = " + System.getenv("MAIL_IMPL"));
        System.out.println("APP_MAIL_IMPL = " + System.getenv("APP_MAIL_IMPL"));
    }
}