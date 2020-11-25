package com.bigdata.signs;

import com.bigdata.md5s.MD5Utils;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

/**
 * 签名工具类！
 * @author huhong
 * @date 2020-09-01 15:30
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
public class RsaSignature {

    private static final String CHARSET = "UTF-8";

    private static final String KEY_ALGORITHM = "RSA";

    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    private static final String PRIVATE_KEY = "30820157020100300d06092a864886f70d0101010500048201413082013d020100024100c2f4850e93be547f80c8a36b7a1e089e57d30faa7932acaed2c39f3064413975ff08fb4dfb661580ac05e6d8ecac955b2820acefa78cc4a8a59e5a92fbe0ac230203010001024100ac6389d0b5062533c5ce24eb61c572fb894f61d7ed69c8d6a21a47068470447cad04e3bf406bcf7f2e4db836827beae19b0dd002b47cdecbfc5a71ce3fc0dd91022100f28e9f0441c06f7146c55b61c091e45d1a26acd2d016778ed5635d65c8241b19022100cdc286b69948b44d62138ccd9252eba1fafbd8c00ee403797dee16869332e49b022100aaadb92fa1a4ebc665ea5217430e66072d73b180d67438c5055ada49d8bfaf6102210087d81a208bead83f0eb5618c87427f971da110651c6fa56b9c9c87faa94c3c69022100d6aa5e6b7898ecd3080ff1c1fa95dd996f70be459472cee62a5031bc67059546";

    public static final String PUBLIC_KEY = "305c300d06092a864886f70d0101010500034b003048024100c2f4850e93be547f80c8a36b7a1e089e57d30faa7932acaed2c39f3064413975ff08fb4dfb661580ac05e6d8ecac955b2820acefa78cc4a8a59e5a92fbe0ac230203010001";

    private static Logger logger = LoggerFactory.getLogger(RsaSignature.class);

    /**
     * 校验签名是否正确
     * @param params 参数map
     * @param sign 签名
     * @param publicKey 公钥
     * @return boolean
     * @author huhong
     * @date 2020-09-01 21:06
     */
    public static boolean rsaCheck256V1(Map<String, String> params, String sign, String publicKey) {
        String content = getSignCheckContentV1(params);
        if (StringUtils.isAnyEmpty(content, sign)) {
            return false;
        }
        return rsaCheckContent(content, sign, publicKey);
    }

    /**
     * 创建一个秘钥对
     * @return java.util.Map<java.lang.String       ,       java.lang.String>
     * @author huhong
     * @date 2020-09-01 21:06
     */
    public static Map<String, String> createRsaKey() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            keyPairGenerator.initialize(512);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            RSAPublicKey rsaPublicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyPair.getPrivate();
            String privateKey = Hex.encodeHexString(rsaPrivateKey.getEncoded());
            String publicKey = Hex.encodeHexString(rsaPublicKey.getEncoded());
            Map<String, String> result = new HashMap<>(16);
            result.put("privateKey", privateKey);
            result.put("publicKey", publicKey);

            System.out.println(privateKey);
            System.out.println(publicKey);
            return result;

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static String rsa256Sign(String content) {
        return rsa256Sign(content, PRIVATE_KEY);
    }

    /**
     * 将内容进行签名
     *
     * @param content content
     * @return java.lang.String
     * @author huhong
     * @date 2020-09-01 19:10
     */
    public static String rsa256Sign(String content, String privateKeyStr) {
        try {
            PrivateKey privateKey = getPrivateKeyFromPKCS8(privateKeyStr);
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(content.getBytes(CHARSET));
            byte[] result = signature.sign();
            return Hex.encodeHexString(result);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取私钥 将字符串转为私钥
     *
     * @param privateKey privateKey
     * @return java.security.PrivateKey
     * @author huhong
     * @date 2020-09-01 19:10
     */
    public static PrivateKey getPrivateKeyFromPKCS8(String privateKey) throws Exception {
        if (StringUtils.isEmpty(privateKey)) {
            return null;
        }
        byte[] keyBytes = Hex.decodeHex(privateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return keyFactory.generatePrivate(keySpec);
    }


    public static boolean rsaCheckContent(String content, String sign) {
        return rsaCheckContent(content, sign, PUBLIC_KEY);

    }

    public static boolean rsaCheckContent(String content, String sign, String publicKey) {
        try {
            PublicKey pubKey = getPublicKeyFromX509(publicKey);
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(pubKey);
            signature.update(content.getBytes(CHARSET));

            return signature.verify(Hex.decodeHex(sign));
        } catch (Exception e) {
            logger.error("RSA content = " + content + ",sign=" + sign, e);
            return false;

        }

    }


    private static PublicKey getPublicKeyFromX509(String publicKey) throws Exception {
        byte[] keyBytes;
        keyBytes = Hex.decodeHex(publicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return keyFactory.generatePublic(keySpec);
    }

    private static String getSignCheckContentV1(Map<String, String> params) {
        if (params == null) {
            return null;
        }

        StringBuilder content = new StringBuilder();
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);
            content.append((i == 0 ? "" : "&") + key + "=" + value);
        }

        return content.toString();
    }

    public static void main(String[] args) throws Exception {

        /** 直接测试 */
        //String test = rsa256Sign("18919562953");
        //System.out.println(test);
        //boolean sss = rsaCheckContent("18919562953", test, PUBLIC_KEY);
        //System.out.println(sss);

        /** 加解密测试 */
        //String test = rsa256Sign("测试内容efafssad",PRIVATE_KEY);
        //System.out.println(test);
        //boolean sss = rsaCheckContent("测试内容efafssad", test, PUBLIC_KEY);
        //System.out.println(sss);
        //System.out.println((new BASE64Encoder()).encode(PRIVATE_KEY.getBytes(CHARSET)));

        System.out.println("================================开始遍历================================");

        /**
         * 豌豆思维内部手机号MD5加密
         */
        BufferedReader phone = new BufferedReader(new FileReader("/Users/iCocos/Desktop/BigData/SuperBigData/producer/src/main/java/com/bigdata/signs/regist.txt"));
        String lineP = null;
        List<String> listP = new ArrayList<String>();
        while ((lineP = phone.readLine()) != null) {
            String temp = lineP.trim();
            if (temp != null && !"".equals(temp)) {
                String signs = rsa256Sign(temp,PRIVATE_KEY).toLowerCase();
                //saveAsFileWriter(signs + "\n");
                listP.add(signs);
            }
        }
        String[] phoneArray = (String[]) listP.toArray(new String[listP.size()]);
        System.out.println(phoneArray.length);

        /**
         * 外部撞库手机号Md5码：魔力耳朵
         */
        BufferedReader md5s = new BufferedReader(new FileReader("/Users/iCocos/Desktop/BigData/SuperBigData/producer/src/main/java/com/bigdata/signs/signs.txt"));
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
//        for (String i : results) {
//            System.out.println(i);
//        }

        BufferedReader phone1 = new BufferedReader(new FileReader("/Users/iCocos/Desktop/BigData/SuperBigData/producer/src/main/java/com/bigdata/signs/regist.txt"));
        String lineP1 = null;
        while ((lineP1 = phone1.readLine()) != null) {
            String temp = lineP1.trim();
            if (temp != null && !"".equals(temp)) {
                String signs = rsa256Sign(temp,PRIVATE_KEY).toLowerCase();
                Set<String> set = new HashSet<String>(Arrays.asList(results));
                if (set.contains(signs)) {
                    System.out.println(temp + "   " + signs  + "\n");
                }
            }
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
            fwriter = new FileWriter("/Users/iCocos/Desktop/BigData/SuperBigData/producer/src/main/java/com/bigdata/signs/Phone_signs.txt", true);
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
