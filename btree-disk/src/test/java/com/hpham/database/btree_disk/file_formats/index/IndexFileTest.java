package com.hpham.database.btree_disk.file_formats.index;

import com.hpham.database.btree_disk.BTreeNode;
import com.hpham.database.btree_disk.data_types.IntField;
import com.hpham.database.btree_disk.data_types.LongField;
import com.hpham.database.btree_disk.data_types.StringField;
import com.hpham.database.btree_disk.util.SerializationUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static com.hpham.database.btree_disk.constants.DataConstants.INT_TYPE_SIGNAL;
import static com.hpham.database.btree_disk.constants.DataConstants.STRING_TYPE_SIGNAL;
import static org.assertj.core.api.Assertions.assertThat;

public class IndexFileTest {
  private IndexFile indexFile;

  @BeforeEach
  void beforeEach() {
    indexFile = new IndexFile();
  }

  @AfterEach
  void afterEach() throws IOException {
    indexFile.delete();
  }

  @Test
  void testCreateFileWithIntKey() throws IOException {
    Random rand = new Random();

    indexFile.openFile(String.format("index-%d.tc", rand.nextInt()));
    IntStream.range(0, 10).forEach(
        i -> {
          BTreeNode<Integer> node = BTreeNode.createLeafNode();
          node.setKeys(
              List.of(
                  IntField.fromValue(rand.nextInt()),
                  IntField.fromValue(rand.nextInt()),
                  IntField.fromValue(rand.nextInt())
              )
          );
          ByteBuffer byteBuffer = SerializationUtil.serialize(node);
          try {
            indexFile.append(byteBuffer);
            ByteBuffer nodeFromDisk = indexFile.read(i);
            BTreeNode<Integer> deserializedNode = SerializationUtil.deserializeBTreeNode(nodeFromDisk, INT_TYPE_SIGNAL);

            assertThat(deserializedNode.getIsLeaf()).isTrue();
            assertThat(deserializedNode.getKeys()).containsExactlyElementsOf(node.getKeys());
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
    );
  }

//  @Test
//  void testCreateFileWithStringKey() throws IOException {
//    BTreeNode<String> node = BTreeNode.createLeafNode();
//    BTreeNode<String> parent = BTreeNode.createInternalNode();
//
//    node.setParentOffset(LongField.fromValue(283123L));
//    node.setParent(parent);
//    node.setRecordOffsets(List.of(
//        LongField.fromValue(123456L),
//        LongField.fromValue(234567L),
//        LongField.fromValue(345678L)
//    ));
//    node.setKeys(
//        List.of(
//            StringField.fromValue("12345"),
//            StringField.fromValue("23456"),
//            StringField.fromValue("34567")
//        )
//    );
//
//    ByteBuffer byteBuffer = SerializationUtil.serialize(node);
//
//    file = indexFile.createIndexFile();
//    indexFile.writeFile(byteBuffer, file);
//
//    byte[] fileContent = Files.readAllBytes(file.toPath());
//    BTreeNode<String> deserializedNode = SerializationUtil.deserializeBTreeNode(fileContent, STRING_TYPE_SIGNAL);
//
//    assertThat(deserializedNode.getIsLeaf()).isTrue();
//    assertThat(deserializedNode.getParentOffset()).isEqualTo(node.getParentOffset());
//    assertThat(deserializedNode.getRecordOffsets()).containsExactlyElementsOf(node.getRecordOffsets());
//    assertThat(deserializedNode.getKeys()).containsExactlyElementsOf(node.getKeys());
//  }
}
