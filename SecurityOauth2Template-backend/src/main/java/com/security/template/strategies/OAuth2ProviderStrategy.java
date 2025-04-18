package com.security.template.strategies;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface OAuth2ProviderStrategy {
    ResponseEntity<?> exchangeCodeForToken(String code, String codeVerifier);
}
