package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.dto.SpecialDishDto;
import com.sky.entity.Dish;
import com.sky.entity.ShoppingCart;
import com.sky.entity.SpecialDish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);

    Page<DishVO> GetDishList(DishPageQueryDTO item);

    @Select("select * from dish where id = #{id}")
    Dish FindDishById(Long id);

    @Select("select * from shopping_cart where name = #{name} and user_id = #{UserId}")
    ShoppingCart FindItemByUserIDAndDishName(Long UserId, String name);

    @Delete("delete from dish where id = #{id}")
    void DeleteDish(Long id);

    void DeleteDishs(List<Long> ids);

    @Insert("insert into tb_special_dish (stock, description, pay_val, actual_val, begin_time, end_time)" +
            "values (#{stock}, #{description}, #{payVal}, #{actualVal},  #{begin}, #{end})")
    void AddSpecialDish(SpecialDishDto dish);

    @Update("update tb_special_dish set stock= stock - 1 where id = #{id} and stock > 0")
    void RemoveOneSpecialDish(Long id);

    @Select("select * from tb_special_dish where id = #{id}")
    SpecialDish HasStock(SpecialDish id);


    @AutoFill(value = OperationType.UPDATE)
    void UpdateDish(Dish dish);

    @Select("select * from dish where category_id = #{categoryId} and status = 1")
    List<DishVO> GetDishByCategoryId(Dish dish);
}
