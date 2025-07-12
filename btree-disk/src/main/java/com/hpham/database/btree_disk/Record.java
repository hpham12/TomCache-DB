package com.hpham.database.btree_disk;

import com.hpham.database.btree_disk.data_types.SortableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Class representing a record that is stored in the leaf node.
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Record<K extends Comparable<K>> implements Comparable<Record<K>> {
  private @NonNull SortableField<K> key;
  private @NonNull RecordValue value;

  @Override
  public int compareTo(Record<K> r) {
    return key.compareTo(r.key);
  }

  public int getSize() {
    return key.getSize() + value.getSize();
  }
}
