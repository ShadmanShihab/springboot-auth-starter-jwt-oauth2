package com.security.template.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    JavaMailSender mailSender;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Principal principal) {
        sendEmail("shadmanshihab1@gmail.com", "Test Message", "Demo test message for spring jwt starter template");
        if (principal == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Unauthorized"));
        }
        return ResponseEntity.ok(Map.of("email", principal.getName()));
    }

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom("your_email@gmail.com");

        mailSender.send(message);
    }
}
