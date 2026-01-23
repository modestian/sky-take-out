package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ReportService reportService;

    /**
     * 根据起始和结束日期得到日期列表 private
     * @param begin
     * @param end
     * @return
     */
    private List<LocalDate> getDateListBetween(LocalDate begin, LocalDate end){
        //初始化
        List<LocalDate> dateList = new ArrayList<>();

        //加入首个日期
        dateList.add(begin);

        //加入剩下的日期
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        return dateList;
    }

    /**
     * 统计指定时间区间内的营业额
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnOverStatistics(LocalDate begin, LocalDate end){

        List<LocalDate> dateList = getDateListBetween(begin, end);//用于存放begin到end每一天的日期
        List<Double> turnoverList = new ArrayList<>();

        for(LocalDate date:dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnOver = orderMapper.sumByMap(map);
            turnOver = turnOver == null ? 0.0 : turnOver;
            turnoverList.add(turnOver);
        }

        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * 统计指定时间区间内的用户数据
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end){
        //存档begin到end之前每天的日期
        List<LocalDate> dateList = getDateListBetween(begin, end);

        //存放每天新增用户数量
        List<Integer> newUserList = new ArrayList<>();

        //存放每天的总用户数量
        List<Integer> totalUserList = new ArrayList<>();

        for(LocalDate date:dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();

            map.put("endTime", endTime);
            Integer totalUser = userMapper.countByMap(map);

            map.put("beginTime", beginTime);
            Integer newUser = userMapper.countByMap(map);

            newUserList.add(newUser);
            totalUserList.add(totalUser);
        }

        //封装数据结果
        return UserReportVO
                .builder()
                .dateList(StringUtils.join(dateList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .build();
    }


    /**
     * 统计指定时间区间内的订单数据
     * @param begin
     * @param end
     * @return
     */
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end){
        //存档begin到end之前每天的日期
        List<LocalDate> dateList = getDateListBetween(begin, end);

        //存放每日订单数
        List<Integer> orderCountList = new ArrayList<>();

        //存放每日有效订单数
        List<Integer> validOrderCountList = new ArrayList<>();

        //遍历dateList集合查询每天的有效订单数
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //查询每天的订单总数
            Integer orderCount = getOrderCount(beginTime, endTime, null);

            //查询每天的有效订单数
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);

            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }

        //总订单数
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();

        //有效订单数
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();

        //订单完成率
        Double orderCompleteRate = 0.0;
        if(totalOrderCount != 0){
            orderCompleteRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        return OrderReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompleteRate)
                .build();
    }

    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status){
        Map map = new HashMap();
        map.put("beginTime", begin);
        map.put("endTime", end);
        map.put("status", status);

        return orderMapper.countByMap(map);
    }
}
