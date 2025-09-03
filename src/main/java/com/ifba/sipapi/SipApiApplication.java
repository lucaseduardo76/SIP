package com.ifba.sipapi;

import com.ifba.sipapi.notification.application.UserNotificationDto;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@SpringBootApplication
public class SipApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SipApiApplication.class, args);
    }

    @Bean
    public CommandLineRunner sendStartupMessage(SimpMessagingTemplate messagingTemplate) {
        return args -> {
            new Thread(() -> {
                try {
                    Thread.sleep(10_000); // wait 10 seconds
                    var msg = new UserNotificationDto("✅ SIP API started successfully!");
                    messagingTemplate.convertAndSend("/topic/public", msg);
                    System.out.println("Startup message sent to /topic/public after 10s");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        };
    }
}
