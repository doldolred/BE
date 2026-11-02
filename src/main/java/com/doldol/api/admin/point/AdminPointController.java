package com.doldol.api.admin.point;

import com.doldol.api.common.PageResponse;
import com.doldol.api.point.PointHistoryResponse;
import com.doldol.api.point.PointService;
import com.doldol.api.point.PointSummaryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/members/{memberId}/points")
@RequiredArgsConstructor
public class AdminPointController {
    private final PointService pointService;
    @GetMapping
    public PointSummaryResponse summary(@PathVariable Long memberId){return pointService.summary(memberId);}
    @GetMapping("/history")
    public PageResponse<PointHistoryResponse> history(@PathVariable Long memberId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size){return pointService.history(memberId, page, size);}
    @PostMapping
    public PointSummaryResponse adjust(@PathVariable Long memberId, @Valid @RequestBody PointAdjustRequest r){return pointService.adjust(memberId, r.amount(), r.memo());}
}
