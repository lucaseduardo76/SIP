package com.ifba.sipapi.mail.application.service;

import com.ifba.sipapi.config.handler.APIException;
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

    @Value("${spring.application.applicationUrl}")
    private String applicationUrl;

    public void sendVerificationEmail(String to) {
        log.info("[start] EmailService - sendVerificationEmail");

        User user = userRepository.findByEmail(to)
                .orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        String verificationCode = user.getAccountVerificationCode();
        String token = tokenService.generateToken(new UserAccountVerificationPayloadDto(to, verificationCode));

        Context context = new Context();
        context.setVariable("verificationLink", applicationUrl + "/authentication/verify-account/token/" + token);
        context.setVariable("verificationCode", verificationCode);

        String body = templateEngine.process("email_verification", context);

        sendHtmlEmail(to, "Verificação de Email - SIP", body);

        log.debug("[finish] EmailService - sendVerificationEmail");
    }

    public void sendReactivationEmail(String sendTo) {
        log.info("[start] EmailService - sendReactivationEmail");

        User user = userRepository.findByEmail(sendTo)
                .orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        String code = user.getAccountVerificationCode();
        String token = tokenService.generateToken(new UserAccountVerificationPayloadDto(sendTo, code));

        Context context = new Context();
        context.setVariable("verificationLink", applicationUrl + "/authentication/reactivate/token/" + token);
        context.setVariable("verificationCode", code);

        String body = templateEngine.process("email_reactivation", context);

        sendHtmlEmail(sendTo, "Reativação de conta - SIP", body);
        log.debug("[finish] EmailService - sendReactivationEmail");
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
