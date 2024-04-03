package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
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
    @Autowired
    private WorkspaceService workspaceService;

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
     *
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


    /**
     * 导出运营数据报表
     *
     * @param response
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        // 1,查询数据库 获取营业数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));
        // 2.通过POI将数据写入到EXCEL中
        // 获取输入流对象
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            // 基于模板文件创建一个新的Excel
            XSSFWorkbook excel = new XSSFWorkbook(in);
            // 获取表格sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");
            // 第二行第二列 填充字符
            sheet.getRow(1).getCell(1).setCellValue("时间" + dateBegin + "至" + dateEnd);
            // 获取第四行数据
            XSSFRow row = sheet.getRow(3);
            // 营业额数据填充第四行第三列
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            // 订单完成率
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            // 新增用户数
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());
            // 获取第五行数据
            row = sheet.getRow(4);
            // 有效订单
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            // 评价客单价
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            // 明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                BusinessDataVO businessData = workspaceService
                        .getBusinessData(
                                LocalDateTime.of(dateBegin, LocalTime.MIN),
                                LocalDateTime.of(dateEnd, LocalTime.MAX));
                //活动7+i行
                row = sheet.getRow(7 + i);
                //日期
                row.getCell(1).setCellValue(date.toString());
                //营业额
                row.getCell(2).setCellValue(businessData.getTurnover());
                //有效订单
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                //订单完成率
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                //平均客单价
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                //新增用户数
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            // 3.通过输出流将Excel文件下载到客户端浏览器
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);

            // 关闭资源
            outputStream.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
