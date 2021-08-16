package com.example.shardingjdbcdemo.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.shardingjdbcdemo.ShardingJdbcDemoApplication;
import com.example.shardingjdbcdemo.model.Order;
import com.example.shardingjdbcdemo.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * <h3></h3>
 *
 * @author zohar
 * @version 1.0
 * 2021/8/16 2:53:11
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ShardingJdbcDemoApplication.class)
public class OrderMapperTest {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testInsert() {
        Random random = new Random(System.currentTimeMillis());
        List<User> users = userMapper.selectList(new QueryWrapper<>());
        List<Long> ids = new ArrayList<>(users.size());
        users.forEach(user -> ids.add(user.getId()));
        for (int i = 0; i < 50; i++) {
            orderMapper.insert(new Order(
                    null,
                    ids.get(random.nextInt(ids.size())),
                    new BigDecimal(random.nextInt(10000)),
                    random.nextInt(6)
            ));
        }
    }

    @Test
    public void testDelete() {
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", Arrays.asList("-4634923367132902829", "-7092194823470965227", "-193447418132914992", "-6989874834663603991"));
        orderMapper.delete(queryWrapper);
    }

    @Test
    public void testQuery() {
        orderMapper.selectList(new QueryWrapper<>()).forEach(System.out::println);
    }

    @Test
    public void testSelectALlWithUserName() {
        try {
            orderMapper.selectALLWithUsername().forEach(System.out::println);
        } catch (BadSqlGrammarException e) {
            System.err.println("分库分表不支持跨库 JOIN！");
        }
    }

    @Test
    public void selectAllWithStatusName() {
        orderMapper.selectAllWithStatusName().forEach(System.out::println);
    }
}