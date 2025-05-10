package com.hpham.database.b_tree;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Record<K extends Comparable<K>, V> {
    private K key;
    private V value;
}
