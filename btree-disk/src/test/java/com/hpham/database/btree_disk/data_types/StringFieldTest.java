package com.hpham.database.btree_disk.data_types;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test suite for {@link StringField}.
 */
public class StringFieldTest {
  @Test
  void testSerialization() {
    String testString = "HelloWorld";
    StringField field = StringField.fromValue(testString);
    ByteBuffer serialized = field.serialize();
    String deserialized = StringField.deserialize(serialized, 0);

    assertThat(deserialized).isEqualTo(testString);
  }

//  @Test
//  void testSerializationWithNonZeroStart() {
//    String testString = "HelloWorld";
//    StringField field = StringField.fromValue(testString);
//    ByteBuffer serialized = field.serialize();
//    byte[] bytes = new byte[20 + serialized.length];
//    int currentIndex = 0;
//
//    for (int i = 0; i < 10; i++) {
//      bytes[currentIndex] = (byte) ('a' + i);
//      currentIndex++;
//    }
//
//    for (byte b : serialized) {
//      bytes[currentIndex] = b;
//      currentIndex++;
//    }
//
//    for (int i = 0; i < 10; i++) {
//      bytes[currentIndex] = (byte) ('a' + i);
//      currentIndex++;
//    }
//
//    String deserialized = StringField.deserialize(bytes, 10);
//
//    assertThat(deserialized).isEqualTo(testString);
//  }
}
