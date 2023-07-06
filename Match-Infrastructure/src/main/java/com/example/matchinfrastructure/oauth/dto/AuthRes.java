package com.example.matchinfrastructure.oauth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

public class AuthRes {
    @Getter
    @NoArgsConstructor
    public static class KakaoTokenResponse{
        private String accessToken;
        private String refreshToken;
        private String idToken;
    }
}
