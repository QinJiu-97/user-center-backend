package com.qinjiu.usercenter.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author QinJiu
 * @Date 2022/9/11
 */
@Data
public class UserVO implements Serializable {


    private static final long serialVersionUID = 9154662229344857323L;
    /**
     * id
     */
    private Long id;

    /**
     * 用户名称

     */
    private String username;

    /**
     * 编号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 用户密码
     */
    private String userPassword;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态 0- 正常
     */
    private Integer userStatus;

    /**
     * 用户身份状态 0- 普通  1- 管理员
     */
    private Integer userRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


    /**
     * 星球编号
     */
    private String planetCode;


    /**
     * 标签
     */
    private String tags;

    /**
     * 个人简介
     */
    private String userProfile;
}
