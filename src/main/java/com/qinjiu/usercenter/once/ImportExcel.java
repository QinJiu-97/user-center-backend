package com.qinjiu.usercenter.once;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.util.ListUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author QinJiu
 * @Date 2022/8/26
 * 导入数据
 */

public class ImportExcel {
    public static void main(String[] args) {
        // 写法1：JDK8+ ,不用额外写一个XingQiuUserInfoListener
        // since: 3.0.0-beta1
        String fileName = "E:\\星球\\用户中心\\userCenter-backend\\src\\main\\java\\com\\qinjiu\\usercenter\\once\\table1.xlsx";
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
        // 这里每次会读取100条数据 然后返回过来 直接调用使用数据就行
//        readByListener(fileName);
        synchronousRead(fileName);
    }

    public static void readByListener(String fileName){
        EasyExcel.read(fileName, XingQiuUserInfo.class, new TableDataListener()).sheet().doRead();
    }
    
    /**
     * 同步的返回，不推荐使用，如果数据量大会把数据放到内存里面
     */

    public static void synchronousRead(String fileName) {
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<XingQiuUserInfo> list = EasyExcel.read(fileName).head(XingQiuUserInfo.class).sheet().doReadSync();
        for (XingQiuUserInfo xingQiuUserInfo : list) {
            System.out.println(xingQiuUserInfo);
        }
    }
}
