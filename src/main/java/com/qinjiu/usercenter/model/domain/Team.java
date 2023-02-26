package com.qinjiu.usercenter.model.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 队伍表
 * @TableName team
 */
@TableName(value ="team")
@Data
public class Team implements Serializable {
    /**
     * 队伍id
     */
    @TableId(value = "teamId", type = IdType.AUTO)
    private Long teamId;

    /**
     * 队伍名称
     */
    @TableField(value = "teamName")
    private String teamName;

    /**
     * 队伍描述
     */
    @TableField(value = "teamDesc")
    private String teamDesc;

    /**
     * 创建人id
     */
    @TableField(value = "userId")
    private Long userId;

    /**
     * 最大人数
     */
    @TableField(value = "maxNum")
    private Integer maxNum;

    /**
     * 过期时间
     */
    @TableField(value = "expireTime")
    private Date expireTime;

    /**
     * 队伍密码
     */
    @TableField(value = "teamPassword")
    private String teamPassword;

    /**
     * 队伍状态：0-公开，1-私有，2-加密
     */
    @TableField(value = "teamStatus")
    private Integer teamStatus;

    /**
     * 队伍创建时间
     */
    @TableField(value = "createTime")
    private Date createTime;

    /**
     * 队伍更新时间
     */
    @TableField(value = "updateTime")
    private Date updateTime;

    /**
     * 是否删除：0-未删，1-已删
     */
    @TableField(value = "isDelete")
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}