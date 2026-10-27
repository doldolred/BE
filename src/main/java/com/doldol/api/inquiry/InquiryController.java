package com.doldol.api.inquiry;
import com.doldol.api.common.PageResponse;
import com.doldol.api.security.LoginMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
public class InquiryController {
    private final InquiryService inquiryService;
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InquiryResponse create(@AuthenticationPrincipal LoginMember loginMember, @Valid @RequestBody CreateInquiryRequest request){return inquiryService.create(loginMember.id(), request);}
    @GetMapping
    public PageResponse<InquiryResponse> list(@AuthenticationPrincipal LoginMember loginMember, @RequestParam(defaultValue = "0")int page, @RequestParam(defaultValue = "20") int size){return inquiryService.getMine(loginMember.id(), page, size);}
    @GetMapping("/{id}")
    public InquiryResponse detail(@AuthenticationPrincipal LoginMember loginMember, @PathVariable Long id){return inquiryService.getMine(loginMember.id(), id);}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal LoginMember loginMember, @PathVariable Long id){inquiryService.deleteMine(loginMember.id(), id);}
}
