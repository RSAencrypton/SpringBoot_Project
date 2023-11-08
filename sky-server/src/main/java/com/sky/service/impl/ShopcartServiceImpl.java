package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.ShopCartMapper;
import com.sky.service.ShopcartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
@Slf4j
public class ShopcartServiceImpl implements ShopcartService {

    @Autowired
    private ShopCartMapper shopCartMapper;

    @Autowired
    private DishMapper dishMapper;
    public void AddCart(ShoppingCartDTO item) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(item, shoppingCart);
        shoppingCart.setUserId(4L);

        List<ShoppingCart> list = shopCartMapper.list(shoppingCart);

        if (list != null && list.size() > 0) {
            shoppingCart = list.get(0);
            shoppingCart.setNumber(shoppingCart.getNumber() + 1);
            shopCartMapper.updateItem(shoppingCart);
        }else {
            Long dishId = item.getDishId();
            Dish dish = dishMapper.FindDishById(dishId);
            shoppingCart.setName(dish.getName());
            shoppingCart.setAmount(dish.getPrice());

            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());

            shopCartMapper.insertItem(shoppingCart);
        }
    }
}
