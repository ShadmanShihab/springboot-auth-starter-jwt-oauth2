package com.security.template.strategies;

import com.security.template.auth.config.JwtUtils;
import com.security.template.auth.oauth2.CustomOAuth2UserService;
import com.security.template.model.User;
import com.security.template.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class GoogleOAuth2Strategy implements OAuth2ProviderStrategy {
    @Autowired
    private JwtUtils jwtUtil;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    private RestTemplate restTemplate;

//    @Deprecated
//    @Override
//    public ResponseEntity<?> handleOAuth2Login(HttpServletRequest request, HttpServletResponse response) {
//        try {
//            String code = request.getParameter("code");
//            ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId("google");
//            String codeVerifier = (String) request.getAttribute("codeVerifier");
//
//            OAuth2AccessToken accessToken = getAccessToken(code, codeVerifier, clientRegistration);
//
//            OAuth2UserRequest userRequest = new OAuth2UserRequest(clientRegistration, accessToken);
//
//            OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
//            OAuth2User oAuth2User = delegate.loadUser(userRequest);
//
//            Authentication authentication = new OAuth2AuthenticationToken(oAuth2User, oAuth2User.getAuthorities(), "google");
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//
//            String token = jwtUtil.generateAccessToken(oAuth2User);
//            String refreshToken = jwtUtil.generateRefreshToken(oAuth2User);
//
//            response.sendRedirect("http://localhost:3000/login/success?token=" + token + "&refreshToken=" + refreshToken);
//           // response.sendRedirect("http://localhost:8082/api/test/hello");
//            return ResponseEntity.ok().build();
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of("error", "Google login failed: " + e.getMessage()));
//        }
//    }

    private OAuth2AccessToken getAccessToken(String code, String codeVerifier, ClientRegistration clientRegistration) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("code", code);
        map.add("client_id", clientRegistration.getClientId());
        map.add("client_secret", clientRegistration.getClientSecret());
        map.add("redirect_uri", "http://localhost:8082/api/oauth2/google/login/oauth2/code/google");
        map.add("grant_type", "authorization_code");
        map.add("code_verifier", codeVerifier);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(clientRegistration.getProviderDetails().getTokenUri(), request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseMap = response.getBody();
            String accessTokenValue = (String) responseMap.get("access_token");
            return new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, accessTokenValue,
                    Instant.now(), Instant.now().plusSeconds(3600));
        } else {
            throw new OAuth2AuthenticationException("Failed to obtain access token: " + response.getStatusCode());
        }
    }

    @Override
    public ResponseEntity<?> exchangeCodeForToken(String code, String codeVerifier) {
        try {
            ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId("google");
            OAuth2AccessToken accessToken = getAccessToken(code, codeVerifier, clientRegistration);
            OAuth2UserRequest userRequest = new OAuth2UserRequest(clientRegistration, accessToken);
            OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
            OAuth2User oAuth2User = delegate.loadUser(userRequest);
            Authentication authentication = new OAuth2AuthenticationToken(oAuth2User, oAuth2User.getAuthorities(), "google");
            SecurityContextHolder.getContext().setAuthentication(authentication);

            oAuth2User = customOAuth2UserService.loadUser(userRequest);

            String token = jwtUtil.generateAccessToken(oAuth2User);
            String refreshToken = jwtUtil.generateRefreshToken(oAuth2User);
            Map<String, String> response = new HashMap<>();
            response.put("accessToken", token);
            response.put("refreshToken", refreshToken);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Google login failed: " + e.getMessage()));
        }
    }

}
