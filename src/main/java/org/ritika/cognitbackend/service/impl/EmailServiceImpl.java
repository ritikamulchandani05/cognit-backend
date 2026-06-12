package org.ritika.cognitbackend.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ritika.cognitbackend.dto.request.EmailRequest;
import org.ritika.cognitbackend.entity.Comment;
import org.ritika.cognitbackend.entity.Post;
import org.ritika.cognitbackend.entity.User;
import org.ritika.cognitbackend.enums.EmailType;
import org.ritika.cognitbackend.service.EmailService;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private static final String FROM_ADDRESS = "noreply@cognit.local";
    private static final String APP_BASE_URL = "http://localhost:8080";
    private final JavaMailSender mailSender;

    private String htmlHeader() {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Cognit</title>
                </head>
                <body style="margin:0;padding:0;background-color:#f1f5f9;font-family:'Segoe UI',Arial,sans-serif;">
                  <table role="presentation" cellpadding="0" cellspacing="0" width="100%"
                         style="background-color:#f1f5f9;padding:32px 0;">
                    <tr><td align="center">
                      <table role="presentation" cellpadding="0" cellspacing="0" width="600"
                             style="background-color:#ffffff;border-radius:12px;
                                    box-shadow:0 4px 24px rgba(0,0,0,0.08);overflow:hidden;">

                        <!-- HEADER -->
                        <tr>
                          <td style="background-color:#1e293b;padding:28px 40px;text-align:center;">
                            <span style="color:#ffffff;font-size:22px;font-weight:700;
                                         letter-spacing:0.5px;">Cognit</span>
                          </td>
                        </tr>

                        <!-- BODY OPEN -->
                        <tr><td style="padding:40px 40px 32px;">
                """;
    }
    private String ctaButton(String label, String url) {
        return """
                <table role="presentation" cellpadding="0" cellspacing="0"
                       style="margin:32px auto 0;">
                  <tr>
                    <td style="background-color:#6366f1;border-radius:8px;">
                      <a href="%s"
                         style="display:inline-block;padding:14px 32px;color:#ffffff;
                                font-size:15px;font-weight:600;text-decoration:none;
                                letter-spacing:0.3px;">%s</a>
                    </td>
                  </tr>
                </table>
                """.formatted(url, label);
    }

    private String htmlFooter() {
        return """
                        </td></tr>

                        <!-- FOOTER -->
                        <tr>
                          <td style="background-color:#f8fafc;padding:20px 40px;text-align:center;
                                     border-top:1px solid #e2e8f0;">
                            <p style="margin:0;font-size:12px;color:#94a3b8;">
                              You are receiving this email because you have an account on Cognit.<br>
                              &copy; 2026 Cognit. All rights reserved.
                            </p>
                          </td>
                        </tr>

                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """;
    }

    private String buildWelcomeHtml(User user) {
        String body = """
                <h2 style="margin:0 0 16px;font-size:24px;color:#1e293b;font-weight:700;">
                  Welcome to Cognit, %s! &#127881;
                </h2>
                <p style="margin:0 0 12px;font-size:15px;line-height:1.7;color:#475569;">
                  We're thrilled to have you here. Your account is all set up and ready to go.
                </p>
                <p style="margin:0 0 12px;font-size:15px;line-height:1.7;color:#475569;">
                  You can browse and read all published articles on the platform straight away.
                  If you've been granted the <strong style="color:#6366f1;">AUTHOR</strong> role,
                  you can also create and publish your own articles — share your ideas with
                  the world!
                </p>
                <p style="margin:0;font-size:15px;line-height:1.7;color:#475569;">
                  Happy reading, and welcome aboard.
                </p>
                %s
                """.formatted(escapeHtml(user.getName()), ctaButton("Start Exploring", APP_BASE_URL));

        return htmlHeader() + body + htmlFooter();
    }

    private String buildPasswordResetHtml(User user, String resetToken) {
        String resetUrl = APP_BASE_URL + "/reset-password?token=" + resetToken;
        String body = """
                <h2 style="margin:0 0 16px;font-size:24px;color:#1e293b;font-weight:700;">
                  Reset your password
                </h2>
                <p style="margin:0 0 12px;font-size:15px;line-height:1.7;color:#475569;">
                  Hi %s, we received a request to reset your Cognit password.
                  Click the button below to choose a new password.
                </p>
                <p style="margin:0 0 12px;font-size:15px;line-height:1.7;color:#475569;">
                  This link is valid for <strong>15 minutes</strong>. If you didn't request
                  a password reset, you can safely ignore this email — your password won't change.
                </p>
                %s
                <p style="margin:28px 0 0;font-size:13px;line-height:1.6;color:#94a3b8;">
                  Or copy this link into your browser:<br>
                  <a href="%s" style="color:#6366f1;word-break:break-all;">%s</a>
                </p>
                """.formatted(
                escapeHtml(user.getName()),
                ctaButton("Reset Password", resetUrl),
                resetUrl, resetUrl
        );

        return htmlHeader() + body + htmlFooter();
    }

    private String buildCommentNotificationHtml(User postAuthor, Comment comment) {
        Post post = comment.getPost();
        String postUrl = APP_BASE_URL + "/posts/" + post.getSlug();
        String commenterName = escapeHtml(comment.getAuthor().getName());
        String preview = escapeHtml(
                comment.getBody().length() > 200
                        ? comment.getBody().substring(0, 200) + "…"
                        : comment.getBody()
        );

        String body = """
                <h2 style="margin:0 0 16px;font-size:24px;color:#1e293b;font-weight:700;">
                  New comment on your post &#128172;
                </h2>
                <p style="margin:0 0 12px;font-size:15px;line-height:1.7;color:#475569;">
                  Hi %s, <strong style="color:#1e293b;">%s</strong> just commented on your post
                  <em style="color:#6366f1;">%s</em>.
                </p>
                <blockquote style="margin:16px 0;padding:16px 20px;background:#f8fafc;
                                   border-left:4px solid #6366f1;border-radius:0 8px 8px 0;">
                  <p style="margin:0;font-size:15px;line-height:1.7;color:#475569;">%s</p>
                </blockquote>
                %s
                """.formatted(
                escapeHtml(postAuthor.getName()),
                commenterName,
                escapeHtml(post.getTitle()),
                preview,
                ctaButton("View Comment", postUrl)
        );

        return htmlHeader() + body + htmlFooter();
    }

    private String buildWeeklyDigestHtml(User user, List<Post> popularPosts) {
        StringBuilder postsHtml = new StringBuilder();
        for (Post post : popularPosts) {
            String postUrl = APP_BASE_URL + "/posts/" + post.getSlug();
            String excerpt = post.getExcerpt() != null && !post.getExcerpt().isBlank()
                    ? escapeHtml(post.getExcerpt())
                    : escapeHtml(post.getContent().length() > 160
                    ? post.getContent().substring(0, 160) + "…"
                    : post.getContent());

            postsHtml.append("""
                    <tr>
                      <td style="padding:16px 0;border-bottom:1px solid #e2e8f0;">
                        <a href="%s"
                           style="font-size:16px;font-weight:600;color:#1e293b;text-decoration:none;
                                  display:block;margin-bottom:6px;">%s</a>
                        <p style="margin:0 0 8px;font-size:14px;line-height:1.6;color:#64748b;">%s</p>
                        <span style="font-size:12px;color:#94a3b8;">
                          &#128065; %d views &nbsp;&bull;&nbsp; &#10084;&#65039; %d likes
                        </span>
                      </td>
                    </tr>
                    """.formatted(
                    postUrl,
                    escapeHtml(post.getTitle()),
                    excerpt,
                    post.getViewCount(),
                    post.getLikeCount()
            ));
        }

        String body = """
                <h2 style="margin:0 0 8px;font-size:24px;color:#1e293b;font-weight:700;">
                  Your weekly digest &#128240;
                </h2>
                <p style="margin:0 0 24px;font-size:15px;line-height:1.7;color:#475569;">
                  Hi %s, here are the most popular articles on Cognit this week. Enjoy!
                </p>
                <table role="presentation" cellpadding="0" cellspacing="0" width="100%%">
                  %s
                </table>
                %s
                """.formatted(
                escapeHtml(user.getName()),
                postsHtml,
                ctaButton("Read More on Cognit", APP_BASE_URL)
        );

        return htmlHeader() + body + htmlFooter();
    }

   private void doSend(String to, String subject, String htmlBody, EmailType type) {
        log.info("[EMAIL] Sending type={} to={} subject=\"{}\" thread={}",
                type, to, subject, Thread.currentThread().getName());
        try{
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(FROM_ADDRESS);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("[EMAIL] Email sent successfully: type={} to={}", type, to);
        }catch(MessagingException e){
            log.error("[EMAIL] SMTP Failure: type={} to={} error={}", type, to, e.getMessage(),e);
        }catch(Exception e){
            log.error("[EMAIL] Unexpected Failure: type={} to={} error={}",  type, to, e.getMessage(),e);
        }
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input
                .replace("&",  "&amp;")
                .replace("<",  "&lt;")
                .replace(">",  "&gt;")
                .replace("\"", "&quot;")
                .replace("'",  "&#x27;");
    }

    @Override
    @Async("taskExecutor")
    public void sendWelcomeEmail(User user) {
        try {
            log.info("[EMAIL] Preparing WELCOME email for user id={} name={}", user.getId(), user.getName());
            doSend(user.getEmail(), "Welcome to Cognit!", buildWelcomeHtml(user), EmailType.WELCOME);
        } catch (Exception e) {
            log.error("[EMAIL] Failed to send WELCOME email to {}: {}", user.getEmail(), e.getMessage(), e);
        }
    }

    @Override
    @Async("taskExecutor")
    public void sendPasswordResetEmail(User user, String resetToken) {
        try {
            log.info("[EMAIL] Preparing PASSWORD_RESET email for user id={}", user.getId());
            doSend(user.getEmail(), "Reset your Cognit password",
                    buildPasswordResetHtml(user, resetToken), EmailType.PASSWORD_RESET);
        } catch (Exception e) {
            log.error("[EMAIL] Failed to send PASSWORD_RESET email to {}: {}", user.getEmail(), e.getMessage(), e);
        }
    }

    @Override
    @Async("taskExecutor")
    public void sendCommentNotification(User postAuthor, Comment comment) {
        try {
            log.info("[EMAIL] Preparing COMMENT_NOTIFICATION email for user id={}", postAuthor.getId());
            doSend(postAuthor.getEmail(), "New comment on your post",
                    buildCommentNotificationHtml(postAuthor, comment), EmailType.COMMENT_NOTIFICATION);
        } catch (Exception e) {
            log.error("[EMAIL] Failed to send COMMENT_NOTIFICATION email to {}: {}", postAuthor.getEmail(), e.getMessage(), e);
        }
    }

    @Override
    @Async("taskExecutor")
    public void sendWeeklyDigest(User user, List<Post> popularPosts) {
        try {
            log.info("[EMAIL] Preparing WEEKLY_DIGEST email for user id={}", user.getId());
            if (popularPosts == null || popularPosts.isEmpty()) {
                log.warn("[EMAIL] Skipping WEEKLY_DIGEST for user {} — no posts provided", user.getId());
                return;
            }
            doSend(user.getEmail(), "Your Cognit weekly digest",
                    buildWeeklyDigestHtml(user, popularPosts), EmailType.WEEKLY_DIGEST);
        } catch (Exception e) {
            log.error("[EMAIL] Failed to send WEEKLY_DIGEST email to {}: {}", user.getEmail(), e.getMessage(), e);
        }
    }

    @Override
    @Async("taskExecutor")
    public void sendOtpEmail(User user, String otp) {
        try {
            log.info("[EMAIL] Preparing OTP email for user id={}", user.getId());
            doSend(user.getEmail(), "Your Cognit verification code", buildOtpHtml(user, otp), EmailType.OTP);
        } catch (Exception e) {
            log.error("[EMAIL] Failed to send OTP email to {}: {}", user.getEmail(), e.getMessage(), e);
        }
    }

    private String buildOtpHtml(User user, String otp) {
        String body = """
            <h2 style="margin:0 0 16px;font-size:24px;color:#1e293b;font-weight:700;">
              Your verification code
            </h2>
            <p style="margin:0 0 12px;font-size:15px;line-height:1.7;color:#475569;">
              Hi %s, use the code below to complete your sign-in.
              It expires in <strong>10 minutes</strong>.
            </p>
            <div style="margin:24px auto;padding:20px 40px;background:#f1f5f9;
                        border-radius:12px;text-align:center;width:fit-content;">
              <span style="font-size:36px;font-weight:700;letter-spacing:8px;color:#1e293b;">%s</span>
            </div>
            <p style="margin:16px 0 0;font-size:13px;line-height:1.6;color:#94a3b8;">
              If you didn't request this code, you can safely ignore this email.
            </p>
            """.formatted(escapeHtml(user.getName()), otp);
        return htmlHeader() + body + htmlFooter();
    }
}