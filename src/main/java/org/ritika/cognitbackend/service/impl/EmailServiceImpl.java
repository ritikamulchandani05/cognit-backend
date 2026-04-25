package org.ritika.cognitbackend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ritika.cognitbackend.dto.request.EmailRequest;
import org.ritika.cognitbackend.entity.Comment;
import org.ritika.cognitbackend.entity.Post;
import org.ritika.cognitbackend.entity.User;
import org.ritika.cognitbackend.enums.EmailType;
import org.ritika.cognitbackend.service.EmailService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private static final String FROM_ADDRESS = "noreply@cognit.local";
    private final JavaMailSender mailSender;

    private void doSend(EmailRequest emailRequest) {
        log.info("[EMAIL] Sending type={} to ={} subject=\"{}\" thread={}",
                emailRequest.getType(),
                emailRequest.getTo(),
                emailRequest.getSubject(),
                Thread.currentThread().getName());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(FROM_ADDRESS);
        message.setTo(emailRequest.getTo());
        message.setSubject(emailRequest.getSubject());
        message.setText(emailRequest.getBody());

        mailSender.send(message);

        log.info("[EMAIL] Sent Successfully type={} to={}", emailRequest.getType(), emailRequest.getTo());
    }
    @Override
    @Async("taskExecutor")
    public void sendWelcomeEmail(User user) {
        try{
            String body = "Hi " + user.getName() + ",\n\n"
                    + "Welcome to Cognit! Your Account has been created successfully.\n\n"
                    + "You can now log in, explored published posts, and - if you have the AUTHOR role - "
                    + "start writing your own articles.\n\n"
                    + "Happy Reading!\n"
                    + "The Cognit Team";

            EmailRequest request = EmailRequest.builder()
                    .to(user.getEmail())
                    .subject("Welcome to Cognit!")
                    .body(body)
                    .type(EmailType.WELCOME)
                    .build();

            log.info("[EMAIL] Preparing WELCOME email for user id={} name={}", user.getId(), user.getName());
            doSend(request);

        } catch(Exception e){
            log.error("[EMAIL] Failed to send WELCOME email to {}: {}", user.getEmail(), e.getMessage(),e);
        }
    }

    @Override
    public void sendPasswordResetEmail(User user, String resetToken) {

    }

    @Override
    public void sendCommentNotification(User postAuthor, Comment comment) {

    }

    @Override
    public void sendWeeklyDigest(User user, List<Post> popularPosts) {

    }
}
