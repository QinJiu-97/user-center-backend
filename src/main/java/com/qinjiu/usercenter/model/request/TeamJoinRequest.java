package com.qinjiu.usercenter.model.request;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * 队伍添加请求类
 * @author QinJiu
 * @Date 2022/9/10
 */
@Data
public class TeamJoinRequest implements Serializable {

    private static final long serialVersionUID = -6532464840778216725L;


    /**
     * 队伍id
     */
    private Long teamId;

    /**
     * 队伍密码
     */
    private String teamPassword;


}
