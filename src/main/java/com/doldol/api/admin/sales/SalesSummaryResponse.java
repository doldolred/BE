package com.doldol.api.admin.sales;

import java.time.LocalDate;
import java.util.List;

public record SalesSummaryResponse(LocalDate from, LocalDate to, long totalSales, long orderCount, long averageOrderValue, long cancelledCount, long cancelledAmount, long pendingCount, List<Daily> daily, List<TopProduct> topProducts) {
    public record Daily(LocalDate date, long sales, long orders) { }
    public record TopProduct(Long productId, String productName, long quantity, long sales) { }

}