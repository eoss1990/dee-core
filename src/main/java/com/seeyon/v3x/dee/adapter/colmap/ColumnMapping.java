package com.seeyon.v3x.dee.adapter.colmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * 存储一组ColumnMapping。
 * 
 * @author wangwenyou
 * 2017.2.10增加序列化支持 by yangyu
 */
 public class ColumnMapping implements Iterable<ColumnMapping.Mapping>,Serializable {
	private List<Mapping> mappingList = new ArrayList<Mapping>();
	private static final String TABLE_COLUMN_SEP = "/";
	private String name;
	@Override
	public String toString() {
		return "ColumnMapping [mappingList=" + mappingList + ", name=" + name
				+ "]";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 建立表映射或列映射。
	 * 
	 * @param src
	 *            表名称或列名称，列名称需带表名称前缀，形如table或table/column。
	 * @param target
	 *            目标表名称或列名称，形如table或table.column。
	 */
	public void mapping(String src, String target,String expr) {

		this.put2List(src, target, expr);
	}

	public void merge(ColumnMapping cm){
		for (Mapping mapping : cm) {
			this.mappingList.add(mapping);
		}
	}
	private void put2List(String src, String target, String decoder) {
		Mapping columnMap = new Mapping();
		String[] arr;
		if (target.contains(TABLE_COLUMN_SEP)) {
			arr = target.split("\\" + TABLE_COLUMN_SEP);
		} else {
			arr = new String[] { target };
		}
		columnMap.setSource(src);
		columnMap.setTarget(arr);
		columnMap.setExpression(decoder);
		this.mappingList.add(columnMap);
	}

	static class Mapping implements Serializable{
		private String source;

		@Override
		public String toString() {
			return "Mapping [source=" + source + ", target="
					+ Arrays.toString(target) + ", expression=" + expression
					+ "]";
		}

		private String expression;
		private String[] target;

		public String getSource() {
			return source;
		}

		public void setSource(String data) {
			this.source = data;
		}

		public String getExpression() {
			return expression;
		}

		public void setExpression(String decoder) {
			this.expression = decoder;
		}

		public String[] getTarget() {
			return target;
		}

		public void setTarget(String[] name) {
			this.target = name;
		}
	}

	@Override
	public Iterator<Mapping> iterator() {
		return this.mappingList.iterator();
	}
}
