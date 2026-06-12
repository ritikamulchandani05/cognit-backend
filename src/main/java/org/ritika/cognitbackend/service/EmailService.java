package org.ritika.cognitbackend.service;

import org.ritika.cognitbackend.entity.Comment;
import org.ritika.cognitbackend.entity.Post;
import org.ritika.cognitbackend.entity.User;

import java.util.List;

public interface EmailService {
    void sendWelcomeEmail(User user);
    void sendPasswordResetEmail(User user, String resetToken);
    void sendCommentNotification(User postAuthor, Comment comment);
    void sendWeeklyDigest(User user, List<Post> popularPosts);
    void sendOtpEmail(User user, String otp);
}
