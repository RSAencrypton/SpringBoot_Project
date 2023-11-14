package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.dto.SpecialDishDto;
import com.sky.entity.Dish;
import com.sky.entity.ShoppingCart;
import com.sky.entity.SpecialDish;
import com.sky.mapper.DishMapper;
import com.sky.mapper.ShopCartMapper;
import com.sky.service.ShopcartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Service
@Slf4j
public class ShopcartServiceImpl implements ShopcartService {

    @Autowired
    private ShopCartMapper shopCartMapper;

    @Autowired
    private DishMapper dishMapper;

    private static final Long TEST_USER_ID = 4L;
    public void AddCart(ShoppingCartDTO item) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(item, shoppingCart);
        shoppingCart.setUserId(TEST_USER_ID);

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

    public void AddSpecialIntoCart(ShoppingCartDTO item, int pay) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(item, shoppingCart);
        shoppingCart.setUserId(TEST_USER_ID);


        Long dishId = item.getDishId();
        Dish dish = dishMapper.FindDishById(dishId);
        shoppingCart.setName("特价" + dish.getName());
        shoppingCart.setAmount(BigDecimal.valueOf(pay));

        shoppingCart.setNumber(1);
        shoppingCart.setCreateTime(LocalDateTime.now());

        shopCartMapper.insertItem(shoppingCart);
    }

    @Transactional
    public boolean AddSpecial(ShoppingCartDTO item) {
        Long dishId = item.getDishId() == null ? item.getSetmealId() : item.getDishId();
        SpecialDish specialDish = new SpecialDish();
        specialDish.setId(dishId);
        specialDish = dishMapper.HasStock(specialDish);

        if (specialDish == null) {
            return false;
        }

        System.out.println(specialDish.getBeginTime());
        System.out.println(specialDish.getEndTime());
        System.out.println(specialDish.getStock());
        System.out.println(specialDish.getPayVal());

        LocalDateTime begin = specialDish.getBeginTime();
        LocalDateTime end = specialDish.getEndTime();
        int stock = specialDish.getStock();
        int pay = specialDish.getPayVal();


        if (begin.isAfter(LocalDateTime.now()) || end.isBefore(LocalDateTime.now())) {
            return false;
        }

        if (stock <= 0) {
            return false;
        }

        dishMapper.RemoveOneSpecialDish(dishId);
        AddSpecialIntoCart(item, pay);

        return true;
    }

    public void RemoveOne(Long id) {
        //获取商品
        ShoppingCart item = shopCartMapper.GetItemById(id);
        //判断商品数量是否为0
        int after = item.getNumber() - 1;
        if (after == 0) {
            //0则删除商品
            shopCartMapper.RemoveOne(item);
        }else {
            //不是则只修改数量
            item.setNumber(after);
            shopCartMapper.updateItem(item);
        }

    }

    public List<ShoppingCart> ListCart(Long id){
        List<ShoppingCart> list = shopCartMapper.ListCart(id);
        return list;
    }

    public void DeleteCart(Long id) {
        shopCartMapper.RemoveAll(id);
    }
}
