package com.doldol.api.product;

import com.doldol.api.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품. 가격 · 재고는 옵션(10포 · 30포 · 60포)마다 있다 (V5).
 * 상태(V13): 판매중 · 출시 예정 · 비공개 — 메인 라인업 · 상세는 판매중 + 출시 예정을 보여 준다.
 * 라인업 항목(영문 이름 · 구성 · 스티커 문구 · 색 · 그림 · 파우치 도안)은 손님 화면 연출용.
 */
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

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ProductStatus status;

	/** status == SELLING 과 늘 같다. 장바구니 · 주문의 "살 수 있나"(ProductOption.isOnSale)가 이 값을 본다 */
	@Column(nullable = false)
	private boolean active;

	/** 영문 이름 — 상세 무대 뒤 큰 글자 (예: RED BEET) */
	@Column(length = 40)
	private String enName;

	/** 구성 — 예: 120ml × 30포 / 60포 */
	@Column(length = 100)
	private String spec;

	/** 스티커 문구 — 줄바꿈으로 나눠 저장 */
	private String points;

	/** 색 — NAVY · PINK · LIME (화면 색은 FE 가 정한다) */
	@Column(nullable = false, length = 20)
	private String theme;

	/** 그림 — POUCH(3D 파우치) · BOTTLE · JAR */
	@Column(nullable = false, length = 20)
	private String art;

	/** 파우치 도안 앞 · 뒤 (펼친 PNG). 없으면 기본 그림 */
	private String artFrontUrl;

	private String artBackUrl;

	/** 라인업 순서 — 작을수록 앞 (맨 앞 판매중 상품이 메인) */
	@Column(nullable = false)
	private int lineupOrder;

	/** 옵션은 상품과 함께 저장된다 (cascade). 지우지 않고 판매중지(active=false)로 둔다 — 지난 주문이 가리키므로 */
	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
	@OrderBy("sortOrder asc, id asc")
	private List<ProductOption> options = new ArrayList<>();

	//관리자 상품 등록
	public static Product create(String name, String description, String imageUrl, ProductStatus status){
		Product product = new Product();
		product.update(name, description, imageUrl, status);
		return product;
	}
	public void update(String name, String description, String imageUrl, ProductStatus status){
		this.name = name.trim();
		this.description = blankToNull(description);
		this.imageUrl = blankToNull(imageUrl);
		changeStatus(status);
	}
	public void changeStatus(ProductStatus status){
		this.status = status;
		this.active = status == ProductStatus.SELLING;
	}

	public void updateLineup(String enName, String spec, List<String> points, String theme, String art,
			String artFrontUrl, String artBackUrl, int lineupOrder) {
		this.enName = blankToNull(enName);
		this.spec = blankToNull(spec);
		this.points = points == null ? null : blankToNull(String.join("\n", points.stream().map(String::trim).filter(p -> !p.isEmpty()).toList()));
		this.theme = theme;
		this.art = art;
		this.artFrontUrl = blankToNull(artFrontUrl);
		this.artBackUrl = blankToNull(artBackUrl);
		this.lineupOrder = lineupOrder;
	}

	public List<String> getPointList() {
		return points == null ? List.of() : Arrays.stream(points.split("\n")).toList();
	}

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
