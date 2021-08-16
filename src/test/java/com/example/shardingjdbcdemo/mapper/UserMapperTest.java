package com.example.shardingjdbcdemo.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.shardingjdbcdemo.ShardingJdbcDemoApplication;
import com.example.shardingjdbcdemo.model.User;
import com.github.javafaker.Faker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Locale;

/**
 * <h3></h3>
 *
 * @author zohar
 * @version 1.0
 * 2021/8/16 4:00:38
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ShardingJdbcDemoApplication.class)
public class UserMapperTest {

    @Resource
    private UserMapper userMapper;

    @Test
    public void bulkInsert() {
        Faker faker = new Faker(Locale.CHINA);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id").last("limit 1");
        User user = userMapper.selectOne(queryWrapper);
        long begin = user == null ? 1 : user.getId() + 1;
        for (int i = 0; i < 100; i++) {
            userMapper.insert(new User(begin + i, faker.name().fullName(), faker.random().nextInt(5)));
        }
    }

}