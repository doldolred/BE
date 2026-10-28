package com.doldol.api.review;

import com.doldol.api.admin.review.AdminReviewUpdateRequest;
import com.doldol.api.common.PageResponse;
import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import com.doldol.api.member.Member;
import com.doldol.api.member.MemberRepository;
import com.doldol.api.order.OrderItem;
import com.doldol.api.order.OrderStatus;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 상품 후기 — 손님(목록 · 요약), 회원(쓰기 · 내 것 · 삭제), 관리자(숨김 · 베스트 · 답글) */
@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

    private static final Sort LATEST = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

    private final ReviewRepository reviewRepository;

    private final MemberRepository memberRepository;

    private final EntityManager entityManager;

    // ── 손님 ─────────────────────────────

    /** sort: latest(기본) · best · rating_high · rating_low */
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getProductReviews(Long productId, String sort, boolean photoOnly, int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 50), sortOf(sort));
        return PageResponse.of(reviewRepository.findVisible(productId, photoOnly, pageable), ReviewResponse::forPublic);
    }

    @Transactional(readOnly = true)
    public ReviewSummaryResponse getSummary(Long productId) {
        return ReviewSummaryResponse.of(reviewRepository.countByRating(productId));
    }

    private static Sort sortOf(String sort) {
        return switch (sort == null ? "" : sort) {
            case "best" -> Sort.by(Sort.Order.desc("best")).and(LATEST);
            case "rating_high" -> Sort.by(Sort.Order.desc("rating")).and(LATEST);
            case "rating_low" -> Sort.by(Sort.Order.asc("rating")).and(LATEST);
            default -> LATEST;
        };
    }

    // ── 회원 ─────────────────────────────

    @Transactional(readOnly = true)
    public List<WritableItemResponse> getWritable(Long memberId) {
        return reviewRepository.findWritableItems(memberId).stream().map(WritableItemResponse::from).toList();
    }

    /** 배송 완료된 내 주문 상품에만, 하나에 한 번 */
    public ReviewResponse create(Long memberId, CreateReviewRequest r) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        OrderItem item = entityManager.find(OrderItem.class, r.orderItemId());
        if (item == null || !item.getOrder().getMember().getId().equals(memberId) || item.getOrder().getStatus() != OrderStatus.DELIVERED) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_WRITABLE);
        }
        if (reviewRepository.existsByOrderItemId(item.getId())) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_WRITTEN);
        }
        List<String> images = r.imageUrls() == null ? List.of() : r.imageUrls();
        try {
            Review review = reviewRepository.saveAndFlush(Review.create(member, item, r.rating(), r.content(), images));
            return ReviewResponse.forPublic(review);
        }
        catch (DataIntegrityViolationException e) {
            // 같은 주문 상품에 동시에 두 번 눌렀을 때 (order_item_id UNIQUE)
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_WRITTEN);
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getMine(Long memberId, int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 50), LATEST);
        return PageResponse.of(reviewRepository.findByMemberId(memberId, pageable), ReviewResponse::forPublic);
    }

    /** 내 후기 지우기 — 지우면 그 주문 상품에 다시 쓸 수 있다 */
    public void deleteMine(Long memberId, Long id) {
        Review review = reviewRepository.findByIdAndMemberId(id, memberId).orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
        reviewRepository.delete(review);
    }

    // ── 관리자 ───────────────────────────

    /** filter: ALL(기본) · HIDDEN · BEST · NO_REPLY */
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> search(String filter, int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 100), LATEST);
        String f = filter == null ? "" : filter;
        Boolean hidden = f.equals("HIDDEN") ? Boolean.TRUE : null;
        Boolean best = f.equals("BEST") ? Boolean.TRUE : null;
        boolean noReply = f.equals("NO_REPLY");
        return PageResponse.of(reviewRepository.search(hidden, best, noReply, pageable), ReviewResponse::forAdmin);
    }

    /** 숨김 · 베스트 · 답글 — 보낸 것만 바꾼다 (reply 가 빈 글이면 답글 삭제) */
    public ReviewResponse update(Long id, AdminReviewUpdateRequest r) {
        Review review = reviewRepository.findWithMemberById(id).orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
        if (r.hidden() != null) {
            review.changeHidden(r.hidden());
        }
        if (r.best() != null) {
            review.changeBest(r.best());
        }
        if (r.reply() != null) {
            review.reply(r.reply());
        }
        reviewRepository.flush();
        return ReviewResponse.forAdmin(review);
    }

}