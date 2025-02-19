package com.livelab.security.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.livelab.security.entity.KeyRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface KeyRecordMapper extends BaseMapper<KeyRecord> {
    
    /**
     * 获取指定keyId的当前活跃密钥
     */
    default KeyRecord getActiveKey(@Param("keyId") String keyId) {
        return selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<KeyRecord>()
                .eq(KeyRecord::getKeyId, keyId)
                .eq(KeyRecord::getActive, true)
        );
    }
    
    /**
     * 获取指定keyId和version的密钥
     */
    default KeyRecord getKeyByVersion(@Param("keyId") String keyId, @Param("version") String version) {
        return selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<KeyRecord>()
                .eq(KeyRecord::getKeyId, keyId)
                .eq(KeyRecord::getVersion, version)
        );
    }
    
    /**
     * 将指定keyId的所有密钥设置为非活跃
     */
    @Update("UPDATE key_record SET active = false, status = 'INACTIVE' WHERE key_id = #{keyId}")
    void deactivateAllKeys(@Param("keyId") String keyId);
    
    /**
     * 获取指定keyId的所有历史密钥记录
     */
    default List<KeyRecord> getKeyHistory(@Param("keyId") String keyId) {
        return selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<KeyRecord>()
                .eq(KeyRecord::getKeyId, keyId)
                .orderByDesc(KeyRecord::getCreateTime)
        );
    }
}
