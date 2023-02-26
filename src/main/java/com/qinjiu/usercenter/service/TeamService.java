package com.qinjiu.usercenter.service;

import com.qinjiu.usercenter.model.DTO.TeamQuery;
import com.qinjiu.usercenter.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qinjiu.usercenter.model.domain.User;
import com.qinjiu.usercenter.model.request.TeamJoinRequest;
import com.qinjiu.usercenter.model.request.TeamQuitRequest;
import com.qinjiu.usercenter.model.request.TeamUpdateRequest;
import com.qinjiu.usercenter.model.vo.TeamUserVO;

import java.util.List;

/**
* @author QinJiu
* @description 针对表【team】的数据库操作Service
* @createDate 2022-09-10 19:59:21
*/
public interface TeamService extends IService<Team> {

    /**
     * 添加队伍
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);

    /**
     * 搜索队伍
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery,boolean isAdmin);

    /**
     * 更新队伍信息
     * @param teamUpdateRequest
     * @param loginUser 当前登录用户
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest,User loginUser);

    /**
     * 加入队伍
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 队长解散队伍
     * @param id
     * @param loginUser
     * @return
     */
    boolean deleteTeam(long id, User loginUser);
}
