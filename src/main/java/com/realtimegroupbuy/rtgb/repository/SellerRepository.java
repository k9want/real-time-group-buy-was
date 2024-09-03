package com.realtimegroupbuy.rtgb.repository;

import com.realtimegroupbuy.rtgb.model.Seller;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerRepository extends JpaRepository<Seller, Long> {

    Optional<Seller> findByUsername(String username);

}