package com.seeyon.v3x.dee.util;

import com.seeyon.v3x.dee.DEEConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;


/**
 * @description 压缩解压文件
 * 
 * @author liuls
 */
public class ZipUtil { 
	
	private static Log log = LogFactory.getLog(ZipUtil.class);
	
	/**
	 * @description 指定压缩文件目录进行压缩
	 * @date 2011-10-20
	 * @author liuls
	 * @param inputFile 指定压缩文件目录
	 * @param zipFileName 压缩后文件名称
	 */
	public static void zip(String inputFile, String zipFileName) {
		zip(new File(inputFile), zipFileName);
	} 
	
	/**
	 * @description 指定压缩文件进行压缩
	 * @date 2011-10-20
	 * @author liuls
	 * @param inputFile 指定压缩文件
	 * @param zipFileName 压缩后文件名称
	 */
	public static void  zip(File inputFile, String zipFileName) {
	
		try { FileOutputStream out = new FileOutputStream(new String(zipFileName.getBytes(DEEConstants.CHARSET_UTF8))); //创建文件输出对象out,提示:注意中文支持
			ZipOutputStream zOut = new ZipOutputStream(out);
			zip(zOut, inputFile, ""); 
			zOut.close(); 
		} catch (Exception e) {
			log.error("压缩文件异常：" + e);
		} 
	} 
	
	/**
	 * @description 文件压缩到指定的流文件中
	 * @date 2011-10-20
	 * @author liuls
	 * @param zOut 文件压缩流
	 * @param file 文件
	 * @param base 文件基本目录
	 */
	public static void  zip(ZipOutputStream zOut, File file, String base) {
	
		try { 
		 	log.debug("Now is zip-->" + file.getName()); 
			if (file.isDirectory()) { 
				File[] listFiles = file.listFiles();
				zOut.putNextEntry(new ZipEntry(base + "/"));
				base =( base.length() == 0 ? "" : base + "/" ); 
				for (int i = 0; i < listFiles.length; i++) { 
				zip(zOut, listFiles[i], base + listFiles[i].getName()); 
				} 
			} else { 
				if (base == null || "".equals(base.trim())) { 
					base = file.getName(); 
				} 
				zOut.putNextEntry(new ZipEntry(base));
				log.debug(file.getPath() + "," + base); 
				FileInputStream in = new FileInputStream(file);
				int len; 
				while ((len = in.read()) != -1) 
				zOut.write(len); 
				in.close(); 
			} 
		} catch (Exception e) {
			log.error("压缩文件异常：" + e);
		} 
	} 
	
	/**
	 * @description 在指定目录下创建目录
	 * @date 2011-10-20
	 * @author liuls
	 * @param directory 指定目录
	 * @param subDirectory 需要创建的子目录
	 */
	private static void createDirectory(String directory, String subDirectory) {
		
		String dir[];
		File fl = new File(directory);
		try { 
			if ("".equals(subDirectory) && fl.exists() != true) 
			fl.mkdir(); 
			else if (!"".equals(subDirectory)) { 
				dir = subDirectory.replace('\\', '/').split("/"); 
				for (int i = 0; i < dir.length; i++) { 
					File subFile = new File(directory + File.separator + dir[i]);
					if (subFile.exists() == false) 
					subFile.mkdir(); 
					directory += File.separator + dir[i];
				} 
			} 
		} catch (Exception ex) {
			log.error(ex.getMessage()); 
		} 
	} 
	
	/**
	 * @description 解压文件
	 * @date 2011-10-20
	 * @author liuls
	 * @param zipFileName 需要解压的zip文件，包括路径和名称
	 * @param outputDirectory 输出解压后文件的文件目录
	 */
	public static void unZip(String zipFileName, String outputDirectory) {
		try { 
			File file = new File(zipFileName);
			unZip(file, outputDirectory);
		}catch (Exception ex) {
		   log.error(ex.getMessage()); 
	    } 
	 
	} 
	public static void  unZip(File file, String outputDirectory){
		try { 
            ZipFile zipFile = new ZipFile(file);
            java.util.Enumeration e = zipFile.getEntries();
            ZipEntry zipEntry = null;
            createDirectory(outputDirectory, "");
			while (e.hasMoreElements()) { 
				zipEntry = (ZipEntry) e.nextElement();
				log.debug(": " + zipEntry.getName()); 
				String name = null;
				if (zipEntry.isDirectory()) { 
					name = zipEntry.getName(); 
					name = name.substring(0, name.length() - 1); 
					File f = new File(outputDirectory + File.separator + name);
					f.mkdir(); 
					log.debug("Create dir：" + outputDirectory + File.separator + name);
				} else { 
					String fileName = zipEntry.getName();
					fileName = fileName.replace('\\', '/'); 
					if (fileName.indexOf("/") != -1) { 
						createDirectory(outputDirectory, fileName.substring(0, fileName.lastIndexOf 
						("/"))); 
						fileName = fileName.substring( 
						fileName.lastIndexOf("/") + 1, fileName.length()); 
					} 
					File f = new File(outputDirectory + File.separator + zipEntry.getName());
					f.createNewFile(); 
					InputStream in = zipFile.getInputStream(zipEntry);
					FileOutputStream out = new FileOutputStream(f);
					byte[] by = new byte[1024]; 
					int c; 
					while ((c = in.read(by)) != -1) { 
						out.write(by, 0, c); 
					} 
					out.close(); 
					in.close(); 
				}
				
			}
			/**
			 * 2011-10-31 lilong
			 * 关闭解压完成后ZipFile对象，解决资源文件被占用
			 */
            zipFile.close();
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
	}
	
	/**
	 * @description  删掉一层目录 
	 * @date 2011-10-20
	 * @author liuls
	 * @param zipFileName 压缩文件
	 * @param outputDirectory 输出的文件目录
	 */
	public void delALayerDir(String zipFileName, String outputDirectory) {
	
		String[] dir = zipFileName.replace('\\', '/').split("/");
		String fileFullName = dir[dir.length - 1];
		int pos = -1; 
		pos = fileFullName.indexOf("."); 
		String fileName = fileFullName.substring(0, pos);
		String sourceDir = outputDirectory + File.separator + fileName;
		try { 
			copyFile(new File(outputDirectory), new File(sourceDir), new File(sourceDir));
			deleteSourceBaseDir(new File(sourceDir));
		} catch (Exception e) {
			log.error("压缩文件出错：" + e); 
		} 
	} 
	
	
	/**
	 * @description 将sourceDir目录的文件全部copy到destDir中去 
	 * @date 2011-10-20
	 * @author liuls
	 * @param destDir 指定目录
	 * @param sourceBaseDir 源目录的基本目录
	 * @param sourceDir  源目录
	 * @throws Exception 异常
	 */
	public void copyFile (File destDir, File sourceBaseDir, File sourceDir) throws Exception {
	
		File[] lists = sourceDir.listFiles();
		if (lists == null) 
		return; 
		for (int i = 0; i < lists.length; i++) { 
			File f = lists[i];
			if (f.isFile()) { 
				FileInputStream fis = new FileInputStream(f);
				String content = "";
				String sourceBasePath = sourceBaseDir.getCanonicalPath();
				String fPath = f.getCanonicalPath();
				String drPath = destDir + fPath.substring(fPath.indexOf
				(sourceBasePath) + sourceBasePath.length()); 
				FileOutputStream fos = new FileOutputStream(drPath);
				byte[] b = new byte[2048]; 
				while (fis.read(b) != -1) { 
					if (content != null) 
					content += new String(b);
					else 
					content = new String(b);
					b = new byte[2048]; 
				} 
				content = content.trim(); 
				fis.close(); 
				fos.write(content.getBytes()); 
				fos.flush(); 
				fos.close(); 
			} else { 
				// 先新建目录 
				new File(destDir + File.separator + f.getName()).mkdir();
				copyFile(destDir, sourceBaseDir, f); // 递归调用 
			} 
		} 
	} 
	
	/**
	 * @description 将sourceDir目录的文件全部copy到destDir中去 
	 * @date 2011-10-20
	 * @author liuls
	 * @param curFile 当前的需要copy出去的目录
	 * @throws Exception 异常
	 */
	public void deleteSourceBaseDir(File curFile) throws Exception {
	
		File[] lists = curFile.listFiles();
		File parentFile = null;
		for (int i = 0; i < lists.length; i++) { 
			File f = lists[i];
			if (f.isFile()) { 
			f.delete(); 
			// 若它的父目录没有文件了，说明已经删完，应该删除父目录 
			parentFile = f.getParentFile(); 
			if (parentFile.list().length == 0) 
				parentFile.delete(); 
			} else { 
				deleteSourceBaseDir(f); // 递归调用 
			} 
		} 
	
	}
	public static void main(String[] args) {
		
	    ZipUtil t = new ZipUtil();
	    
	    // 这里是调用压缩的代码      
	     t.zip("c:\\test", "c:\\test.rar");    
	  
	    // 这里是调用解压缩的代码      
	     t.unZip("c:\\test.rar", "c:\\test1");      
	  
	}       

}
