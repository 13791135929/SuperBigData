package com.bigdata.md5s;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * 签名工具类！
 * @author huhong
 * @date 2020年09月24日12:36
 */
public class MD5Signature {

    public static void main(String[] args) throws Exception {

        System.out.println("================================开始遍历================================");

        BufferedReader phone = new BufferedReader(new FileReader("/Users/iCocos/Desktop/BigData/SuperBigData/producer/src/main/java/com/bigdata/md5s/手机号.txt"));
        String lineP = null;
        List<String> listP = new ArrayList<String>();
        while ((lineP = phone.readLine()) != null) {
            String temp = lineP.trim();
            if (temp != null && !"".equals(temp)) {
                listP.add(MD5Utils.stringToMD5(temp));
            }
        }
        String[] phoneArray = (String[]) listP.toArray(new String[listP.size()]);
        System.out.println(phoneArray.length);

        BufferedReader md5s = new BufferedReader(new FileReader("/Users/iCocos/Desktop/BigData/SuperBigData/producer/src/main/java/com/bigdata/md5s/MD5.txt"));
        String lineM = null;
        List<String> listM = new ArrayList<String>();
        while ((lineM = md5s.readLine()) != null) {
            String temp = lineM.trim();
            if (temp != null && !"".equals(temp)) {
                listM.add(temp);
            }
        }
        String[] md5sArray = (String[]) listM.toArray(new String[listM.size()]);
        System.out.println(md5sArray.length);

        System.out.println("================================遍历匹配================================");


        System.out.println("================================开始匹配================================");

        String[] results = getUnion(phoneArray, md5sArray);
        System.out.println("匹配成功个数 = " + results.length);
        for (String i : results) {
            System.out.println(i);
        }

        System.out.println("================================匹配结束================================");

    }

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

}
