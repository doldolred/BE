package com.doldol.api.product;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

	/** 라인업 순서대로. 옵션은 default_batch_fetch_size 로 한 번에 같이 읽힌다 */
	List<Product> findAllByStatusInOrderByLineupOrderAscIdAsc(Collection<ProductStatus> statuses);

}
