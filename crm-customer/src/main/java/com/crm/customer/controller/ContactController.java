package com.crm.customer.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.result.Result;
import com.crm.customer.entity.Contact;
import com.crm.customer.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 客户联系人 Controller
 */
@RestController
@RequestMapping("/customer/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    /**
     * 分页查询
     */
    @GetMapping("/page")
    @SaCheckPermission("customer:list")
    public Result<IPage<Contact>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<Contact> page = new Page<>(current, size);
        return Result.success(contactService.pageContacts(page, keyword));
    }

    /**
     * 查询详情
     */
    @GetMapping("/{id}")
    public Result<Contact> getById(@PathVariable Long id) {
        return Result.success(contactService.getById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    @SaCheckPermission("customer:add")
    public Result<Boolean> save(@Valid @RequestBody Contact contact) {
        return Result.success(contactService.save(contact));
    }

    /**
     * 修改
     */
    @PutMapping("/{id}")
    @SaCheckPermission("customer:edit")
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody Contact contact) {
        contact.setId(id);
        return Result.success(contactService.updateById(contact));
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("customer:del")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(contactService.removeById(id));
    }
}
