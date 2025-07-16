package com.hpham.database.btree_disk.file_formats.record;

import com.hpham.database.btree_disk.Record;
import com.hpham.database.btree_disk.RecordValue;
import com.hpham.database.btree_disk.data_types.Field;
import com.hpham.database.btree_disk.data_types.IntField;
import com.hpham.database.btree_disk.data_types.Serializable;
import com.hpham.database.btree_disk.data_types.SortableField;
import com.hpham.database.btree_disk.data_types.StringField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class RecordFileTest {
  private RecordFile recordFile;
  private static final Random rand = new Random();

  @BeforeEach
  void beforeEach() {
    recordFile = new RecordFile();
  }

  @AfterEach
  void afterEach() throws IOException {
    recordFile.delete();
  }

  @Test
  void testCreateRecord() throws IOException {
    Map<String, Field<?>> fields = new LinkedHashMap<>();
    fields.put("field1", StringField.fromValue("value1"));
    fields.put("field2", IntField.fromValue(4));
    fields.put("field3", StringField.fromValue("value3"));
    fields.put("field4", IntField.fromValue(4));
    recordFile.openFile(String.format("record-%d.tc", rand.nextInt()));
    IntStream.range(0, 10).forEach(
        i -> {
          var key = IntField.fromValue(rand.nextInt());
          Record<Integer> record = Record.<Integer>builder()
              .key(key)
              .value(RecordValue.recordValueWithFields(fields))
              .build();
          try {
            recordFile.append(record.serialize());
            var read = Record.deserialize(recordFile.read(i));
            assertThat(read.getKey()).isEqualTo(key);
            fields.forEach((fieldName, fieldValue) -> {
              assertThat(read.getValue().getField(fieldName)).isEqualTo(fieldValue);
            });
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
    );
  }

  @Test
  void testUpdateRecord() throws IOException {
    Map<String, Field<?>> fields = new LinkedHashMap<>();
    fields.put("field1", StringField.fromValue("value1"));
    fields.put("field2", IntField.fromValue(4));
    fields.put("field3", StringField.fromValue("value3"));
    fields.put("field4", IntField.fromValue(4));
    recordFile.openFile(String.format("record-%d.tc", rand.nextInt()));
    IntStream.range(0, 10).forEach(
        i -> {
          var key = IntField.fromValue(rand.nextInt());
          Record<Integer> record = Record.<Integer>builder()
              .key(key)
              .value(RecordValue.recordValueWithFields(fields))
              .build();
          try {
            recordFile.append(record.serialize());
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
    );

    long recordOffsetToUpdate = 4L;
    Map<String, Field<?>> updatedFields = new LinkedHashMap<>();
    updatedFields.put("field1", StringField.fromValue("value1-updated"));
    updatedFields.put("field2", IntField.fromValue(123));
    updatedFields.put("field3", StringField.fromValue("value3-updated"));
    updatedFields.put("field4", IntField.fromValue(345));
    IntField updatedKey = IntField.fromValue(rand.nextInt());
    Record<Integer> record = Record.<Integer>builder()
        .key(updatedKey)
        .value(RecordValue.recordValueWithFields(updatedFields))
        .build();

    recordFile.update(record.serialize(), recordOffsetToUpdate);

    var updatedRecord = Record.deserialize(recordFile.read(recordOffsetToUpdate));

    assertThat(updatedRecord.getKey()).isEqualTo(updatedKey);
    updatedFields.forEach((fieldName, fieldValue) -> {
      assertThat(updatedRecord.getValue().getField(fieldName)).isEqualTo(fieldValue);
    });
  }
}
