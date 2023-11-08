package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShopcartService {

    void AddCart(ShoppingCartDTO item);

    List<ShoppingCart> ListCart(Long id);

    void DeleteCart(Long id);

    void RemoveOne(Long id);
}
