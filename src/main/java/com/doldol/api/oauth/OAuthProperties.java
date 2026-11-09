package com.doldol.api.oauth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.oauth")
public record OAuthProperties(String frontendUrl, Provider kakao, Provider naver) {
    public record Provider(String clientId, String clientSecret, String authUrl, String apiUrl){
        public boolean enabled(){
            return clientId != null && !clientId.isBlank();
        }
    }
    public Provider of(SocialProvider provider){
        return provider == SocialProvider.KAKAO ? kakao : naver;
    }
}
