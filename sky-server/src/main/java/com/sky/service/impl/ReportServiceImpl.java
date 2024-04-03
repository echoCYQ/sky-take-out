package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
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
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 统计指定时间内的营业额
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        // 当前集合用于存放begin到end范围内的每天日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            // 日期计算,计算指定日期的后一天
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            // 重新localDate对应的营业额
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }
        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }


    /**
     * 统计指定时间内用户数据
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        // 当前集合用于存放begin到end范围内的每天日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            // 日期计算,计算指定日期的后一天
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        // 每天新增用户量
        List<Integer> newUserList = new ArrayList<>();
        // 总用户量
        List<Integer> totalList = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
            Map map = new HashMap();
            map.put("end", endTime);
            // 用户新增数量
            Integer newUser = userMapper.countByMap(map);
            map.put("begin", beginTime);
            // 总用户数量
            Integer totalUser = userMapper.countByMap(map);
            totalList.add(totalUser);
            newUserList.add(newUser);
        }

        return UserReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalList, ","))
                .build();
    }

    /**
     * 统计指定时间区间内的订单数据
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        // 当前集合用于存放begin到end范围内的每天日期
        ArrayList<LocalDate> dataList = new ArrayList<>();
        dataList.add(begin);
        while (!begin.equals(end)) {
            // 日期计算,计算指定日期的后一天
            begin = begin.plusDays(1);
            dataList.add(begin);
        }
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> vaildOrderCountList = new ArrayList<>();
        // 查询每天有效订单数
        for (LocalDate localDate : dataList) {
            // 查询每天订单总数
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
            Integer orderCount = getOrderCount(beginTime, endTime, null);
            // 查询每天有效订单总数
            Integer vaildOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);
            orderCountList.add(orderCount);
            vaildOrderCountList.add(vaildOrderCount);
        }
        // 时间区间内的订单总数
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        // 时间区间内的有效订单总数
        Integer totalVaildOrederCount = vaildOrderCountList.stream().reduce(Integer::sum).get();
        // 技术订单完成率
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = totalVaildOrederCount.doubleValue() / totalOrderCount;
        }

        return OrderReportVO
                .builder()
                .dateList(StringUtils.join(dataList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(vaildOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(totalVaildOrederCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 根据条件统计订单数量
     *
     * @param begin
     * @param end
     * @param status
     * @return
     */
    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status) {
        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);
        map.put("status", Orders.COMPLETED);
        return orderMapper.countByMap(map);
    }


    /**
     * 销量Top10
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> salesTOP10 = orderMapper.getSalesTOP10(beginTime, endTime);

        List<String> names = salesTOP10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = StringUtils.join(names, ",");

        List<Integer> numbers = salesTOP10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numbersList = StringUtils.join(numbers, ",");

        return SalesTop10ReportVO
                .builder()
                .nameList(nameList)
                .numberList(numbersList)
                .build();
    }
}
