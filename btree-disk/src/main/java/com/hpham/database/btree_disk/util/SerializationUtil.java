package com.hpham.database.btree_disk.util;

import com.hpham.database.btree_disk.BTreeNode;
import com.hpham.database.btree_disk.dataTypes.IntField;
import com.hpham.database.btree_disk.dataTypes.SortableField;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.hpham.database.btree_disk.constants.DataTypeSizes.BOOL_SIZE_BYTES;
import static com.hpham.database.btree_disk.constants.DataTypeSizes.INT_SIZE_BYTES;
import static com.hpham.database.btree_disk.constants.DataTypeSizes.POINTER_SIZE_BYTES;

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
      "pointersOffset", 0   //to be overridden
  );

  public static <K extends Comparable<K>> ByteBuffer serialize(BTreeNode<K> node) {
    Map<String, Integer> sizes;
    if (node.getIsLeaf()) {
      sizes = new HashMap<>(leafNodeSizes);
      sizes.put(
          "recordsOffset",
          Optional.ofNullable(node.getRecordOffsets())
              .map(p -> p.size() * POINTER_SIZE_BYTES).orElse(0)
      );
    } else {
      sizes = new HashMap<>(internalNodeSizes);
      sizes.put(
          "pointersOffset",
          Optional.ofNullable(node.getPointerOffsets())
              .map(p -> p.size() * POINTER_SIZE_BYTES).orElse(0)
      );
    }

    // TODO: check type
    sizes.put("keys", node.getKeys().size() * INT_SIZE_BYTES);

    int totalBytes = sizes.values().stream().reduce(Integer::sum).get();
    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(totalBytes);
    byteBuffer.mark();

    byteBuffer.putChar((char) (node.getIsLeaf() ? 1 : 0));
    byteBuffer.putInt(node.getKeys().size());
    node.getKeys().forEach(key -> byteBuffer.put(key.serialize()));
    if (node.getIsLeaf()) {
      // pointers
      byteBuffer.putInt(0);

      // records
      byteBuffer.putInt(node.getRecords().size());
      node.getRecordOffsets().forEach(recordOffset -> byteBuffer.put(recordOffset.serialize()));
    } else {
      // pointers
      byteBuffer.putInt(node.getPointers().size());
      node.getPointerOffsets().forEach(pointerOffset -> byteBuffer.put(pointerOffset.serialize()));

      // records
      byteBuffer.putInt(0);
    }

    if (node.getParent() == null) {
      byteBuffer.putChar((char) 0);
    } else {
      byteBuffer.putChar((char) 1);
      byteBuffer.put(node.getParentOffset().serialize());
    }

    byteBuffer.reset();
    return byteBuffer;
  }

  @SuppressWarnings("unchecked")
  public static <K extends Comparable<K>> BTreeNode<K> deserialize(byte[] bytes) {
    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
    BTreeNode<K> treeNode;
    char isLeafByte = byteBuffer.getChar();
    if (isLeafByte == 1) {
      treeNode = BTreeNode.createLeafNode();
      int numKey = byteBuffer.getInt();
      // TODO: check type
      IntStream.range(0, numKey)
          .forEach(i -> treeNode.getKeys().add((SortableField<K>) SortableField.fromValue(byteBuffer.getInt())));
      int numPointers = byteBuffer.getInt();
      IntStream.range(0, numPointers)
          .forEach(i -> treeNode.getPointerOffsets().add(IntField.fromValue(byteBuffer.getInt())));
      char hasParent = byteBuffer.getChar();
      if (hasParent != 0) {
        treeNode.setParentOffset(IntField.fromValue(byteBuffer.getInt()));
      }
    } else {
      treeNode = BTreeNode.createInternalNode();
    }

    return treeNode;
  }
}
