package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {

    /**
     * 根据OPenID查询用户
     * @param openid
     * @return
     */
    @Select("select  * from  user where openid=#{openid}")
    User getByOpenId(String openid);

    /**
     * 插入数据
     * @param user
     */
    void insert(User user);


    /**
     * 根据主键查ID
     * @param userId
     * @return
     */
    @Select("select * from user where id = #{id}")
    User getById(Long userId);

    /**
     * 根据动态调节统计用户
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
