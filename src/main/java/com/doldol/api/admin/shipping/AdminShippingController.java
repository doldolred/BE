package com.doldol.api.admin.shipping;

import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import com.doldol.api.shipping.ShippingArea;
import com.doldol.api.shipping.ShippingAreaRepository;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 추가 배송비 지역 (제주 · 도서산간) — 우편번호 구간으로 더하고 뺀다. 일이 단순해서 서비스 없이 리포지토리를 바로 쓴다 */
@RestController
@RequestMapping("/api/admin/shipping/areas")
@RequiredArgsConstructor
public class AdminShippingController {

    private final ShippingAreaRepository shippingAreaRepository;

    @GetMapping
    public List<ShippingAreaResponse> list() {
        return shippingAreaRepository.findAllByOrderByZipFromAsc().stream().map(ShippingAreaResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ShippingAreaResponse create(@Valid @RequestBody ShippingAreaRequest r) {
        if (r.zipFrom().compareTo(r.zipTo()) > 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "시작 우편번호가 끝 우편번호보다 클 수 없습니다.");
        }
        return ShippingAreaResponse.from(shippingAreaRepository.save(ShippingArea.create(r.name(), r.zipFrom(), r.zipTo(), r.extraFee())));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void delete(@PathVariable Long id) {
        ShippingArea area = shippingAreaRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        shippingAreaRepository.delete(area);
    }

}
