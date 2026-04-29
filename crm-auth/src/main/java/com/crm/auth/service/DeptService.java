package com.crm.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.auth.entity.Dept;

import java.util.List;

/**
 * 部门 Service
 */
public interface DeptService extends IService<Dept> {

    /**
     * 构建部门树
     */
    List<Dept> buildDeptTree();
}
