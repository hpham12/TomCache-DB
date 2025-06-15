package com.hpham.database.btree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Class representing a record that is stored in the leaf node.
 * */
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Record<K extends Comparable<K>, V> implements Comparable<Record<K, V>> {
  private @NonNull K key;
  private @NonNull V value;

  @Override
  public int compareTo(Record<K, V> r) {
    return key.compareTo(r.key);
  }
}
