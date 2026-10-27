package com.doldol.api.admin.notice;

import com.doldol.api.common.PageResponse;
import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import com.doldol.api.notice.Notice;
import com.doldol.api.notice.NoticeController;
import com.doldol.api.notice.NoticeRepository;
import com.doldol.api.notice.NoticeResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/notices")
@RequiredArgsConstructor
public class AdminNoticeController {
    private final NoticeRepository noticeRepository;
    @GetMapping
    @Transactional(readOnly = true)
    public PageResponse<NoticeResponse>list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size){
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 100), NoticeController.ORDER);
        return PageResponse.of(noticeRepository.findAll(pageable), NoticeResponse::from);
    }
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public NoticeResponse detail(@PathVariable Long id){
        return NoticeResponse.from(find(id));
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public NoticeResponse create(@Valid @RequestBody NoticeRequest r){
        Notice notice = Notice.create(r.title(), r.content(), r.pinned(), r.published());
        return NoticeResponse.from(noticeRepository.saveAndFlush(notice));
    }
    @PutMapping("/{id}")
    @Transactional
    public NoticeResponse update(@PathVariable Long id, @Valid @RequestBody NoticeRequest r){
        Notice notice = find(id);
        notice.update(r.title(), r.content(), r.pinned(), r.published());
        return NoticeResponse.from(noticeRepository.saveAndFlush(notice));
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void delete(@PathVariable Long id) {
        noticeRepository.delete(find(id));
    }
    private Notice find(Long id){
        return noticeRepository.findById(id).orElseThrow(()-> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
    }
}
