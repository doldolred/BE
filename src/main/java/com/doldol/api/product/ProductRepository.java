package com.doldol.api.product;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

	/** 옵션은 default_batch_fetch_size 로 한 번에 같이 읽힌다 */
	List<Product> findAllByActiveTrueOrderByIdAsc();

}
