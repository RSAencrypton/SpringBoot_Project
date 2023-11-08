package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShopCartMapper {

    List<ShoppingCart> list(ShoppingCart item);

    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateItem(ShoppingCart shoppingCart);

    @Insert("insert into shopping_cart (name ,user_id, dish_id, setmeal_id, dish_flavor, image, amount, number, create_time)" +
            "values (#{name},#{userId}, #{dishId}, #{setmealId},#{dishFlavor},#{image},#{amount}, #{number}, #{createTime})")
    void insertItem(ShoppingCart shoppingCart);
}
