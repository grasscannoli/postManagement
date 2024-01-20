package com.app.patterns;

public interface Registry<K, V> {
    public V getInstance(K key);
}
