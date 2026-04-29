package com.crm.auth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.auth.entity.Dept;
import com.crm.auth.mapper.DeptMapper;
import com.crm.auth.service.DeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Dept> implements DeptService {

    @Override
    @Cacheable(value = "deptTree")
    public List<Dept> buildDeptTree() {
        List<Dept> all = this.lambdaQuery()
                .eq(Dept::getStatus, 1)
                .orderByAsc(Dept::getOrderNum)
                .list();
        return buildTree(all, 0L);
    }

    private List<Dept> buildTree(List<Dept> all, Long parentId) {
        return all.stream()
                .filter(d -> d.getParentId().equals(parentId))
                .peek(d -> d.setChildren(buildTree(all, d.getId())))
                .collect(Collectors.toList());
    }
}
