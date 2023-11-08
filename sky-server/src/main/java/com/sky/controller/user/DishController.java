package com.sky.controller.user;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @Cacheable(cacheNames = "dish", key = "#id")
    public Result<List<DishVO>> FindDishById(@PathVariable Long id) {
//        //Redis
//        String key = "dish_" + id;
//        List<DishVO> dishVOs = (List<DishVO>)redisTemplate.opsForValue().get(key);
//
//        if (dishVOs != null && dishVOs.size() > 0) {
//            return Result.success(dishVOs);
//        }

        //Mysql
        List<DishVO> dishVOs = dishService.GetDishByCategoryId(id);
//        redisTemplate.opsForValue().set(key, dishVOs);
        return dishVOs == null ? Result.error("没有此分类") : Result.success(dishVOs);
    }


}
