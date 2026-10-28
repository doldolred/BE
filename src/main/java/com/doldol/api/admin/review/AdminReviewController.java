package com.doldol.api.admin.review;

import com.doldol.api.common.PageResponse;
import com.doldol.api.review.ReviewResponse;
import com.doldol.api.review.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {
    private final ReviewService reviewService;
    @GetMapping
    public PageResponse<ReviewResponse> list(@RequestParam(defaultValue = "ALL") String filter, @RequestParam(defaultValue ="0") int page, @RequestParam(defaultValue = "20") int size){return reviewService.search(filter, page, size);}
    @PatchMapping("/{id}")
    public ReviewResponse update(@PathVariable Long id, @Valid @RequestBody AdminReviewUpdateRequest request){return reviewService.update(id, request);}
}
