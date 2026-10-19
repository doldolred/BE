# DolDol BE — 2단계: 회원가입 · 로그인(JWT) (.java 18개)

경로: `BE/src/main/java/com/doldol/api/`. **1단계를 먼저 옮긴 뒤** 진행. 별도 복사본에서 빌드 · 테스트 · 실제 요청 · FE 화면 연동까지 확인한 코드.

- 새 파일 17개(`member/` 12, `security/` 5) + `config/SecurityConfig.java` 통째로 교체
- `member/`, `security/` 의 `.gitkeep` 은 파일을 넣고 나면 지워도 된다
- 설정(`build.gradle` 의 jjwt, `application.yml` 의 `app.jwt.*`, DB 의 `member` 테이블)은 이미 들어가 있다

흐름:

1. `POST /api/auth/login` → `AuthService` 가 비밀번호 확인 → `JwtTokenProvider` 가 토큰 발급
2. FE 는 이후 요청마다 `Authorization: Bearer {토큰}` 을 붙인다
3. `JwtAuthenticationFilter` 가 토큰을 읽어 `LoginMember` 로 로그인 상태를 만든다
4. 컨트롤러는 `@AuthenticationPrincipal LoginMember loginMember` 로 받는다
5. 막힌 요청은 `SecurityErrorHandler` → `GlobalExceptionHandler` 로 넘어가 401/403 JSON

확인 (`cd BE && ./gradlew bootRun` 후, 또는 FE `npm run dev` 에서 회원가입 → 마이페이지):

| 요청 | 기대 결과 |
| --- | --- |
| `POST /api/auth/signup` 정상 | 201 `{id, email, name}` |
| 같은 이메일로 다시 | 409 `DUPLICATE_EMAIL` |
| 이메일 형식 · 짧은 비밀번호 | 400 `INVALID_INPUT` + `fieldErrors` |
| `POST /api/auth/login` 정상 (대문자 이메일도 OK) | 200 `{accessToken, tokenType:"Bearer", expiresIn:7200, member}` |
| 비밀번호 틀림 | 401 `LOGIN_FAILED` |
| `GET /api/members/me` + 토큰 | 200 내 정보 |
| 토큰 없음 / 잘못된 토큰 | 401 `UNAUTHORIZED` |
| USER 토큰으로 `/api/admin/**` | 403 `FORBIDDEN` |

관리자 만들기 (가입 후 DB 에서 직접):

```sql
UPDATE member SET role = 'ADMIN' WHERE email = '관리자이메일';
```

## member/Role.java (새 파일)

```java
package com.doldol.api.member;

/** 권한. Spring Security 에서는 "ROLE_" + name() 으로 쓴다 (예: ROLE_ADMIN) */
public enum Role {

	USER, ADMIN

}
```

## member/Member.java (새 파일)

```java
package com.doldol.api.member;

import com.doldol.api.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String email;

	/** BCrypt 로 암호화된 값만 저장 */
	@Column(nullable = false)
	private String password;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(length = 20)
	private String phone;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Role role;

	/** 회원가입 — 권한은 항상 USER 로 시작 */
	public static Member create(String email, String encodedPassword, String name, String phone) {
		Member member = new Member();
		member.email = email;
		member.password = encodedPassword;
		member.name = name;
		member.phone = phone;
		member.role = Role.USER;
		return member;
	}

}
```

## member/MemberRepository.java (새 파일)

```java
package com.doldol.api.member;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

	boolean existsByEmail(String email);

	Optional<Member> findByEmail(String email);

}
```

## member/SignupRequest.java (새 파일)

```java
package com.doldol.api.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(

		@NotBlank(message = "이메일을 입력해 주세요.")
		@Email(message = "이메일 형식이 아닙니다.")
		@Size(max = 255, message = "이메일이 너무 깁니다.")
		String email,

		@NotBlank(message = "비밀번호를 입력해 주세요.")
		@Size(min = 8, max = 64, message = "비밀번호는 8~64자로 입력해 주세요.")
		String password,

		@NotBlank(message = "이름을 입력해 주세요.")
		@Size(max = 50, message = "이름은 50자 이하로 입력해 주세요.")
		String name,

		/** 선택. 010-1234-5678 / 01012345678 둘 다 허용 */
		@Pattern(regexp = "^01[0-9]-?\\d{3,4}-?\\d{4}$", message = "휴대폰 번호 형식이 아닙니다.")
		String phone) {

}
```

## member/SignupResponse.java (새 파일)

```java
package com.doldol.api.member;

public record SignupResponse(Long id, String email, String name) {

	public static SignupResponse from(Member member) {
		return new SignupResponse(member.getId(), member.getEmail(), member.getName());
	}

}
```

## member/LoginRequest.java (새 파일)

```java
package com.doldol.api.member;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

		@NotBlank(message = "이메일을 입력해 주세요.")
		String email,

		@NotBlank(message = "비밀번호를 입력해 주세요.")
		String password) {

}
```

## member/LoginResponse.java (새 파일)

```java
package com.doldol.api.member;

/** expiresIn 은 초 단위 */
public record LoginResponse(String accessToken, String tokenType, long expiresIn, MemberSummary member) {

	public record MemberSummary(Long id, String email, String name, Role role) {
	}

	public static LoginResponse of(String accessToken, long expiresIn, Member member) {
		MemberSummary summary = new MemberSummary(member.getId(), member.getEmail(), member.getName(), member.getRole());
		return new LoginResponse(accessToken, "Bearer", expiresIn, summary);
	}

}
```

## member/MemberResponse.java (새 파일)

```java
package com.doldol.api.member;

/** GET /api/members/me */
public record MemberResponse(Long id, String email, String name, String phone, Role role) {

	public static MemberResponse from(Member member) {
		return new MemberResponse(member.getId(), member.getEmail(), member.getName(), member.getPhone(),
				member.getRole());
	}

}
```

## member/AuthService.java (새 파일)

```java
package com.doldol.api.member;

import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import com.doldol.api.security.JwtTokenProvider;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

	private final MemberRepository memberRepository;

	private final PasswordEncoder passwordEncoder;

	private final JwtTokenProvider jwtTokenProvider;

	@Transactional
	public SignupResponse signup(SignupRequest request) {
		String email = normalize(request.email());
		if (memberRepository.existsByEmail(email)) {
			throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
		}
		Member member = Member.create(email, passwordEncoder.encode(request.password()), request.name().trim(),
				request.phone());
		memberRepository.save(member);
		return SignupResponse.from(member);
	}

	public LoginResponse login(LoginRequest request) {
		// 이메일이 없는 경우와 비밀번호가 틀린 경우를 같은 에러로 — 가입 여부를 알려주지 않기 위해
		Member member = memberRepository.findByEmail(normalize(request.email()))
				.filter(m -> passwordEncoder.matches(request.password(), m.getPassword()))
				.orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));
		String accessToken = jwtTokenProvider.createAccessToken(member);
		return LoginResponse.of(accessToken, jwtTokenProvider.getValiditySeconds(), member);
	}

	/** 대소문자가 달라도 같은 이메일로 (A@doldol.com == a@doldol.com) */
	private String normalize(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

}
```

## member/AuthController.java (새 파일)

```java
package com.doldol.api.member;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/signup")
	@ResponseStatus(HttpStatus.CREATED)
	public SignupResponse signup(@Valid @RequestBody SignupRequest request) {
		return authService.signup(request);
	}

	@PostMapping("/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

}
```

## member/MemberService.java (새 파일)

```java
package com.doldol.api.member;

import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

	private final MemberRepository memberRepository;

	public MemberResponse getMe(Long memberId) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
		return MemberResponse.from(member);
	}

}
```

## member/MemberController.java (새 파일)

```java
package com.doldol.api.member;

import com.doldol.api.security.LoginMember;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;

	/** @AuthenticationPrincipal — JwtAuthenticationFilter 가 넣어 둔 로그인 회원 */
	@GetMapping("/me")
	public MemberResponse me(@AuthenticationPrincipal LoginMember loginMember) {
		return memberService.getMe(loginMember.id());
	}

}
```

## security/JwtProperties.java (새 파일)

```java
package com.doldol.api.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** application.yml 의 app.jwt.* 를 그대로 받는다 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, Duration accessTokenValidity) {

}
```

## security/LoginMember.java (새 파일)

```java
package com.doldol.api.security;

import com.doldol.api.member.Role;

/** 토큰에서 꺼낸 로그인 회원 — 컨트롤러에서 @AuthenticationPrincipal LoginMember 로 받는다 */
public record LoginMember(Long id, String email, Role role) {

}
```

## security/JwtTokenProvider.java (새 파일)

```java
package com.doldol.api.security;

import com.doldol.api.member.Member;
import com.doldol.api.member.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

/** Access Token(JWT) 만들기 · 검증 */
@Component
public class JwtTokenProvider {

	private final SecretKey key;

	private final Duration validity;

	public JwtTokenProvider(JwtProperties properties) {
		// HS256 은 32바이트 이상 키가 필요 — 짧으면 여기서 바로 에러가 나서 서버가 안 뜬다
		this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
		this.validity = properties.accessTokenValidity();
	}

	public String createAccessToken(Member member) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(String.valueOf(member.getId()))
				.claim("email", member.getEmail())
				.claim("role", member.getRole().name())
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plus(validity)))
				.signWith(key)
				.compact();
	}

	public long getValiditySeconds() {
		return validity.toSeconds();
	}

	/** 서명 · 만료를 확인하고 회원 정보를 꺼낸다. 잘못된 토큰이면 빈 값 */
	public Optional<LoginMember> parse(String token) {
		try {
			Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
			return Optional.of(new LoginMember(Long.valueOf(claims.getSubject()), claims.get("email", String.class),
					Role.valueOf(claims.get("role", String.class))));
		}
		catch (JwtException | IllegalArgumentException e) {
			return Optional.empty();
		}
	}

}
```

## security/JwtAuthenticationFilter.java (새 파일)

```java
package com.doldol.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 요청마다 Authorization: Bearer {토큰} 을 읽어 로그인 상태로 만든다.
 * 토큰이 없거나 잘못돼도 여기서 막지 않는다 — 막는 건 SecurityConfig 의 권한 규칙이 한다.
 * @Component 를 붙이지 않는다(붙이면 서블릿 필터로 한 번 더 등록된다). SecurityConfig 에서 직접 만든다.
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtTokenProvider jwtTokenProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header != null && header.startsWith(BEARER_PREFIX)) {
			jwtTokenProvider.parse(header.substring(BEARER_PREFIX.length())).ifPresent(member -> {
				var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + member.role().name()));
				var authentication = new UsernamePasswordAuthenticationToken(member, null, authorities);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			});
		}
		chain.doFilter(request, response);
	}

}
```

## security/SecurityErrorHandler.java (새 파일)

```java
package com.doldol.api.security;

import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * 시큐리티에서 막힌 요청(401 · 403)도 GlobalExceptionHandler 와 같은 JSON 으로 응답한다.
 * 필터 단계라 @RestControllerAdvice 가 직접 못 잡으므로, 예외를 HandlerExceptionResolver 에 넘겨 처리하게 한다.
 */
@Component
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

	private final HandlerExceptionResolver resolver;

	// @Qualifier 가 생성자 파라미터에 있어야 해서 롬복 대신 직접 생성자를 쓴다
	public SecurityErrorHandler(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
		this.resolver = resolver;
	}

	/** 로그인 안 됨 → 401 */
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) {
		resolver.resolveException(request, response, null, new BusinessException(ErrorCode.UNAUTHORIZED));
	}

	/** 로그인은 됐지만 권한 없음 → 403 */
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) {
		resolver.resolveException(request, response, null, new BusinessException(ErrorCode.FORBIDDEN));
	}

}
```

## config/SecurityConfig.java (통째로 교체)

```java
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
				.requestMatchers(HttpMethod.POST, "/api/auth/signup", "/api/auth/login").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
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
```
