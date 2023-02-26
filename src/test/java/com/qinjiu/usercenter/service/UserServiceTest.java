package com.qinjiu.usercenter.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;


import com.qinjiu.usercenter.mapper.UserMapper;
import com.qinjiu.usercenter.model.domain.User;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author QinJiu
 * @date 2022/3/13
 */
@SpringBootTest
public class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    public void TestAddUser() {
        User user = new User();
        user.setUsername("qinjiu");
        user.setUserAccount("1234");
        user.setAvatarUrl("41234");
        user.setGender(0);
        user.setUserPassword("xxx");
        user.setPhone("1231");
        user.setEmail("1231");
        user.setUserStatus(0);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setIsDelete(0);

        boolean result = userService.save(user);
        System.out.println(user.getId());
        assertTrue(result);

    }

    @Test
    void userRegister() {
        String userAccount = "qinjiu333";
        String password = "12345678";
        String checkPassword = "12345678";
        String planet = "285";
        long result = userService.UserRegister(userAccount, password, checkPassword, planet);
        assertEquals(-1, result);


    }

    @Test
    public void searchByTags() {
        List<String> list = Arrays.asList("java", "c++", "python");
        List<User> users = userService.searchByTags(list);
        assertNotNull(users);

    }

    @Resource
    UserMapper userMapper;

    /**
     * 批量插入用户
     */
    @Test

    public void insertUser() {
        final int INSERT_NUM = 300050;
        StopWatch stopWatch = new StopWatch();
        List<User> userList = new ArrayList<>();
        stopWatch.start();
        for (int i = 50 ; i <= INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("假用户" + i);
            user.setUserAccount("fakeUser" + i);
            user.setAvatarUrl("https://xingqiu-tuchuang-1256524210.cos.ap-shanghai.myqcloud.com/285/picture微信图片_20220330160120.jpg");
            user.setGender(0);
            user.setUserPassword("37a674446dbc187b548bf13df7279642");
            user.setPhone("123" + i);
            user.setEmail(i + ".com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setIsDelete(0);
            user.setPlanetCode(i + "50");
            user.setTags("[\"java\",\"c++\"]");
            user.setUserProfile("这是假用户" + i);
            userList.add(user);
        }
        userService.saveBatch(userList,10000);
        stopWatch.stop();//25.9s

    }
    /**
     * 异步批量插入用户
     */
    @Test

    public void ConcurrencyInsertUser() {
        final int INSERT_NUM = 300000;
        StopWatch stopWatch = new StopWatch();

        stopWatch.start();
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        int i = 0;
        //分10组异步任务，一组30000个操作
        for (int j = 0; j < 10; j++) {
            List<User> userList = new ArrayList<>();
            do {
                i++;
                User user = new User();
                user.setUsername("假用户" + i);
                user.setUserAccount("fakeUser" + i);
                user.setAvatarUrl("https://xingqiu-tuchuang-1256524210.cos.ap-shanghai.myqcloud.com/285/picture微信图片_20220330160120.jpg");
                user.setGender(0);
                user.setUserPassword("37a674446dbc187b548bf13df7279642");
                user.setPhone("123" + i);
                user.setEmail(i + ".com");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setIsDelete(0);
                user.setPlanetCode(i + "50");
                user.setTags("[\"java\",\"c++\"]");
                user.setUserProfile("这是假用户" + i);
                userList.add(user);
            } while (i % 30000 != 0);

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> userService.saveBatch(userList,10000));
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();


        stopWatch.stop();//7s

    }

}