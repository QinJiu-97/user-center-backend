package com.qinjiu.usercenter.once;

import com.alibaba.excel.EasyExcel;
import org.apache.commons.lang3.StringUtils;


import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author QinJiu
 * @Date 2022/8/26
 */
public class ImportExcelUser {
    public static void main(String[] args) {
        String fileName = "E:\\星球\\用户中心\\userCenter-backend\\src\\main\\java\\com\\qinjiu\\usercenter\\once\\table1.xlsx";
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<XingQiuUserInfo> list = EasyExcel.read(fileName).head(XingQiuUserInfo.class).sheet().doReadSync();
        System.out.println("用户总数" + list.size());
        Map<String, List<XingQiuUserInfo>> collect = list.stream()
                .filter(userInfo -> StringUtils.isEmpty(userInfo.getUsername()))
                .collect(Collectors.groupingBy(XingQiuUserInfo::getUsername));
    }
}
