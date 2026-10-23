package com.doldol.api.admin.sales;

import com.doldol.api.admin.AdminPeriod;
import com.doldol.api.order.OrderStatus;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminSalesService {

    private static final int TOP_PRODUCTS = 5;
    private final AdminSalesRepository adminSalesRepository;
    public SalesSummaryResponse summary(LocalDate from, LocalDate to) {
        AdminPeriod period = AdminPeriod.of(from, to);
        var start = period.start();
        var end = period.endExclusive();
        SalesTotal sales = adminSalesRepository.total(OrderStatus.SALES, start, end);
        SalesTotal cancelled = adminSalesRepository.total(Set.of(OrderStatus.CANCELLED), start, end);
        SalesTotal pending = adminSalesRepository.total(Set.of(OrderStatus.PENDING_PAYMENT), start, end);
        long average = sales.count() == 0 ? 0 : sales.amount() / sales.count();
        List<String> saleStatuses = OrderStatus.SALES.stream().map(Enum::name).toList();
        Map<String, DailySalesRow> byDay = adminSalesRepository.daily(saleStatuses, start, end).stream().collect(Collectors.toMap(DailySalesRow::getDay, r -> r));
        List<SalesSummaryResponse.Daily> daily = new ArrayList<>();
        for (LocalDate d = period.from(); !d.isAfter(period.to()); d = d.plusDays(1)) {
            DailySalesRow row = byDay.get(d.toString());
            daily.add(new SalesSummaryResponse.Daily(d, row == null ? 0 : row.getSales(), row == null ? 0 : row.getOrders()));
        }
        List<SalesSummaryResponse.TopProduct> top = adminSalesRepository.topProducts(OrderStatus.SALES, start, end, PageRequest.of(0, TOP_PRODUCTS)).stream().map(r -> new SalesSummaryResponse.TopProduct(r.productId(), r.productName(), r.quantity(), r.sales())).toList();
        return new SalesSummaryResponse(period.from(), period.to(), sales.amount(), sales.count(), average, cancelled.count(), cancelled.amount(), pending.count(), daily, top);
    }

}