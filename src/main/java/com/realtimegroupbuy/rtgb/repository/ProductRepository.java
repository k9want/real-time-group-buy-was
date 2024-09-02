package com.realtimegroupbuy.rtgb.repository;

import com.realtimegroupbuy.rtgb.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

}
