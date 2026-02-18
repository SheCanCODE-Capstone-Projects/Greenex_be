package org.example.greenexproject.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import sendinblue.ApiClient;
import sendinblue.Configuration;
import sibApi.TransactionalEmailsApi;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailSender;
import sibModel.SendSmtpEmailTo;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${BREVO_API_KEY}")
    private String apiKey;

    @Value("${MAIL_FROM}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            ApiClient defaultClient = Configuration.getDefaultApiClient();
            defaultClient.setApiKey(apiKey);

            TransactionalEmailsApi api = new TransactionalEmailsApi();

            SendSmtpEmailSender sender = new SendSmtpEmailSender();
            sender.setEmail(fromEmail);
            sender.setName("Greenex");

            SendSmtpEmailTo to = new SendSmtpEmailTo();
            to.setEmail(toEmail);

            SendSmtpEmail email = new SendSmtpEmail();
            email.setSender(sender);
            email.addToItem(to);
            email.setSubject("Your OTP Code for GreenEx");
            email.setHtmlContent(
                    "<p>Dear User,</p>" +
                            "<p>Your verification OTP is: <b>" + otp + "</b></p>" +
                            "<p>This OTP expires in 5 minutes.</p>"
            );

            api.sendTransacEmail(email);
            log.info("OTP email sent successfully to {}", toEmail);

        } catch (Exception e) {
            log.error("Warning: OTP/email sending failed: {}", e.getMessage());
        }
    }
}
