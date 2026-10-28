package com.doldol.api.review;

import java.util.List;

public record ReviewSummaryResponse(long total, double average, List<Long> counts) {
    public static ReviewSummaryResponse of(List<RatingCount> rows){
        long[] byRating = new long[6];
        for(RatingCount row : rows){
            byRating[row.rating()] = row.count();
        }
        long total = 0;
        long sum = 0;
        for(int rating = 1; rating <= 5; rating++){
            total += byRating[rating];
            sum += byRating[rating] * rating;
        }
        double average = total == 0 ? 0 : Math.round(sum * 10.0 / total) / 10.0;
        return new ReviewSummaryResponse(total, average, List.of(byRating[5], byRating[4],byRating[3],byRating[2],byRating[1] ));
    }
}
