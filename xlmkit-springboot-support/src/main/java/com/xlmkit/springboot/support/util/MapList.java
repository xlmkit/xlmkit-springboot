package com.xlmkit.springboot.support.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapList<K,G> {
	private List<G> array = new ArrayList<>();
	private Map<K, G> map = new HashMap<K, G>();
	private IdFunction<K,G> idFunction;

	public MapList(IdFunction<K,G> idFunction) {
		this.idFunction = idFunction;
	}

	public MapList(IdFunction<K,G> idFunction, List<G> list) {
		this.idFunction = idFunction;
		addAll(list);
	}

	public static interface IdFunction<K,T> {
		K getValue(T t);
	}

	public List<G> toList() {
		return array;
	}

	public K getKey(G node) {
		return idFunction.getValue(node);
	}

	public void remove(G node) {
		Object key = getKey(node);
		map.remove(key);
		array.remove(node);

	}

	public void add(G node) {
		K key = getKey(node);
		array.add(node);
		map.put(key, node);
	}

	public G get(K id) {
		return map.get(id);
	}

	public void sort(Comparator<? super G> object) {
		array.sort(object);
	}

	public Set<K> keySet() {
		return map.keySet();
	}

	public MapList<K,G> addAll(List<G> list) {
		for (G t : list) {
			add(t);
		}
		return this;
	}

	public void removeAll(List<G> list) {
		for (G t : list) {
			remove(t);
		}
	}

	public G removeByKey(K key) {
		G t = get(key);
		map.remove(key);
		array.remove(t);
		return t;
	}

	public synchronized void clear() {
		map.clear();
		array.clear();
	}

}
