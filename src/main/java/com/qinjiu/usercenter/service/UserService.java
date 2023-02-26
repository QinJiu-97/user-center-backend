package com.qinjiu.usercenter.service;

import com.qinjiu.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qinjiu.usercenter.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.qinjiu.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.qinjiu.usercenter.constant.UserConstant.USER_LOGIN_STATUS;

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

    /**
     * 根据用户标签搜索用户
     * @param tagNameList 标签列表
     * @return
     */
    List<User> searchByTags(List<String> tagNameList);

    /**
     * 更新用户信息
     * @param user
     * @return
     */
    int   updateUser(User user, User loginUser);

    /**
     * 获取用户登录信息
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    boolean isAdmin(User loginUser);

    /**
     * 匹配用户
     * @param n
     * @param loginUser
     * @return
     */
    List<User> matchUser(int n, User loginUser);
}
