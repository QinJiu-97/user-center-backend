package com.qinjiu.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qinjiu.usercenter.common.ErrorCode;
import com.qinjiu.usercenter.exception.BusinessException;
import com.qinjiu.usercenter.model.domain.User;
import com.qinjiu.usercenter.service.UserService;
import com.qinjiu.usercenter.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.qinjiu.usercenter.constant.UserConstant.USER_LOGIN_STATUS;

/**
 * @author 琴酒
 * @description 针对表【User】的数据库操作Service实现
 * @createDate 2022-03-13 15:58:52
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {


    @Resource
    private UserMapper userMapper;

    //盐值，混淆密码
    private final String SALT = "QinJiu";

    @Override
    public long UserRegister(String userAccount, String userPassword, String checkPassword, String planet) {
        //校验
        if (StringUtils.isAllBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "参数为空");

        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码长度小于8位");

        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账号长度小于4位");

        }
        if (planet.length() > 5) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "星球编号大于5位");

        }

        String validPattern = "[`~!@#$%^&*()+=|{}';:',\\\\[\\\\].<>/?~·！@#￥%……&*（）——+|}【】’；：“”、。，？]";

        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户账户异常");
        }

        //密码和校验密码一致
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "两次输入密码不一致");
        }

        //账户不重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账号重复");
        }
        //查询星球id是否存在
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planet);
        long code = userMapper.selectCount(queryWrapper);
        if (code > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账号已存在");
        }

        //密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes(StandardCharsets.UTF_8));
        User user = new User();
        //插入数据
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planet);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "插入数据失败");
        }
        return user.getId();
    }


    @Override
    public User UserLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //校验
        if (StringUtils.isAllBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账号或密码错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码长度错误");

        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账号长度错误");

        }


        String validPattern = "[`~!@#$%^&*()+=|{}';:,\\\\[\\\\].<>/?·！￥…（）—【】’；：“”、。，？]";

        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账号输入特殊字符");

        }

        //密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes(StandardCharsets.UTF_8));


        //查询用户是否存在
        QueryWrapper<User> queryUserInfo = new QueryWrapper<>();
        queryUserInfo.eq("userAccount", userAccount);
        queryUserInfo.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryUserInfo);
        //用户不存在
        if (user == null) {
            log.info("login failed,userAccount not matched userPassword");
            throw new BusinessException(ErrorCode.PARAM_ERROR,"该用户不存在");

        }


        //用户脱敏,防止数据库字段泄露
        User safetyUser = getSafetUser(user);

        //记录用户登陆态
        request.getSession().setAttribute(USER_LOGIN_STATUS, safetyUser);

        return safetyUser;
    }

    @Override
    public User getSafetUser(User originUser) {
        if (originUser == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR,"用户信息为空");

        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        return safetyUser;
    }

    @Override
    public int UserLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATUS);

        return 1;
    }
}




