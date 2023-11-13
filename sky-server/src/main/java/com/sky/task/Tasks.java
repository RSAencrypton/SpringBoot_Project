package com.sky.task;


import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class Tasks {

    @Autowired
    private OrderMapper orderMapper;

//    @Scheduled(cron = "0 * * * * ?")
//    public void ExcuteTask() {
//        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
//        List<Orders> orders = orderMapper.listOvertimeOrder(Orders.PENDING_PAYMENT, time);
//
//        if (orders != null && orders.size() > 0) {
//            for (Orders order : orders) {
//                order.setStatus(Orders.CANCELLED);
//                order.setCancelReason("超时未支付");
//                order.setCancelTime(LocalDateTime.now());
//                orderMapper.insert(order);
//            }
//        }
//    }
//
//    @Scheduled(cron = "0 0 1 * * ?")
//    public void ExcuteDeliveryOrder() {
//        LocalDateTime time = LocalDateTime.now().minusHours(1);
//        List<Orders> orders = orderMapper.listOvertimeOrder(Orders.DELIVERY_IN_PROGRESS, time);
//
//        if (orders != null && orders.size() > 0) {
//            for (Orders order : orders) {
//                order.setStatus(Orders.COMPLETED);
//                orderMapper.insert(order);
//            }
//        }
//    }
}
