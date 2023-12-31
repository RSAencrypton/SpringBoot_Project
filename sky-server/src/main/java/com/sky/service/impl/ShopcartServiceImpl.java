package com.sky.service.impl;

import com.fasterxml.jackson.databind.util.BeanUtil;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.dto.SpecialDishDto;
import com.sky.entity.Dish;
import com.sky.entity.ShoppingCart;
import com.sky.entity.SpecialDish;
import com.sky.entity.SpecialOrder;
import com.sky.idGenerator.IdGeneratorUtil;
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
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;


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
    private ShopcartService shopcartService;

    @Autowired
    private IdGeneratorUtil idGeneratorUtil;

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
    public void AddSpecialIntoCart(SpecialOrder item) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(item.getUserId());


        Long dishId = item.getVoucherId();
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


    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final AtomicBoolean running = new AtomicBoolean(true);

    @PostConstruct
    private void Init() {
        executorService.submit(new SpecialDishHandler());
    }

    @PreDestroy
    private void OnExit() {
        running.set(false);
    }

    private class SpecialDishHandler implements Runnable {
        String QueueName = "stream.orders";
        @Override
        public void run() {
            while (running.get()) {
                try {
                    List<MapRecord<String, Object, Object>> read = redisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                            StreamOffset.create(QueueName, ReadOffset.lastConsumed())
                    );

                    if (read == null || read.isEmpty()) {
                        continue;
                    }
                    MapRecord<String, Object, Object> record = read.get(0);
                    Map<Object, Object> values = record.getValue();
                    SpecialOrder item = new SpecialOrder();

                    if (values.containsKey("userId")) {
                        item.setUserId(Long.valueOf(values.get("userId").toString()));
                    }
                    if (values.containsKey("voucherId")) {
                        item.setVoucherId(Long.valueOf(values.get("voucherId").toString()));
                    }

                    shopcartService.AddSpecialIntoCart(item);
                    redisTemplate.opsForStream().acknowledge(QueueName, "g1", record.getId());
                }catch (Exception e) {
                    log.error("特价商品处理失败", e);
                    HandlePendingList();
                }
            }
        }

        private void HandlePendingList() {
            while (running.get()) {
                try {
                    List<MapRecord<String, Object, Object>> read = redisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                            StreamOffset.create(QueueName, ReadOffset.from("0"))
                    );

                    if (read == null || read.isEmpty()) {
                        break;
                    }

                    MapRecord<String, Object, Object> record = read.get(0);
                    Map<Object, Object> values = record.getValue();
                    SpecialOrder item = new SpecialOrder();

                    if (values.containsKey("userId")) {
                        item.setUserId(Long.valueOf(values.get("userId").toString()));
                    }
                    if (values.containsKey("voucherId")) {
                        item.setVoucherId(Long.valueOf(values.get("voucherId").toString()));
                    }

                    shopcartService.AddSpecialIntoCart(item);
                    redisTemplate.opsForStream().acknowledge(QueueName, "g1", record.getId());
                }catch (Exception e) {
                    log.error("pending-list处理失败", e);
                }
            }
        }
    }


    public Long AddSpecial(ShoppingCartDTO item) {
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

        Long orderId = idGeneratorUtil.nextId("order");
        Long result = (Long)redisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                item.getDishId().toString(),
                TEST_USER_ID.toString()
        );

        int trueRes = result.intValue();

        if (trueRes != 0) {
            return null;
        }

        return orderId;
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
