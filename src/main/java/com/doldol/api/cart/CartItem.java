package com.doldol.api.cart;
import com.doldol.api.common.BaseEntity;
import com.doldol.api.member.Member;
import com.doldol.api.product.Product;
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    @Column(nullable = false)
    private int quantity;
    public static CartItem create(Member member, Product product, int quantity){
        CartItem item = new CartItem();
        item.member = member;
        item.product = product;
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
        return product.getPrice() * quantity;
    }
}
