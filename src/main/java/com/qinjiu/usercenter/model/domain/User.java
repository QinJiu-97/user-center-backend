package com.qinjiu.usercenter.model.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户表
 * @TableName User
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名称

     */
    @TableField(value = "username")
    private String username;

    /**
     * 编号
     */
    @TableField(value = "userAccount")
    private String userAccount;

    /**
     * 用户头像
     */
    @TableField(value = "avatarUrl")
    private String avatarUrl;

    /**
     * 性别

     */
    @TableField(value = "gender")
    private Integer gender;

    /**
     * 用户密码

     */
    @TableField(value = "userPassword")
    private String userPassword;

    /**
     * 电话
     */
    @TableField(value = "phone")
    private String phone;

    /**
     * 邮箱
     */
    @TableField(value = "Email")
    private String email;

    /**
     * 状态 0- 正常
     */
    @TableField(value = "userStatus")
    private Integer userStatus;

    /**
     * 用户身份状态 0- 普通  1- 管理员
     */
    @TableField(value = "userRole")
    private Integer userRole;

    /**
     * 创建时间
     */
    @TableField(value = "createTime")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "updateTime")
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableField(value = "isDelete")
    @TableLogic
    private Integer isDelete;

    /**
     * 星球编号
     */
    @TableField(value = "planetCode")
    private String planetCode;


    /**
     * 标签
     */
    @TableField(value = "tags")
    private String tags;

    /**
     * 个人简介
     */
    @TableField(value = "user_profile")
    private String userProfile;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}