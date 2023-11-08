package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {

    public void AddDish(DishDTO item);

    PageResult GetDishList(DishPageQueryDTO item);

    void DeleteDish(List<Long> ids);

    DishVO GetDishById(Long id);

    void UpdateDish(DishDTO item);

    List<DishVO> GetDishByCategoryId(Long id);
}
