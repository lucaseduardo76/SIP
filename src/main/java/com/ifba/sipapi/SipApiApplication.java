package com.ifba.sipapi;

import com.ifba.sipapi.notification.application.UserNotificationDto;
import com.ifba.sipapi.notification.infra.NotificationProducer;
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
    public CommandLineRunner sendStartupMessage(NotificationProducer producer) {
        return args -> {
            new Thread(() -> {
                try {
                    Thread.sleep(10_000); // wait 10 seconds
                    producer.send("✅ SIP API started successfully!");
                    System.out.println("Startup message published to Kafka (consumer will broadcast to /topic/public)");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        };
    }
}
