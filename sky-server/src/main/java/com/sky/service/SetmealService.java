package com.sky.service;

import com.sky.dto.SetmealDTO;

public interface SetmealService {

    /**
     * 新增套餐数据以及包含的菜品
     * @param setmealDTO
     */
    void saveWithDish(SetmealDTO setmealDTO);
}
