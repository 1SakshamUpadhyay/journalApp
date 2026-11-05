package net.saku.journalApp.service;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Value("${resend.from.email}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String body) {
        try {
            Resend resend = new Resend(resendApiKey);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject(subject)
                    .text(body)
                    .build();

            resend.emails().send(params);

            log.info("Email sent successfully to {}", to);

        } catch (Exception e) {
            log.error("Exception while sending email", e);
        }
    }
}