package com.doldol.api.admin.claim;

import com.doldol.api.claim.ClaimResponse;
import com.doldol.api.claim.ClaimService;
import com.doldol.api.claim.ClaimStatus;
import com.doldol.api.claim.ClaimType;
import com.doldol.api.common.PageResponse;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/claims")
@RequiredArgsConstructor
public class AdminClaimController {
    private final ClaimService claimService;
    @GetMapping
    public PageResponse<ClaimResponse> list(@RequestParam(required = false) ClaimStatus status, @RequestParam(required = false) ClaimType type, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size){
        return claimService.search(status, type, page, size);
    }
    @GetMapping("/requested-count")
    public Map<String, Long> requestedCount(){
        return Map.of("count", claimService.countRequested());
    }
    @GetMapping("/{id}")
    public ClaimResponse detail(@PathVariable Long id){
        return claimService.get(id);
    }
    @PostMapping("/{id}/approve")
    public ClaimResponse approve(@PathVariable Long id){
        return claimService.approve(id);
    }
    @PostMapping("/{id}/reject")
    public ClaimResponse reject(@PathVariable Long id, @Valid @RequestBody ClaimRejectRequest request){
        return claimService.reject(id, request.reason());
    }
    @PostMapping("/{id}/complete")
    public ClaimResponse complete(@PathVariable Long id, @Valid @RequestBody(required = false) ClaimCompleteRequest request){
        return claimService.complete(id, request);
    }
}
