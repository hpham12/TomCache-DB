package com.hpham.database.btree_disk;

import com.hpham.database.btree_disk.BTreeNode;
import com.hpham.database.btree_disk.StorageManager;
import com.hpham.database.btree_disk.dataTypes.IntField;
import com.hpham.database.btree_disk.dataTypes.StringField;
import com.hpham.database.btree_disk.util.SerializationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class StorageManagerTest {
  private StorageManager storageManager;

  @BeforeEach
  void beforeEach() {
    storageManager = new StorageManager();
  }

  @Test
  void testCreateFileWithIntKey() throws IOException {
    var file = storageManager.createFile();
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
    var file = storageManager.createFile();
    BTreeNode<String> node = BTreeNode.createLeafNode();
    node.setKeys(
        List.of(
            StringField.fromValue("12345"),
            StringField.fromValue("23456"),
            StringField.fromValue("34567")
        )
    );

    ByteBuffer byteBuffer = SerializationUtil.serialize(node);

    storageManager.writeFile(byteBuffer, file);

    byte[] fileContent = Files.readAllBytes(file.toPath());

    BTreeNode<String> deserializedNode = SerializationUtil.deserialize(fileContent);

    assertThat(deserializedNode.getIsLeaf()).isTrue();
    assertThat(deserializedNode.getKeys()).containsExactlyElementsOf(node.getKeys());
  }
}
