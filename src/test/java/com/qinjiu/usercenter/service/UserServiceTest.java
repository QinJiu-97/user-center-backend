package com.qinjiu.usercenter.service;
import java.util.Date;


import com.qinjiu.usercenter.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

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
    public void TestAddUser(){
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
    void userRegister(){
        String userAccount = "qinjiu333";
        String password = "12345678";
        String checkPassword = "12345678";
        String planet = "285";
        long result = userService.UserRegister(userAccount,password,checkPassword,planet);
        assertEquals(-1, result);


    }

}