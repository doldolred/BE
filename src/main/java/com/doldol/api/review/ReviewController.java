package com.doldol.api.review;

import com.doldol.api.common.PageResponse;
import com.doldol.api.security.LoginMember;
import com.doldol.api.upload.ImageStorage;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 상품 후기.
 *   GET /api/products/{productId}/reviews · /summary — 로그인 없이 (SecurityConfig 의 GET /api/products/**)
 *   /api/reviews/** — 로그인한 회원
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    private final ImageStorage imageStorage;

    public record ImageResponse(String url) {
    }

    @GetMapping("/products/{productId}/reviews")
    public PageResponse<ReviewResponse> productReviews(@PathVariable Long productId, @RequestParam(defaultValue = "latest") String sort,
                                                       @RequestParam(defaultValue = "false") boolean photoOnly, @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        return reviewService.getProductReviews(productId, sort, photoOnly, page, size);
    }

    @GetMapping("/products/{productId}/reviews/summary")
    public ReviewSummaryResponse summary(@PathVariable Long productId) {
        return reviewService.getSummary(productId);
    }

    /** 후기 사진 올리기 — 받은 url 을 후기 imageUrls 에 넣는다 */
    @PostMapping(path = "/reviews/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ImageResponse uploadImage(@RequestPart("file") MultipartFile file) {
        return new ImageResponse(imageStorage.store(file, "reviews"));
    }

    @GetMapping("/reviews/writable")
    public List<WritableItemResponse> writable(@AuthenticationPrincipal LoginMember loginMember) {
        return reviewService.getWritable(loginMember.id());
    }

    @PostMapping("/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse create(@AuthenticationPrincipal LoginMember loginMember, @Valid @RequestBody CreateReviewRequest request) {
        return reviewService.create(loginMember.id(), request);
    }

    @GetMapping("/reviews/mine")
    public PageResponse<ReviewResponse> mine(@AuthenticationPrincipal LoginMember loginMember, @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        return reviewService.getMine(loginMember.id(), page, size);
    }

    @DeleteMapping("/reviews/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal LoginMember loginMember, @PathVariable Long id) {
        reviewService.deleteMine(loginMember.id(), id);
    }

}