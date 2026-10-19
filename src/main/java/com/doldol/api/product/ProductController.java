package com.doldol.api.product;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
	private final ProductService productService;
	@GetMapping
	public List<ProductResponse> list() {return productService.getProducts();}
	@GetMapping("/{id}")
	public ProductResponse detail(@PathVariable Long id) {return productService.getProduct(id);}

}