package com.doldol.api.cart;
import com.doldol.api.common.BaseEntity;
import com.doldol.api.member.Member;
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
@Table(name = "cart_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    /** 담은 옵션 (예: 30포). 상품은 option.getProduct() */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    private ProductOption option;
    @Column(nullable = false)
    private int quantity;
    public static CartItem create(Member member, ProductOption option, int quantity){
        CartItem item = new CartItem();
        item.member = member;
        item.option = option;
        item.quantity = quantity;
        return item;
    }
    public void addQuantity(int amount){
        this.quantity += amount;
    }
    public void changeQuantity(int quantity){
        this.quantity = quantity;
    }
    public int getLineTotal(){
        return option.getPrice() * quantity;
    }
}
