package com.doldol.api.inquiry;

import com.doldol.api.admin.inquiry.AdminInquiryResponse;
import com.doldol.api.common.PageResponse;
import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import com.doldol.api.mail.MailService;
import com.doldol.api.member.Member;
import com.doldol.api.member.MemberRepository;
import com.doldol.api.order.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 1:1 문의 — 회원(쓰기 · 내 것 보기 · 답 전 삭제)과 관리자(목록 · 답하기)가 같이 쓴다 */
@Service
@Transactional
public class InquiryService {

    private static final Sort LATEST = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
    private final InquiryRepository inquiryRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final MailService mailService;
    private final String frontendUrl;
    public InquiryService(InquiryRepository inquiryRepository, MemberRepository memberRepository, OrderRepository orderRepository, MailService mailService, @Value("${app.frontend-url}") String frontendUrl) {
        this.inquiryRepository = inquiryRepository;
        this.memberRepository = memberRepository;
        this.orderRepository = orderRepository;
        this.mailService = mailService;
        this.frontendUrl = frontendUrl.replaceAll("/+$", "");
    }

    // ── 회원 ─────────────────────────────

    public InquiryResponse create(Long memberId, CreateInquiryRequest r) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        String orderNumber = r.orderNumber() == null || r.orderNumber().isBlank() ? null : r.orderNumber().trim();
        if (orderNumber != null && orderRepository.findByOrderNumberAndMemberId(orderNumber, memberId).isEmpty()) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        Inquiry inquiry = Inquiry.create(member, r.category(), r.title(), r.content(), orderNumber);
        return InquiryResponse.from(inquiryRepository.saveAndFlush(inquiry));
    }

    @Transactional(readOnly = true)
    public PageResponse<InquiryResponse> getMine(Long memberId, int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 50), LATEST);
        return PageResponse.of(inquiryRepository.findByMemberId(memberId, pageable), InquiryResponse::from);
    }

    @Transactional(readOnly = true)
    public InquiryResponse getMine(Long memberId, Long id) {
        return InquiryResponse.from(findMine(memberId, id));
    }

    public void deleteMine(Long memberId, Long id) {
        Inquiry inquiry = findMine(memberId, id);
        if (inquiry.isAnswered()) {
            throw new BusinessException(ErrorCode.INQUIRY_ALREADY_ANSWERED);
        }
        inquiryRepository.delete(inquiry);
    }

    private Inquiry findMine(Long memberId, Long id) {
        return inquiryRepository.findByIdAndMemberId(id, memberId).orElseThrow(() -> new BusinessException(ErrorCode.INQUIRY_NOT_FOUND));
    }

    // ── 관리자 ───────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<AdminInquiryResponse> getAll(InquiryStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 100), LATEST);
        var result = status == null ? inquiryRepository.findAllBy(pageable) : inquiryRepository.findByStatus(status, pageable);
        return PageResponse.of(result, AdminInquiryResponse::from);
    }

    @Transactional(readOnly = true)
    public AdminInquiryResponse get(Long id) {
        return AdminInquiryResponse.from(findWithMember(id));
    }

    @Transactional(readOnly = true)
    public long countWaiting() {
        return inquiryRepository.countByStatus(InquiryStatus.WAITING);
    }

    public AdminInquiryResponse answer(Long id, String answer) {
        Inquiry inquiry = findWithMember(id);
        boolean first = inquiry.answer(answer);
        inquiryRepository.flush();
        if (first) {
            Member member = inquiry.getMember();
            mailService.send(member.getEmail(), "[돌돌] 문의하신 내용에 답변을 드렸어요", """
                    %s님, 안녕하세요. 돌돌입니다.

                    남겨 주신 문의 "%s"에 답변을 드렸어요. 아래에서 확인해 주세요.
                    %s/mypage/inquiries/%d

                    더 궁금한 점은 새 문의로 남겨 주세요.
                    """.formatted(member.getName(), inquiry.getTitle(), frontendUrl, inquiry.getId()));
        }
        return AdminInquiryResponse.from(inquiry);
    }

    private Inquiry findWithMember(Long id) {
        return inquiryRepository.findWithMemberById(id).orElseThrow(() -> new BusinessException(ErrorCode.INQUIRY_NOT_FOUND));
    }

}