package com.doldol.api.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

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

	@Column(nullable = false, updatable = false, insertable = false)
	private OffsetDateTime createdAt;

	@Column(nullable = false, insertable = false)
	private OffsetDateTime updatedAt;

}
