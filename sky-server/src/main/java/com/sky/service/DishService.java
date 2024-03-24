package com.sky.service;

import com.sky.dto.DishDTO;
import io.swagger.v3.oas.annotations.servers.Server;


public interface DishService {

    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     */
    void saveWithFlavor(DishDTO dishDTO);

}
