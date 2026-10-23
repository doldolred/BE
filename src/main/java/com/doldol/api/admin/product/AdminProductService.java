package com.doldol.api.admin.product;

import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import com.doldol.api.product.Product;
import com.doldol.api.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminProductService {
    private final ProductRepository productRepository;
    public List<AdminProductResponse> getProducts(){return productRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream().map(AdminProductResponse::from).toList();}
    public AdminProductResponse getProduct(Long id){return AdminProductResponse.from(find(id));}
    @Transactional
    public AdminProductResponse create(AdminProductRequest r){
        Product product = Product.create(r.name(), r.description(), r.price(), r.stock(), r.imageUrl(), r.active());
        return AdminProductResponse.from(productRepository.save(product));
    }
    @Transactional
    public AdminProductResponse update(Long id, AdminProductRequest r){
        Product product = find(id);
        product.update(r.name(), r.description(), r.price(), r.stock(), r.imageUrl(), r.active());
        return AdminProductResponse.from(product);
    }
    @Transactional
    public AdminProductResponse changeActive(Long id, boolean active){
        Product product= find(id);
        product.changeActive(active);
        return AdminProductResponse.from(product);
    }
    private Product find(Long id){return productRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}
