package com.doldol.api.product;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

	List<Product> findAllByActiveTrueOrderByIdAsc();
	@Modifying
	@Query("update Product p set p.stock = p.stock - :quantity where p.id = :id and p.stock >= :quantity")
	int decreaseStock(@Param("id") Long id, @Param("quantity") int quantity);
	@Modifying
	@Query("update Product p set p.stock = p.stock + :quantity where p.id = :id")
	int increaseStock(@Param("id") Long id, @Param("quantity") int quantity);

}