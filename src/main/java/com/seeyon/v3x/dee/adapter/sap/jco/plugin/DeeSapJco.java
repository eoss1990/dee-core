package com.seeyon.v3x.dee.adapter.sap.jco.plugin;

import com.sap.conn.jco.JCoContext;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoRepository;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;
import com.sap.conn.jco.ext.Environment;
import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.Document.Element;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.datasource.XMLDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DeeSapJco {
	private static Log log = LogFactory.getLog(DeeSapJco.class);
	private static DeeSapJco sjPlg = null;
	private static SapDestinationDataProvider mDest = null;
	public DeeSapJco(){}
	public static DeeSapJco getInstance(String host, String sysnr, String client, String usr, String pwd) throws TransformException {

		if(sjPlg == null || mDest == null){
			sjPlg = new DeeSapJco();
			mDest = new  SapDestinationDataProvider(host,sysnr,client,usr,pwd);

		}
		else if(!host.equals(mDest.getDestinationProperties("").getProperty(mDest.JCO_ASHOST))){
			mDest = new  SapDestinationDataProvider(host,sysnr,client,usr,pwd);
		}
		
		try {
			Environment.registerDestinationDataProvider(mDest);
		} catch (Throwable e) {
			log.error(e);
			throw new TransformException("初始化JCO出现异常："+e.getLocalizedMessage(), e);
		}
		
		return sjPlg;
	}


	public Document getSAPJCOData(Document input,String func,Map<String, String> map,Map<String, String> jcoReturnMap,Map<String, String> StructureMapping,Map<String, String> TableMapping) throws IOException, JCoException, TransformException {
		JCoRepository mRepository = null;
		JCoDestination dest= JCoDestinationManager.getDestination("");
		JCoFunction function = null;
		//定义map用于装载返回结果
		Map<String,String> retStrMap = new HashMap<String,String>();
		Map<String,JCoStructure> jcoStructureMap = new HashMap<String,JCoStructure>();
		Map<String,JCoTable> jcoTableMap = new HashMap<String,JCoTable>();

		try {
			mRepository=dest.getRepository();
			//清理func缓存
			mRepository.removeFunctionTemplateFromCache(func);
			function=mRepository.getFunction(func);
//			JCoStructure st = function.getImportParameterList().getStructure("RETURN");
			if(map != null){
				for(Entry<String, String> entry : map.entrySet()) {
					if("".equals(entry.getKey()))
						continue;
		    		function.getImportParameterList().setValue(entry.getKey(), entry.getValue());
		    	}
			}
			
			
			//document与jcoStructure的映射以及向Structure填充值
			if(StructureMapping!=null)
			{
				List<jcoData> structureMappingList = new ArrayList<jcoData>();
				Map<String,JCoStructure> StructureIn = new HashMap<String,JCoStructure>();
				String StructureName = null;
				
				//现将map转换成List并排序
				for(Entry<String, String> entry : StructureMapping.entrySet()) {
		    		String[] struc = entry.getKey().split(",");
		    		String[] doc = entry.getValue().split(",");
		    		jcoData jd = new jcoData(struc[0],struc[1],doc[0],doc[1]);
		    		structureMappingList.add(jd);
		    	}
				ComparatorJCoData comparator = new ComparatorJCoData();
				Collections.sort(structureMappingList, comparator);
				
				//将Structure从sap取过来
				for(jcoData jd:structureMappingList)
				{
					if(!jd.getJcoName().equals(StructureName))
					{
						StructureIn.put(jd.getJcoName(),function.getImportParameterList().getStructure(jd.getJcoName()));
						StructureName = jd.getJcoName();
					}
				}
				
				//设置jcoStructure的值
				for(Entry<String,JCoStructure> entry : StructureIn.entrySet())
				{
					JCoStructure jcoStruct = entry.getValue();
					List<jcoData> oneStructMapping = new ArrayList<jcoData>();

					for(jcoData jd:structureMappingList)
					{
						if(jd.getJcoName().equals(entry.getKey())) oneStructMapping.add(jd);
					}

					Element docTable = input.getRootElement().getChild(oneStructMapping.get(0).getDocName());
					if(docTable==null)
						throw new Exception("document中不存在："+oneStructMapping.get(0).getDocName()+"表节点！");
					List<Element> rowList = docTable.getChildren();
					if(rowList==null||rowList.size()<1)
						throw new Exception("document中"+oneStructMapping.get(0).getDocName()+"表节点没有数据！");
					//结构体只有一行数据，如果映射的表中有多行数据的的情况下默认取第一行数据
					for(jcoData jd:oneStructMapping)
					{
						Object value = rowList.get(0).getChild(jd.getDocValue()).getValue();
						jcoStruct.setValue(jd.getJcoValue(), value==null?"":value.toString());
					}
//					function.getImportParameterList().setValue(entry.getKey(), jcoStruct);

				}
			}
			
			//document与jcoTable的映射以及向JCoTable填充值
			if(TableMapping!=null)
			{
				List<jcoData> tableMappingList = new ArrayList<jcoData>();
				Map<String,JCoTable> tableIn = new HashMap<String,JCoTable>();
				String TableName = null;
				
				//现将map转换成List并排序
				for(Entry<String, String> entry : TableMapping.entrySet()) {
		    		String[] table = entry.getKey().split(",");
		    		String[] doc = entry.getValue().split(",");
		    		jcoData jd = new jcoData(table[0],table[1],doc[0],doc[1]);
		    		tableMappingList.add(jd);
		    	}
				ComparatorJCoData comparator = new ComparatorJCoData();
				Collections.sort(tableMappingList, comparator);
				
				//将Table从sap取过来
				for(jcoData jd:tableMappingList)
				{
					if(!jd.getJcoName().equals(TableName))
					{
						tableIn.put(jd.getJcoName(),function.getTableParameterList().getTable(jd.getJcoName()));
						TableName = jd.getJcoName();
					}
				}
				
				//设置jcoTable的值
				for(Entry<String,JCoTable> entry : tableIn.entrySet())
				{
					JCoTable jcoTable = entry.getValue();
					List<jcoData> oneTableMapping = new ArrayList<jcoData>();

					for(jcoData jd:tableMappingList)
					{
						if(jd.getJcoName().equals(entry.getKey())) oneTableMapping.add(jd);
					}

					Element docTable = input.getRootElement().getChild(oneTableMapping.get(0).getDocName());
					if(docTable==null)
						throw new Exception("document中不存在："+oneTableMapping.get(0).getDocName()+"表节点！");
					List<Element> rowList = docTable.getChildren();
					if(rowList==null||rowList.size()<1)
						throw new Exception("document中"+oneTableMapping.get(0).getDocName()+"表节点没有数据！");
					for(Element row:rowList)
					{
						jcoTable.appendRow();
						for(jcoData jd:oneTableMapping)
						{
							Object value = row.getChild(jd.getDocValue()).getValue();
							jcoTable.setValue(jd.getJcoValue(), value==null?"":value.toString());
						}
					}
//					function.getImportParameterList().setValue(entry.getKey(), jcoTable);

				}
			}
			
			//执行
			function.execute(dest);
			
			//通过map取回sap返回结果
			if(jcoReturnMap != null){
				for(Entry<String, String> entry : jcoReturnMap.entrySet()) {
					if("String".equals(entry.getValue()))
						retStrMap.put(entry.getKey(), function.getExportParameterList().getString(entry.getKey()));
					else if("JCoStructure".equals(entry.getValue()))
						jcoStructureMap.put(entry.getKey(), function.getExportParameterList().getStructure(entry.getKey()));
					else if("JCoTable".equals(entry.getValue()))
						jcoTableMap.put(entry.getKey(), function.getTableParameterList().getTable(entry.getKey()));
		    	}
			}
			
			try{
				//解析String类型的返回结果map，将返回的String类型的值放入document的context中
				if(retStrMap != null){
					for(Entry<String, String> entry : retStrMap.entrySet()) {
						if("".equals(entry.getKey()))
							continue;
						input.getContext().setAttribute(entry.getKey(), entry.getValue());
						input.getContext().getParameters().add(entry.getKey(), entry.getValue());
			    	}
				}
				
				//解析JCoStructure，合并到document当中来
				if(jcoStructureMap != null){
					for(Entry<String, JCoStructure> entry : jcoStructureMap.entrySet()) {
						if("".equals(entry.getKey()))
							continue;
						
						String StructureXml = entry.getValue().toXML();
						Document newDoc = new XMLDataSource(StructureXml).parse();
						List<Element> listEle = newDoc.getRootElement().getChildren();
						
						if(input.getRootElement().getChild(entry.getKey())==null)
							input.getRootElement().addChild(entry.getKey());
							
						Element row = input.createElement("row");
						for(Element ele:listEle)
						{
							row.addChild(ele);
						}
						Element structure = input.getRootElement().getChild(entry.getKey());
						structure.addChild(row);
						int count = structure.getChildren().size();
						structure.setAttribute("totalCount", count);
						structure.setAttribute("count", count);
			    	}
				}
				
				//解析JCoTable，合并到document中来
				if(jcoTableMap != null){
					for(Entry<String, JCoTable> entry : jcoTableMap.entrySet()) {
						if("".equals(entry.getKey()))
							continue;
						
						String tableXml = entry.getValue().toXML();
						tableXml=tableXml.replace("<item>", "<row>");
						tableXml=tableXml.replace("</item>", "</row>");
						Document newInput = new XMLDataSource(tableXml).parse();
						List<Element> listEle = newInput.getRootElement().getChildren();
						
						if(input.getRootElement().getChild(entry.getKey())==null)
						input.getRootElement().addChild(entry.getKey());
						
						Element table = input.getRootElement().getChild(entry.getKey());
						for(Element ele:listEle)
						{
							table.addChild(ele);
						}
						
						int count = table.getChildren().size();
						table.setAttribute("totalCount", count);
						table.setAttribute("count", count);
			    	}
				}		
			}catch(DocumentException e)
			{
				//转换失败，继续执行下面任务
				log.error(e);
				throw new TransformException("返回数据转换document失败:"+e.getMessage()+"\n", e);
			}
			
//			log.debug("SAP 返回数据为："+retStr);
		} catch (Exception ex) {
			log.error(ex);
			throw new TransformException("取SAP数据异常：" + ex.getLocalizedMessage(), ex);
		} finally {
			try {
				JCoContext.end(dest);
				Environment.unregisterDestinationDataProvider(mDest);
			} catch (JCoException e) {
				log.error(e);
				throw new TransformException("释放SAP JCoDestination异常："+ e.getLocalizedMessage(), e);
			}
		}
		return input;
    }
    
}
