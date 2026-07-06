package com.feedback.feedback360;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailPropertyDebug {

    private final Environment env;

    @PostConstruct
    public void debug() {

        System.out.println("===== SPRING MAIL PROPERTIES =====");

        System.out.println("spring.mail.host="
                + env.getProperty("spring.mail.host"));

        System.out.println("spring.mail.port="
                + env.getProperty("spring.mail.port"));

        System.out.println("spring.mail.username="
                + env.getProperty("spring.mail.username"));

        String pwd = env.getProperty("spring.mail.password");

        System.out.println("spring.mail.password.present="
                + (pwd != null));

        System.out.println("spring.mail.password.length="
                + (pwd == null ? 0 : pwd.length()));

        System.out.println("==================================");
    }
}
