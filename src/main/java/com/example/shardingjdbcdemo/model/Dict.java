package com.example.shardingjdbcdemo.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * <h3></h3>
 *
 * +------------+-------------+------+-----+---------+----------------+
 * | Field      | Type        | Null | Key | Default | Extra          |
 * +------------+-------------+------+-----+---------+----------------+
 * | id         | int         | NO   | PRI | NULL    | auto_increment |
 * | type       | int         | NO   |     | NULL    |                |
 * | enum_value | int         | NO   |     | NULL    |                |
 * | name       | varchar(64) | NO   |     |         |                |
 * +------------+-------------+------+-----+---------+----------------+
 *
 * @author zohar
 * @version 1.0
 * 2021/8/16 15:42:01
 */
@Data
@AllArgsConstructor
@TableName(value = "t_dict")
public class Dict {

    public static final int TYPE_ORDER_STATUS = 1;
    public static final int TYPE_USER_STATUS = 2;

    @TableId
    private Integer id;
    private Integer type;
    private Integer enumValue;
    private String name;

}
