package com.doldol.api.admin.order;

import com.doldol.api.common.PageResponse;
import com.doldol.api.order.OrderStatus;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;
    @GetMapping
    public PageResponse<AdminOrderSummaryResponse> list(@RequestParam(required = false) OrderStatus status, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to, @RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {return adminOrderService.search(status, from, to, keyword, page, size);}
    @GetMapping("/{orderNumber}")
    public AdminOrderDetailResponse detail(@PathVariable String orderNumber) {return adminOrderService.getOrder(orderNumber);}
    @PatchMapping("/{orderNumber}/status")
    public AdminOrderDetailResponse changeStatus(@PathVariable String orderNumber, @Valid @RequestBody ChangeOrderStatusRequest request) {return adminOrderService.changeStatus(orderNumber, request);}

}