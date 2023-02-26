package com.qinjiu.usercenter.model.DTO;

import com.qinjiu.usercenter.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * 队伍查询封装类
 *
 * @author QinJiu
 * @Date 2022/9/10
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TeamQuery extends PageRequest {
    /**
     * 队伍id
     */
    private Long teamId;

    /**
     * 队伍id列表
     */
    private List<Long> idList;

    /**
     * 队伍名称
     */
    private String teamName;

    /**
     * 搜索关键字（同时对队伍描述和名字搜索）
     */
    private String searchText;

    /**
     * 队伍描述
     */
    private String teamDesc;

    /**
     * 创建人id
     */
    private Long userId;

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

    /**
     * 队伍状态：0-公开，1-私有，2-加密
     */
    private Integer teamStatus;



}
