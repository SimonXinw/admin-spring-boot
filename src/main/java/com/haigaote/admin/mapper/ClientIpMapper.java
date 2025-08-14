package com.haigaote.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haigaote.admin.entity.ClientIp;
import org.apache.ibatis.annotations.Mapper;

/**
 * ClientIp Mapper接口
 * 提供客户端IP数据的数据库操作
 * 
 * @author xw
 * @version 1.0
 * @since 2025-01-01
 */
@Mapper
public interface ClientIpMapper extends BaseMapper<ClientIp> {
    
    // BaseMapper已经提供了基础的CRUD操作：
    // insert(T entity) - 插入一条记录
    // deleteById(Serializable id) - 根据ID删除
    // updateById(T entity) - 根据ID更新
    // selectById(Serializable id) - 根据ID查询
    // selectList(Wrapper<T> queryWrapper) - 根据条件查询列表
    // 等等...
    
    // 如果需要自定义SQL查询，可以在这里添加方法声明
    // 例如：
    // List<ClientIp> findByIpAddress(@Param("ip") String ip);
} 