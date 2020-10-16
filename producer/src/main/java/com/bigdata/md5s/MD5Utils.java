package com.bigdata.md5s;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

//使用方式
//String resultString1 = MD5Utils.stringToMD5("1234");
//System.out.println(resultString1);
////81dc9bdb52d04dc20036dbd8313ed055
public class MD5Utils {

    public static String stringToMD5(String plainText) {
        byte[] secretBytes = null;
        try {
            secretBytes = MessageDigest.getInstance("md5").digest(
                    plainText.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("没有这个md5算法！");
        }
        String md5code = new BigInteger(1, secretBytes).toString(16);
        for (int i = 0; i < 32 - md5code.length(); i++) {
            md5code = "0" + md5code;
        }
        return md5code;
    }

}

//方法二
//spring自带的工具DigestUtils实现
//org.springframework.util.DigestUtils
//DigestUtils.md5DigestAsHex("1234".getBytes())