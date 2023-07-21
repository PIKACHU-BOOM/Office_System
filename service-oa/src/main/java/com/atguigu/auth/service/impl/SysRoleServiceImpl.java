package com.atguigu.auth.service.impl;

import com.atguigu.auth.mapper.SysRoleMapper;
import com.atguigu.auth.mapper.SysUserRoleMapper;
import com.atguigu.auth.service.SysRoleService;
import com.atguigu.auth.service.SysUserRoleService;
import com.atguigu.model.system.SysRole;
import com.atguigu.model.system.SysUserRole;
import com.atguigu.vo.system.AssginRoleVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.sql.rowset.spi.SyncResolver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service

public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Autowired
    private SysUserRoleService sysUserRoleService;



    //查询所有角色 和 当前用户所属角色
    @Override
    public Map<String, Object> findRoleDataByUserId(Long userId) {
        //1 查询所有的角色，返回list集合
        List<SysRole> allRolesList = baseMapper.selectList(null);

        //2 根据userid 查询角色用户关系表，查询userid对应的的角色id
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId,userId);
        List<SysUserRole> existUserRoleList = sysUserRoleService.list(wrapper);
        //获取角色id

        List<Long> existRoleIdList = existUserRoleList.stream().map(c->c.getRoleId()).collect(Collectors.toList());


        //3 根据角色id、 找到对应角色信息
        // 根据角色id到所有的角色list集合进行比较
        List<SysRole> assginRoleList = new ArrayList<>();
        for (SysRole role : allRolesList) {
            //比较
            //已分配
            if(existRoleIdList.contains(role.getId())) {
                assginRoleList.add(role);
            }
        }
        //4 把得到的两部分数据封装到map集合并返回
        Map<String, Object> roleMap = new HashMap<>();
        roleMap.put("assginRoleList", assginRoleList);
        roleMap.put("allRolesList", allRolesList);
        return roleMap;
    }


    //为用户分配角色
    @Override
    public void doAssign(AssginRoleVo assginRoleVo) {
        //将用户之前分配的角色数据删除
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole :: getUserId, assginRoleVo.getUserId());
        sysUserRoleService.remove(wrapper);
        //重新进行分配
        List<Long> roleIdList = assginRoleVo.getRoleIdList();
        for(Long roleId:roleIdList){
            if (StringUtils.isEmpty(roleId)){
                continue;
            }
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setUserId(assginRoleVo.getUserId());
            sysUserRole.setRoleId(roleId);
            sysUserRoleService.save(sysUserRole);
        }

    }
}