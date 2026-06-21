package org.ritika.cognitbackend.service.impl;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.ritika.cognitbackend.dto.request.*;
import org.ritika.cognitbackend.dto.response.AuthResponse;
import org.ritika.cognitbackend.dto.response.UserResponse;
import org.ritika.cognitbackend.entity.User;
import org.ritika.cognitbackend.enums.Role;
import org.ritika.cognitbackend.exception.BadRequestException;
import org.ritika.cognitbackend.exception.ResourceNotFoundException;
import org.ritika.cognitbackend.exception.UnauthorizedException;
import org.ritika.cognitbackend.security.JwtUtil;
import org.ritika.cognitbackend.service.AuthService;
import org.ritika.cognitbackend.service.EmailService;
import org.ritika.cognitbackend.service.OtpService;
import org.ritika.cognitbackend.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of AuthService interface.
 * Handles user registration, login, and token refresh operations.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final OtpService otpService;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userService.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }

        // Create new user with hashed password
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .role(resolveSignupRole(request.getRole()))
                .emailVerified(false)
                .isDeleted(false)
                .build();

        // Save user
        user = userService.createUser(user);

        // Fire-and-Forget: send welcome email on background thread
        emailService.sendWelcomeEmail(user);

        // Generate tokens and return response
        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userService.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        // 2FA gate
        if (Boolean.TRUE.equals(user.getTwoFaEnabled())) {
            otpService.generateAndSend(user.getId());
            String tempToken = jwtUtil.generateTempToken(user);   // short TTL, type="temp"
            return AuthResponse.builder()
                    .requires2fa(true)
                    .tempToken(tempToken)
                    .build();
        }

        return buildAuthResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // Validate refresh token
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        // Verify it's a refresh token (not an access token)
        Claims claims = jwtUtil.extractAllClaims(refreshToken);
        String tokenType = claims.get("type", String.class);
        if (!"refresh".equals(tokenType)) {
            throw new UnauthorizedException("Invalid token type");
        }

        // Get user from token
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Generate new tokens and return response
        return buildAuthResponse(user);
    }

    /**
     * Resolve the role a new user signs up as. RegisterRequest.role is already
     * constrained to "SUBSCRIBER"/"AUTHOR" by bean validation, so ADMIN can
     * never reach here — this is a second guard against that regardless.
     */
    private Role resolveSignupRole(String requestedRole) {
        if (requestedRole == null || requestedRole.isBlank()) {
            return Role.SUBSCRIBER;
        }

        Role role = Role.valueOf(requestedRole);
        if (role == Role.ADMIN) {
            throw new BadRequestException("Cannot self-register as ADMIN");
        }

        return role;
    }

    /**
     * Build AuthResponse with tokens and user info.
     *
     * @param user the authenticated user
     * @return AuthResponse containing tokens and user info
     */
    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration / 1000) // Convert ms to seconds
                .user(UserResponse.fromEntity(user))
                .build();
    }

    @Override
    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        if (!jwtUtil.validateTempToken(request.getTempToken())) {
            throw new UnauthorizedException("Invalid or expired session");
        }
        Claims claims = jwtUtil.extractAllClaims(request.getTempToken());
        if (!"temp".equals(claims.get("type", String.class))) {
            throw new UnauthorizedException("Invalid token type");
        }

        Long userId = jwtUtil.getUserIdFromToken(request.getTempToken());
        boolean ok  = otpService.verify(userId, request.getOtp());
        if (!ok) {
            throw new UnauthorizedException("Invalid or expired OTP");
        }

        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Silently succeed even if email not found (enumeration prevention)
        userService.findByEmail(request.getEmail()).ifPresent(user -> {
            String resetToken = jwtUtil.generatePasswordResetToken(user);  // 15-min TTL, type="pwd_reset"
            emailService.sendPasswordResetEmail(user, resetToken);
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!jwtUtil.validateToken(request.getToken())) {
            throw new UnauthorizedException("Invalid or expired reset token");
        }
        Claims claims = jwtUtil.extractAllClaims(request.getToken());
        if (!"pwd_reset".equals(claims.get("type", String.class))) {
            throw new UnauthorizedException("Invalid token type");
        }
        Long userId = jwtUtil.getUserIdFromToken(request.getToken());
        User user   = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userService.createUser(user);   // save through existing path
    }

}

