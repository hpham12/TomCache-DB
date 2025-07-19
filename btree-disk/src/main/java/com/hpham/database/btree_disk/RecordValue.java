package com.hpham.database.btree_disk;

import com.hpham.database.btree_disk.data_types.Field;
import com.hpham.database.btree_disk.data_types.IntField;
import com.hpham.database.btree_disk.data_types.StringField;
import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.hpham.database.btree_disk.constants.DataConstants.STRING_SIZE_BYTES;
import static com.hpham.database.btree_disk.constants.DataConstants.TYPE_SIGNAL_SIZE_BYTES;

@Setter
@Getter
public class RecordValue {
  private Map<String, Field<?>> fields = new LinkedHashMap<>();

  RecordValue(boolean withDefaultFields) {
    if (withDefaultFields) {
      fields.put("field1", StringField.fromValue("value1"));
      fields.put("field2", IntField.fromValue(4));
      fields.put("field3", StringField.fromValue("value3"));
      fields.put("field4", IntField.fromValue(4));
    }
  }

  public static RecordValue recordValueWithFields(Map<String, Field<?>> fields) {
    RecordValue recordValue = new RecordValue(false);
    recordValue.setFields(fields);

    return recordValue;
  }

  static RecordValue emptyRecordValue() {
    return new RecordValue(false);
  }

  static RecordValue recordValueWithDefaults() {
    return new RecordValue(true);
  }

  public RecordValue withField(String fieldName, Field<?> fieldValue) {
    fields.put(fieldName, fieldValue);
    return this;
  }

  public Field<?> getField(String fieldName) {
    return fields.get(fieldName);
  }

  int getSize() {
    return fields.values()
        .stream()
        .map(Field::getSize)
        .reduce(Integer::sum)
        .orElse(0);
  }

  ByteBuffer serialize() {
    AtomicInteger size = new AtomicInteger();
    fields.forEach((fieldName, field) -> {
      size.getAndAdd(STRING_SIZE_BYTES);        // fieldName
      size.getAndAdd(TYPE_SIGNAL_SIZE_BYTES);   // signal
      size.getAndAdd(field.getSize());          // field
    });

    ByteBuffer bb = ByteBuffer.allocateDirect(size.get());

    fields.forEach((fieldName, field) -> {
      bb.put(StringField.fromValue(fieldName).serialize());
      bb.put(field.getTypeSignal());
      bb.put(field.serialize());
    });

    bb.position(0);

    return bb;
  }
}
