package com.doldol.api.notice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    Page<Notice> findByPublishedTrue(Pageable pageable);
    Optional<Notice> findByIdAndPublishedTrue(Long id);
}
