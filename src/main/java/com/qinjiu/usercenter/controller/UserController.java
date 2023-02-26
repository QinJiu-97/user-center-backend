package com.qinjiu.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qinjiu.usercenter.common.BaseResponse;
import com.qinjiu.usercenter.common.ErrorCode;
import com.qinjiu.usercenter.common.ResultUtils;
import com.qinjiu.usercenter.exception.BusinessException;
import com.qinjiu.usercenter.model.domain.User;
import com.qinjiu.usercenter.model.request.UserLoginRequest;
import com.qinjiu.usercenter.model.request.UserRegisterRequest;
import com.qinjiu.usercenter.model.vo.UserVO;
import com.qinjiu.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.qinjiu.usercenter.constant.UserConstant.USER_LOGIN_STATUS;

/**
 * 用户接口
 *
 * @author QinJiu
 * @date 2022/3/20
 */
@RestController
@RequestMapping("/user")
//局部跨域
//@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8000"}, allowedHeaders = "*", allowCredentials = "true")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            return ResultUtils.error(ErrorCode.PARAM_ERROR);
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
            throw new BusinessException(ErrorCode.PARAM_ERROR, "当前用户异常");
        }

        int userLogout = userService.UserLogout(request);
        return ResultUtils.success(userLogout);


    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATUS);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            return ResultUtils.error(ErrorCode.NOT_LOGIN, "用户未登录");
        }

        Long userId = currentUser.getId();
        //todo 校验用户是否合法
        User safetUser = userService.getSafetUser(userService.getById(userId));
        return ResultUtils.success(safetUser);


    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {

        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "不是管理员");

        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }

        List<User> users = userService.list(queryWrapper);
        List<User> userList = users.stream().map(user -> userService.getSafetUser(user)).collect(Collectors.toList());
        return ResultUtils.success(userList);

    }

    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request) {

        User loginUser = userService.getLoginUser(request);
        String redisKey = String.format("QinJiu:matchFriend:recommend:%s", loginUser.getId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        //有缓存直接读取
        Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
        if (userPage != null) {
            return ResultUtils.success(userPage);
        }
        //无缓存，查数据库写入redis
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userPage = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
        try {
            valueOperations.set(redisKey, userPage, 60000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.info("redis set key error", e);
        }
        return ResultUtils.success(userPage);

    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "不是管理员");
        }

        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "输入id不合法");
        }
        boolean remove = userService.removeById(id);
        return ResultUtils.success(remove);

    }

    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUserByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        List<User> userList = userService.searchByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        //1. 校验参数是否为空
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);

        }
        User loginUser = userService.getLoginUser(request);

        int result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);
    }

    @GetMapping("/match")
    public BaseResponse<List<User>> matchUser(@RequestParam int n, HttpServletRequest request) {
        if (n < 0 || n > 15) {
            return ResultUtils.error(ErrorCode.PARAM_ERROR, "请求参数异常");

        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.matchUser(n, loginUser));

    }
}
