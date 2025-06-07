package com.hpham.database.b_tree;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Record<K extends Comparable<K>, V> implements Comparable<Record<K, V>> {
    private @NonNull K key;
    private @NonNull V value;

    @Override
    public int compareTo(Record<K, V> r) {
        return key.compareTo(r.key);
    }
}
