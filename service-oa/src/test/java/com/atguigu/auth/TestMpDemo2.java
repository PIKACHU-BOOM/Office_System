package com.atguigu.auth;

import com.atguigu.auth.mapper.SysRoleMapper;
import com.atguigu.auth.service.SysRoleService;
import com.atguigu.model.system.SysRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class TestMpDemo2 {

    //注入
    @Autowired
    private SysRoleService service;

    //查询所有记录
    @Test
    public void getALL(){
        List<SysRole> list = service.list();
        System.out.println(list);
    }
}
