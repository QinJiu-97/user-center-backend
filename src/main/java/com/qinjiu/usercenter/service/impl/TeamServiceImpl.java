package com.qinjiu.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qinjiu.usercenter.common.ErrorCode;
import com.qinjiu.usercenter.exception.BusinessException;
import com.qinjiu.usercenter.mapper.TeamMapper;
import com.qinjiu.usercenter.model.DTO.TeamQuery;
import com.qinjiu.usercenter.model.Enums.TeamStatusEnum;
import com.qinjiu.usercenter.model.domain.Team;
import com.qinjiu.usercenter.model.domain.User;
import com.qinjiu.usercenter.model.domain.UserTeam;
import com.qinjiu.usercenter.model.request.TeamJoinRequest;
import com.qinjiu.usercenter.model.request.TeamQuitRequest;
import com.qinjiu.usercenter.model.request.TeamUpdateRequest;
import com.qinjiu.usercenter.model.vo.TeamUserVO;
import com.qinjiu.usercenter.model.vo.UserVO;
import com.qinjiu.usercenter.service.TeamService;
import com.qinjiu.usercenter.service.UserService;
import com.qinjiu.usercenter.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author QinJiu
 * @description 针对表【team】的数据库操作Service实现
 * @createDate 2022-09-10 19:59:21
 */
@Service
@Slf4j
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        //校验信息
        // 1. 队伍人数
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "队伍人数不满足要求");
        }
        // 2. 队伍标题
        String teamName = team.getTeamName();
        if (StringUtils.isBlank(teamName) || teamName.length() < 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "标题字数不满足要求");

        }
        final Long userId = loginUser.getId();
        // 3. 队伍描述
        String description = team.getTeamDesc();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "队伍描述不满足要求");

        }
        // 4. status状态 0-公开，1-私有
        int status = Optional.ofNullable(team.getTeamStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "队伍状态不满足要求");
        }
        // 5. 加密
        String password = team.getTeamPassword();
        if (TeamStatusEnum.ENCRYPTION.equals(statusEnum) && (StringUtils.isBlank(password) || password.length() < 1)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码设置不满足要求");
        }
        // 6. 当前时间 > 超时时间
        Date expireTime = team.getExpireTime();
        if (expireTime != null && new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "当前时间 > 超时时间");
        }
        // 7. 校验用户最多有5个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long countTeamNum = this.count(queryWrapper);
        if (countTeamNum >= 5) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "已达到最大创建队伍数量");
        }
        // 8. 插入队伍信息到队伍表中

        team.setTeamId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getTeamId();
        if (!result) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "创建队伍失败");
        }
        // 9. 插入信息到 用户队伍关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setId(0L);
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "创建队伍失败");
        }

        return teamId;
    }

    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        // 1. 组合条件查询
        if (teamQuery != null) {
            //根据 队伍id 查询队伍
            Long teamId = teamQuery.getTeamId();
            if (teamId != null && teamId > 0) {
                queryWrapper.eq("teamId", teamId);
            }
            //根据id列表查询
            List<Long> idList = teamQuery.getIdList();
            if (CollectionUtils.isNotEmpty(idList)) {
                queryWrapper.in("teamId", idList);
            }
            //根据关键词搜索
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw.like("teamDesc", searchText).or().like("teamName", searchText));
            }
            //根据队伍描述查询队伍
            String teamDesc = teamQuery.getTeamDesc();
            if (StringUtils.isNotBlank(teamDesc)) {
                queryWrapper.like("teamDesc", teamDesc);
            }
            // 根据队伍名来查询用户
            String teamName = teamQuery.getTeamName();
            if (StringUtils.isNotBlank(teamName)) {
                queryWrapper.like("teamName", teamName);
            }
            //根据队伍状态查询
            Integer teamStatus = teamQuery.getTeamStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamStatus);
            if (statusEnum == null) {
                statusEnum = TeamStatusEnum.PUBLIC;
            }
            if (statusEnum.equals(TeamStatusEnum.PRIVATE) && !isAdmin) {
                throw new BusinessException(ErrorCode.NO_AUTH, "没有权限访问");
            }
            queryWrapper.eq("teamStatus", statusEnum.getValue());

            //根据队伍最大人数查询
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxNum", maxNum);
            }
            //根据队伍创建人 id 搜索
            Long userId = teamQuery.getUserId();
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }
        }

        // 已过期的队伍不展示,仅展示未过期的队伍
        //expire is  null or expire > now()
        queryWrapper.and(qw -> qw.isNull("expireTime").or().gt("expireTime", new Date()));
        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        //关联查询创建人的信息
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }

            User user = userService.getById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            //脱敏用户信息

            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }

        return teamUserVOList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Long teamId = teamUpdateRequest.getTeamId();
        if (teamId == null || teamId < 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        Team oldTeam = this.getById(teamId);
        if (oldTeam == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "没有该队伍");
        }
        //只有管理员和自己能更新信息
        if (!oldTeam.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "没有权限操作");
        }
        //将其他房间更新为加密房间
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getTeamStatus());
        if (statusEnum.equals(TeamStatusEnum.ENCRYPTION) && StringUtils.isBlank(teamUpdateRequest.getTeamPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "加密房间要设置密码");
        }
        //将加密房间更新为其他房间
        if (!statusEnum.equals(TeamStatusEnum.ENCRYPTION)) {
            teamUpdateRequest.setTeamPassword("");
        }
        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, updateTeam);
        return this.updateById(updateTeam);
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        Long teamId = teamJoinRequest.getTeamId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }

        if (team.getExpireTime() != null && team.getExpireTime().before(new Date())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该队伍已经过期");
        }

        Integer status = team.getTeamStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(teamStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "禁止加入私有队伍");
        }
        String password = teamJoinRequest.getTeamPassword();
        if (TeamStatusEnum.ENCRYPTION.equals(teamStatusEnum)) {
            if (password.length() != 4) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "请输入正确长度的密码");
            }
            if (StringUtils.isBlank(password) || !password.equals(team.getTeamPassword())) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "房间密码错误");
            }
        }
        // todo 解决不同用户抢同一个锁的问题，应该只锁同一个用户的多次请求
        // 使用redis 的setnx，以用户账号作为key，队伍信息为val ,缺点：过期时间不好控制，容易引发一系列问题
        // 使用Reidsson 的 getLock("xxx")
        // 1.获取锁，只要锁的名字一样，获取到的锁就是同一把锁。
        String userAccount = String.format("QinJiu:matchFriend:userAccount:%s", loginUser.getId());
        RLock lock = redissonClient.getLock(userAccount);
        log.info("======线程id："+Thread.currentThread().getId()+"=======获取到锁："+lock);

        try {
            // 这里要根据实际业务使用isLocked()
            if (lock.tryLock(0, -1, TimeUnit.SECONDS)) {
                //该用户已经加入的队伍数量
                Long userId = loginUser.getId();
                QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("userId", userId);
                long teamNum = userTeamService.count(queryWrapper);
                if (teamNum >= 5) {
                    throw new BusinessException(ErrorCode.PARAM_ERROR, "最多加入或创建 5 个队伍");
                }
                //不能重复加入已经加入的队伍
                queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("userId", userId);
                queryWrapper.eq("teamId", teamId);
                long hasUserJoinTeam = userTeamService.count(queryWrapper);
                if (hasUserJoinTeam > 0) {
                    throw new BusinessException(ErrorCode.PARAM_ERROR, "你已经在该队伍中");
                }
                //该队伍的人数
                Long userNum = this.countTeamUserById(teamId);
                if (userNum >= team.getMaxNum()) {
                    throw new BusinessException(ErrorCode.PARAM_ERROR, "人数已满");
                }
                //修改队伍信息
                UserTeam userTeam = new UserTeam();
                userTeam.setUserId(userId);
                userTeam.setTeamId(teamId);
                userTeam.setJoinTime(new Date());
                Thread.sleep(10000);
                return userTeamService.save(userTeam);
            }else {
                log.info(Thread.currentThread().getId()+">>>>>>>>>>没有成功上锁");
            }

        } catch (InterruptedException e) {
            System.out.println("获取锁异常");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                log.info("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }

        }
        return false;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Long teamId = teamQuitRequest.getTeamId();
        Team team = getTeamById(teamId);
        Long userId = loginUser.getId();
        UserTeam queryUserTeam = new UserTeam();
        queryUserTeam.setTeamId(teamId);
        queryUserTeam.setUserId(userId);
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>(queryUserTeam);
        long count = userTeamService.count(userTeamQueryWrapper);
        if (count < 0) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "未加入该队伍");
        }
        Long teamJoinNum = this.countTeamUserById(teamId);
        //队伍只剩一人就解散
        if (teamJoinNum == 1) {
            //从team表和 userTeam表中删除
            this.removeById(teamId);
        } else {
            //为队长
            //至少还剩两人
            if (team.getUserId().equals(userId)) {
                //查询已加入的所有用户的加入时间
                QueryWrapper<UserTeam> wrapper = new QueryWrapper<>();
                wrapper.eq("teamId", teamId).last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(wrapper);
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                //取出下一个人设置为队长
                Long nextHeader = userTeamList.get(1).getUserId();
                Team updateTeam = new Team();
                updateTeam.setTeamId(teamId);
                updateTeam.setUserId(nextHeader);
                boolean result = this.updateById(updateTeam);
                if (!result) {
                    throw new BusinessException(ErrorCode.PARAM_ERROR, "更新队长失败");
                }
            }

        }
        //移除关系
        return userTeamService.remove(userTeamQueryWrapper);

    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(long teamId, User loginUser) {
        //1. 校验队伍是否存在
        Team teamById = this.getTeamById(teamId);
        //校验当前用户是否为队长
        if (!teamById.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "仅队长能解散队伍");
        }
        // 移除所有队伍关联信息
        QueryWrapper<UserTeam> wrapper = new QueryWrapper<>();
        wrapper.eq("teamId", teamId);
        boolean result = userTeamService.remove(wrapper);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "移除队伍关联信息失败");
        }
        // 删除队伍
        return this.removeById(teamId);
    }


    /**
     * 根据队伍id获取队伍
     *
     * @param teamId
     * @return
     */
    private Team getTeamById(Long teamId) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return team;
    }

    /**
     * 获取某队伍当前人数
     *
     * @param teamId
     * @return
     */
    private Long countTeamUserById(Long teamId) {
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId);
        return userTeamService.count(queryWrapper);
    }
}




















