package com.security.template.auth.config;

import com.security.template.model.PasswordResetToken;
import com.security.template.model.User;
import com.security.template.model.UserRole;
import com.security.template.repository.PasswordResetTokenRepo;
import com.security.template.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.SecretKey;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtUtils {
    @Value("${frontend.redirect.uri}")
    private String frontendRedirectUri;

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.refresh.secret}")
    private String refreshSecret;
    private final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;
    private final long JWT_REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60;

    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordResetTokenRepo passwordResetTokenRepository;

    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        List<String> roles = getUserRoles(userDetails);
        claims.put("roles", roles);
        return createToken(claims, userDetails.getUsername(), JWT_TOKEN_VALIDITY, secret);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        List<String> roles = getUserRoles(userDetails);
        claims.put("roles", roles);
        return createToken(claims, userDetails.getUsername(), JWT_REFRESH_TOKEN_VALIDITY, refreshSecret);
    }

    public String generateAccessToken(OAuth2User oAuth2User) {
        Map<String, Object> claims = new HashMap<>();

        String email = oAuth2User.getAttribute("email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<GrantedAuthority> authorities = user.getUserRoles()
                .stream()
                .map(UserRole::getRole)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());

        claims.put("roles", authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));

        return createToken(claims, oAuth2User.getName(), JWT_TOKEN_VALIDITY, secret);
    }

    public String generateRefreshToken(OAuth2User oAuth2User) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, oAuth2User.getName(), JWT_REFRESH_TOKEN_VALIDITY, refreshSecret);
    }

    private String createToken(Map<String, Object> claims, String subject, long validity, String secretKey) {
        Date tokenValidity = new Date(System.currentTimeMillis() + validity * 1000);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setExpiration(tokenValidity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public List<String> getUserRoles(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    public String extractUsername(String token, String secretKey) {
        return extractClaim(token, Claims::getSubject, secretKey);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver, String secretKey) {
        final Claims claims = extractAllClaims(token, secretKey);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token, String secretKey) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }

    public boolean isTokenExpired(String token, String secretKey) {
        return extractExpiration(token, secretKey).before(new Date());
    }

    public Date extractExpiration(String token, String secretKey) {
        return extractClaim(token, Claims::getExpiration, secretKey);
    }

    public Date extractLastIssuedAt(String token, String secretKey) {
        return extractClaim(token, Claims::getIssuedAt, secretKey);
    }

    public boolean validateToken(String token, UserDetails userDetails, String secretKey) {
        final String username = extractUsername(token, secretKey);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token, secretKey));
    }

    public String getSecret() { // Added getSecret() method
        return secret;
    }

    public String getRefreshSecret() {
        return refreshSecret;
    }

    public String generatePasswordResetToken(User user) {
        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(1)) // Token valid for 1 hour
                .build();
        passwordResetTokenRepository.save(passwordResetToken);

        String resetLink = UriComponentsBuilder.fromUriString(frontendRedirectUri)
                .queryParam("token", token)
                .build()
                .toUriString();

        return resetLink;
    }
}
