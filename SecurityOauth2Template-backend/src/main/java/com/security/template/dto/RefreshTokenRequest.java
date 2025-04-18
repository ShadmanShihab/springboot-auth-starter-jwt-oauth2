package com.security.template.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenRequest {
    private String refreshToken;

    public RefreshTokenRequest(){}

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
