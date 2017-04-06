package com.seeyon.v3x.dee.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.SecureRandom;
import java.security.Security;

/**
 * DES加密
 *
 * @author zhangfb
 */
public class DesUtil {
	private static Log log = LogFactory.getLog(DesUtil.class);
    private Key key;

    public DesUtil() {
    }

    public DesUtil(String str) {
        setKey(str);        // 生成密匙
    }

    /**
     * 根据参数生成KEY
     *
     * @param strKey 参数
     */
    public void setKey(String strKey) {
		// 加载sun的provider
		if (null == Security.getProvider("SunJCE")) {
			Security.addProvider(new com.sun.crypto.provider.SunJCE());
		}
		if (null == Security.getProvider("SUN")) {
			Security.addProvider(new sun.security.provider.Sun());
		}
        try {
        	//指定调用sun的KeyGenerator
            KeyGenerator _generator = KeyGenerator.getInstance("DES","SunJCE");
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG","SUN");
            secureRandom.setSeed(strKey.getBytes());
            _generator.init(56, secureRandom);
//            _generator.init(new SecureRandom(strKey.getBytes()));
            this.key = _generator.generateKey();
            _generator = null;
        } catch (Exception e) {
            throw new RuntimeException("Error initializing SqlMap class. Cause: " + e);
        }
    }

    /**
     * 加密，String明文输入，String密文输出
     *
     * @param strMing String明文
     * @return String密文
     */
    public String encryptStr(String strMing) {
        BASE64Encoder base64en = new BASE64Encoder();
        try {
            byte[] byteMing = strMing.getBytes("UTF8");
            byte[] byteMi = this.encryptByte(byteMing);
            return base64en.encode(byteMi);
        } catch (Exception e) {
            throw new RuntimeException("Error initializing SqlMap class. Cause: " + e);
        }
    }

    /**
     * 解密，以String密文输入，String明文输出
     *
     * @param strMi String密文
     * @return String明文
     */
    public String decryptStr(String strMi) {
        BASE64Decoder base64De = new BASE64Decoder();
        try {
            byte[] byteMi = base64De.decodeBuffer(strMi);
            byte[] byteMing = this.decryptByte(byteMi);
            return new String(byteMing, "UTF8");
        } catch (Exception e) {
            throw new RuntimeException("Error initializing SqlMap class. Cause: " + e);
        }
    }

    /**
     * 加密，以byte[]明文输入，byte[]密文输出
     *
     * @param byteMing byte明文
     * @return byte密文
     */
    private byte[] encryptByte(byte[] byteMing) {
        try {
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(byteMing);
        } catch (Exception e) {
            throw new RuntimeException("Error initializing SqlMap class. Cause: " + e);
        }
    }

    /**
     * 解密，以byte[]密文输入，以 byte[]明文输出
     *
     * @param byteMi byte密文
     * @return byte明文
     */
    private byte[] decryptByte(byte[] byteMi) {
        try {
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(byteMi);
        } catch (Exception e) {
            throw new RuntimeException("Error initializing SqlMap class. Cause: " + e);
        }
    }

    /**
     * 文件加密，文件“file”进行加密并保存目标文件“destFile”中
     *
     * @param file     要加密的文件 如 c:/test/srcFile.txt
     * @param destFile 加密后存放的文件名 如 c:/ 加密后文件 .txt
     */
    public void encryptFile(String file, String destFile) throws Exception {
       
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.ENCRYPT_MODE, this.key);
        InputStream is = null;
        OutputStream out = null;
        CipherInputStream cis = null;

        try {
            is = new FileInputStream(file);
            out = new FileOutputStream(destFile);
            cis = new CipherInputStream(is, cipher);
            byte[] buffer = new byte[1024];
            int r;
            while ((r = cis.read(buffer)) > 0) {
                out.write(buffer, 0, r);
            }
        } catch (IOException e) {
            log.error("输入输出流读写异常：" + e);
        } finally {
            if (null != cis) {
                cis.close();
            }
            if (null != is) {
                is.close();
            }
            if (null != out) {
                out.close();
            }
        }

    }

    /**
     * 文件解密，文件采用“DES”算法解密文件
     *
     * @param file     已加密的文件 如 c:/ 加密后文件 .txt *
     * @param destFile 解密后存放的文件名 如 c:/ test/ 解密后文件 .txt
     */
    public void decryptFile(String file, String destFile) throws Exception {
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.DECRYPT_MODE, this.key);
        InputStream is = null;
        OutputStream out = null;
        CipherOutputStream cos = null;

        try {
            is = new FileInputStream(file);
            out = new FileOutputStream(destFile);
            cos = new CipherOutputStream(out, cipher);
            byte[] buffer = new byte[1024];
            int r;
            while ((r = is.read(buffer)) >= 0) {
                cos.write(buffer, 0, r);
            }
        } catch (IOException e) {
    		log.error("错误信息："+e.getMessage()); 
        } finally {
            if (null != cos) {
                cos.close();
            }
            if (null != out) {
                out.close();
            }
            if (null != is) {
                is.close();
            }
        }
    }

    /**
     * 文件解密，根据加密后的文件，生成解密后的文件，然后载入成byte数组。
     *
     * @param path 源文件路径
     * @return byte数组
     * @throws Exception
     */
    public byte[] decryptFile(String path) throws Exception {
        String targetPath = path + ".bak";
        decryptFile(path, targetPath);
        InputStream is = null;
        ByteArrayOutputStream baos = null;

        try {
            is = new FileInputStream(targetPath);
            baos = new ByteArrayOutputStream();
            int bufferSize = 4096;
            byte[] buffer = new byte[bufferSize];
            int bytesNumRead = 0;
            while ((bytesNumRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesNumRead);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("输入输出流读写异常：" + e);
        } finally {
            if (null != is) {
                is.close();
            }
            if (null != baos) {
                baos.close();
            }
        }


        return null;
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }
}
