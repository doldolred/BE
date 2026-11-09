package com.doldol.api.address;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberAddressRepository extends JpaRepository<MemberAddress, Long> {

    List<MemberAddress> findByMemberIdOrderByDefaultAddressDescIdDesc(Long memberId);

    Optional<MemberAddress> findByIdAndMemberId(Long id, Long memberId);

    void deleteAllByMemberId(Long memberId);

}