package com.realtimegroupbuy.rtgb.repository;

import com.realtimegroupbuy.rtgb.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
