package com.doldol.api.product;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductRepository productRepository;

	@GetMapping
	public List<ProductResponse> list() {
		return productRepository.findAllByActiveTrueOrderByIdAsc().stream()
				.map(ProductResponse::from)
				.toList();
	}

	public record ProductResponse(Long id, String name, String description, int price, int stock, String imageUrl) {

		static ProductResponse from(Product p) {
			return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getStock(),
					p.getImageUrl());
		}

	}

}
