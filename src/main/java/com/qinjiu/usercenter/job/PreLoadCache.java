package com.qinjiu.usercenter.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qinjiu.usercenter.common.ResultUtils;
import com.qinjiu.usercenter.model.domain.User;
import com.qinjiu.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 提前预热缓存
 *
 * @author QinJiu
 * @Date 2022/9/9
 */
@Component
@Slf4j
public class PreLoadCache {
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 重点用户，先写死，后面再修改
     */
    private List<Long> userList = Arrays.asList(1L, 2L, 3L);

    /**
     * 提前加载用户
     */
    @Scheduled(cron = "0 49 17 * * *")
    public void doPreLoadCacheForRecommendUser() {
        RLock rLock = redissonClient.getLock("QinJiu:matchFriend:doCache:lock");

        try {
            /*
                tryLock
                第一个参数是最大等待时间
                第二个参数是过期时间，如果自己设置了过期时间则不会激活续期，
                线程挂掉（debug也会被当作挂掉）也不会续期；
                其他情况下 默认过期时间 30 秒，期间会每 10 秒检测一次程序运行状态，如果没挂就续期到 30 秒
             */
            if (rLock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                log.info("getLock: " + Thread.currentThread().getId());
                for (Long userId : userList) {
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
                    String redisKey = String.format("QinJiu:matchFriend:recommend:%s", userId);
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    //写缓存
                    try {
                        valueOperations.set(redisKey, userPage, 86400, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        log.info("redis set key error", e);
                    }
                }
            }

        } catch (InterruptedException e) {
            log.error("doPreLoadCacheForRecommendUser error :" , e);
        }finally {
            if(rLock.isHeldByCurrentThread()){
                log.info("unLock: " + Thread.currentThread().getId());
                rLock.unlock();
            }
        }


    }
}
