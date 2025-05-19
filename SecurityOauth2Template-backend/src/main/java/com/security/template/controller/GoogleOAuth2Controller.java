package com.security.template.controller;

import com.security.template.records.TokenRequest;
import com.security.template.strategies.GoogleOAuth2Strategy;
import com.security.template.strategies.OAuth2ProviderStrategy;
import com.security.template.utils.PKCEUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/oauth2/google")
public class GoogleOAuth2Controller {
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("#{'${spring.security.oauth2.client.registration.google.scope}'.split(',')}")
    private List<String> googleScopes;

    @Value("${frontend.redirect.uri}")
    private String frontendRedirectUri;

    @Autowired
    private OAuth2ProviderStrategy oAuth2ProviderStrategy;

    //This endpoint will be called when the user hits login with google
    //This endpoint will be used when we want to request to the authorizationUrl from backend application
    @GetMapping("/login")
    public ResponseEntity<Map<String, String>> googleLogin() {
        String scopeString = googleScopes.stream().collect(Collectors.joining(" "));

        String authorizationUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
                "response_type=code" +
                "&client_id=" + googleClientId +
                "&redirect_uri=" + "http://localhost:8082/api/oauth2/google/login/oauth2/code/google" +
                // "&redirect_uri=" + "http://localhost:5173/oauth2/callback" +
                "&scope=" + scopeString +
                "&state=state_parameter_xyz" +
                "&code_challenge=CODE_CHALLENGE_FROM_FRONTEND" + // Frontend will replace this
                "&code_challenge_method=S256";

        Map<String, String> response = new HashMap<>();
        response.put("authorizationUrl", authorizationUrl);
        return ResponseEntity.ok(response);
    }


    /*
    redirect_uri
    After Google sign in, request will be redirected to this URI. 
    */ 
    @GetMapping("/login/oauth2/code/google")
    public RedirectView googleCallback(String code, String state) {
        String redirectUri = frontendRedirectUri + "?code=" + code;
        return new RedirectView(redirectUri);
    }

    @PostMapping("/token")
    public ResponseEntity<?> googleToken(@RequestBody TokenRequest tokenRequest) {
        return oAuth2ProviderStrategy.exchangeCodeForToken(tokenRequest.code(), tokenRequest.codeVerifier());
    }


    //This endpoint will be called when the user hits login with google
    //This endpoint will be used when we want to request to the authorizationUrl from backend application
    /*
    @GetMapping("/login")
    public void googleLogin(HttpServletResponse response, HttpSession session) throws NoSuchAlgorithmException, IOException {
        String codeVerifier = PKCEUtils.generateCodeVerifier();
        String codeChallenge = PKCEUtils.generateCodeChallenge(codeVerifier);

        session.setAttribute("codeVerifier", codeVerifier);

        String scopeString = googleScopes.stream()
                .collect(Collectors.joining(" "));

        String authorizationUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
                "response_type=code" +
                "&client_id=" + googleClientId +
                "&redirect_uri=" + "http://localhost:8082/oauth2/google/login/oauth2/code/google" +
                "&scope=" + scopeString +
                "&state=state_parameter_xyz" +
                "&code_challenge=" + codeChallenge +
                "&code_challenge_method=S256";

        response.sendRedirect(authorizationUrl);
    }


    //redirect uri http://localhost:8082/oauth2/google/login/oauth2/code/google
    @GetMapping("/login/oauth2/code/google")
    public ResponseEntity<?> googleCallback(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String codeVerifier = (String) session.getAttribute("codeVerifier");
        session.removeAttribute("codeVerifier");

        if (codeVerifier == null) {
            return ResponseEntity.badRequest().body("Code verifier not found in session.");
        }

        request.setAttribute("codeVerifier", codeVerifier);

        return googleOAuth2Strategy.handleOAuth2Login(request, response);
    }

     */
}
