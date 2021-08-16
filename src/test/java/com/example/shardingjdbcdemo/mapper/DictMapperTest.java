package com.example.shardingjdbcdemo.mapper;

import com.example.shardingjdbcdemo.ShardingJdbcDemoApplication;
import com.example.shardingjdbcdemo.model.Dict;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * <h3></h3>
 *
 * @author zohar
 * @version 1.0
 * 2021/8/16 15:50:18
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ShardingJdbcDemoApplication.class)
public class DictMapperTest {

    @Resource
    private DictMapper dictMapper;

    @Test
    public void testInsert() {
        dictMapper.insert(new Dict(1, Dict.TYPE_ORDER_STATUS, 0, "未定义"));
        dictMapper.insert(new Dict(2, Dict.TYPE_ORDER_STATUS, 1, "未付款"));
        dictMapper.insert(new Dict(3, Dict.TYPE_ORDER_STATUS, 2, "已付款"));
        dictMapper.insert(new Dict(4, Dict.TYPE_ORDER_STATUS, 3, "退款中"));
        dictMapper.insert(new Dict(5, Dict.TYPE_ORDER_STATUS, 4, "已退款"));
        dictMapper.insert(new Dict(6, Dict.TYPE_ORDER_STATUS, 5, "已完成"));
        dictMapper.insert(new Dict(7, Dict.TYPE_USER_STATUS, 0, "未定义"));
        dictMapper.insert(new Dict(8, Dict.TYPE_USER_STATUS, 1, "已创建"));
        dictMapper.insert(new Dict(9, Dict.TYPE_USER_STATUS, 2, "已验证"));
        dictMapper.insert(new Dict(10, Dict.TYPE_USER_STATUS, 3, "已冻结"));
        dictMapper.insert(new Dict(11, Dict.TYPE_USER_STATUS, 4, "已注销"));
        dictMapper.insert(new Dict(12, Dict.TYPE_USER_STATUS, 5, "已删除"));
    }
}