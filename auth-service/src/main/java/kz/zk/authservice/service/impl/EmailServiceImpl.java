package kz.zk.authservice.service.impl;

import jakarta.mail.internet.MimeMessage;
import kz.zk.authservice.common.exception.BusinessValidationException;
import kz.zk.authservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.host}")
    private String host;

    @Override
    @Async
    public void sendVerificationEmail(String to, String token, String locale) {
        try {
            String template = loadTemplate(locale);
            String verificationUrl = String.format("%s/%s/auth/verify-email?token=%s", host, locale, token);
            String htmlContent = template.replace("{{verificationUrl}}", verificationUrl);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(getSubject(locale));
            helper.setText(htmlContent, true);

            mailSender.send(message);
        }  catch (Exception e) {
            log.error("Failed to send verification email to: {}", to, e);
        }
    }

    private String loadTemplate(String locale) {
        try {
            String templatePath = String.format("templates/email/verification/%s.html", locale);
            ClassPathResource resource = new ClassPathResource(templatePath);
            try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                return FileCopyUtils.copyToString(reader);
            }
        } catch (Exception e) {
            log.error("Failed to load email template for locale: {}", locale, e);
            throw new BusinessValidationException("email.template.load.error");
        }
    }

    private String getSubject(String locale) {
        return switch (locale) {
            case "ru" -> "Подтверждение Email";
            case "kk" -> "Email Растау";
            default -> "Email Verification";
        };
    }
}
