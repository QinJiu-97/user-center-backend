package com.qinjiu.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qinjiu.usercenter.common.BaseResponse;
import com.qinjiu.usercenter.common.ErrorCode;
import com.qinjiu.usercenter.common.ResultUtils;
import com.qinjiu.usercenter.exception.BusinessException;
import com.qinjiu.usercenter.model.domain.User;
import com.qinjiu.usercenter.model.domain.request.UserLoginRequest;
import com.qinjiu.usercenter.model.domain.request.UserRegisterRequest;
import com.qinjiu.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.qinjiu.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.qinjiu.usercenter.constant.UserConstant.USER_LOGIN_STATUS;

/**
 * 用户就接口
 *
 * @author QinJiu
 * @date 2022/3/20
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;


    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            return  ResultUtils.error(ErrorCode.PARAM_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String password = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planet = userRegisterRequest.getPlanetCode();
        if (StringUtils.isAnyBlank(userAccount, password, checkPassword, planet)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);

        }

        long userRegister = userService.UserRegister(userAccount, password, checkPassword, planet);

        return ResultUtils.success(userRegister);
    }


    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return ResultUtils.error(ErrorCode.PARAM_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String password = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, password)) {
            return ResultUtils.error(ErrorCode.PARAM_ERROR);
        }

        User userLogin = userService.UserLogin(userAccount, password, request);
        return ResultUtils.success(userLogin);

    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR,"当前用户异常");
        }

        int userLogout = userService.UserLogout(request);
        return ResultUtils.success(userLogout);


    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATUS);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN,"用户未登录");
        }
        Long userId = currentUser.getId();
        //todo 校验用户是否合法
        User safetUser = userService.getSafetUser(userService.getById(userId));
        return ResultUtils.success(safetUser);


    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {

        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH,"不是管理员");

        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }

        List<User> users = userService.list(queryWrapper);
        List<User> userList = users.stream().map(user -> userService.getSafetUser(user)).collect(Collectors.toList());
        return ResultUtils.success(userList);

    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH,"不是管理员");
        }

        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR,"输入id不合法");
        }
        boolean remove = userService.removeById(id);
        return ResultUtils.success(remove);

    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    private boolean isAdmin(HttpServletRequest request) {
        //仅管理员课查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATUS);
        User user = (User) userObj;

        return user != null && user.getUserRole() == ADMIN_ROLE;
    }
}
