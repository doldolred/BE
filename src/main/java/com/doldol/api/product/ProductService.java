package com.doldol.api.product;

import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;
    public List<ProductResponse> getProducts(){
        return productRepository.findAllByActiveTrueOrderByIdAsc().stream().map(ProductResponse::from).toList();
    }
    public ProductResponse getProduct(Long id){
        Product product = productRepository.findById(id).filter(Product::isActive).orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        return ProductResponse.from(product);
    }
}
