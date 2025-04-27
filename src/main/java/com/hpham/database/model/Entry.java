package com.hpham.database.model;

import lombok.Value;

@Value
public class Entry<K extends Comparable<K>, V> {
    K key;
    V value;
}
