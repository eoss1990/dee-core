package com.seeyon.v3x.dee.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangfb
 */
public class Utils {
    private static final String DEE_HOME = "DEE_HOME";

    /**
     * 传入参数为Boolean.TRUE，返回true，其他情况返回false
     *
     * @param obj 参数对象
     * @return
     */
    public static boolean obj2Boolean(Object obj) {
        if (obj != null && obj instanceof Boolean) {
            return (Boolean) obj;
        }
        return false;
    }

    public static String getDeeHome() {
        String tmp = System.getProperty(DEE_HOME);
        if (tmp == null || "".equals(tmp.trim())) {
            tmp = System.getenv(DEE_HOME);
        }
        return tmp;
    }

    public static String getDeeHomePath(String path) {
        return getDeeHome() + path;
    }

    public static List<String> parse2ImportList(String codeText) throws IOException {
        List<String> fileList = new ArrayList<String>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new StringReader(codeText));
            String line = br.readLine();

            while (line != null) {
                String[] array = line.split(";");
                for (String tmp : array) {
                    if (tmp == null || "".equals(tmp.trim())) {
                        continue;
                    }

                    String tmpTrim = tmp.trim();

                    if (tmpTrim.startsWith("import ")) {
                        String file = parseSingleImport(tmpTrim);
                        if (file != null) {
                            fileList.add(file);
                        }
                    } else if (tmpTrim.matches("\\w.*")) {
                        break;
                    }
                }
                line = br.readLine();
            }
        } finally {
            FileUtil.close(br);
        }

        return fileList;
    }

    private static String parseSingleImport(String s) {
        // 去掉前端的import和两侧的空格
        String tmp = s.substring("import ".length()).trim();

        // 去掉剩余的空格和分号
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < tmp.length(); i++) {
            if (' ' != tmp.charAt(i) && ';' != tmp.charAt(i)) {
                builder.append(tmp.charAt(i));
            }
        }

        tmp = builder.toString();
        if (tmp.startsWith("com.seeyon.dee.codelib.")) {
            return tmp;
        }

        return null;
    }
}
