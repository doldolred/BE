package com.doldol.api.admin.member;

import com.doldol.api.common.PageResponse;
import com.doldol.api.security.LoginMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {
    private final AdminMemberService adminMemberService;
    @GetMapping
    public PageResponse<AdminMemberResponse> list(@RequestParam(required = false)String keyword, @RequestParam(defaultValue = "0")int page, @RequestParam(defaultValue = "20") int size){return adminMemberService.search(keyword, page, size);}
    @GetMapping("/{id}")
    public AdminMemberDetailResponse detail(@PathVariable Long id){return adminMemberService.getMember(id);}
    @PatchMapping("/{id}/role")
    public AdminMemberDetailResponse changeRole(@AuthenticationPrincipal LoginMember loginMember, @PathVariable Long id, @Valid @RequestBody ChangeRoleRequest request){return adminMemberService.changeRole(loginMember.id(), id, request.role());}
}
