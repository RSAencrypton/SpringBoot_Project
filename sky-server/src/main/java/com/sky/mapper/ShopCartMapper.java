package com.sky.mapper;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ShopCartMapper {

    List<ShoppingCart> list(ShoppingCart item);

    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateItem(ShoppingCart shoppingCart);

    @Insert("insert into shopping_cart (name ,user_id, dish_id, setmeal_id, dish_flavor, image, amount, number, create_time)" +
            "values (#{name},#{userId}, #{dishId}, #{setmealId},#{dishFlavor},#{image},#{amount}, #{number}, #{createTime})")
    void insertItem(ShoppingCart shoppingCart);

    @Select("select * from shopping_cart where user_id = #{id}")
    List<ShoppingCart> ListCart(Long id);

    @Delete("delete from shopping_cart where user_id = #{id}")
    void RemoveAll(Long id);

    @Select("select * from shopping_cart where dish_id = #{dishId}")
    ShoppingCart GetItemById(Long dishId);

    @Delete("delete from shopping_cart where id = #{id} and dish_id = #{dishId}")
    void RemoveOne(ShoppingCart item);

}
