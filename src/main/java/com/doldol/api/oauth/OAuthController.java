package com.doldol.api.oauth;

import com.doldol.api.member.AuthCookies;
import com.doldol.api.member.AuthService;
import com.doldol.api.member.LoginResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/oauth")
@RequiredArgsConstructor
public class OAuthController {
    private final OAuthService oauthService;
    private final AuthCookies authCookies;
    @GetMapping("/providers")
    public List<OAuthService.ProviderInfo> providers(){return oauthService.providers();}
    @PostMapping("/{provider}")
    public ResponseEntity<CallbackResponse> callback(@PathVariable String provider, @Valid @RequestBody CallbackRequest request){
        OAuthService.CallbackResult result = oauthService.callback(SocialProvider.fromPath(provider), request.code(), request.state());
        if (result.login() == null) {return ResponseEntity.ok(new CallbackResponse("SIGNUP", null, result.signup()));}
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, authCookies.issue(result.login().refreshToken()).toString()).body(new CallbackResponse("LOGIN", result.login().body(), null));
    }
    @PostMapping("/signup")
    public ResponseEntity<LoginResponse> signup(@Valid @RequestBody SocialSignupRequest request){AuthService.LoginResult result = oauthService.signup(request);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, authCookies.issue(result.refreshToken()).toString()).body(result.body());
    }
    public record CallbackRequest(@NotBlank(message = "다시 로그인해 주세요.") String code, String state) {}
    public record CallbackResponse(String status, LoginResponse login, OAuthService.SignupInfo signup) {}
}