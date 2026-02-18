package org.example.greenexproject.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${MAIL_FROM}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Your OTP Code for GreenEx");
            helper.setText(
                    "<p>Dear User,</p>" +
                            "<p>Your verification OTP is: <b>" + otp + "</b></p>" +
                            "<p>This OTP expires in 1 minute.</p>",
                    true
            );

            mailSender.send(message);
            System.out.println("OTP email sent successfully to " + toEmail);

        } catch (MessagingException e) {

            System.err.println("Warning: Failed to send OTP email to " + toEmail + ": " + e.getMessage());
        }
    }
}
