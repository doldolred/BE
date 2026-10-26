package com.doldol.api.order;

import com.doldol.api.product.Product;
import com.doldol.api.product.ProductOption;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    private ProductOption option;
    /** 주문 당시 이름 · 가격을 그대로 남긴다 (나중에 상품을 고쳐도 지난 주문은 바뀌지 않게) */
    @Column(nullable = false, length = 100)
    private String productName;
    @Column(nullable = false, length = 50)
    private String optionName;
    @Column(nullable = false)
    private int unitPrice;
    @Column(nullable = false)
    private int quantity;
    public static OrderItem create(ProductOption option, int quantity) {
        OrderItem item = new OrderItem();
        item.product = option.getProduct();
        item.option = option;
        item.productName = option.getProduct().getName();
        item.optionName = option.getName();
        item.unitPrice = option.getPrice();
        item.quantity = quantity;
        return item;
    }
    void assignOrder(Order order) {this.order = order;}
    public int getLineTotal() {return unitPrice * quantity;}
    /** 목록 요약용: "돌돌 레드비트즙 30포" */
    public String getDisplayName() {return productName + " " + optionName;}
}
