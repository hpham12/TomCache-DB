package com.hpham.database.btree_disk.util;

import com.hpham.database.btree_disk.BTreeNode;
import com.hpham.database.btree_disk.Record;
import com.hpham.database.btree_disk.data_types.Field;
import com.hpham.database.btree_disk.data_types.IntField;
import com.hpham.database.btree_disk.data_types.LongField;
import com.hpham.database.btree_disk.data_types.SortableField;
import com.hpham.database.btree_disk.data_types.StringField;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.stream.IntStream;

import static com.hpham.database.btree_disk.BTree.FANOUT;
import static com.hpham.database.btree_disk.constants.DataConstants.BOOL_SIZE_BYTES;
import static com.hpham.database.btree_disk.constants.DataConstants.INT_SIZE_BYTES;
import static com.hpham.database.btree_disk.constants.DataConstants.INT_TYPE_SIGNAL;
import static com.hpham.database.btree_disk.constants.DataConstants.PAGE_SIZE_BYTES;
import static com.hpham.database.btree_disk.constants.DataConstants.POINTER_SIZE_BYTES;
import static com.hpham.database.btree_disk.constants.DataConstants.STRING_SIZE_BYTES;
import static com.hpham.database.btree_disk.constants.DataConstants.STRING_TYPE_SIGNAL;

public class SerializationUtil {
  /**
   * Immutable template to calculate leaf node size.
   */
  private static final Map<String, Integer> leafNodeSizes = Map.of(
      "isLeaf", BOOL_SIZE_BYTES,
      "numKeys", INT_SIZE_BYTES,
      "keys", 0,  //to be overridden
      "hasParent", BOOL_SIZE_BYTES,
      "parentOffset", POINTER_SIZE_BYTES,
      "numRecords", INT_SIZE_BYTES,
      "recordsOffset", 0 //to be overridden
  );

  /**
   * Immutable template to calculate internal node size.
   */
  private static final Map<String, Integer> internalNodeSizes = Map.of(
      "isLeaf", BOOL_SIZE_BYTES,
      "numKeys", INT_SIZE_BYTES,
      "keys", 0,  //to be overridden
      "hasParent", BOOL_SIZE_BYTES,
      "parentOffset", POINTER_SIZE_BYTES,
      "numPointers", INT_SIZE_BYTES,
      "pointersOffset", POINTER_SIZE_BYTES   //to be overridden
  );

  public static <K extends Comparable<K>> ByteBuffer serialize(BTreeNode<K> node) {
    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(PAGE_SIZE_BYTES);

    byteBuffer.putChar((char) (node.getIsLeaf() ? 1 : 0));

    if (node.getParent() == null) {
      byteBuffer.putChar((char) 0);
      byteBuffer.putLong(0);
    } else {
      byteBuffer.putChar((char) 1);
      byteBuffer.put(node.getParentOffset().serialize());
    }

    if (node.getIsLeaf()) {
      // records
      byteBuffer.putInt(node.getRecordOffsets().size());
      node.getRecordOffsets().forEach(recordOffset -> byteBuffer.put(recordOffset.serialize()));

      // add padding to make the size constant
      for (int i = node.getRecordOffsets().size(); i < FANOUT; i++) {
        byteBuffer.putLong(0);
      }
    } else {
      // pointers
      byteBuffer.putInt(node.getPointers().size());
      node.getPointerOffsets().forEach(pointerOffset -> byteBuffer.put(pointerOffset.serialize()));

      // add padding to make the size constant
      for (int i = node.getPointerOffsets().size(); i < FANOUT; i++) {
        byteBuffer.putLong(0);
      }
    }

    byteBuffer.putInt(node.getKeys().size());

    node.getKeys().forEach(key -> byteBuffer.put(key.serialize()));
    while (byteBuffer.position() < PAGE_SIZE_BYTES) {
      byteBuffer.put((byte) 0);
    }

    byteBuffer.position(PAGE_SIZE_BYTES);
    byteBuffer.flip();
    return byteBuffer;
  }

  @SuppressWarnings("unchecked")
  public static <K extends Comparable<K>> BTreeNode<K> deserializeBTreeNode(
      ByteBuffer byteBuffer,
      int typeSignal
  ) {
    BTreeNode<K> treeNode;
    char isLeafByte = byteBuffer.getChar();
    if (isLeafByte == 1) {
      treeNode = BTreeNode.createLeafNode();
    } else {
      treeNode = BTreeNode.createInternalNode();
    }

    char hasParent = byteBuffer.getChar();

    long parentOffset = byteBuffer.getLong();
    if (hasParent != 0) {
      treeNode.setParentOffset(LongField.fromValue(parentOffset));
    }

    int numRecords = byteBuffer.getInt();
    IntStream.range(0, numRecords)
        .forEach(i -> treeNode.getRecordOffsets().add(LongField.fromValue(byteBuffer.getLong())));

    // skip the padding
    for (int i = numRecords; i < FANOUT; i++) {
      byteBuffer.getLong();
    }

    int numKey = byteBuffer.getInt();

    // TODO: expand type support
    if (numKey > 0) {
      switch (typeSignal) {
        case INT_TYPE_SIGNAL -> IntStream.range(0, numKey)
            .forEach(i -> treeNode.getKeys().add(
                (SortableField<K>) Field.fromValue(byteBuffer.getInt()))
            );

        case STRING_TYPE_SIGNAL -> IntStream.range(0, numKey)
            .forEach(i -> {
                  byte[] stringBytes = new byte[STRING_SIZE_BYTES];
                  byteBuffer.get(stringBytes);
                  treeNode.getKeys().add(
                      (SortableField<K>) Field.fromValue(StringField.deserialize(stringBytes, 0)));
                }
            );
      }
    }

    return treeNode;
  }

  /**
   * Serialize a record. A serialized record has the following format:
   * <code>
   *    [key][fieldValue][fieldValue][...]
   * </code>
   * */
  public static <K extends Comparable<K>> ByteBuffer serialize(Record<K> record) {
    int recordSize = record.getSize();
    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(recordSize);

    byteBuffer.put(record.getKey().serialize());
    record.getValue().serializeFields()
        .forEach(byteBuffer::put);

    byteBuffer.flip();
    return byteBuffer;
  }
}
