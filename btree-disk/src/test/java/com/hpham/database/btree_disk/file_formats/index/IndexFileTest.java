package com.hpham.database.btree_disk.file_formats.index;

import static com.hpham.database.btree_disk.constants.DataConstants.INT_TYPE_SIGNAL;
import static com.hpham.database.btree_disk.constants.DataConstants.STRING_TYPE_SIGNAL;
import static org.assertj.core.api.Assertions.assertThat;

import com.hpham.database.btree_disk.BTreeNode;
import com.hpham.database.btree_disk.data_types.IntField;
import com.hpham.database.btree_disk.data_types.LongField;
import com.hpham.database.btree_disk.data_types.StringField;
import com.hpham.database.btree_disk.util.SerializationUtil;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class IndexFileTest {
  private IndexFile indexFile;
  private Random rand;

  @BeforeEach
  void beforeEach() {
    indexFile = new IndexFile();
    rand = new Random();
  }

  @AfterEach
  void afterEach() throws IOException {
    indexFile.delete();
  }

  @Test
  void testCreateFileWithIntKey() throws IOException {
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
          node.setRecordOffsets(
              List.of(
                  LongField.fromValue(rand.nextLong()),
                  LongField.fromValue(rand.nextLong()),
                  LongField.fromValue(rand.nextLong())
              )
          );
          ByteBuffer byteBuffer = SerializationUtil.serialize(node);
          try {
            indexFile.append(byteBuffer);
            ByteBuffer nodeFromDisk = indexFile.read(i);
            BTreeNode<Integer> deserializedNode = SerializationUtil
                .deserializeBTreeNode(nodeFromDisk, INT_TYPE_SIGNAL);

            assertThat(deserializedNode.getIsLeaf()).isTrue();
            assertThat(deserializedNode.getKeys()).containsExactlyElementsOf(node.getKeys());
            assertThat(deserializedNode.getRecordOffsets())
                .containsExactlyElementsOf(node.getRecordOffsets());
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
    );
  }

  @Test
  void testCreateFileWithStringKey() throws IOException {
    indexFile.openFile(String.format("index-%d.tc", rand.nextInt()));
    IntStream.range(0, 10).forEach(
        i -> {
          BTreeNode<String> node = BTreeNode.createLeafNode();
          node.setKeys(
              List.of(
                  StringField.fromValue(String.format("%d", rand.nextInt())),
                  StringField.fromValue(String.format("%d", rand.nextInt())),
                  StringField.fromValue(String.format("%d", rand.nextInt()))
              )
          );
          node.setPointerOffsets(
              List.of(
                  LongField.fromValue(rand.nextLong()),
                  LongField.fromValue(rand.nextLong()),
                  LongField.fromValue(rand.nextLong())
              )
          );
          ByteBuffer byteBuffer = SerializationUtil.serialize(node);
          try {
            indexFile.append(byteBuffer);
            ByteBuffer nodeFromDisk = indexFile.read(i);
            BTreeNode<String> deserializedNode = SerializationUtil
                .deserializeBTreeNode(nodeFromDisk, STRING_TYPE_SIGNAL);

            assertThat(deserializedNode.getIsLeaf()).isTrue();
            assertThat(deserializedNode.getKeys()).containsExactlyElementsOf(node.getKeys());
            assertThat(deserializedNode.getRecordOffsets())
                .containsExactlyElementsOf(node.getRecordOffsets());
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
    );
  }
}
