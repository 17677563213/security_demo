package com.windsurfing.security.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.windsurfing.security.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    /**
     * 根据手机号摘要查询用户
     */
    List<User> selectByPhoneDigest(@Param("phoneDigest") String phoneDigest);
    
    /**
     * 根据身份证号摘要查询用户
     */
    List<User> selectByIdCardDigest(@Param("idCardDigest") String idCardDigest);
}
