package com.qinjiu.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qinjiu.usercenter.model.domain.UserTeam;
import com.qinjiu.usercenter.service.UserTeamService;
import com.qinjiu.usercenter.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author QinJiu
* @description 针对表【user_team(用户队伍关系表)】的数据库操作Service实现
* @createDate 2022-09-10 20:13:05
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




