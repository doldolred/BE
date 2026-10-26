package com.doldol.api.product;

import com.doldol.api.common.BaseEntity;
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

/** 상품 옵션 (예: 30포). 장바구니 · 주문은 옵션을 담는다 */
@Entity
@Table(name = "product_option")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 50)
    private String name;

    /** 정상가 (할인 전) */
    @Column(nullable = false)
    private int listPrice;

    /** 판매가 */
    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int stock;

    @Column(nullable = false)
    private int sortOrder;

    @Column(nullable = false)
    private boolean active;

    static ProductOption create(Product product, String name, int listPrice, int price, int stock, int sortOrder, boolean active) {
        ProductOption option = new ProductOption();
        option.product = product;
        option.update(name, listPrice, price, stock, sortOrder, active);
        return option;
    }

    public void update(String name, int listPrice, int price, int stock, int sortOrder, boolean active) {
        this.name = name.trim();
        this.listPrice = listPrice;
        this.price = price;
        this.stock = stock;
        this.sortOrder = sortOrder;
        this.active = active;
    }

    public void deactivate() {this.active = false;}

    /** 장바구니 · 주문에 담을 수 있는가 — 옵션과 상품 둘 다 판매중이어야 한다 */
    public boolean isOnSale() {return active && product.isActive();}

    public boolean isSoldOut() {return stock <= 0;}

    /** 정상가 대비 할인율(%) — 정수로 내림. 할인이 없으면 0 */
    public int getDiscountRate() {
        return listPrice <= 0 || price >= listPrice ? 0 : (listPrice - price) * 100 / listPrice;
    }

}
