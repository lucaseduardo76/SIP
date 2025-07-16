package com.ifba.sipapi.mail.application.service;

import com.ifba.sipapi.config.security.TokenService;
import com.ifba.sipapi.user.domain.User;
import com.ifba.sipapi.user.dto.UserAccountVerificationPayloadDto;
import com.ifba.sipapi.user.infra.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final TokenService tokenService;
    private final UserRepository userRepository;

    @Value("${spring.application.baseUrl}")
    private String baseUrl;

    public void sendVerificationEmail(String to, String subject) {
        log.info("[start] EmailService - sendVerificationEmail");

        User user = userRepository.findByEmail(to)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String verificationCode = user.getAccountVerificationCode();
        String token = tokenService.generateToken(new UserAccountVerificationPayloadDto(to, verificationCode));

        Context context = new Context();
        context.setVariable("verificationLink", baseUrl + "/authentication/verify-account/token/" + token);
        context.setVariable("verificationCode", verificationCode);

        String body = templateEngine.process("email_verification", context);

        sendHtmlEmail(to, subject, body);

        log.debug("[finish] EmailService - sendVerificationEmail");
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        log.info("[start] EmailService - sendHtmlEmail");
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            ClassPathResource logo = new ClassPathResource("/static/images/sip-logo.jpg");
            helper.addInline("sip-logo", logo);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
        log.debug("[finish] EmailService - sendHtmlEmail");
    }

}
