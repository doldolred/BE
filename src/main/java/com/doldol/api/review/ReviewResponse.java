package com.doldol.api.review;

import java.time.Instant;
import java.util.List;

public record ReviewResponse(Long id, Long productId, String productName, String optionName, int rating, String content, List<String> imageUrls, String author, boolean best, boolean hidden, String reply, Instant repliedAt, Instant createdAt, Long memberId, String memberEmail) {
    public static ReviewResponse forPublic(Review r) {
        return of(r, mask(r.getMember().getName()), null, null);
    }

    public static ReviewResponse forAdmin(Review r) {
        return of(r, r.getMember().getName(), r.getMember().getId(), r.getMember().getEmail());
    }
    private static ReviewResponse of(Review r, String author, Long memberId, String memberEmail) {
        return new ReviewResponse(r.getId(), r.getProduct().getId(), r.getProduct().getName(), r.getOptionName(), r.getRating(), r.getContent(), List.copyOf(r.getImageUrls()), author, r.isBest(), r.isHidden(), r.getReply(), r.getRepliedAt(), r.getCreatedAt(), memberId, memberEmail);
    }
    static String mask(String name) {
        String n = name == null ? "" : name.strip();
        if (n.length() <= 1) {
            return n + "*";
        }
        if (n.length() == 2) {
            return n.charAt(0) + "*";
        }
        return n.charAt(0) + "*".repeat(n.length() - 2) + n.charAt(n.length() - 1);
    }

}