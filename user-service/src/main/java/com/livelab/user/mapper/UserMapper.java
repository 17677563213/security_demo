package com.livelab.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.livelab.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
