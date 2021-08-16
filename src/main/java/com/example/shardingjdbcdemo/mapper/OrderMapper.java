package com.example.shardingjdbcdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.shardingjdbcdemo.model.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <h3></h3>
 *
 * @author zohar
 * @version 1.0
 * 2021/8/16 2:34:20
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {


    /**
     * ERROR!!!
     * 分库分表不支持跨库 Join。
     *
     * @return Order
     */
    @Select(value = "SELECT t_order.*, t_user.name as user_name FROM t_order LEFT JOIN t_user ON t_order.user_id = t_user.id")
    List<Order> selectALLWithUsername();


    /**
     * SUCCESS.
     * 公共表可以进行全局 Join
     *
     * @return Order List
     */
    @Select(value = "SELECT o.*, d.name as status_name FROM t_order o LEFT JOIN t_dict d ON d.type = 1 AND d.enum_value = o.status")
    List<Order> selectAllWithStatusName();
}
