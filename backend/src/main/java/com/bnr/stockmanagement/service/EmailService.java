package com.bnr.stockmanagement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOTP(String to, String otp) {
        logger.info("Attempting to send OTP to: {}", to);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("BNR System Verification");
            message.setText("Your verification code is: " + otp);
            
            mailSender.send(message);
            logger.info("OTP successfully sent to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send OTP to: {}. Error: {}", to, e.getMessage());
            // We don't rethrow here to prevent the whole registration/request flow from breaking,
            // but in a real app, you might want to handle this more strictly.
        }
    }
}
