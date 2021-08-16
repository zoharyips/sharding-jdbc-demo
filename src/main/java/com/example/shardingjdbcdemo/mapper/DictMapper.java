package com.example.shardingjdbcdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.shardingjdbcdemo.model.Dict;
import org.apache.ibatis.annotations.Mapper;

/**
 * <h3></h3>
 *
 * @author zohar
 * @version 1.0
 * 2021/8/16 15:46:11
 */
@Mapper
public interface DictMapper extends BaseMapper<Dict> {
}
