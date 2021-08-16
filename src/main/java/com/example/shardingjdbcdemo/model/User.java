package com.example.shardingjdbcdemo.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * <h3></h3>
 *
 * +-------+--------------+------+-----+---------+-------+
 * | Field | Type         | Null | Key | Default | Extra |
 * +-------+--------------+------+-----+---------+-------+
 * | id    | bigint       | NO   | PRI | NULL    |       |
 * | name  | varchar(128) | NO   |     |         |       |
 * | type  | int          | NO   |     | 1       |       |
 * +-------+--------------+------+-----+---------+-------+
 *
 * @author zohar
 * @version 1.0
 * 2021/8/16 3:56:03
 */
@Data
@AllArgsConstructor
@TableName(value = "t_user")
public class User {
    @TableId
    private Long id;
    private String name;
    private Integer type;
}
