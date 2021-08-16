package com.example.shardingjdbcdemo.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <h3></h3>
 *
 * +---------+---------------+------+-----+---------+-------+
 * | Field   | Type          | Null | Key | Default | Extra |
 * +---------+---------------+------+-----+---------+-------+
 * | id      | bigint        | NO   | PRI | NULL    |       |
 * | user_id | bigint        | NO   |     | NULL    |       |
 * | price   | decimal(10,2) | NO   |     | NULL    |       |
 * | status  | int           | NO   |     | 1       |       |
 * +---------+---------------+------+-----+---------+-------+
 *
 * @author zohar
 * @version 1.0
 * 2021/8/16 2:50:18
 */
@Data
@AllArgsConstructor
@TableName(value = "t_order")
public class Order implements Serializable {

    @TableId
    private Long id;
    private Long userId;
    private BigDecimal price;
    private Integer status;

    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String statusName;

    public Order(Long id, Long userId, BigDecimal price, Integer status) {
        this.id = id;
        this.userId = userId;
        this.price = price;
        this.status = status;
    }

    public Order(Long id, Long userId, BigDecimal price, Integer status, String statusName) {
        this.id = id;
        this.userId = userId;
        this.price = price;
        this.status = status;
        this.statusName = statusName;
    }
}
