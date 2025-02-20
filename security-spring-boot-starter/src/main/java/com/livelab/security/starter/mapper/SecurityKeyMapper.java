package com.livelab.security.starter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.livelab.security.starter.entity.SecurityKey;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SecurityKeyMapper extends BaseMapper<SecurityKey> {
}
