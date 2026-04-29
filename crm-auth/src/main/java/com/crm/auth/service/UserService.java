package com.crm.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.auth.entity.User;

/**
 * 用户 Service
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录
     * @param username 用户名
     * @param password 明文密码
     * @return token
     */
    String login(String username, String password);

    /**
     * 根据用户名查询
     */
    User getByUsername(String username);

    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 原密码（明文）
     * @param newPassword 新密码（明文）
     */
    void updatePassword(Long userId, String oldPassword, String newPassword);
}
