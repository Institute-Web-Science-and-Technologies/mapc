package net.sf.beenuts.util;

import java.io.Serializable;

public class Pair<K, V> implements Serializable{

	/**
	 * kill warning
	 */
	private static final long serialVersionUID = -6021907631675732900L;

	public Pair(K key, V value) {
		this.key = key;
		this.value = value;
	}
	
	public K getKey() {
		return key;
	}
	
	public V getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return "(" + key.toString() + ", " + value.toString() + ")";
	}
	
	private K key;
	
	private V value;
}
