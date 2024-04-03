package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    /**
     * 添加订单
     * @param orders
     */
    void insert(Orders orders);



    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where id = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    @Select("select * from orders where number = #{orderNumber}")
    Orders getByorderNumber(Long orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);


    /**
     * 分页条件查询并按下单时间排序
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    @Delete("delete from orders where id =#{id}")
    void deleteById(Long id);


    /**
     * 根据状态统计订单数量
     * @param status
     */
    @Select("select count(id) from orders where status = #{status}")
    Integer countStatus(Integer status);


    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 更加订单状态和下单时间查询
     * @param status
     * @param orderTime
     * @return
     */
    @Select("select * from orders where status=#{status} and order_time< #{orderTime}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime);

    @Update("update orders set pay_status=1 ,status=2 where number = #{orderNumber}")
    void pay(String orderNumber);

    /**
     * 金额统计
     * @param map
     * @return
     */
    Double sumByMap(Map map);

    /**
     * 根据动态生成统计订单数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);

    /**
     * 统计时间区间内销量TOP10菜品
     * @param begin
     * @param end
     * @return
     */
    List<GoodsSalesDTO> getSalesTOP10(LocalDateTime begin,LocalDateTime end);
}
