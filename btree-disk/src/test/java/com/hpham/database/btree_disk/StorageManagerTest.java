package com.hpham.database.btree_disk;

import com.hpham.database.btree_disk.dataTypes.IntField;
import com.hpham.database.btree_disk.dataTypes.StringField;
import com.hpham.database.btree_disk.util.SerializationUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class StorageManagerTest {
  private StorageManager storageManager;
  File file;

  @BeforeEach
  void beforeEach() {
    storageManager = new StorageManager();
  }

  @AfterEach
  void afterEach() {
    file.delete();
  }

  @Test
  void testCreateFileWithIntKey() throws IOException {
    file = storageManager.createFile();
    BTreeNode<Integer> node = BTreeNode.createLeafNode();
    node.setKeys(
        List.of(
            IntField.fromValue(12345),
            IntField.fromValue(23456),
            IntField.fromValue(34567)
        )
    );

    ByteBuffer byteBuffer = SerializationUtil.serialize(node);

    storageManager.writeFile(byteBuffer, file);

    byte[] fileContent = Files.readAllBytes(file.toPath());

    BTreeNode<Integer> deserializedNode = SerializationUtil.deserialize(fileContent);

    assertThat(deserializedNode.getIsLeaf()).isTrue();
    assertThat(deserializedNode.getKeys()).containsExactlyElementsOf(node.getKeys());
  }

  @Test
  void testCreateFileWithStringKey() throws IOException {
    BTreeNode<String> node = BTreeNode.createLeafNode();
    BTreeNode<String> parent = BTreeNode.createInternalNode();

    node.setParentOffset(IntField.fromValue(283123));
    node.setParent(parent);
    node.setRecordOffsets(List.of(
        IntField.fromValue(123456),
        IntField.fromValue(234567),
        IntField.fromValue(345678)
    ));
    node.setKeys(
        List.of(
            StringField.fromValue("12345"),
            StringField.fromValue("23456"),
            StringField.fromValue("34567")
        )
    );

    ByteBuffer byteBuffer = SerializationUtil.serialize(node);

    file = storageManager.createFile();
    storageManager.writeFile(byteBuffer, file);

    byte[] fileContent = Files.readAllBytes(file.toPath());
    BTreeNode<String> deserializedNode = SerializationUtil.deserialize(fileContent);

    assertThat(deserializedNode.getIsLeaf()).isTrue();
    assertThat(deserializedNode.getParentOffset()).isEqualTo(node.getParentOffset());
    assertThat(deserializedNode.getRecordOffsets()).containsExactlyElementsOf(node.getRecordOffsets());
    assertThat(deserializedNode.getKeys()).containsExactlyElementsOf(node.getKeys());
  }
}
