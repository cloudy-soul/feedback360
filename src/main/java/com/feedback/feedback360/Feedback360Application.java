package com.feedback.feedback360;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Feedback360Application {
    public static void main(String[] args) {
        SpringApplication.run(Feedback360Application.class, args);
    }
}