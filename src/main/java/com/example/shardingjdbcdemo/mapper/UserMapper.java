package com.example.shardingjdbcdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.shardingjdbcdemo.model.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * <h3></h3>
 *
 * @author zohar
 * @version 1.0
 * 2021/8/16 3:57:10
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
