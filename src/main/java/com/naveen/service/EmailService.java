package com.naveen.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;

    public void sendWelcomeEmail(String to, String name) {
        sendHtmlEmail(to, "Welcome to Our Platform", "welcome-email", name, null);
    }

    public void sendOtpEmail(String to, String name, String otp) {
        sendHtmlEmail(to, "Email Verification OTP", "verify-email-otp", name, otp);
    }

    public void sendResetOtpEmail(String to, String name, String otp) {
        sendHtmlEmail(to, "Password Reset OTP", "reset-password-otp", name, otp);
    }

    public void sendPasswordChangedNotification(String to, String name) {
        sendHtmlEmail(to, "Password Changed Successfully", "password-change-notification", name, null);
    }

    private void sendHtmlEmail(String to, String subject, String templateName, String name, String otp) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            context.setVariable("name", name);
            if (otp != null) {
                context.setVariable("otp", otp);
            }

            String htmlContent = templateEngine.process(templateName + ".html", context);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("Email sent to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}", to, e);
        }
    }

//    public void sendWelcomeEmail(String toEmail, String name){
//        SimpleMailMessage mailMessage = new SimpleMailMessage();
//        mailMessage.setFrom(fromEmail);
//        mailMessage.setTo(toEmail);
//        mailMessage.setSubject("Welcome to Secured System By Naveen!");
//
//        mailMessage.setText("Hello " + name + ",\n\n"
//                + "Welcome to our Secured Platform! We're excited to have you with us.\n\n"
//                + "Thank you for registering. If you need any help, feel free to reach out!\n\n"
//                + "Best regards,\n"
//                + "Secured System By Naveen Team");
//
//        javaMailSender.send(mailMessage);
//    }
//
//    public void sendResetOtpEmail(String toEmail, String name, String otp){
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(toEmail);
//        message.setFrom(fromEmail);
//        message.setSubject("Password Reset OTP");
//        message.setText("Hi " + name + "!\n\n"
//                + "We received a request to reset your password. Your one-time password (OTP) is:\n\n"
//                + otp + "\n\n"
//                + "Use this OTP to proceed with resetting your password. Please note that it will expire in 10 minutes for security reasons.\n\n"
//                + "If you did not request this, you can safely ignore this email.\n\n"
//                + "Thank you,\n"
//                + "Secured System By Naveen Support Team");
//        javaMailSender.send(message);
//    }
//
//    public void sendOtpEmail(String toEmail, String name, String otp){
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(toEmail);
//        message.setFrom(fromEmail);
//        message.setSubject("Account Verification OTP");
//        message.setText("Hi "+name+"!\n\nYour OTP is : "+otp+". Verify your account using this OTP.\n\nThank you");
//        javaMailSender.send(message);
//    }
//
//    public void sendPasswordChangedNotification(String toEmail) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        String subject = "Your Password Was Successfully Changed";
//        String text = "Hello,\n\nWe wanted to let you know that your account password was successfully changed. "
//                + "If you did not make this change, please contact our support team immediately.\n\n"
//                + "Regards,\nTeam Secure";
//        message.setTo(toEmail);
//        message.setFrom(fromEmail);
//        message.setSubject(subject);
//        message.setText(text);
//        javaMailSender.send(message);
//    }


}
