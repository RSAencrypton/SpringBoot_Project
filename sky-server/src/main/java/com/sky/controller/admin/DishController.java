package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dish")
@Api("菜品管理")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @PostMapping
    public Result AddDish(@RequestBody DishDTO item) {
        dishService.AddDish(item);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("分页查询菜品列表")
    public Result<PageResult> GetDishList(DishPageQueryDTO item) {
        PageResult pageResult = dishService.GetDishList(item);
        return Result.success(pageResult);
    }

    @PutMapping("/update")
    public Result UpdateDish(@RequestBody DishDTO item) {
        return Result.success();
    }

    @DeleteMapping
    @ApiOperation("删除菜品")
    public Result DeleteDish(@RequestParam List<Long> ids) {
        dishService.DeleteDish(ids);
        return Result.success();
    }

}
