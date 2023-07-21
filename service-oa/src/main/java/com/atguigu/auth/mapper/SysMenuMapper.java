package com.atguigu.auth.mapper;


import com.atguigu.model.system.SysMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 菜单表 Mapper 接口
 * </p>
 *
 * @author atguigu
 * @since 2023-06-17
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {


    //1.2 如果不是管理员，根据userId查询可以操作的菜单
    List<SysMenu> findMenuListByUserId(@Param("userId") Long userId);
}
