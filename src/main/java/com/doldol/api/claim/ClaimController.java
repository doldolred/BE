package com.doldol.api.claim;

import com.doldol.api.common.PageResponse;
import com.doldol.api.security.LoginMember;
import com.doldol.api.upload.ImageStorage;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

/** 내 취소 · 반품 · 교환 신청 (로그인 필요) */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    private final ImageStorage imageStorage;

    public record ImageResponse(String url) {
    }

    @PostMapping("/orders/{orderNumber}/claims")
    @ResponseStatus(HttpStatus.CREATED)
    public ClaimResponse create(@AuthenticationPrincipal LoginMember loginMember, @PathVariable String orderNumber,
                                @Valid @RequestBody CreateClaimRequest request) {
        return claimService.create(loginMember.id(), orderNumber, request);
    }

    @GetMapping("/orders/{orderNumber}/claims")
    public List<ClaimResponse> byOrder(@AuthenticationPrincipal LoginMember loginMember, @PathVariable String orderNumber) {
        return claimService.getByOrder(loginMember.id(), orderNumber);
    }

    @PostMapping(path = "/claims/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ImageResponse uploadImage(@RequestPart("file") MultipartFile file) {
        return new ImageResponse(imageStorage.store(file, "claims"));
    }

    @GetMapping("/claims")
    public PageResponse<ClaimResponse> mine(@AuthenticationPrincipal LoginMember loginMember, @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return claimService.getMine(loginMember.id(), page, size);
    }

    @GetMapping("/claims/{id}")
    public ClaimResponse detail(@AuthenticationPrincipal LoginMember loginMember, @PathVariable Long id) {
        return claimService.getMine(loginMember.id(), id);
    }

    /** 철회 — 접수 상태에서만 */
    @PostMapping("/claims/{id}/withdraw")
    public ClaimResponse withdraw(@AuthenticationPrincipal LoginMember loginMember, @PathVariable Long id) {
        return claimService.withdraw(loginMember.id(), id);
    }

}