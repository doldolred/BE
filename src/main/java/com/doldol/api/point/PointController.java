package com.doldol.api.point;

import com.doldol.api.common.PageResponse;
import com.doldol.api.security.LoginMember;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {
    private final PointService pointService;
    @GetMapping
    public PointSummaryResponse summary(@AuthenticationPrincipal LoginMember loginMember){
        return pointService.summary(loginMember.id());
    }
    @GetMapping("/history")
    public PageResponse<PointHistoryResponse> history(@AuthenticationPrincipal LoginMember loginMember, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size){
        return pointService.history(loginMember.id(), page, size);
    }
}