package com.hpham.database.btree_disk.dataTypes;

import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.stream.IntStream;

import static com.hpham.database.btree_disk.constants.DataTypeSizes.INT_SIZE_BYTES;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test suite for {@link StringField}
 * */
public class StringFieldTest {
  @Test
  void testSerialization() {
    String testString = "HelloWorld";
    StringField field = StringField.fromValue(testString);
    byte[] serialized = field.serialize();

    assertThat(serialized.length).isEqualTo(testString.length() + INT_SIZE_BYTES);
  }

  @Test
  void testDeserialization() {
    String testString = "HelloWorld";
    StringField field = StringField.fromValue(testString);
    byte[] serialized = field.serialize();
    String deserialized = StringField.deserialize(serialized, 0);

    assertThat(deserialized).isEqualTo(testString);
  }

  @Test
  void testDeserializationWithNonZeroStart() {
    String testString = "HelloWorld";
    StringField field = StringField.fromValue(testString);
    byte[] serialized = field.serialize();
    byte[] bytes = new byte[20 + serialized.length];
    int currentIndex = 0;

    for (int i = 0; i < 10; i++) {
      bytes[currentIndex] = (byte) ('a' + i);
      currentIndex++;
    }

    for (byte b : serialized) {
      bytes[currentIndex] = b;
      currentIndex++;
    }

    for (int i = 0; i < 10; i++) {
      bytes[currentIndex] = (byte) ('a' + i);
      currentIndex++;
    }

    String deserialized = StringField.deserialize(bytes, 10);

    assertThat(deserialized).isEqualTo(testString);
  }
}
