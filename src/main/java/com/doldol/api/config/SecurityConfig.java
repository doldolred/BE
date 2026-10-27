package com.doldol.api.config;

import com.doldol.api.security.JwtAuthenticationFilter;
import com.doldol.api.security.JwtProperties;
import com.doldol.api.security.JwtTokenProvider;
import com.doldol.api.security.SecurityErrorHandler;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtTokenProvider jwtTokenProvider;

	private final SecurityErrorHandler securityErrorHandler;

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
				.cors(Customizer.withDefaults())
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.httpBasic(b -> b.disable())
				.formLogin(f -> f.disable())
				// 401 · 403 도 { code, message } JSON 으로
				.exceptionHandling(e -> e
						.authenticationEntryPoint(securityErrorHandler)
						.accessDeniedHandler(securityErrorHandler))
				// 매 요청마다 Bearer 토큰을 읽어 로그인 상태로
				.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
				.authorizeHttpRequests(auth -> auth
						// 404/500 등이 /error 로 넘어갈 때 403 으로 바뀌지 않도록
						.requestMatchers("/error").permitAll()
						.requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/auth/signup", "/api/auth/login", "/api/auth/refresh", "/api/auth/logout",
								"/api/auth/password/forgot", "/api/auth/password/reset").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/products/**", "/api/shipping/**", "/api/notices/**", "/api/banners/**", "/uploads/**").permitAll()
						// 나이스페이 결제 결과 (브라우저 form POST) — 로그인 대신 서명으로 검증한다
						.requestMatchers(HttpMethod.POST, "/api/payments/nicepay/return").permitAll()
						.requestMatchers("/api/admin/**").hasRole("ADMIN")
						.anyRequest().authenticated());
		return http.build();
	}

	/** 기본 BCrypt. 저장값 앞에 {bcrypt} 가 붙어 나중에 알고리즘을 바꿔도 예전 비밀번호가 그대로 맞는다 */
	@Bean
	PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins}") List<String> allowedOrigins) {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(allowedOrigins);
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", config);
		return source;
	}

}