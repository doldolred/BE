package com.doldol.api.admin.product;

import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import com.doldol.api.product.Product;
import com.doldol.api.product.ProductOption;
import com.doldol.api.product.ProductRepository;
import com.doldol.api.product.ProductStatus;
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
    /** 라인업 순서대로 (손님 화면과 같은 순서) */
    public List<AdminProductResponse> getProducts(){return productRepository.findAll(Sort.by("lineupOrder", "id")).stream().map(AdminProductResponse::from).toList();}
    public AdminProductResponse getProduct(Long id){return AdminProductResponse.from(find(id));}
    @Transactional
    public AdminProductResponse create(AdminProductRequest r){
        Product product = Product.create(r.name(), r.description(), r.imageUrl(), r.status());
        updateLineup(product, r);
        int order = 1;
        for (AdminProductRequest.OptionRequest o : r.optionsOrEmpty()) {
            if (o.id() != null) throw new BusinessException(ErrorCode.INVALID_INPUT, "새 상품의 옵션에는 id 를 넣지 않습니다.");
            product.addOption(o.name(), o.listPrice(), o.price(), o.stock(), order++, o.active());
        }
        requireSellable(product);
        return AdminProductResponse.from(productRepository.save(product));
    }
    /** 옵션: id 가 있으면 수정, 없으면 추가, 요청에 없는 기존 옵션은 판매중지 (지난 주문이 가리키므로 지우지 않는다). 순서는 요청 순서 */
    @Transactional
    public AdminProductResponse update(Long id, AdminProductRequest r){
        Product product = find(id);
        product.update(r.name(), r.description(), r.imageUrl(), r.status());
        updateLineup(product, r);
        Map<Long, ProductOption> existing = product.getOptions().stream().collect(Collectors.toMap(ProductOption::getId, Function.identity()));
        Set<Long> kept = new HashSet<>();
        int order = 1;
        for (AdminProductRequest.OptionRequest o : r.optionsOrEmpty()) {
            if (o.id() == null) {
                product.addOption(o.name(), o.listPrice(), o.price(), o.stock(), order++, o.active());
                continue;
            }
            ProductOption option = existing.get(o.id());
            if (option == null || !kept.add(o.id())) throw new BusinessException(ErrorCode.INVALID_INPUT, "이 상품의 옵션이 아닙니다: " + o.id());
            option.update(o.name(), o.listPrice(), o.price(), o.stock(), order++, o.active());
        }
        existing.values().stream().filter(o -> !kept.contains(o.getId())).forEach(ProductOption::deactivate);
        requireSellable(product);
        productRepository.flush(); // 새 옵션 id · 수정 시각을 응답에 담으려고
        return AdminProductResponse.from(product);
    }
    /** 목록에서 바로 판매중 · 출시 예정 · 비공개 바꾸기 */
    @Transactional
    public AdminProductResponse changeStatus(Long id, ProductStatus status){
        Product product= find(id);
        product.changeStatus(status);
        requireSellable(product);
        return AdminProductResponse.from(product);
    }
    private void updateLineup(Product product, AdminProductRequest r) {
        product.updateLineup(r.enName(), r.spec(), r.points(), r.theme(), r.art(), r.artFrontUrl(), r.artBackUrl(), r.lineupOrder());
    }
    /** 판매중인데 살 수 있는 옵션이 없으면 손님이 상세에서 아무것도 못 고른다 */
    private void requireSellable(Product product) {
        if (product.getStatus() == ProductStatus.SELLING && product.getActiveOptions().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "판매중으로 두려면 판매할 옵션이 하나 이상 있어야 합니다.");
        }
    }
    private Product find(Long id){return productRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}
