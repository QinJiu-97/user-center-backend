package com.qinjiu.usercenter.once;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author QinJiu
 * @Date 2022/8/26
 * 星球表格用户信息
 */
@Data
public class XingQiuUserInfo {
    /**
     * id
     */
    @ExcelProperty("成员编号")
    private String planCode;
    /**
     * 用户昵称
     */
    @ExcelProperty("成员昵称")
    private String username;


}
