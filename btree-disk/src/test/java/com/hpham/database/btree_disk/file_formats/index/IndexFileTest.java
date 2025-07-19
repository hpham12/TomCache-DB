package com.hpham.database.btree_disk.file_formats.index;

import com.hpham.database.btree_disk.BTreeNode;
import com.hpham.database.btree_disk.data_types.IntField;
import com.hpham.database.btree_disk.data_types.LongField;
import com.hpham.database.btree_disk.data_types.SortableField;
import com.hpham.database.btree_disk.data_types.StringField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static com.hpham.database.btree_disk.constants.DataConstants.INT_TYPE_SIGNAL;
import static com.hpham.database.btree_disk.constants.DataConstants.PAGE_SIZE_BYTES;
import static com.hpham.database.btree_disk.constants.DataConstants.STRING_TYPE_SIGNAL;
import static org.assertj.core.api.Assertions.assertThat;


public class IndexFileTest {
  private IndexFile indexFile;
  private static final Random rand = new Random();

  @BeforeEach
  void beforeEach() {
    indexFile = new IndexFile();
  }

  @AfterEach
  void afterEach() throws IOException {
    indexFile.deleteAll();
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
          ByteBuffer byteBuffer = node.serialize();
          try {
            indexFile.append(byteBuffer, INT_TYPE_SIGNAL);
            ByteBuffer nodeFromDisk = indexFile.read(i);
            BTreeNode<Integer> deserializedNode = BTreeNode
                .deserialize(nodeFromDisk, INT_TYPE_SIGNAL);

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
          BTreeNode<String> node = BTreeNode.createInternalNode();
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
          ByteBuffer byteBuffer = node.serialize();
          try {
            indexFile.append(byteBuffer, STRING_TYPE_SIGNAL);
            ByteBuffer nodeFromDisk = indexFile.read(i);
            BTreeNode<String> deserializedNode = BTreeNode
                .deserialize(nodeFromDisk, STRING_TYPE_SIGNAL);

            assertThat(deserializedNode.getIsLeaf()).isFalse();
            assertThat(deserializedNode.getKeys()).containsExactlyElementsOf(node.getKeys());
            assertThat(deserializedNode.getPointerOffsets())
                .containsExactlyElementsOf(node.getPointerOffsets());
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
    );
  }

  @Test
  void testUpdateIndex() throws IOException {
    indexFile.openFile(String.format("index-%d.tc", rand.nextInt()));
    IntStream.range(0, 5).forEach(
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
          ByteBuffer byteBuffer = node.serialize();
          try {
            indexFile.append(byteBuffer, INT_TYPE_SIGNAL);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
    );

    long updatedOffset = 2L;

    // Read a random node from disl
    ByteBuffer nodeFromDisk = indexFile.read(updatedOffset);
    BTreeNode<Integer> deserializedNode = BTreeNode.deserialize(nodeFromDisk, INT_TYPE_SIGNAL);

    List<SortableField<Integer>> updatedKeys = List.of(
        IntField.fromValue(rand.nextInt()),
        IntField.fromValue(rand.nextInt()),
        IntField.fromValue(rand.nextInt())
    );

    List<LongField> updatedRecordOffsets = List.of(
        LongField.fromValue(rand.nextLong()),
        LongField.fromValue(rand.nextLong()),
        LongField.fromValue(rand.nextLong())
    );

    deserializedNode.setKeys(updatedKeys);
    deserializedNode.setRecordOffsets(updatedRecordOffsets);

    indexFile.update(deserializedNode.serialize(), updatedOffset);

    ByteBuffer updatedNodeFromDisk = indexFile.read(updatedOffset);
    BTreeNode<Integer> deserializedUpdatedNode = BTreeNode
        .deserialize(updatedNodeFromDisk, INT_TYPE_SIGNAL);

    assertThat(deserializedUpdatedNode.getKeys()).containsExactlyElementsOf(updatedKeys);
    assertThat(deserializedUpdatedNode.getRecordOffsets())
        .containsExactlyElementsOf(updatedRecordOffsets);
  }

  @Test
  void testDelete() throws IOException {
    indexFile.openFile(String.format("index-%d.tc", rand.nextInt()));
    IntStream.range(0, 5).forEach(
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
          ByteBuffer byteBuffer = node.serialize();
          try {
            indexFile.append(byteBuffer, INT_TYPE_SIGNAL);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
    );

    long offsetToDelete = 2L;

    indexFile.delete(offsetToDelete);

    ByteBuffer bb = indexFile.read(offsetToDelete);

    byte[] bytes = new byte[PAGE_SIZE_BYTES];
    bb.get(bytes);

    for (byte b : bytes) {
      assertThat(b).isEqualTo((byte) 0);
    }
  }
}
