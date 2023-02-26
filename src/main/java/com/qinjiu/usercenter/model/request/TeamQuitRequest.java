package com.qinjiu.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户退出队伍请求类
 * @author QinJiu
 * @Date 2022/9/12
 */
@Data
public class TeamQuitRequest implements Serializable {

    private static final long serialVersionUID = -445351975134215714L;

    /**
     * 队伍id
     */
    private Long teamId;
}
