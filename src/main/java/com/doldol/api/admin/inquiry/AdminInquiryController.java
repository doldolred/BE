package com.doldol.api.admin.inquiry;

import com.doldol.api.common.PageResponse;
import com.doldol.api.inquiry.InquiryService;
import com.doldol.api.inquiry.InquiryStatus;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/inquiries")
@RequiredArgsConstructor
public class AdminInquiryController {
    private final InquiryService inquiryService;
    @GetMapping
    public PageResponse<AdminInquiryResponse> list(@RequestParam(required = false) InquiryStatus status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size){
        return inquiryService.getAll(status, page, size);
    }
    @GetMapping("/waiting-count")
    public Map<String, Long> waitingCount(){return Map.of("count", inquiryService.countWaiting());}
    @GetMapping("/{id}")
    public AdminInquiryResponse detail(@PathVariable Long id){return inquiryService.get(id);}
    @PutMapping("/{id}/answer")
    public AdminInquiryResponse answer(@PathVariable Long id, @Valid @RequestBody AnswerInquiryRequest request){return inquiryService.answer(id, request.answer());}
}
