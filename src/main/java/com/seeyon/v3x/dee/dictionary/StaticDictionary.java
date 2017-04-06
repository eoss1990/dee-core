package com.seeyon.v3x.dee.dictionary;

import com.seeyon.v3x.dee.TransformException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 静态列表字典，所有数据都保存在内存中，适用于数据量不大的场合。
 * 
 * @author wangwenyou
 * 
 * @param <K>
 * @param <V>
 */
public class StaticDictionary<K, V> implements Dictionary<K, V> {
	private final Map<K, V> map;

	public StaticDictionary(Map<K, V> map) {
		this.map = new HashMap<K, V>(map);
	}

	public StaticDictionary() {
		this.map = new HashMap<K, V>();
	}

	@Override
	public V get(K key) {
		return this.map.get(key);
	}

	@Override
	public boolean containsKey(K key) {
		return this.map.containsKey(key);
	}

	@Override
	public void put(K key, V value) {
		this.map.put(key, value);
	}

	@Override
	public void clear() {
		this.map.clear();
	}

	@Override
	public boolean isEmpty() {
		return this.map.isEmpty();
	}

	@Override
	public Set<K> keySet() {
		return this.map.keySet();
	}

	@Override
	public int size() {
		return this.map.size();
	}

	@Override
	public void load() throws TransformException {
		// TODO Auto-generated method stub
	}
}
