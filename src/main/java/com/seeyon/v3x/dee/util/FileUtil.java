package com.seeyon.v3x.dee.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 文件处理类
 *
 * @author liuls
 */
public class FileUtil {
	private static final Log log = LogFactory.getLog(FileUtil.class);

	public static final String CHARSET_UTF8 = "UTF-8";

	/**
	 * 删除文件夹
	 *
	 * @param folderPath 文件夹完整绝对路径
	 */
	public static void delFolder(String folderPath) {
		try {
			delAllFile(folderPath); //删除完里面所有内容
			String filePath = folderPath;
			filePath = filePath.toString();
			File myFilePath = new File(filePath);
			myFilePath.delete(); //删除空文件夹
		} catch (Exception e) {
            log.error("删除文件异常：" + e);
		}
	}

	/**
	 * 删除指定文件夹下所有文件
	 *
	 * @param path 文件夹完整绝对路径
	 * @return
	 */
	public static boolean delAllFile(String path) {
		boolean flag = false;
		File file = new File(path);
		if (!file.exists()) {
			return flag;
		}
		if (!file.isDirectory()) {
			return flag;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFile(path + File.separator + tempList[i]);//先删除文件夹里面的文件
				delFolder(path + File.separator + tempList[i]);//再删除空文件夹
				flag = true;
			}
		}
		return flag;
	}

	/**
	 * Fetch the entire contents of a text file, and return it in a String. This
	 * style of implementation does not throw Exceptions to the caller.
	 *
	 * @param file
	 *            is a file which already exists and can be read.
	 * @throws java.io.IOException
	 */
	public static String readText(File file) throws IOException {
		// ...checks on aFile are elided
		StringBuilder contents = new StringBuilder();
		// use buffering, reading one line at a time
		// FileReader always assumes default encoding is OK!
		BufferedReader input = new BufferedReader(new FileReader(file));
		try {
			String line = null; // not declared within while loop
			/*
			 * readLine is a bit quirky : it returns the content of a line MINUS
			 * the newline. it returns null only for the END of the stream. it
			 * returns an empty String if two newlines appear in a row.
			 */
			while ((line = input.readLine()) != null) {
				contents.append(line);
				contents.append(System.getProperty("line.separator"));
			}
		} finally {
			input.close();
		}
		return contents.toString();
	}
	/**
	 * @description 获取资源文件，对jar包的文件可用
	 * @date 2012-2-21
	 * @author liuls
	 * @param filePath 文件路径
	 * @return
	 * @throws java.io.IOException
	 */
	public static String getResource(String filePath) throws IOException {
		//返回读取指定资源的输入流
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream(filePath);
		StringBuilder contents = new StringBuilder();

		BufferedReader br=new BufferedReader(new InputStreamReader(is,CHARSET_UTF8) );
		String s="";
		while((s=br.readLine())!=null){
			contents.append(s);
			contents.append(System.getProperty("line.separator"));
		}
		return contents.toString();
	}
	/**
	 * 获取A8Home路径
	 *
	 * @param
	 * @return
	 * @throws com.seeyon.v3x.dee.TransformException
	 */	
	public static boolean isA8Home() {
    	boolean isA8Flag = false;
		try {
			Object app = (Class.forName("com.seeyon.ctp.common.SystemEnvironment")
					.getMethod("getApplicationFolder", null).invoke(null, null));
			if(app != null) 
				isA8Flag = true;
		} catch (Exception e1) {
		}
		return isA8Flag;
	}

	/**
	 * 创建目录
	 *
	 * @param dirPath 目录路径
	 * @return true：创建成功，false：创建失败
	 */
	public static boolean createDir(String dirPath) {
		try {
			File file = new File(dirPath);
			if (!file.exists() || !file.isDirectory()) {
				if (file.mkdirs()) {
					return true;
				} else {
					log.error("创建文件路径失败：" + dirPath);

				}
			}
		} catch (Exception e) {
			log.error("创建文件路径失败：" + e.getLocalizedMessage(), e);
		}
		return false;
	}

	public static void close(Closeable x) {
		if (x == null) {
			return;
		}

		try {
			x.close();
		} catch (Exception e) {
			log.debug("close error", e);
		}
	}
}
