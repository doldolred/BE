package com.doldol.api.member;

import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
    public MemberResponse getMe(Long memberId){
        return MemberResponse.from(find(memberId));
    }
    /** 마케팅 수신 동의 / 철회 */
    @Transactional
    public MemberResponse changeMarketing(Long memberId, boolean agreed){
        Member member = find(memberId);
        member.changeMarketingAgreed(agreed);
        return MemberResponse.from(member);
    }
    private Member find(Long memberId){
        return memberRepository.findById(memberId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
