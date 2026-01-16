package com.sky.service.impl;

import com.sky.dto.SetmealDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增套餐数据以及包含的菜品
     * @param setmealDTO
     */
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO){

        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);

        //向套餐表插入数据
        setmealMapper.insert(setmeal);

        //根据生成的套餐获取id（主键回写）
        Long setmealId = setmeal.getId();

        //将菜品和套餐关联到一个套餐口味表中
        List<SetmealDish> setmealDishes= setmealDTO.getSetmealDishes();
        if(setmealDishes != null && setmealDishes.size()>0){
            setmealDishes.forEach(setmealDish->{
                setmealDish.setSetmealId(setmealId);
            });

            setmealDishMapper.insertBatch(setmealDishes);
        }


    }
}
