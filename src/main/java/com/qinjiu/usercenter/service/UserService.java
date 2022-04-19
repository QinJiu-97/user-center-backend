package com.qinjiu.usercenter.service;

import com.qinjiu.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
* @author 琴酒
* @description 针对表【User】的数据库操作Service
* @createDate 2022-03-13 15:58:52
*/
public interface UserService extends IService<User> {



    /**
     *
     * @param userAccount   用户名
     * @param userPassword      用户密码
     * @param checkPassword 校验密码
     * @return              用户id
     */
    long UserRegister(String userAccount , String userPassword , String checkPassword, String planet);

    /**
     *
     * @param userAccount   用户名
     * @param userPassword      用户密码
     * @return  脱敏后的用户信息
     */
    User UserLogin(String userAccount , String userPassword , HttpServletRequest request );

    /**
     * 用户信息脱敏
     * @param originUser
     * @return
     */
    User getSafetUser(User originUser);

    /**
     * 用户注销
     * @param request
     */
    int UserLogout(HttpServletRequest request);
}
