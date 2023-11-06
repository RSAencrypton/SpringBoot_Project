package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.FlavourMapper;
import com.sky.mapper.MealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private FlavourMapper flavourMapper;

    @Autowired
    private MealMapper mealMapper;

    @Transactional
    public void AddDish(DishDTO item) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(item, dish);
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

    public PageResult GetDishList(DishPageQueryDTO item){
        PageHelper.startPage(item.getPage(), item.getPageSize());
        Page<DishVO> dishList = dishMapper.GetDishList(item);
        return new PageResult(dishList.getTotal(), dishList.getResult());
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
}
