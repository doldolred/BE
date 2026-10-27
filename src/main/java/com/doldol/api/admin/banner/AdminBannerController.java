package com.doldol.api.admin.banner;

import com.doldol.api.banner.Banner;
import com.doldol.api.banner.BannerRepository;
import com.doldol.api.banner.BannerResponse;
import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/banners")
@RequiredArgsConstructor
public class AdminBannerController {
    private final BannerRepository bannerRepository;
    @GetMapping
    @Transactional(readOnly = true)
    public List<BannerResponse> list(){
        return bannerRepository.findAllByOrderBySortOrderAscIdAsc().stream().map(BannerResponse::from).toList();
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public BannerResponse create(@Valid @RequestBody BannerRequest r){
        checkPeriod(r);
        Banner banner = Banner.create(r.message(), r.linkUrl(), r.theme(), r.startsAt(), r.endsAt(), r.active(), r.sortOrder());
        return BannerResponse.from(bannerRepository.saveAndFlush(banner));
    }
    @PutMapping("/{id}")
    @Transactional
    public BannerResponse update(@PathVariable Long id, @Valid @RequestBody BannerRequest r){
        checkPeriod(r);
        Banner banner = find(id);
        banner.update(r.message(), r.linkUrl(), r.theme(), r.startsAt(), r.endsAt(), r.active(), r.sortOrder());
        return BannerResponse.from(bannerRepository.saveAndFlush(banner));
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void delete(@PathVariable Long id){bannerRepository.delete(find(id));
    }
    private void checkPeriod(BannerRequest r){
        if (r.startsAt() != null && r.endsAt() != null && !r.endsAt().isAfter(r.startsAt())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "끝나는 때가 시작보다 뒤여야 해요.");
        }
    }
    private Banner find(Long id){
        return bannerRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.BANNER_NOT_FOUND));
    }
}
