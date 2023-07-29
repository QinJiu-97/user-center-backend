package com.qinjiu.usercenter.utils;

import java.util.List;
import java.util.Objects;

/**
 * @author QinJiu
 * @Date 2022/10/22
 */
public class AlgorithmUtils {


    public static float minDistance(List<String> tags1, List<String> tags2) {
        //计算两个字符串的长度。
        int len1 = tags1.size();
        int len2 = tags2.size();
        //建立上面说的数组，比字符长度大一个空间
        int[][] dif = new int[len1 + 1][len2 + 1];
        //赋初值，步骤B。
        for (int a = 0; a <= len1; a++) {
            dif[a][0] = a;
        }
        for (int a = 0; a <= len2; a++) {
            dif[0][a] = a;
        }


        int temp;
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                if (Objects.equals(tags1.get(i - 1), tags2.get(j - 1))) {
                    temp = 0;
                } else {
                    temp = 1;
                }
                //取三个值中最小的
                int temp1 = dif[i - 1][j - 1] + temp;
                int temp2 = dif[i][j - 1] + 1;
                int temp3 = dif[i - 1][j] + 1;
                int[] arr = new int[]{temp1, temp2, temp3};

                dif[i][j] = min(arr);
            }
        }
        //计算相似度
        return 1 - (float) dif[len1][len2] / Math.max(len1, len2);
    }

    //得到最小值
    private static int min(int[] arr) {
        int min = arr[0];
        for (int i : arr) {
            if (min > i) {
                min = i;
            }
        }
        return min;
    }

}
