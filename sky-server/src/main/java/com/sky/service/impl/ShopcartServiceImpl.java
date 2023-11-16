package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.dto.SpecialDishDto;
import com.sky.entity.Dish;
import com.sky.entity.ShoppingCart;
import com.sky.entity.SpecialDish;
import com.sky.lock.RedisLock;
import com.sky.mapper.DishMapper;
import com.sky.mapper.ShopCartMapper;
import com.sky.service.ShopcartService;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Service
@Slf4j
public class ShopcartServiceImpl implements ShopcartService {

    @Autowired
    private ShopCartMapper shopCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redisson;

    @Autowired
    private ShopcartServiceImpl shopcartService;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("CheckSpecialDish.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

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

    @Transactional
    public void AddSpecialIntoCart(ShoppingCartDTO item) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(item, shoppingCart);
        shoppingCart.setUserId(TEST_USER_ID);


        Long dishId = item.getDishId();
        Dish dish = dishMapper.FindDishById(dishId);

        SpecialDish specialDish = new SpecialDish();
        specialDish.setId(dishId);
        specialDish = dishMapper.HasStock(specialDish);
        int pay = specialDish.getPayVal();


        shoppingCart.setName("特价" + dish.getName());
        shoppingCart.setAmount(BigDecimal.valueOf(pay));
        shoppingCart.setNumber(1);
        shoppingCart.setCreateTime(LocalDateTime.now());

        shopCartMapper.insertItem(shoppingCart);
        dishMapper.RemoveOneSpecialDish(dishId);
    }

    private BlockingQueue<ShoppingCartDTO> orderQueue = new ArrayBlockingQueue<>(1024 * 1024);
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @PostConstruct
    private void Init() {
        executorService.submit(new SpecialDishHandler());
    }

    private class SpecialDishHandler implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    ShoppingCartDTO item = orderQueue.take();

                    RLock lock =  redisson.getLock("order:" + TEST_USER_ID);
                    boolean res = lock.tryLock();

                    if (!res) {
                        return;
                    }

                    try {
                        shopcartService.AddSpecialIntoCart(item);
                    }finally {
                        lock.unlock();
                    }
                } catch (InterruptedException e) {
                    log.error("error", e);
                }
            }
        }
    }


    public boolean AddSpecial(ShoppingCartDTO item) {
//
//        if (specialDish == null) {
//            return false;
//        }
//
//
//        LocalDateTime begin = specialDish.getBeginTime();
//        LocalDateTime end = specialDish.getEndTime();
//        int stock = specialDish.getStock();
//
//
//        if (begin.isAfter(LocalDateTime.now()) || end.isBefore(LocalDateTime.now())) {
//            return false;
//        }
//
//        if (stock <= 0) {
//            return false;
//        }
//        RedisLock redisLock = new RedisLock("order:" + TEST_USER_ID, redisTemplate);

        Long result = (Long)redisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                item.getDishId().toString(),
                TEST_USER_ID.toString()
        );

        int trueRes = result.intValue();

        if (trueRes != 0) {
            return false;
        }

        orderQueue.add(item);

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
