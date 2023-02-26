package com.qinjiu.usercenter.service;
import java.util.Date;

import com.qinjiu.usercenter.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

/**
 * @author QinJiu
 * @Date 2022/9/9
 */
@SpringBootTest
public class RedisTest {
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Test
    void test(){
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();

        operations.set("qinjiu1","ok");
        operations.set("qinjiuInt",1);
        User user = new User();
        user.setId(2L);
        user.setUsername("asd");
        user.setUserAccount("sdf");
        user.setAvatarUrl("sdf");

        operations.set("qinjiuUser",user);
    }
}
