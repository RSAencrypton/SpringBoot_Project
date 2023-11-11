package com.sky.controller.user;


import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("UserOrderController")
@RequestMapping("/user/order")
@Api(tags = "用户订单")
@Slf4j
public class OrderController {


    @Autowired
    private OrderService orderService;
    @PostMapping("/submit")
    @ApiOperation(value = "提交订单")
    public Result<OrderSubmitVO> SubmitOrder(@RequestBody OrdersSubmitDTO item){
        OrderSubmitVO orderSubmitVO = orderService.SubmitOrder(item);
        return Result.success(orderSubmitVO);
    }

    @GetMapping("/reminder/{orderId}")
    @ApiOperation(value = "催单")
    public Result reminder(@PathVariable Long orderId) {
        orderService.reminder(orderId);
        return null;
    }
}
