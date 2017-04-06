package com.seeyon.v3x.dee.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.Document.Element;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.TransformFactory;
import com.seeyon.v3x.dee.resource.DbDataSource;

public class ProcedureUtil {
	public static Document exeProcedure(String procedureName, String parms, DbDataSource ds)
			throws Exception {
		Connection connection = null;
		CallableStatement callableStatement = null;
		ResultSet rs = null;
		Document doc = TransformFactory.getInstance().newDocument("root");
		try {
			connection = ds.getConnection();
			String[] parmsArr = parms.split(",");
			String dataProductName = "";
			try {
				dataProductName = connection.getMetaData().getDatabaseProductName().toLowerCase();
			} catch (SQLException e) {
				throw new TransformException(dataProductName + "获取异常:" + e.getMessage());
			}
			String func = "";
			if (dataProductName.startsWith("mysql")) {
				func = "call " + procedureName + "(";
				for (int i = 0; i < parmsArr.length && !"".equals(parms) && parms != null; i++) {
					if (("call " + procedureName + "(").equals(func)) {
						func = func + "?";
					} else {
						func = func + ", ?";
					}
				}
				func = func + ")";
			} else {
				func = "{call " + procedureName + "(";
				for (int i = 0; i < parmsArr.length && !"".equals(parms) && parms != null; i++) {
					if (("{call " + procedureName + "(").equals(func)) {
						func = func + "?";
					} else {
						func = func + ", ?";
					}
				}
				func = func + ")}";
			}
			callableStatement = connection.prepareCall(func);
			for (int i = 0; i < parmsArr.length && !"".equals(parms) && parms != null; i++) {
				callableStatement.setString(i + 1, parmsArr[i]);
			}
			rs = callableStatement.executeQuery();
			Element root = doc.getRootElement();
			Element table = root.addChild("from");
			Map cmap = getColumnInfo(rs);
			int count = 0;
			try {
				while (rs.next()) {
					Element row = table.addChild("row");
					Iterator it = cmap.keySet().iterator();
					while (it.hasNext()) {
						Object o = null;
						String columnName = (String) it.next();
						Element column = row.addChild(columnName.trim());
						if (Types.CLOB == (Integer) cmap.get(columnName)) {
							Clob clob = rs.getClob(columnName);
							if (null != clob)
								column.setValue(ClobToObj(clob));

						} else if (Types.TIMESTAMP == (Integer) cmap.get(columnName)) {
							o = rs.getObject(columnName);
							if (o != null && "oracle.sql.TIMESTAMP".equals(o.getClass().getName())) {
								Class clz = o.getClass();
								Method method = clz.getMethod("timestampValue");
								column.setValue(method.invoke(o));
							} else {
								column.setValue(o);
							}
						} else {
							o = rs.getObject(columnName);
							column.setValue(o);
						}
					}
					count++;
				}
				if (count == 0) {
					Element row = table.addChild("nullrow");
					Iterator it = cmap.keySet().iterator();
					while (it.hasNext()) {
						row.addChild(((String) it.next()).trim());
					}
				}
			} catch (Exception e) {
				throw e;
			}
			int recordCount = table.getChildren("row").size();
			table.setAttribute("count", recordCount);
			table.setAttribute("totalCount", recordCount);
		} catch (Exception e) {
			throw e;
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (callableStatement != null) {
				callableStatement.close();
			}
			if (connection != null) {
				connection.close();
			}
		}
		return doc;
	}

	public static Map<String, Integer> getColumnInfo(ResultSet rs) throws SQLException {
		ResultSetMetaData metaData = rs.getMetaData();
		Map<String, Integer> cmap = new HashMap<String, Integer>();
		int columnCount = metaData.getColumnCount();
		for (int i = 1; i < columnCount + 1; i++) {
			cmap.put(metaData.getColumnLabel(i), metaData.getColumnType(i));
		}
		return cmap;
	}

	public static Object ClobToObj(Clob clob) throws Exception {
		String reString = "";
		java.io.Reader is = null;
		try {
			is = clob.getCharacterStream();
		} catch (Exception e) {
			return clob;
		}
		BufferedReader br = new BufferedReader(is);
		String s = null;
		try {
			s = br.readLine();
		} catch (Exception e) {
			throw e;
		}
		StringBuffer sb = new StringBuffer();
		while (s != null) {
			sb.append(s);
			sb.append("\r\n");
			try {
				s = br.readLine();
			} catch (Exception e) {
				throw e;
			}
		}
		if (br != null) {
			try {
				br.close();
			} catch (IOException e) {
				throw e;
			}
		}
		reString = sb.toString();
		return reString;
	}
}
