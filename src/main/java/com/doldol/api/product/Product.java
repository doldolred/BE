package com.doldol.api.product;

import com.doldol.api.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 상품. 가격 · 재고는 옵션(10포 · 30포 · 60포)마다 있다 (V5) */
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

	private String imageUrl;

	@Column(nullable = false)
	private boolean active;

	/** 옵션은 상품과 함께 저장된다 (cascade). 지우지 않고 판매중지(active=false)로 둔다 — 지난 주문이 가리키므로 */
	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
	@OrderBy("sortOrder asc, id asc")
	private List<ProductOption> options = new ArrayList<>();

	//관리자 상품 등록
	public static Product create(String name, String description, String imageUrl, boolean active){
		Product product = new Product();
		product.update(name, description, imageUrl, active);
		return product;
	}
	public void update(String name, String description, String imageUrl, boolean active){
		this.name = name.trim();
		this.description = blankToNull(description);
		this.imageUrl = blankToNull(imageUrl);
		this.active = active;
	}
	public void changeActive(boolean active){this.active = active;}

	public ProductOption addOption(String name, int listPrice, int price, int stock, int sortOrder, boolean active) {
		ProductOption option = ProductOption.create(this, name, listPrice, price, stock, sortOrder, active);
		options.add(option);
		return option;
	}

	/** 손님에게 보이는 옵션 — 판매중인 것만 */
	public List<ProductOption> getActiveOptions() {
		return options.stream().filter(ProductOption::isActive).toList();
	}

	private static String blankToNull(String value){return value == null || value.isBlank() ? null : value.trim();
	}
}
