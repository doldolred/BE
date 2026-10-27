package com.doldol.api.banner;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class BannerController {
    private final BannerRepository bannerRepository;
    @GetMapping
    @Transactional(readOnly = true)
    public List<BannerResponse> showing(){
        return bannerRepository.findShowing(Instant.now()).stream().map(BannerResponse::from).toList();
    }
}
