package com.ifba.sipapi.mail.application.service;

import com.ifba.sipapi.config.handler.APIException;
import com.ifba.sipapi.config.security.TokenService;
import com.ifba.sipapi.mail.domain.EmailData;
import com.ifba.sipapi.mail.domain.EmailDetailsDto;
import com.ifba.sipapi.user.domain.User;
import com.ifba.sipapi.user.infra.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@Log4j2
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final TokenService tokenService;
    private final UserRepository userRepository;

    @Value("${application.front-end.url}")
    private String applicationUrl;

    public void sendEmailWithCode(
            EmailDetailsDto emailDetailsDto
    ) {
        log.info("[start] EmailService - sendEmailWithCode | to={}", emailDetailsDto.getTo());

        User user = userRepository.findByEmail(emailDetailsDto.getTo())
                .orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        String token = tokenService.generateTokenToEmail(new EmailData(user.getEmail(), user.getAccountCode()));

        Context context = new Context();
        context.setVariable("applicationLink", applicationUrl + emailDetailsDto.getRoute() + token);
        context.setVariable("verificationCode", user.getAccountCode());

        String body = templateEngine.process(emailDetailsDto.getTemplate(), context);
        sendHtmlEmail(emailDetailsDto.getTo(), emailDetailsDto.getSubject(), body);

        log.debug("[finish] EmailService - sendEmailWithCode | to={}", emailDetailsDto.getTo());
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
