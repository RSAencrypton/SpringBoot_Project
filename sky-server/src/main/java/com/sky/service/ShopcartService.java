package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.entity.SpecialOrder;

import java.util.List;

public interface ShopcartService {

    void AddCart(ShoppingCartDTO item);

    List<ShoppingCart> ListCart(Long id);

    void DeleteCart(Long id);

    void RemoveOne(Long id);

    Long AddSpecial(ShoppingCartDTO item);

    void AddSpecialIntoCart(SpecialOrder item);
}
