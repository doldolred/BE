package com.doldol.api.inquiry;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    Page<Inquiry> findByMemberId(Long memberId, Pageable pageable);
    Optional<Inquiry> findByIdAndMemberId(Long id, Long memberId);
    @EntityGraph(attributePaths = "member")
    Page<Inquiry> findAllBy(Pageable pageable);
    @EntityGraph(attributePaths = "member")
    Page<Inquiry> findByStatus(InquiryStatus status, Pageable pageable);
    @EntityGraph(attributePaths = "member")
    Optional<Inquiry> findWithMemberById(Long id);
    long countByStatus(InquiryStatus status);
}
