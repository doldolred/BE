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
    /** 판매중 + 출시 예정, 라인업 순서대로 (비공개는 빼고) */
    public List<ProductResponse> getProducts(){
        return productRepository.findAllByStatusInOrderByLineupOrderAscIdAsc(ProductStatus.PUBLIC).stream().map(ProductResponse::from).toList();
    }
    public ProductResponse getProduct(Long id){
        Product product = productRepository.findById(id).filter(p -> p.getStatus().isPublic()).orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        return ProductResponse.from(product);
    }
}
