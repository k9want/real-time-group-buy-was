package com.realtimegroupbuy.rtgb.repository;

import com.realtimegroupbuy.rtgb.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p JOIN FETCH p.seller")
    Page<Product> findAllWithSeller(Pageable pageable);
}
