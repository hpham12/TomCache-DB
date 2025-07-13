package com.hpham.database.btree_disk;

import com.hpham.database.btree_disk.data_types.Field;
import com.hpham.database.btree_disk.data_types.IntField;
import com.hpham.database.btree_disk.data_types.StringField;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RecordValue {
  private Map<String, Field<?>> fields = new LinkedHashMap<>();

  public RecordValue(boolean withDefaultFields) {
    if (withDefaultFields) {
      fields.put("field1", StringField.fromValue("value1"));
      fields.put("field2", IntField.fromValue(4));
      fields.put("field3", StringField.fromValue("value3"));
      fields.put("field4", IntField.fromValue(4));
    }
  }

  public static RecordValue emptyRecordValue() {
    return new RecordValue(false);
  }

  public static RecordValue recordValueWithDefaults() {
    return new RecordValue(true);
  }

  public RecordValue withField(String fieldName, Field<?> fieldValue) {
    fields.put(fieldName, fieldValue);
    return this;
  }

  public Field<?> getField(String fieldName) {
    return fields.get(fieldName);
  }

  public int getSize() {
    return fields.values()
        .stream()
        .map(Field::getSize)
        .reduce(Integer::sum)
        .orElse(0);
  }

  public List<ByteBuffer> serializeFields() {
    return fields.values()
        .stream()
        .map(Field::serialize)
        .toList();
  }
}
