package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.RedisConstants;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.dto.SpecialDishDto;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.SpecialDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.FlavourMapper;
import com.sky.mapper.MealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private FlavourMapper flavourMapper;

    @Autowired
    private MealMapper mealMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Transactional
    public void AddDish(DishDTO item) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(item, dish);
        dish.setStatus(StatusConstant.ENABLE);
        dishMapper.insert(dish);

        Long dishId = dish.getId();
        List<DishFlavor> flavors = item.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishId);
            }
            flavourMapper.InsertAllFlavour(flavors);
        }
    }

    public void AddSpecialDish(SpecialDishDto item) {
        dishMapper.AddSpecialDish(item);
        redisTemplate.opsForValue().set("STOCK:" + item.getId(),item.getStock().toString());
    }

    public DishVO GetDishById(Long id) {
        Dish dish = dishMapper.FindDishById(id);
        List<DishFlavor> flavors = flavourMapper.GetFlavorByDishId(id);

        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(flavors);

        return dishVO;
    }

    public PageResult GetDishList(DishPageQueryDTO item){
        PageHelper.startPage(item.getPage(), item.getPageSize());
        Page<DishVO> dishList = dishMapper.GetDishList(item);
        return new PageResult(dishList.getTotal(), dishList.getResult());
    }


    public List<DishVO> GetDishByCategoryId(Long id) {
        List<Category> categories = categoryMapper.HasChildren(id);
        if (categories == null || categories.size() == 0) {
            return null;
        }

        Dish dish = new Dish();
        dish.setCategoryId(id);
        dish.setStatus(StatusConstant.ENABLE);
        List<DishVO> dishVOs = dishMapper.GetDishByCategoryId(dish);
        return dishVOs;
    }

    @Transactional
    public void DeleteDish(List<Long> ids) {
        for (Long id : ids) {
            Dish dish = dishMapper.FindDishById(id);
            if (dish.getStatus().equals(StatusConstant.ENABLE)) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        List<Long> mealIds = mealMapper.getMealsByDishId(ids);
        if (mealIds != null && mealIds.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

//        for (Long id : ids) {
//            dishMapper.DeleteDish(id);
//            flavourMapper.DeleteFlavourByDishId(id);
//        }

        dishMapper.DeleteDishs(ids);
        flavourMapper.DeleteFlavourByDishIds(ids);
    }

    public void UpdateDish(DishDTO item) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(item, dish);
        dishMapper.UpdateDish(dish);

        flavourMapper.DeleteFlavourByDishId(item.getId());


        List<DishFlavor> flavors = item.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(item.getId());
            }
            flavourMapper.InsertAllFlavour(flavors);
        }
    }
}
