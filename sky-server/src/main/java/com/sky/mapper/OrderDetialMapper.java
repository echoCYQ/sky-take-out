package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface OrderDetialMapper {
    void insertBatch(ArrayList<OrderDetail> orderDetaillist);

    @Select("select * from order_detail where order_id =#{id}")
    List<OrderDetail> getByOrderId(Long id);

    @Delete("delete from order_detail where  order_id = #{id}")
    void deleteById(Long id);
}
