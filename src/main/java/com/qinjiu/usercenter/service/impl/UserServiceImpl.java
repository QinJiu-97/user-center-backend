package com.qinjiu.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qinjiu.usercenter.common.ErrorCode;
import com.qinjiu.usercenter.exception.BusinessException;
import com.qinjiu.usercenter.mapper.UserMapper;
import com.qinjiu.usercenter.model.domain.User;
import com.qinjiu.usercenter.service.UserService;
import com.qinjiu.usercenter.utils.AlgorithmUtils;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.qinjiu.usercenter.constant.UserConstant.ADMIN_ROLE;
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

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    //盐值，混淆密码
    private final String SALT = "QinJiu";

    //设置默认头像
    private final String DEFAULT_AVATAR = "https://qinjiu-class-1311121186.cos.ap-beijing.myqcloud.com/com/qinjiu/avatar/default.jpg";

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
        user.setAvatarUrl(DEFAULT_AVATAR);
        user.setUserAccount(userAccount);
        user.setUsername(planet + userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planet);
        boolean saveResult = this.save(user);
        if (!saveResult){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
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
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该用户不存在");

        }


        //用户脱敏,防止数据库字段泄露
        User safetyUser = getSafetUser(user);

        //记录用户登陆态
        request.getSession().setAttribute(USER_LOGIN_STATUS, safetyUser);

        //将该用户缓存到redis
        String redisKey = String.format("QinJiu:matchFriend:userAccount:%s", user.getId());
        ValueOperations<String, String> opsForSet = stringRedisTemplate.opsForValue();
        opsForSet.set(redisKey, userAccount, 86400, TimeUnit.SECONDS);

        return safetyUser;
    }

    @Override
    public User getSafetUser(User originUser) {
        if (originUser == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户信息为空");

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
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    @Override
    public int UserLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATUS);

        return 1;
    }

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList 用户拥有的标签
     * @return 脱敏用户信息
     */
    @Override
    public List<User> searchByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求参数为空");
        }

        //1。 先查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        //在内存中判断是否包含要求的标签
        return userList.stream().filter(user -> {
            String tagStr = user.getTags();
            if (StringUtils.isAllBlank(tagStr)) {
                return false;
            }
            Set<User> tempTagNameSet = gson.fromJson(tagStr, new TypeToken<Set<String>>() {
            }.getType());
            //如果前面的值为空，取到的值就是后面那个
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for (String tagName : tagNameList) {
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetUser).collect(Collectors.toList());

    }

    @Override
    public int updateUser(User user, User loginUser) {

        // todo 用户头像上传不能只存数据库，存在图传或者本地，新用户给默认头像
        Long userId = user.getId();
        if (userId < 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        if (!isAdmin(loginUser) && !userId.equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        Object objUser = request.getSession().getAttribute(USER_LOGIN_STATUS);
        if (objUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH);

        }
        return (User) objUser;
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        //仅管理员课查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATUS);
        User user = (User) userObj;

        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public boolean isAdmin(User loginUser) {
        //仅管理员课查

        return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public List<User> matchUser(int n, User loginUser) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.select("id", "tags");
        wrapper.isNotNull("tags");
        List<User> userList = this.list(wrapper);
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        // 用户列表的下标 =》 相似度
        List<Pair<User, Float>> topList = new ArrayList<>();
        for (User user : userList) {
            String userTags = user.getTags();
            // 无标签
            if (StringUtils.isBlank(userTags) || user.getId().equals(loginUser.getId())) {
                continue;
            }
            List<String> userTagsList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            // 计算相似度
            float similarity = AlgorithmUtils.minDistance(tagList, userTagsList);
            topList.add(new Pair<>(user, similarity));
        }

        List<Pair<User, Float>> maxDistanceIndexList = topList.stream()
                .sorted((a, b) -> (int) (b.getValue() - a.getValue()))
                .limit(n)
                .collect(Collectors.toList());
        List<Long> userIdList = maxDistanceIndexList.stream()
                .map(pair -> pair.getKey().getId())
                .collect(Collectors.toList());
        wrapper = new QueryWrapper<User>().in("id", userIdList);

        Map<Long, List<User>> listMap = this.list(wrapper).stream()
                .map(this::getSafetUser)
                .collect(Collectors.groupingBy(User::getId));
        List<User> finalList = new ArrayList<>();
        for (Long userId : userIdList) {
            finalList.add(listMap.get(userId).get(0));
        }
        return finalList;

    }

    /**
     * 根据标签搜索用户(sql 查询版）
     *
     * @param tagNameList 用户拥有的标签
     * @return 脱敏用户信息
     */
    @Deprecated
    private List<User> searchByTagsBySql(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求参数为空");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //拼接 and 操作
        //like "%java" and "%php%"
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tags", tagName);
        }

        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafetUser).collect(Collectors.toList());


    }


}




