package com.security.template.service;

public interface EmailService {
    public void sendSimpleMessage(String to, String subject, String text);    

    public boolean isValidEmail(String email);
}
