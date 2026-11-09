package com.doldol.api.oauth;

import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import java.time.Duration;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 카카오 · 네이버 OAuth — 인가 코드 → 액세스 토큰 → 회원 정보.
 * 카카오: kauth.kakao.com/oauth/token · kapi.kakao.com/v2/user/me
 * 네이버: nid.naver.com/oauth2.0/token · openapi.naver.com/v1/nid/me
 */
@Slf4j
@Component
public class OAuthClient {

    private static final ParameterizedTypeReference<Map<String, Object>> JSON = new ParameterizedTypeReference<>() {};

    private final OAuthProperties properties;
    private final RestClient restClient;

    public OAuthClient(OAuthProperties properties) {
        this.properties = properties;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    /** 소셜에서 받은 회원 정보. emailVerified 가 true 일 때만 같은 이메일 회원에 연결한다 */
    public record Profile(SocialProvider provider, String id, String email, boolean emailVerified, String name, String phone) {}

    /** 로그인 버튼이 보낼 주소 — FE 가 state 만 붙인다 */
    public String authorizeUrl(SocialProvider provider) {
        OAuthProperties.Provider p = properties.of(provider);
        return UriComponentsBuilder.fromUriString(p.authUrl() + (provider == SocialProvider.KAKAO ? "/oauth/authorize" : "/oauth2.0/authorize"))
                .queryParam("response_type", "code")
                .queryParam("client_id", p.clientId())
                .queryParam("redirect_uri", redirectUri(provider))
                .encode()
                .build()
                .toUriString();
    }

    /** 각 개발자 센터에 등록하는 콜백 주소 (FE 화면) */
    public String redirectUri(SocialProvider provider) {
        return properties.frontendUrl().replaceAll("/+$", "") + "/oauth/" + provider.path() + "/callback";
    }

    public Profile fetchProfile(SocialProvider provider, String code, String state) {
        try {
            String accessToken = accessToken(provider, code, state);
            return provider == SocialProvider.KAKAO ? kakao(accessToken) : naver(accessToken);
        }
        catch (RestClientException e) {
            log.warn("{} 로그인 실패", provider, e);
            throw new BusinessException(ErrorCode.OAUTH_FAILED);
        }
    }

    private String accessToken(SocialProvider provider, String code, String state) {
        OAuthProperties.Provider p = properties.of(provider);
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", p.clientId());
        if (p.clientSecret() != null && !p.clientSecret().isBlank()) {
            form.add("client_secret", p.clientSecret());
        }
        form.add("redirect_uri", redirectUri(provider));
        form.add("code", code);
        if (state != null) {
            form.add("state", state);
        }
        Map<String, Object> res = restClient.post()
                .uri(p.authUrl() + (provider == SocialProvider.KAKAO ? "/oauth/token" : "/oauth2.0/token"))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(JSON);
        String token = res == null ? null : str(res, "access_token");
        if (token == null) {
            log.warn("{} 토큰 발급 실패 {}", provider, res);
            throw new BusinessException(ErrorCode.OAUTH_FAILED);
        }
        return token;
    }

    /** 카카오 — 이메일은 동의했을 때만, 이름이 없으면 닉네임. 휴대폰은 +82 10-1234-5678 꼴 */
    private Profile kakao(String accessToken) {
        Map<String, Object> me = me(properties.kakao().apiUrl() + "/v2/user/me", accessToken);
        Map<String, Object> account = map(me, "kakao_account");
        boolean verified = Boolean.TRUE.equals(account.get("is_email_valid")) && Boolean.TRUE.equals(account.get("is_email_verified"));
        String name = str(account, "name");
        if (name == null) {
            name = str(map(account, "profile"), "nickname");
        }
        String phone = str(account, "phone_number");
        if (phone != null) {
            phone = phone.replaceFirst("^\\+82\\s*", "0");
        }
        return profile(SocialProvider.KAKAO, str(me, "id"), str(account, "email"), verified, name, phone);
    }

    /** 네이버 — 이메일 · 이름 · 휴대폰은 앱 설정에서 고른 항목만 온다 */
    private Profile naver(String accessToken) {
        Map<String, Object> res = map(me(properties.naver().apiUrl() + "/v1/nid/me", accessToken), "response");
        String email = str(res, "email");
        return profile(SocialProvider.NAVER, str(res, "id"), email, email != null, str(res, "name"), str(res, "mobile"));
    }

    private static Profile profile(SocialProvider provider, String id, String email, boolean verified, String name, String phone) {
        if (id == null) {
            throw new BusinessException(ErrorCode.OAUTH_FAILED);
        }
        return new Profile(provider, id, email, verified, name, phone);
    }

    private Map<String, Object> me(String url, String accessToken) {
        Map<String, Object> res = restClient.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(JSON);
        return res == null ? Map.of() : res;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Map<String, Object> source, String key) {
        return source.get(key) instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of();
    }

    /** 숫자 id 도 문자열로. 빈 값은 null */
    private static String str(Map<String, Object> source, String key) {
        Object value = source.get(key);
        return value == null || value.toString().isBlank() ? null : value.toString();
    }

}