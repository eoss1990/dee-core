package com.seeyon.v3x.dee.util;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileDigest {
	private static Log log = LogFactory.getLog(FileDigest.class);
	public static final String ALGORITHMS_MD2 = "MD2";
	public static final String ALGORITHMS_MD5 = "MD5";
	public static final String ALGORITHMS_SHA = "SHA";
	public static final String ALGORITHMS_SHA256 = "SHA-256";
	public static final String ALGORITHMS_SHA384 = "SHA-384";
	public static final String ALGORITHMS_SHA512 = "SHA-512";

	/**
	 * 取得文件的MD5码
	 * 
	 * @param file
	 * @return
	 */
	public static String getFileDigest(File file, String algorithms) {
		if (!file.isFile()) {
			return null;
		}
		MessageDigest digest = null;
		FileInputStream in = null;
		byte buffer[] = new byte[1024];
		int len;
		try {
			digest = MessageDigest.getInstance(algorithms);
			in = new FileInputStream(file);
			while ((len = in.read(buffer, 0, 1024)) != -1) {
				digest.update(buffer, 0, len);
			}
			in.close();
		} catch (Exception e) {
            log.error("输入输出流读写异常：" + e);
			return null;
		}
		BigInteger bigInt = new BigInteger(1, digest.digest());
		return bigInt.toString(16);
	}

	/**
	 * 取得目录下所有文件的MD5码并保存到Map里，文件完整路径为key，md5为value
	 * 
	 * @param file
	 *            指定目录
	 * @param listChild
	 *            是否包括子目录
	 * @return
	 */
	public static Map<String, String> getDirDigest(File file,
			boolean listChild, String algorithms) {
		if (!file.isDirectory()) {
			return null;
		}
		Map<String, String> map = new HashMap<String, String>();
		String md5;
		File files[] = file.listFiles();
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (f.isDirectory() && listChild) {
				map.putAll(getDirDigest(f, listChild, algorithms));
			} else {
				md5 = getFileDigest(f, algorithms);
				if (md5 != null) {
					map.put(f.getPath(), md5);
				}
			}
		}
		return map;
	}

//	public static void main(String[] str) {
//		Map<String, String> fMap = getDirDigest(new File("E:\\Music"), true,
//				ALGORITHMS_MD5);
//		for (Map.Entry<String, String> entry : fMap.entrySet()) {
//			System.out.println(entry.getKey() + " " + ALGORITHMS_MD5 + ":"
//					+ entry.getValue());
//		}
//	}
}
