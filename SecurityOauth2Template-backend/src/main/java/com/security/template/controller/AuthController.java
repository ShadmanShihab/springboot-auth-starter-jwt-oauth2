package com.security.template.controller;

import com.security.template.auth.config.JwtUtils;
import com.security.template.dto.AuthenticationRequest;
import com.security.template.dto.AuthenticationResponse;
import com.security.template.dto.RefreshTokenRequest;
import com.security.template.dto.RegistrationRequest;
import com.security.template.model.Role;
import com.security.template.model.User;
import com.security.template.model.UserRole;
import com.security.template.repository.RoleRepository;
import com.security.template.repository.UserRepository;
import com.security.template.repository.UserRoleRepository;
import com.security.template.service.EmailService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtils jwtUtil;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) {
        try {
            Authentication authentication = UsernamePasswordAuthenticationToken.unauthenticated(authenticationRequest.getEmail(), authenticationRequest.getPassword());
            Authentication authResponse = authenticationManager.authenticate(authentication);
            if (authResponse != null && authResponse.isAuthenticated()) {
                UserDetails userDetails = (UserDetails) authResponse.getPrincipal();
                String accessToken = jwtUtil.generateAccessToken(userDetails);
                String refreshToken = jwtUtil.generateRefreshToken(userDetails);
                return ResponseEntity.ok(new AuthenticationResponse(accessToken, refreshToken));
            }
            else {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid credentials"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid credentials"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegistrationRequest registrationRequest) {
        Optional<User> existingUser = userRepository.findByEmail(registrationRequest.getEmail());
        if (existingUser.isPresent()) {
            log.info("Another user already exists");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "User already exists"));
        }

        User user = new User();
        user.setEmail(registrationRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        user.setName(registrationRequest.getName());
        userRepository.save(user);

        // Add Role Assignment
        Role role = roleRepository.findByName("USER"); // Fetch the "USER" role
        if (role == null) { 
            // Handle the case where the "USER" role doesn't exist (create it if needed)
            role = new Role();
            role.setName("USER");
            role = roleRepository.save(role);
        }

        UserRole userRoleMapping = new UserRole();
        userRoleMapping.setUser(user);
        userRoleMapping.setRole(role);
        userRoleRepository.save(userRoleMapping);

        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();
        String username = jwtUtil.extractUsername(refreshToken, jwtUtil.getRefreshSecret());

        UserDetails userDetails = userRepository.findByEmail(username).map(user -> org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles("USER")
                .build()).orElse(null);

        if (userDetails != null && jwtUtil.validateToken(refreshToken, userDetails, jwtUtil.getRefreshSecret())) {
            String token = jwtUtil.generateAccessToken(userDetails);
            return ResponseEntity.ok(new AuthenticationResponse(token, refreshToken));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid refresh token"));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@PathVariable String email) {
        // Validate the email format
        if (email == null || email.trim().isEmpty() || !emailService.isValidEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid email address."));
        }

        // Check if the user exists in the database
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String resetLink = jwtUtil.generatePasswordResetToken(user);

            // Send the email with the reset link
            String subject = "Password Reset Request";
            String text = "To reset your password, click the link below:\n" + resetLink;
            emailService.sendSimpleMessage(email, subject, text);

            return ResponseEntity.ok(Map.of("message", "Password reset link sent to your email."));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
        }
    }

    // @PostMapping("/reset-password")
    // public ResponseEntity<?> resetPassword(@PathVariable String token, @PathVariable String newPassword) {
    //     // Validate the token and extract the email (you can implement this method)
    //     String email = jwtUtil.extractEmailFromToken(token);

    //     if (email == null) {
    //         return ResponseEntity.badRequest().body(Map.of("error", "Invalid token."));
    //     }

    //     // Find the user by email
    //     Optional<User> userOptional = userRepository.findByEmail(email);
    //     if (userOptional.isPresent()) {
    //         User user = userOptional.get();
    //         user.setPassword(passwordEncoder.encode(newPassword));
    //         userRepository.save(user);

    //         return ResponseEntity.ok(Map.of("message", "Password reset successfully."));
    //     } else {
    //         return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
    //     }
    // // 1. Validate the email.
    // if (email == null || email.trim().isEmpty() || !emailService.isValidEmail(email)) {
    //     return ResponseEntity.badRequest().body(Map.of("error", "Invalid email format."));
    // }

    // }
        
}
