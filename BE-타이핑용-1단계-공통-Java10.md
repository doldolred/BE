# DolDol BE — 1단계: 공통 예외 처리 + 상품 상세 (.java 10개)

경로: `BE/src/main/java/com/doldol/api/`. 별도 복사본에서 빌드 · 테스트 · 실제 요청까지 확인한 코드.

- 새 파일 8개, 통째로 교체 2개(`Product.java`, `ProductController.java`) + `SecurityConfig.java` 두 줄 추가
- 기존 `ProductController` 안에 있던 `ProductResponse` record 는 별도 파일로 뺐다
- `common/`, `error/` 의 `.gitkeep` 은 파일을 넣고 나면 지워도 된다

확인 (`cd BE && ./gradlew bootRun` 후):

| 요청 | 기대 결과 |
| --- | --- |
| `GET /api/products/1` | 200 상품 정보 |
| `GET /api/products/999` | 404 `{"code":"PRODUCT_NOT_FOUND", ...}` |
| `GET /api/products/abc` | 400 `{"code":"INVALID_INPUT", ...}` |

## common/BaseEntity.java (새 파일)

```java
package com.doldol.api.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 모든 엔티티가 상속받는 생성/수정 시각 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@LastModifiedDate
	@Column(nullable = false)
	private Instant updatedAt;

}
```

## config/JpaConfig.java (새 파일)

```java
package com.doldol.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** BaseEntity 의 @CreatedDate / @LastModifiedDate 를 자동으로 채워줌 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {

}
```

## error/ErrorCode.java (새 파일)

```java
package com.doldol.api.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/** docs/api.md 의 에러 코드 표와 1:1. 응답의 code 는 enum 이름 그대로 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// 400
	INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값을 확인해 주세요."),

	// 401, 403
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
	LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

	// 404
	NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 주소를 찾을 수 없습니다."),
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
	PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
	CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니 상품을 찾을 수 없습니다."),
	ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),

	// 409
	DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
	OUT_OF_STOCK(HttpStatus.CONFLICT, "재고가 부족합니다."),
	CART_EMPTY(HttpStatus.CONFLICT, "장바구니가 비어 있습니다."),
	ORDER_NOT_CANCELLABLE(HttpStatus.CONFLICT, "취소할 수 없는 주문입니다."),

	// 500
	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");

	private final HttpStatus status;

	private final String message;

}
```

## error/BusinessException.java (새 파일)

```java
package com.doldol.api.error;

import lombok.Getter;

/** 서비스 로직에서 throw new BusinessException(ErrorCode.XXX) 로 사용 */
@Getter
public class BusinessException extends RuntimeException {

	private final ErrorCode errorCode;

	public BusinessException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	/** 기본 메시지 대신 상황에 맞는 메시지를 줄 때 */
	public BusinessException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

}
```

## error/ErrorResponse.java (새 파일)

```java
package com.doldol.api.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/** 모든 에러 응답의 형태: { code, message, fieldErrors? } */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(String code, String message, List<FieldError> fieldErrors) {

	public record FieldError(String field, String message) {
	}

	public static ErrorResponse of(ErrorCode errorCode) {
		return new ErrorResponse(errorCode.name(), errorCode.getMessage(), List.of());
	}

	public static ErrorResponse of(ErrorCode errorCode, String message) {
		return new ErrorResponse(errorCode.name(), message, List.of());
	}

	public static ErrorResponse of(ErrorCode errorCode, List<FieldError> fieldErrors) {
		return new ErrorResponse(errorCode.name(), errorCode.getMessage(), fieldErrors);
	}

}
```

## error/GlobalExceptionHandler.java (새 파일)

```java
package com.doldol.api.error;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** 컨트롤러에서 터진 예외를 ErrorResponse JSON 으로 바꿔줌 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	/** 우리가 직접 던진 비즈니스 예외 */
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
		ErrorCode code = e.getErrorCode();
		return ResponseEntity.status(code.getStatus()).body(ErrorResponse.of(code, e.getMessage()));
	}

	/** @Valid 검증 실패 → 필드별 메시지 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
		List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult().getFieldErrors().stream()
				.map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
				.toList();
		return ResponseEntity.badRequest().body(ErrorResponse.of(ErrorCode.INVALID_INPUT, fieldErrors));
	}

	/** JSON 형식 오류, 경로 변수 타입 오류(/api/products/abc) */
	@ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class })
	public ResponseEntity<ErrorResponse> handleBadRequest(Exception e) {
		return ResponseEntity.badRequest().body(ErrorResponse.of(ErrorCode.INVALID_INPUT));
	}

	/** 없는 URL */
	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(NoResourceFoundException e) {
		return ResponseEntity.status(ErrorCode.NOT_FOUND.getStatus()).body(ErrorResponse.of(ErrorCode.NOT_FOUND));
	}

	/** 지원하지 않는 HTTP 메서드 (예: GET 만 있는데 POST) */
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
		return ResponseEntity.status(e.getStatusCode()).body(ErrorResponse.of(ErrorCode.INVALID_INPUT, e.getMessage()));
	}

	/** 그 외 예상 못 한 예외 — 로그 남기고 500 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
		log.error("Unexpected error", e);
		return ResponseEntity.internalServerError().body(ErrorResponse.of(ErrorCode.INTERNAL_ERROR));
	}

}
```

## product/Product.java (통째로 교체)

```java
package com.doldol.api.product;

import com.doldol.api.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;

	private String description;

	@Column(nullable = false)
	private Integer price;

	@Column(nullable = false)
	private Integer stock;

	private String imageUrl;

	@Column(nullable = false)
	private boolean active;

}
```

## product/ProductResponse.java (새 파일)

```java
package com.doldol.api.product;

public record ProductResponse(Long id, String name, String description, int price, int stock, String imageUrl) {

	public static ProductResponse from(Product product) {
		return new ProductResponse(product.getId(), product.getName(), product.getDescription(), product.getPrice(),
				product.getStock(), product.getImageUrl());
	}

}
```

## product/ProductService.java (새 파일)

```java
package com.doldol.api.product;

import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

	private final ProductRepository productRepository;

	public List<ProductResponse> getProducts() {
		return productRepository.findAllByActiveTrueOrderByIdAsc().stream()
				.map(ProductResponse::from)
				.toList();
	}

	public ProductResponse getProduct(Long id) {
		Product product = productRepository.findById(id)
				.filter(Product::isActive) // 판매 중지 상품은 없는 것처럼
				.orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
		return ProductResponse.from(product);
	}

}
```

## product/ProductController.java (통째로 교체)

```java
package com.doldol.api.product;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	@GetMapping
	public List<ProductResponse> list() {
		return productService.getProducts();
	}

	@GetMapping("/{id}")
	public ProductResponse detail(@PathVariable Long id) {
		return productService.getProduct(id);
	}

}
```

## config/SecurityConfig.java (두 줄 추가)

`.authorizeHttpRequests(auth -> auth` 바로 아래, actuator 줄 위에. 2단계에서 이 파일을 통째로 바꾸니 지금은 이 두 줄만.

```java
				// 404/500 등이 /error 로 넘어갈 때 403 으로 바뀌지 않도록
				.requestMatchers("/error").permitAll()
```
