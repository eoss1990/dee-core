package com.seeyon.v3x.dee.dictionary;


import com.seeyon.v3x.dee.TransformException;

import java.util.Set;

/**
 * 字典，用于编码（枚举值）转换。可以是静态列表，也支持动态数据源。<br/>
 * 例如源系统性别“男”的编码为“1”，而目标系统“男”的编码为“M” 就需要使用Dictionary进行转换。<br/>
 * 同样也适用于异构系统之间主键映射的转换。
 * 
 * @author wangwenyou
 * 
 * @param <K>
 * @param <V>
 */
public interface Dictionary<K, V> {
	V get(K key);

	boolean containsKey(K key);

	void put(K key, V value);

	/**
	 * Remove all entries from this dictionary (optional operation).
	 * 
	 * @throws UnsupportedOperationException
	 *             if clear is not supported
	 */
	void clear();

	/**
	 * Returns true if the dictionary contains no mappings.
	 * 
	 * @return true if the dictionary is empty
	 */
	boolean isEmpty();

	/**
	 * Returns a set view of the keys in this Dictionary. The set is backed by
	 * the map, so that changes in one show up in the other. Modifications made
	 * while an iterator is in progress cause undefined behavior. If the set
	 * supports removal, these methods remove the underlying mapping from the
	 * map: <code>Iterator.remove</code>, <code>Set.remove</code>,
	 * <code>removeAll</code>, <code>retainAll</code>, and <code>clear</code>.
	 * Element addition, via <code>add</code> or <code>addAll</code>, is not
	 * supported via this set.
	 * 
	 * @return the set view of all keys
	 */
	Set<K> keySet();

	/**
	 * Returns the number of key-value mappings in the dictionary. If there are
	 * more than Integer.MAX_VALUE mappings, return Integer.MAX_VALUE.
	 * 
	 * @return the number of mappings
	 */
	int size();

	void load() throws TransformException;
}
