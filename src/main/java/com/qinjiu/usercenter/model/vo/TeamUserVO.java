package com.qinjiu.usercenter.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 队伍和用户封装类
 *
 * @author QinJiu
 * @Date 2022/9/11
 */
@Data
public class TeamUserVO implements Serializable {


    private static final long serialVersionUID = -5638161211210079976L;


    /**
     * 用户id
     */
    private Long userId;

    /**
     * 队伍id
     */
    private Long teamId;

    /**
     * 队伍名称
     */
    private String teamName;

    /**
     * 队伍描述
     */
    private String teamDesc;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 队伍状态：0-公开，1-私有，2-加密
     */
    private Integer teamStatus;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建人信息
     */
    UserVO createUser;

    /**
     * 用户是否已经加入队伍
     */
    private boolean hasJoin;

    /**
     * 已加入队伍的人数
     */
    private Integer hasJoinNum;
}
