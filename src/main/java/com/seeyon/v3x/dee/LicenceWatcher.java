package com.seeyon.v3x.dee;

import com.seeyon.ctp.common.init.MclclzUtil;
import com.seeyon.v3x.dee.util.FileDigest;
import com.seeyon.v3x.dee.util.FileUtil;
import org.dom4j.DocumentException;
import www.seeyon.com.mocnoyees.LRWMMocnoyees;
import www.seeyon.com.mocnoyees.MSGMocnoyees;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LicenceWatcher extends DirectoryWatcher {
	protected Map<String, String> digestMap;
	private DEEClient client = new DEEClient();
	private static final String USER_TYPE_INNER = "1";

	public LicenceWatcher(String directoryToWatch, Pattern pattern) {
		this(new File(directoryToWatch), pattern);
	}

	public LicenceWatcher(File directoryToWatch, Pattern pattern) {
		super(directoryToWatch, pattern);
        if (isInA8AndDev()) {
            return;
        }
		digestMap = FileDigest.getDirDigest(directoryToWatch, false,
				FileDigest.ALGORITHMS_MD5);

		// 首次查询指定文件目录时要先处理
		File[] files = _directoryToWatch.listFiles(this);
		if (files == null || files.length == 0) {
			return;
		}
		Set<String> userTypeSet = new HashSet<String>();
		String userType = "";
		for (File file : files) {
			userType = addOrUpdateFlowId(file);
			if (userType != null) {
				userTypeSet.add(userType);
			}
		}
		setIsInner(userTypeSet);
	}

	public void run() {
        if (isInA8AndDev()) {
            return;
        }

        File[] files = _directoryToWatch.listFiles(this);
		if (files == null || files.length == 0) {
			return;
		}
		// c.getLicenceFlowIdList().clear();//清除原有的flowId集合
		Set<String> userTypeSet = new HashSet<String>();
		if (preProcess(_directoryToWatch)) {
			String userType = "";
			for (File file : files) {
				try {
					userType = parseFile(file);
					if (userType != null) {
						userTypeSet.add(userType);
					}
				} catch (Exception e) {
					log.error(e);
				}
			}
		}

		setIsInner(userTypeSet);
	}

	/**
	 * @description 解析Licence文件
	 * @date 2012-4-28
	 * @author liuls
	 * @param file
	 *            需要解析的文件
	 * @return
	 * @throws TransformException
	 * @throws org.dom4j.DocumentException
	 * @throws java.io.IOException
	 */
	protected String parseFile(File file) throws TransformException,
			DocumentException, IOException {
		String fileMD5 = FileDigest.getFileDigest(file,
				FileDigest.ALGORITHMS_MD5);
		String mapMD5 = digestMap.get(file.getPath());
		if (mapMD5 == null || !fileMD5.equals(mapMD5)) {
			String data = readFile(file);
			if (data == null)
				throw new TransformException("读取Flow licence文件失败！");
			digestMap.put(file.getPath(), fileMD5);
			return addOrUpdateFlowId(file);
		}
		return null;
	}

	/**
	 * @description 在有新的licence文件加入是更新或添加flowId到内存中
	 * @date 2012-4-28
	 * @author liuls
	 * @param f
	 *            licence文件
	 * @return
	 */
	private String addOrUpdateFlowId(File f) {
		try {
			LRWMMocnoyees lrwmmocnoyees = new LRWMMocnoyees(f);
			MSGMocnoyees dog = new MSGMocnoyees(lrwmmocnoyees);
			String userType = dog.methoda("value");
			if (!USER_TYPE_INNER.equals(userType)) {
				String s = dog.methodz("EE.EE1");
				if (s == null) {
					s = dog.methodz("EE");
					if (null == s) {
						return null;
					} else {
						String regex = "<\\s*value\\s*>(.*?)<\\s*/value\\s*>";
						Pattern p = Pattern.compile(regex);
						Matcher m = p.matcher(s);
						String flowIdStr = "";
						if (m.find()) {
							flowIdStr = m.group(1);
							if (!"".equals(flowIdStr)) {
								flowIdStr = flowIdStr.trim();
								String[] sa = flowIdStr.split("\\|");
								for (String ss : sa) {
									client.getLicenceFlowIdList().add(ss.trim());
								}
							}
						}
					}
				} else {
					String[] sa = s.split("\\|");
					for (String ss : sa) {
						client.getLicenceFlowIdList().add(ss.trim());
					}
				}
			}
			return userType;
		} catch (Throwable e) {
			log.error("获取flow licence时出错：", e);
		}
		return null;
	}

	/**
	 * @description 读取licence文件
	 * @date 2012-4-28
	 * @author liuls
	 * @param dataFile
	 *            文件名
	 * @return
	 * @throws java.io.IOException
	 */
	private String readFile(File dataFile) throws IOException {
		if (dataFile.exists()) {
			FileInputStream fis = new FileInputStream(dataFile);
			byte[] b = new byte[fis.available()];// 创建一个字节数组，数组长度与file中获得的字节数相等
			while (fis.read(b) != -1) {
				// System.out.println(new String(b));// 打印出从file文件读取的内容
			}
			fis.close();
			return new String(b);
		} else {
			return null;
		}
	}

	/**
	 * @description 检查是只有一个用户标示，并且是“1”
	 * @date 2012-4-28
	 * @author liuls
	 * @param userTypeSet
	 *            用户类别列表
	 */
	private void setIsInner(Set<String> userTypeSet) {
		if (userTypeSet.size() == 1 && userTypeSet.contains(USER_TYPE_INNER)) {
			try {
				client.setInner(true);
			} catch (Throwable e) {
				log.error("检查是只有一个用户标示出错:" + e);
			}
		}
	}

	@Override
	protected File processFile(File file) throws Exception {
		return null;
	}

    private boolean isInA8AndDev() {
        boolean isInA8 = FileUtil.isA8Home();
        if (isInA8) {
            Class<?> c1s = MclclzUtil.ioiekc("com.seeyon.ctp.product.ProductInfo");
            try {
                Boolean isDev = (Boolean) c1s.getMethod("isDev").invoke(null);
                if (isDev) {
                    return true;
                }
            } catch (InvocationTargetException e) {
                log.error(e.getTargetException().getLocalizedMessage(), e.getTargetException());
            } catch (Exception e) {
                log.error(e.getLocalizedMessage(), e);
            }
        }
        return false;
    }
}
