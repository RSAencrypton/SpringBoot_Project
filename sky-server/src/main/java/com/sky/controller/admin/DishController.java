package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.service.DishService;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/getlist")
    public Result GetDishList(@RequestParam Integer id) {
        return Result.success();
    }

    @PutMapping("/update")
    public Result UpdateDish(@RequestBody DishDTO item) {
        return Result.success();
    }

    @DeleteMapping("/delete")
    public Result DeleteDish(@RequestParam Integer id) {
        return Result.success();
    }

}
