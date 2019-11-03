package ch.ethz.fimeier;

public class Pair<K, V> {
	
	private K key;
	private V value;
	
	public K getKey() {
		return key;
	};
	public V getValue() {
		return value;
	};
	public Pair(K _key, V _value ) {
		key = _key;
		value = _value;
	}
}