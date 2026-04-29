package com.crm.auth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.auth.entity.Menu;
import com.crm.auth.mapper.MenuMapper;
import com.crm.auth.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    private final MenuMapper menuMapper;

    @Override
    public List<Menu> buildMenuTree(List<Menu> menus) {
        List<Menu> roots = menus.stream()
                .filter(m -> m.getParentId() == 0)
                .collect(Collectors.toList());
        for (Menu root : roots) {
            root.setChildren(getChildren(root.getId(), menus));
        }
        return roots;
    }

    private List<Menu> getChildren(Long parentId, List<Menu> all) {
        List<Menu> children = all.stream()
                .filter(m -> m.getParentId().equals(parentId))
                .collect(Collectors.toList());
        for (Menu child : children) {
            child.setChildren(getChildren(child.getId(), all));
        }
        return children;
    }

    @Override
    @Cacheable(value = "userMenus", key = "#p0")
    public List<Menu> getUserMenuTree(Long userId) {
        List<Menu> menus = menuMapper.selectMenusByUserId(userId);
        // 只显示目录和菜单，过滤按钮
        List<Menu> filtered = menus.stream()
                .filter(m -> "M".equals(m.getMenuType()) || "C".equals(m.getMenuType()))
                .collect(Collectors.toList());
        return buildMenuTree(filtered);
    }

    @Override
    public List<Menu> getAllMenuTree() {
        List<Menu> all = this.lambdaQuery()
                .orderByAsc(Menu::getOrderNum)
                .list();
        return buildMenuTree(all);
    }
}
