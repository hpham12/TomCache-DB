package com.hpham.database.btree_disk;

import com.hpham.database.btree_disk.data_types.Field;
import com.hpham.database.btree_disk.data_types.IntField;
import com.hpham.database.btree_disk.data_types.LongField;
import com.hpham.database.btree_disk.data_types.SortableField;
import com.hpham.database.btree_disk.data_types.StringField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.hpham.database.btree_disk.constants.DataConstants.INT_TYPE_SIGNAL;
import static com.hpham.database.btree_disk.constants.DataConstants.LONG_TYPE_SIGNAL;
import static com.hpham.database.btree_disk.constants.DataConstants.STRING_TYPE_SIGNAL;

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

  public ByteBuffer serialize() {
    ByteBuffer serializedValue = value.serialize();

    ByteBuffer bb = ByteBuffer.allocateDirect(
        serializedValue.limit()
            + key.getTypeSignal()
            + key.getSize()
    );

    bb.put(key.getTypeSignal());
    bb.put(key.serialize());
    bb.put(serializedValue);

    bb.position(0);

    return bb;
  }

  @SuppressWarnings("unchecked")
  public static <K extends Comparable<K>> Record<K> deserialize(ByteBuffer bb) {
    // key
    byte keyTypeSignal = bb.get();
    SortableField<K> key;

    switch (keyTypeSignal) {
      case INT_TYPE_SIGNAL -> key = (SortableField<K>) IntField.fromValue(bb.getInt());
      case STRING_TYPE_SIGNAL -> key = (SortableField<K>) StringField.fromValue(StringField.deserialize(bb,
          bb.position()));
      case LONG_TYPE_SIGNAL -> key = (SortableField<K>) LongField.fromValue(bb.getLong());
      default -> key = null; // TODO: do something better!
    }

    Map<String, Field<?>> fields = new LinkedHashMap<>();

    // values
    while (bb.position() < bb.limit() - 1) {
      // get field name
      String fieldName = StringField.deserialize(bb, bb.position());
      byte valueTypeSignal = bb.get();

      Field<?> field;

      switch (valueTypeSignal) {
        case INT_TYPE_SIGNAL -> field = IntField.fromValue(bb.getInt());
        case STRING_TYPE_SIGNAL -> field = StringField.fromValue(StringField.deserialize(bb,
            bb.position()));
        case LONG_TYPE_SIGNAL -> field = LongField.fromValue(bb.getLong());
        default -> field = null; // TODO: do something better!
      }

      fields.put(fieldName, field);
    }

    return Record.<K>builder()
        .key(key)
        .value(RecordValue.recordValueWithFields(fields))
        .build();
  }
}
