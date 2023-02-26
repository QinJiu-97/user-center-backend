package com.qinjiu.usercenter.service;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author QinJiu
 * @Date 2022/9/10
 */
@SpringBootTest
public class RedissonTest {
    @Resource
    private RedissonClient redissonClient;

    @Test
    void test(){
        //list
        RList<String> rList = redissonClient.getList("test_list");
        rList.add("qinjiu12");


        //map
        //set
    }
}
