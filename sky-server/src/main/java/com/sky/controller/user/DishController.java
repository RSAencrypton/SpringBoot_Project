package com.sky.controller.user;

import cn.hutool.json.JSONUtil;
import com.alibaba.druid.support.json.JSONUtils;

import com.google.gson.Gson;
import com.sky.constant.RedisConstants;

import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.utils.RedisData;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.BooleanUtils;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController("UserDishController")
@RequestMapping("/user/dish")
@Api("菜品管理")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;





    @GetMapping("/{id}")
    @ApiOperation("根据分类id查询菜品")
//    @Cacheable(cacheNames = "dish", key = "#id")
    public Result<List<DishVO>> FindDishById(@PathVariable Long id) {
        List<DishVO> dishVOs = null;

        if (dishService.MightHasThisDish(id)) {
            dishVOs = QueryWithLogicalExpire(id);
        }

        return dishVOs == null ? Result.error("没有此分类") : Result.success(dishVOs);
    }

    public List<DishVO> CachePassThrough(Long id) {
        String key = "dish_" + id;
        List<DishVO> dishVOs = (List<DishVO>)redisTemplate.opsForValue().get(key);

        if (dishVOs == null) {
            return dishVOs;
        }


        String LockName = "Mutex" + id;
        boolean IsGet = TryGetLock(LockName);
        try {
            if (!IsGet) {
                return CachePassThrough(id);
            }else {
                dishVOs = (List<DishVO>)redisTemplate.opsForValue().get(key);

                if (dishVOs != null) {
                    return dishVOs;
                }
            }
            //Mysql
            dishVOs = dishService.GetDishByCategoryId(id);

            if (dishVOs == null || dishVOs.size() == 0) {
                redisTemplate.opsForValue().set(key,"", RedisConstants.NULL_OBJECT_TTL, TimeUnit.MINUTES);
            }else {
                redisTemplate.opsForValue().set(key, dishVOs);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            ReleaseLock(LockName);
        }
        return dishVOs;
    }

    private static final ExecutorService CACHE_REBUILD_POOL = Executors.newFixedThreadPool(10);

    public List<DishVO> QueryWithLogicalExpire(Long id) {
        String key = "dish_" + id;
        String obj = (String) redisTemplate.opsForValue().get(key);
        RedisData data = JSONUtil.toBean(obj, RedisData.class);

        if (data == null) {
            return SaveDishVO2RedisData(id);
        }
        List<DishVO> dishVOs = (List<DishVO>)data.getData();
        LocalDateTime expireTime = data.getExpireTime();

        if (expireTime.isAfter(LocalDateTime.now())) {
            return dishVOs;
        }

        String LockName = "Mutex" + id;
        boolean IsGet = TryGetLock(LockName);
        if (IsGet) {
            CACHE_REBUILD_POOL.submit(()-> {
                try {
                    this.SaveDishVO2RedisData(id);
                }catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    ReleaseLock(LockName);
                }
            });
        }
        return dishVOs;
    }

    public List<DishVO> SaveDishVO2RedisData(Long id) {
        String key = "dish_" + id;
        List<DishVO> dishVOs = dishService.GetDishByCategoryId(id);
        RedisData redisData = new RedisData();
        redisData.setData(dishVOs);
        redisData.setExpireTime(LocalDateTime.now().plusMinutes(5));
        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));

        return dishVOs;
    }

    private boolean TryGetLock(String key) {
        boolean res =  redisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtils.isTrue(res);
    }

    private void ReleaseLock(String key) {
        redisTemplate.delete(key);
    }
}
