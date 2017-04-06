package com.seeyon.v3x.dee.util;

import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.Parameters;
import com.seeyon.v3x.dee.TransformException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 拓展工具类，调用A8WebService接口时将paraMap参数拼接成XML
 * 仅支持A8Webservice接口
 * @author lilong
 * @modify 2012-03-12 修改增加支持参数传递
 */
public class ParaMapToXMLUtil {

	/**
	 * 支持参数传递修改此工具类
	 * 支持senderLoginName,templateCode,subject,param,attachments的参数传递
	 * @param paraMap
	 * @param output
	 * @return
	 * @throws com.seeyon.v3x.dee.TransformException
	 */
	public static String paraMapToXML(Map<String, String> paraMap, Document output)
			throws TransformException {
		StringBuilder para = new StringBuilder();
		for (Map.Entry<String, String> entry : paraMap.entrySet()) {
			if (entry.getKey() != null && !"".equals(entry.getKey())) {
				if("token".equals(entry.getKey())) {
					continue;
				} else {
					para.append("<").append(entry.getKey()).append(">");
					if("senderLoginName".equals(entry.getKey())
						|| "templateCode".equals(entry.getKey())
						|| "subject".equals(entry.getKey())
						|| "param".equals(entry.getKey())
						|| "attachments".equals(entry.getKey())) {
						para.append(output.getContext().getParameters().evalString(entry.getValue()));
					} else {
						para.append(entry.getValue());
					}
					para.append("</").append(entry.getKey()).append(">");
				}
			}
		}
		return para.toString();
	}
	/**
	 * 参数传递修改此工具类
	 * 参数传递
	 * @param paraMap
	 * @param output
	 * @return
	 * @throws com.seeyon.v3x.dee.TransformException
	 */
	public static String paraCommonMapToXML(Map<String, String> paraMap, Document output)
			throws TransformException {
		StringBuilder para = new StringBuilder();
		String dataName = "";
		for (Map.Entry<String, String> entry : paraMap.entrySet()) {
			if (entry.getKey() != null && !"".equals(entry.getKey())) {
				if("token".equalsIgnoreCase(entry.getKey()) || "#".equalsIgnoreCase(entry.getKey().substring(0,1))) {
					continue;
				}
				else if("dataName".equalsIgnoreCase(entry.getKey())){
					dataName = entry.getValue();
					continue;
				}
				else if("data".equalsIgnoreCase(entry.getKey())){
					para.append("<").append("#%*data*%#").append(">");
					para.append(entry.getValue());
					para.append("</").append("#%*data*%#").append(">");
				}
				else{
					String paramValue = entry.getValue();
					if("[".equalsIgnoreCase(paramValue.substring(0,1))
							&& "]".equalsIgnoreCase(paramValue.substring(paramValue.length()-1))){
						paramValue = paramValue.substring(1,paramValue.length()-1);
						String[] pArr = paramValue.split(",");
						for(String pValue:pArr){
							para.append("<").append(entry.getKey()).append(">");
							para.append(pValue);
							para.append("</").append(entry.getKey()).append(">");
						}
					}
					else{
						para.append("<").append(entry.getKey()).append(">");
						para.append(paramValue);
						para.append("</").append(entry.getKey()).append(">");
					}
				}
			}
		}
		String retPara = para.toString();
		if("".equalsIgnoreCase(dataName)){
			retPara = retPara.replace("#%*data*%#>", "data>");
		}
		else{
			retPara = retPara.replace("#%*data*%#>", dataName + ">");
		}
		return retPara;
	}

	/**
	 * 参数解析为Object组
	 *
	 * @param paraMap
	 * @return
	 * @throws com.seeyon.v3x.dee.TransformException
	 */
	public static Object[] paraCommonMapToArr(Map<String, String> paraMap,String a8WSToken,Parameters params)
			throws TransformException {
		if(paraMap == null)
			return null;
		List<Object> pMList = new ArrayList<Object>();
		for (Map.Entry<String, String> entry : paraMap.entrySet()) {
			if (entry.getKey() != null && !"".equals(entry.getKey())) {
				if("token".equalsIgnoreCase(entry.getKey())) {
					pMList.add(a8WSToken);
				} 
				else if("#".equalsIgnoreCase(entry.getKey().substring(0,1)) || "dataName".equalsIgnoreCase(entry.getKey())){
					continue;
				}
				else if("data".equalsIgnoreCase(entry.getKey())){
					pMList.add(entry.getValue().replaceAll("<forms >", "<forms version=\"2.1\">"));
				}
				else{
					String paramValue = entry.getValue();
					if(paramValue != null && paramValue.length()>0 
							&& "[".equals(paramValue.substring(0,1)) 
							&& "]".equals(paramValue.substring(paramValue.length()-1))){
						paramValue = paramValue.substring(1,paramValue.length()-1);
						String[] pArr = paramValue.split(",");
						pMList.add(pArr);
					}
					else{
						if(params == null){
							pMList.add(paramValue);
						}
						else{
							pMList.add(params.evalString(paramValue));
						}
					}
				}
			}
		}
		return pMList.toArray();
	}
	
}
