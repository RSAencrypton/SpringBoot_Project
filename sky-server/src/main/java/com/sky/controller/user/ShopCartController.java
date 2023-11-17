package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.idGenerator.IdGeneratorUtil;
import com.sky.result.Result;
import com.sky.service.ShopcartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shopcart")
@Slf4j
@Api("购物车管理")
public class ShopCartController {

    @Autowired
    private ShopcartService shopcartService;


    @PostMapping("/add")
    @ApiOperation("添加购物车")
    public Result AddCart(@RequestBody ShoppingCartDTO item) {
        shopcartService.AddCart(item);
        return Result.success();
    }

    @PostMapping("/addSpecial")
    @ApiOperation("添加特价商品到购物车")
    public Result AddSpecial(@RequestBody ShoppingCartDTO item) {
        Long res =  shopcartService.AddSpecial(item);
        return res != null ? Result.success(res) : Result.error("抢购失败");
    }

    @PostMapping("/removeOne/{Dishid}")
    @ApiOperation("减少一个商品数量")
    public Result RemoveOne(@PathVariable Long Dishid) {
        shopcartService.RemoveOne(Dishid);
        return Result.success();
    }

    @GetMapping("/list/{id}")
    @ApiOperation("购物车列表")
    public Result<List<ShoppingCart>> ListCart(@PathVariable() Long id) {
        return Result.success(shopcartService.ListCart(id));
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除购物车")
    public Result DeleteCart(@PathVariable() Long id) {
        shopcartService.DeleteCart(id);
        return Result.success();
    }
}
