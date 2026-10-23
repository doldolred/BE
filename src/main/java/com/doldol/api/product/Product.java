package com.doldol.api.product;

import com.doldol.api.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;

	private String description;

	@Column(nullable = false)
	private Integer price;

	@Column(nullable = false)
	private Integer stock;

	private String imageUrl;

	@Column(nullable = false)
	private boolean active;

	//관리자 상품 등록
	public static Product create(String name, String description, int price, int stock, String imageUrl, boolean active){
		Product product = new Product();
		product.update(name, description, price, stock, imageUrl, active);
		return product;
	}
	public void update(String name, String description, int price, int stock, String imageUrl, boolean active){
		this.name = name.trim();
		this.description = blankToNull(description);
		this.price = price;
		this.stock = stock;
		this.imageUrl = blankToNull(imageUrl);
		this.active = active;
	}
	public void changeActive(boolean active){this.active = active;}
	private static String blankToNull(String value){return value == null || value.isBlank() ? null : value.trim();
	}
}