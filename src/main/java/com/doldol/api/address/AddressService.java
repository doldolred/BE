package com.doldol.api.address;

import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import com.doldol.api.member.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 배송지 관리 (V16) — 최대 10개, 기본 배송지는 항상 하나 (있으면) */
@Service
@RequiredArgsConstructor
@Transactional
public class AddressService {

    private static final int MAX = 10;
    private final MemberAddressRepository addressRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public List<AddressResponse> list(Long memberId) {
        return mine(memberId).stream().map(AddressResponse::from).toList();
    }

    /** 첫 배송지는 자동으로 기본 */
    public AddressResponse create(Long memberId, AddressRequest request) {
        List<MemberAddress> mine = mine(memberId);
        if (mine.size() >= MAX) {
            throw new BusinessException(ErrorCode.ADDRESS_LIMIT);
        }
        MemberAddress address = addressRepository.save(MemberAddress.create(memberRepository.getReferenceById(memberId), request));
        if (mine.isEmpty() || Boolean.TRUE.equals(request.makeDefault())) {
            makeDefault(mine, address);
        }
        return AddressResponse.from(address);
    }

    public AddressResponse update(Long memberId, Long id, AddressRequest request) {
        MemberAddress address = find(memberId, id);
        address.update(request);
        if (Boolean.TRUE.equals(request.makeDefault())) {
            makeDefault(mine(memberId), address);
        }
        return AddressResponse.from(address);
    }

    public AddressResponse makeDefault(Long memberId, Long id) {
        MemberAddress address = find(memberId, id);
        makeDefault(mine(memberId), address);
        return AddressResponse.from(address);
    }

    /** 기본 배송지를 지우면 가장 최근 배송지가 기본이 된다 */
    public void delete(Long memberId, Long id) {
        MemberAddress address = find(memberId, id);
        addressRepository.delete(address);
        if (address.isDefaultAddress()) {
            mine(memberId).stream().filter(a -> !a.getId().equals(id)).findFirst().ifPresent(a -> a.markDefault(true));
        }
    }

    private void makeDefault(List<MemberAddress> mine, MemberAddress target) {
        mine.forEach(a -> a.markDefault(false));
        target.markDefault(true);
    }

    private List<MemberAddress> mine(Long memberId) {
        return addressRepository.findByMemberIdOrderByDefaultAddressDescIdDesc(memberId);
    }

    private MemberAddress find(Long memberId, Long id) {
        return addressRepository.findByIdAndMemberId(id, memberId).orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND));
    }

}