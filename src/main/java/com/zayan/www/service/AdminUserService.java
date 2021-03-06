package com.zayan.www.service;

import com.zayan.www.model.entity.AdminUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zayan.www.model.form.admin.user.AdminEditPasswordForm;
import com.zayan.www.model.vo.user.AdminUserVO;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author AnYuan
 * @since 2020-08-17
 */
public interface AdminUserService extends IService<AdminUser> {

    /**
     * 用户登录
     *
     * @param phone    帐号
     * @param password 密码
     * @return String token
     */
    String login(String phone, String password);

    /**
     * 用户信息
     *
     * @param userId userId
     * @return AdminUserInfoVO
     */
    AdminUserVO userInfo(Integer userId);

    /**
     * 用户更改密码
     *
     * @param form form
     * @return AdminUserVO
     */
    AdminUserVO editPassword(AdminEditPasswordForm form);
}
