package com.atguigu.auth.service.impl;


import com.atguigu.auth.mapper.SysMenuMapper;
import com.atguigu.auth.service.SysMenuService;
import com.atguigu.auth.service.SysRoleMenuService;
import com.atguigu.auth.util.MenuHelper;
import com.atguigu.common.config.exception.GuiguException;
import com.atguigu.model.system.SysMenu;
import com.atguigu.model.system.SysRoleMenu;
import com.atguigu.vo.system.AssginMenuVo;
import com.atguigu.vo.system.MetaVo;
import com.atguigu.vo.system.RouterVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜单表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2023-06-17
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Autowired
    private SysRoleMenuService sysRoleMenuService;


    //菜单列表接口
    @Override
    public List<SysMenu> findNodes() {

        //查询所有菜单数据
        List<SysMenu> sysMenuList =  baseMapper.selectList(null);
        //构建树形结构,使用工具类实现





        List<SysMenu> resultlist =  MenuHelper.buildTree(sysMenuList);
        return resultlist;
    }

    //删除菜单
    @Override
    public void removeMenuById(Long id) {
    //判断当前菜单是否有下一层菜单
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getParentId , id);
        Integer count = baseMapper.selectCount(wrapper);
        if(count > 0){
            throw new GuiguException(201,"菜单不能删除");
        }
        baseMapper.deleteById(id);

    }

    //查询所有菜单和角色分配的菜单
    @Override
    public List<SysMenu> findMenuByRoleId(long roleId) {
        //1  查询所有菜单-添加条件：status=1
        LambdaQueryWrapper<SysMenu> wrapperSysMenu = new LambdaQueryWrapper<>();
        wrapperSysMenu.eq(SysMenu::getStatus, 1);
        List<SysMenu> allSysMenuList = baseMapper.selectList(wrapperSysMenu);

        //2  根据角色id roleid查询 角色菜单关系表中 角色id对应的所有菜单id
        LambdaQueryWrapper<SysRoleMenu> wrapperSysRoleMenu = new LambdaQueryWrapper<>();
        wrapperSysRoleMenu.eq(SysRoleMenu::getRoleId,roleId);
        List<SysRoleMenu> sysRoleMenuList = sysRoleMenuService.list(wrapperSysRoleMenu);

        //3  根据获取的菜单id 获取对应的菜单对象
        List<Long> menuIdList = sysRoleMenuList.stream().map(c -> c.getMenuId()).collect(Collectors.toList());


        //3.1 将菜单id与所有菜单集合中的id进行比较，若相同就封装
        allSysMenuList.stream().forEach(item -> {
            if(menuIdList.contains(item.getId())){
                item.setSelect(true);
            } else {
                item.setSelect(false);
            }

        });
        //4 返回规定树形格式菜单列表
        List<SysMenu> sysMenuList = MenuHelper.buildTree(allSysMenuList);
        return sysMenuList;

    }

    //角色分配菜单
    @Override
    public void doAssign(AssginMenuVo assignMenuVo) {
        //1 根据角色id删除菜单角色表中分配的数据
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId, assignMenuVo.getRoleId());
        sysRoleMenuService.remove(wrapper);

        //2 从参数中获取角色新分配菜单id列表，进行遍历，把每个id数据添加菜单角色表中
        List<Long> menuIdList = assignMenuVo.getMenuIdList();
        for(Long menuId : menuIdList){
            if (StringUtils.isEmpty(menuId)) continue;
            SysRoleMenu sysRoleMenu = new SysRoleMenu();
            sysRoleMenu.setMenuId(menuId);
            sysRoleMenu.setRoleId(assignMenuVo.getRoleId());

            sysRoleMenuService.save(sysRoleMenu);


        }

    }

    //4 根据用户id获取用户可以操作的菜单列表
    @Override
    public List<RouterVo> findUserMenuListByUserId(Long userId) {


        List<SysMenu> sysMenuList = null;
        //1 判断当前用户是否为管理员 userId=1 是管理员
        //1.1 如果是管理员，查询所有菜单
        if(userId.longValue() == 1){
            //查询所有菜单
            LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysMenu::getStatus, 1);
            wrapper.orderByAsc(SysMenu::getSortValue);
            sysMenuList = baseMapper.selectList(wrapper);

        }else {
            //1.2 如果不是管理员，根据userId查询可以操作的菜单
            //多表关联查询：用户角色关系表，角色菜单关系表，菜单表
            sysMenuList = baseMapper.findMenuListByUserId(userId);

        }

        //2 把查询出来的数据列表构建成框架要求的路由数据结构
        //使用菜单操作工具类构建树形结构
        List<SysMenu> sysMenuTreeList = MenuHelper.buildTree(sysMenuList);
        //构建成框架要求的路由结构
        List<RouterVo> routerList = this.buildRouter(sysMenuTreeList);
        return routerList;
    }

    //构建成框架要求的路由结构
    @Override
    public List<RouterVo> buildRouter(List<SysMenu> menus) {

        //创建list集合，存储最终数据
        List<RouterVo> routers = new ArrayList<>();

        //menus遍历
        for(SysMenu menu : menus){
            RouterVo router = new RouterVo();
            router.setHidden(false);
            router.setAlwaysShow(false);
            router.setPath(getRouterPath(menu));
            router.setComponent(menu.getComponent());
            router.setMeta(new MetaVo(menu.getName(), menu.getIcon()));
            //下一层数据部分
            List<SysMenu> children = menu.getChildren();
            if (menu.getType().intValue() == 1) {
                //加载出来下面的隐藏路由
                List<SysMenu> hiddenMenuList = children.stream().filter(item -> !StringUtils.isEmpty(item.getComponent())).collect(Collectors.toList());
                for (SysMenu hiddenMenu : hiddenMenuList) {
                    RouterVo hiddenRouter = new RouterVo();
                    hiddenRouter.setHidden(true);
                    hiddenRouter.setAlwaysShow(false);
                    hiddenRouter.setPath(getRouterPath(hiddenMenu));
                    hiddenRouter.setComponent(hiddenMenu.getComponent());
                    hiddenRouter.setMeta(new MetaVo(hiddenMenu.getName(), hiddenMenu.getIcon()));
                    routers.add(hiddenRouter);
                }
            }   else{
                    if (!CollectionUtils.isEmpty(children)) {
                        if(children.size() > 0) {
                            router.setAlwaysShow(true);
                        }
                        //递归
                        router.setChildren(buildRouter(children));
                    }

                }
            routers.add(router);
            }
        return routers;
    }
    /**
     * 获取路由地址
     *
     * @param menu 菜单信息
     * @return 路由地址
     */
    public String getRouterPath(SysMenu menu) {
        String routerPath = "/" + menu.getPath();
        if(menu.getParentId().intValue() != 0) {
            routerPath = menu.getPath();
        }
        return routerPath;
    }



    //5 根据用户id获取用户可以操作的按钮列表
    @Override
    public List<String> findUserPermsByUserId(Long userId) {
        //1 判断当前用户是否为管理员 userId=1 是管理员  如果是管理员，查询所有按钮
        List<SysMenu> sysMenuList = null;
        if(userId.longValue() == 1){
            //查询所有菜单
            LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysMenu::getStatus, 1);
            sysMenuList = baseMapper.selectList(wrapper);

        }else {
            //2 如果不是管理员，根据userId查询可以操作的按钮
            //多表关联查询：用户角色关系表，角色菜单关系表，菜单表
            sysMenuList = baseMapper.findMenuListByUserId(userId);
        }
        //3 从查询出来的数据中 获取可以操作按钮值的list集合，返回
        List<String> permsList =  sysMenuList.stream()
                .filter(item -> item.getType() == 2)
                .map(item -> item.getPerms())
                .collect(Collectors.toList());

        return permsList;
    }


}
