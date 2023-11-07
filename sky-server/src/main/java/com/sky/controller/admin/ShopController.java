package com.sky.controller.admin;


import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/shop")
@Api(tags = "商铺管理")
@Slf4j
public class ShopController {

    private static final String TAG = "ShopStatus";
    @Autowired
    private RedisTemplate redisTemplate;

    @PutMapping("/{status}")
    @ApiOperation("设置商铺状态")
    public Result SetShopStatus(@PathVariable int status) {
        redisTemplate.opsForValue().set(TAG, status);
        return Result.success();
    }

    @GetMapping("/status")
    @ApiOperation("获取商铺状态")
    public Result<Integer> GetShopStatus() {
        Integer status = (Integer) redisTemplate.opsForValue().get(TAG);
        return Result.success(status);
    }
}
