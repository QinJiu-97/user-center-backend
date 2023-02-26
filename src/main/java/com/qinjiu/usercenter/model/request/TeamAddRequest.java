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
public class TeamAddRequest implements Serializable {

    private static final long serialVersionUID = -6532464840778216725L;

    /**
     * 队伍名称
     */
    private String teamName;

    /**
     * 队伍描述
     */
    private String teamDesc;

    /**
     * 创建人id
     */
    private Long userId;

    /**
     * 队伍状态：0-公开，1-私有，2-加密
     */
    private Integer teamStatus;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 队伍密码
     */
    private String teamPassword;


}
