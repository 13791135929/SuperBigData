package com.bigdata.md5s;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * 签名工具类！
 * @author huhong
 * @date 2020年09月24日12:36
 *
 * -- 豌豆付费用户数（剔除退费）: 125266
 * SELECT COUNT(DISTINCT user_id) from bdl_online.fact_order where pay_status in (1) and package_course_type = 1;
 *
 * -- 豌豆付费用户数（不剔除退费: 132753
 * SELECT COUNT(DISTINCT user_id) from bdl_online.fact_order where pay_status in (1,3) and package_course_type = 1;
 *
 * SELECT b.mobile from (
 * SELECT user_id from bdl_online.fact_order where pay_status in (1) and package_course_type = 1 GROUP BY user_id
 * ) as a
 * LEFT JOIN bdl_online.fact_register as b on a.user_id = b.user_id;
 *
 */
public class MD5Signature {

    public static void main(String[] args) throws Exception {

        System.out.println("================================开始遍历================================");

        /**
         * 豌豆思维内部手机号MD5加密
         */
        BufferedReader phone = new BufferedReader(new FileReader("/Users/iCocos/Desktop/BigData/SuperBigData/producer/src/main/java/com/bigdata/md5s/Phone_orgs.txt"));
        String lineP = null;
        List<String> listP = new ArrayList<String>();
        while ((lineP = phone.readLine()) != null) {
            String temp = lineP.trim();
            if (temp != null && !"".equals(temp)) {
                String md5_wd = MD5Utils.stringToMD5(temp).toLowerCase();
                saveAsFileWriter(md5_wd + "\n");
                listP.add(md5_wd);
            }
        }
        String[] phoneArray = (String[]) listP.toArray(new String[listP.size()]);
        System.out.println(phoneArray.length);

        /**
         * 外部撞库手机号Md5码：魔力耳朵
         */
        BufferedReader md5s = new BufferedReader(new FileReader("/Users/iCocos/Desktop/BigData/SuperBigData/producer/src/main/java/com/bigdata/md5s/MD5_bind.txt"));
        String lineM = null;
        List<String> listM = new ArrayList<String>();
        while ((lineM = md5s.readLine()) != null) {
            String temp = lineM.trim();
            if (temp != null && !"".equals(temp)) {
                listM.add(temp.toLowerCase());
            }
        }
        String[] md5sArray = (String[]) listM.toArray(new String[listM.size()]);
        System.out.println(md5sArray.length);

        System.out.println("================================遍历匹配================================");


        System.out.println("================================开始匹配================================");

        /**
         * 手机号Md5匹配结果
         */
        String[] results = getUnion(phoneArray, md5sArray);
        System.out.println("匹配成功个数 = " + results.length);
        for (String i : results) {
            System.out.println(i);
        }

        System.out.println("================================匹配结束================================");

    }

    /**
     * 数据匹配结果，使用HashSet加速匹配
     */
    private static String[] getUnion(String[] m, String[] n) {
        List<String> rs = new ArrayList<String>();
        // 将较长的数组转换为set
        Set<String> set = new HashSet<String>(Arrays.asList(m.length > n.length ? m : n));
        // 遍历较短的数组，实现最少循环
        for (String i : m.length > n.length ? n : m) {
            if (set.contains(i)) {
                rs.add(i);
            }
        }
        String[] arr = {};
        return rs.toArray(arr);
    }

    /**
     * 将豌豆思维内部手机号加密结果写到文件，用于测试与结果校验
     */
    private static void saveAsFileWriter(String content) {
        FileWriter fwriter = null;
        try {
            // true表示不覆盖原来的内容，而是加到文件的后面。若要覆盖原来的内容，直接省略这个参数就好
            fwriter = new FileWriter("/Users/iCocos/Desktop/BigData/SuperBigData/producer/src/main/java/com/bigdata/md5s/Phone_md5.txt", true);
            fwriter.write(content);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                fwriter.flush();
                fwriter.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}
