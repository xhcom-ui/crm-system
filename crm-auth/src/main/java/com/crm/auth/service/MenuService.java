package com.crm.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.auth.entity.Menu;

import java.util.List;

/**
 * 菜单 Service
 */
public interface MenuService extends IService<Menu> {

    /**
     * 构建树形菜单
     */
    List<Menu> buildMenuTree(List<Menu> menus);

    /**
     * 获取用户的菜单树
     */
    List<Menu> getUserMenuTree(Long userId);

    /**
     * 获取所有菜单树（管理用）
     */
    List<Menu> getAllMenuTree();
}
