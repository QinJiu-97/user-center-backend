package com.qinjiu.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用分页请求参数
 * @author QinJiu
 * @Date 2022/9/10
 */
@Data
public class PageRequest implements Serializable {


    private static final long serialVersionUID = -665160406144690818L;
    /**
     * 页面大小
     */
    private int pageSize = 10;

    /**
     * 当前页面
     */
    private int pageNum = 1;
}
