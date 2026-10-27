package com.doldol.api.notice;

import com.doldol.api.common.PageResponse;
import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeController {
    public static final Sort ORDER = Sort.by(Sort.Order.desc("pinned"), Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
    private final NoticeRepository noticeRepository;
    @GetMapping
    public PageResponse<NoticeResponse> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size){
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 50), ORDER);
        return PageResponse.of(noticeRepository.findByPublishedTrue(pageable), NoticeResponse::from);
    }
    @GetMapping("/{id}")
    public NoticeResponse detail(@PathVariable Long id){
        return noticeRepository.findByIdAndPublishedTrue(id).map(NoticeResponse::from).orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
    }
}
