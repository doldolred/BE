package com.doldol.api.admin.product;

import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import com.doldol.api.product.Product;
import com.doldol.api.product.ProductOption;
import com.doldol.api.product.ProductRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminProductService {
    private final ProductRepository productRepository;
    public List<AdminProductResponse> getProducts(){return productRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream().map(AdminProductResponse::from).toList();}
    public AdminProductResponse getProduct(Long id){return AdminProductResponse.from(find(id));}
    @Transactional
    public AdminProductResponse create(AdminProductRequest r){
        Product product = Product.create(r.name(), r.description(), r.imageUrl(), r.active());
        int order = 1;
        for (AdminProductRequest.OptionRequest o : r.options()) {
            if (o.id() != null) throw new BusinessException(ErrorCode.INVALID_INPUT, "새 상품의 옵션에는 id 를 넣지 않습니다.");
            product.addOption(o.name(), o.listPrice(), o.price(), o.stock(), order++, o.active());
        }
        return AdminProductResponse.from(productRepository.save(product));
    }
    /** 옵션: id 가 있으면 수정, 없으면 추가, 요청에 없는 기존 옵션은 판매중지 (지난 주문이 가리키므로 지우지 않는다). 순서는 요청 순서 */
    @Transactional
    public AdminProductResponse update(Long id, AdminProductRequest r){
        Product product = find(id);
        product.update(r.name(), r.description(), r.imageUrl(), r.active());
        Map<Long, ProductOption> existing = product.getOptions().stream().collect(Collectors.toMap(ProductOption::getId, Function.identity()));
        Set<Long> kept = new HashSet<>();
        int order = 1;
        for (AdminProductRequest.OptionRequest o : r.options()) {
            if (o.id() == null) {
                product.addOption(o.name(), o.listPrice(), o.price(), o.stock(), order++, o.active());
                continue;
            }
            ProductOption option = existing.get(o.id());
            if (option == null || !kept.add(o.id())) throw new BusinessException(ErrorCode.INVALID_INPUT, "이 상품의 옵션이 아닙니다: " + o.id());
            option.update(o.name(), o.listPrice(), o.price(), o.stock(), order++, o.active());
        }
        existing.values().stream().filter(o -> !kept.contains(o.getId())).forEach(ProductOption::deactivate);
        productRepository.flush(); // 새 옵션 id · 수정 시각을 응답에 담으려고
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
