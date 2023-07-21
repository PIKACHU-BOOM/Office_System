package com.atguigu.auth.service;


import com.atguigu.model.system.SysMenu;
import com.atguigu.vo.system.AssginMenuVo;
import com.atguigu.vo.system.RouterVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 菜单表 服务类
 * </p>
 *
 * @author atguigu
 * @since 2023-06-17
 */
public interface SysMenuService extends IService<SysMenu> {

    //菜单列表接口
    List<SysMenu> findNodes();

    //删除菜单
    void removeMenuById(Long id);

    //查询所有菜单和角色分配的菜单
    List<SysMenu> findMenuByRoleId(long roleId);

    //角色分配菜单
    void doAssign(AssginMenuVo assignMenuVo);

    // 根据用户id获取用户可以操作的菜单列表
    List<RouterVo> findUserMenuListByUserId(Long userId);

    //5 根据用户id获取用户可以操作的按钮列表
    List<String> findUserPermsByUserId(Long userId);

    //构建成框架要求的路由结构
    List<RouterVo> buildRouter(List<SysMenu> sysMenuTreeList);
}
