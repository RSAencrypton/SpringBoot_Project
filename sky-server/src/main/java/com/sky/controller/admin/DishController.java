package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.result.Result;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController("AdminDishController")
@RequestMapping("/admin/dish")
@Api("菜品管理")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    @CacheEvict(cacheNames = "dish", key = "#item.getCategoryId()")
    public Result AddDish(@RequestBody DishDTO item) {
        dishService.AddDish(item);
//        CleanRedis(item.getCategoryId());
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("分页查询菜品列表")
    public Result<PageResult> GetDishList(DishPageQueryDTO item) {
        PageResult pageResult = dishService.GetDishList(item);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> FindDishById(@PathVariable Long id) {
        DishVO dishVO = dishService.GetDishById(id);
        return Result.success(dishVO);
    }

    @PutMapping
    @CacheEvict(cacheNames = "dish", allEntries = true)
    public Result UpdateDish(@RequestBody DishDTO item)
    {
        dishService.UpdateDish(item);

//        CleanAllRedis();
        return Result.success();
    }

    @DeleteMapping
    @ApiOperation("删除菜品")
    @CacheEvict(cacheNames = "dish", allEntries = true)
    public Result DeleteDish(@RequestParam List<Long> ids) {
        dishService.DeleteDish(ids);
//        CleanAllRedis();
        return Result.success();
    }

    private void CleanRedis(Long categoryId) {
        String key = "dish_" + categoryId;
        redisTemplate.delete(key);
    }

    private void CleanAllRedis() {
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
    }

}
