package com.doldol.api.admin.member;

import com.doldol.api.member.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminMemberRepository extends JpaRepository<Member, Long> {

    Page<Member> findByEmailContainingIgnoreCaseOrNameContainingIgnoreCase(String email, String name,
                                                                           Pageable pageable);

}