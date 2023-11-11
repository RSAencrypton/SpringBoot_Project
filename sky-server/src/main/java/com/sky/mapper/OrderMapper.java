package com.sky.mapper;

import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.core.annotation.Order;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

    void insert(Orders orders);

    @Select("select * from orders where status = #{status}} and order_time < #{time}")
    List<Orders> listOvertimeOrder(Integer status, LocalDateTime time);

    @Select("select * from orders where number = #{orderId}")
    Orders getById(Long orderId);
}
