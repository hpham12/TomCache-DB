package com.hpham.database.btree_disk;

import com.hpham.database.btree_disk.annotations.ForSerialization;
import com.hpham.database.btree_disk.data_types.Field;
import com.hpham.database.btree_disk.data_types.IntField;
import com.hpham.database.btree_disk.data_types.LongField;
import com.hpham.database.btree_disk.data_types.Serializable;
import com.hpham.database.btree_disk.data_types.SortableField;
import com.hpham.database.btree_disk.data_types.StringField;
import com.hpham.database.btree_disk.exceptions.InvalidMethodInvocationException;
import com.hpham.database.btree_disk.exceptions.RecordAlreadyExistException;
import com.hpham.database.btree_disk.exceptions.RecordNotFoundException;
import com.hpham.database.btree_disk.file_formats.Store;
import com.hpham.database.btree_disk.file_formats.index.IndexFile;
import com.hpham.database.btree_disk.file_formats.record.RecordFile;
import com.hpham.database.btree_disk.util.SearchUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.hpham.database.btree_disk.BTree.FANOUT;
import static com.hpham.database.btree_disk.constants.DataConstants.INT_TYPE_SIGNAL;
import static com.hpham.database.btree_disk.constants.DataConstants.PAGE_SIZE_BYTES;
import static com.hpham.database.btree_disk.constants.DataConstants.STRING_TYPE_SIGNAL;

/**
 * Class representing B-Tree node.
 * <br>
 * If a node is a leaf node, pointers and keys will be {@code null},
 * and records will contain the actual record
 */
@Getter
@Setter
public class BTreeNode<K extends Comparable<K>> implements Serializable {
  @ForSerialization
  private Boolean isLeaf;
  @ForSerialization
  private List<SortableField<K>> keys;
  @ForSerialization
  private List<Long> pointerOffsets;
  @ForSerialization
  private List<Long> recordOffsets;
  @ForSerialization
  LongField parentOffset;
  @ForSerialization
  Byte keyTypeSignal;
  @ForSerialization
  Long offset;

  private BTreeNode(Boolean isLeaf, Byte keyTypeSignal){
    this.keyTypeSignal = keyTypeSignal;
    this.isLeaf = isLeaf;
    keys = new ArrayList<>();
    if (isLeaf) {
      recordOffsets = new ArrayList<>();
    } else {
      pointerOffsets = new ArrayList<>();
    }
    try {
      this.offset = appendNode(this);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Create a leaf node.
   */
  public static <K extends Comparable<K>> BTreeNode<K> createLeafNode(Byte keyTypeSignal) {
    return new BTreeNode<>(true, keyTypeSignal);
  }

  /**
   * Create an internal node.
   */
  public static <K extends Comparable<K>> BTreeNode<K> createInternalNode(Byte keyTypeSignal) {
    return new BTreeNode<>(false, keyTypeSignal);
  }

//  /**
//   * Update an existing record.
//   *
//   * @throws InvalidMethodInvocationException if the record key is not found
//   */
//  Record<K> updateRecord(@NonNull Record<K> newRecord) {
//    if (!this.isLeaf) {
//      throw new InvalidMethodInvocationException("Cannot update record in an internal node");
//    }
//
//    Optional<Record<K>> existingRecordOptional = this.records.stream()
//        .filter(record -> record.getKey().equals(newRecord.getKey()))
//        .findAny();
//
//    if (existingRecordOptional.isEmpty()) {
//      throw new RecordNotFoundException(newRecord.getKey());
//    }
//
//    return existingRecordOptional.map(r -> {
//      r.setValue(newRecord.getValue());
//      return r;
//    }).get();
//  }

  // TODO: need to be more perfomant
  private void sortRecordOffsets() {
    this.recordOffsets.sort((o1, o2) -> {
      try {
        return getRecord(o1).compareTo(getRecord(o2));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  /**
   * Add new record to the current leaf node.
   *
   * @param newRecord new record to add
   * @return New root of the B-Tree, {@code null} if the root does not change
   * @throws InvalidMethodInvocationException if this is not a leaf node
   */
  Optional<BTreeNode<K>> addNewRecord(@NonNull Record<K> newRecord) throws IOException {
    if (!this.isLeaf) {
      throw new InvalidMethodInvocationException("Cannot add new record to an internal node");
    }

    // check for duplicate
    // TODO - make this more performant
    boolean hasDuplicate = this.recordOffsets.stream().anyMatch(
        offset -> {
          try {
            return getRecord(offset).getKey().equals(newRecord.getKey());
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
    );

    if (hasDuplicate) {
      throw new RecordAlreadyExistException(newRecord.getKey());
    }

    if (this.recordOffsets.size() == FANOUT) {
      // The leaf node is full, so we need to split
//      return splitLeafNode(this, newRecord); // TODO: proceed!

      return null;
    } else {
      this.recordOffsets.add(Store.RECORD_FILE.append(newRecord.serialize()));
      sortRecordOffsets();
      this.keys.add(newRecord.getKey());
      Collections.sort(this.keys);

      updateNode(this.offset, this);

      return Optional.empty();
    }
  }

//  /**
//   * Split the overflowed node, then add new record.
//   */
//  private Optional<BTreeNode<K>> splitLeafNode(
//      @NonNull BTreeNode<K> nodeToSplit,
//      @NonNull Record<K> newRecord
//  ) {
//    final BTreeNode<K> newLeafNode = createLeafNode();
//    List<Record<K>> combinedRecords = nodeToSplit.records;
//    nodeToSplit.records = new ArrayList<>();
//    nodeToSplit.keys = new ArrayList<>();
//    combinedRecords.add(newRecord);
//    combinedRecords.sort(Comparator.comparing(Record::getKey));
//
//    // split records into 2 halves
//    for (int i = 0; i <= FANOUT; i++) {
//      Record<K> currentRecord = combinedRecords.get(i);
//      if (i < FANOUT / 2) {
//        nodeToSplit.keys.add(currentRecord.getKey());
//        nodeToSplit.records.add(currentRecord);
//      } else {
//        newLeafNode.keys.add(currentRecord.getKey());
//        newLeafNode.records.add(currentRecord);
//      }
//    }
//
//    // bubble the key up to parent node
//    SortableField<K> keyBubbledUp = combinedRecords.get(FANOUT / 2).getKey();
//
//    if (nodeToSplit.parent == null) {
//      // parent == null implies that nodeToSplit is the root,
//      // thus we need to create a new parent node
//      BTreeNode<K> newParent = createInternalNode();
//      nodeToSplit.parent = newParent;
//      newLeafNode.parent = newParent;
//
//      nodeToSplit.parent.pointers.add(nodeToSplit);
//      nodeToSplit.parent.pointers.add(newLeafNode);
//      nodeToSplit.parent.keys.add(keyBubbledUp);
//
//      return Optional.ofNullable(nodeToSplit.parent);
//    }
//
//    newLeafNode.parent = nodeToSplit.parent;
//    return nodeToSplit.parent.addNewKey(keyBubbledUp, newLeafNode);
//  }
//
//  public Boolean isRootNode() {
//    return this.parent == null;
//  }

//  /**
//   * Add new key to the current internal node.
//   *
//   * @param newKey       new key to add.
//   * @param newChildNode new child node bubbling up.
//   * @return New root of the B-Tree, {@code null} if the root does not change
//   * @throws InvalidMethodInvocationException if the current node is not an internal node
//   */
//  private Optional<BTreeNode<K>> addNewKey(@NonNull SortableField<K> newKey, @NonNull BTreeNode<K> newChildNode) {
//    if (isLeaf) {
//      throw new InvalidMethodInvocationException("Cannot call addNewKey on an internal node");
//    }
//
//    if (pointers.size() == FANOUT) {
//      // The internal node is full, so we need to split
//      return splitInternalNodeAndAddNewKey(this, newKey, newChildNode);
//    } else {
//      // No need to split as the current node can still contain more key(s) and pointer(s)
//      return addNewKeyWithoutSplitting(this, newKey, newChildNode);
//    }
//  }
//
//  private Optional<BTreeNode<K>> addNewKeyWithoutSplitting(
//      BTreeNode<K> node,
//      SortableField<K> newKey,
//      BTreeNode<K> newChildNode
//  ) {
//    int newKeyIndex = SearchUtil.findFirstLargerIndex(newKey, keys);
//    node.keys.add(newKeyIndex, newKey);
//    newChildNode.setParent(node);
//    node.pointers.add(newKeyIndex + 1, newChildNode);
//    return Optional.empty();
//  }
//
//  /**
//   * Split an internal node.
//   */
//  private Optional<BTreeNode<K>> splitInternalNodeAndAddNewKey(
//      @NonNull BTreeNode<K> nodeToSplit,
//      @NonNull SortableField<K> newKey,
//      @NonNull BTreeNode<K> newChildNode
//  ) {
//    final BTreeNode<K> newNode = createInternalNode();
//    List<SortableField<K>> combinedKeys = nodeToSplit.keys;
//    final List<BTreeNode<K>> combinedPointers = nodeToSplit.pointers;
//
//    nodeToSplit.keys = new ArrayList<>();
//    nodeToSplit.pointers = new ArrayList<>();
//
//    int newKeyIndex = SearchUtil.findFirstLargerIndex(newKey, combinedKeys);
//    combinedKeys.add(newKeyIndex, newKey);
//
//    // split keys into 2 halves
//    for (int i = 0; i < FANOUT; i++) {
//      if (i < FANOUT / 2) {
//        nodeToSplit.keys.add(combinedKeys.get(i));
//      } else if (i > FANOUT / 2) {
//        newNode.keys.add(combinedKeys.get(i));
//      }
//    }
//
//    combinedPointers.add(newKeyIndex + 1, newChildNode);
//
//    // Split pointers
//    for (int i = 0; i <= FANOUT; i++) {
//      if (i <= FANOUT / 2) {
//        nodeToSplit.pointers.add(combinedPointers.get(i));
//        combinedPointers.get(i).setParent(this);
//      } else {
//        newNode.pointers.add(combinedPointers.get(i));
//        combinedPointers.get(i).setParent(newNode);
//      }
//    }
//
//    // bubble the key up to parent node
//    SortableField<K> keyBubbledUp = combinedKeys.get(FANOUT / 2);
//
//    if (parent == null) {
//      // parent == null implies that nodeToSplit is the root,
//      // thus we need to create a new parent node
//      BTreeNode<K> newParent = createInternalNode();
//      nodeToSplit.parent = newParent;
//      newNode.parent = newParent;
//
//      nodeToSplit.parent.pointers.add(this);
//      nodeToSplit.parent.pointers.add(newNode);
//      nodeToSplit.parent.keys.add(keyBubbledUp);
//
//      return Optional.of(nodeToSplit.parent);
//    } else {
//      return nodeToSplit.parent.addNewKey(keyBubbledUp, newNode);
//    }
//  }

//  /**
//   * Delete a record, given a {@code key}.
//   *
//   * @param key key of the record to be deleted
//   * @return New root of the B-Tree, {@code null} if the root does not change
//   * @throws InvalidMethodInvocationException if the current node is not a leaf node
//   * @throws RecordNotFoundException          if there is no record associated with the given {@code key}.
//   */
//  Optional<BTreeNode<K>> deleteRecord(@NonNull SortableField<K> key) {
//    if (!isLeaf) {
//      throw new InvalidMethodInvocationException("Cannot call deleteRecord on an internal node");
//    }
//
//    Record<K> recordToDelete = records
//        .stream()
//        .filter(r -> r.getKey().equals(key))
//        .findAny()
//        .orElse(null);
//
//    if (recordToDelete == null) {
//      throw new RecordNotFoundException(key);
//    }
//
//    this.records.remove(recordToDelete);
//    this.keys.remove(recordToDelete.getKey());
//
//    // This is the case where the current leaf node is also a root
//    if (this.parent == null) {
//      return Optional.of(this);
//    }
//
//    if (this.records.size() < Math.ceil(((double) FANOUT) / 2)) {
//      // needs to merge/rebalance
//      BTreeNode<K> nodeToMergeWith = findNodeToMerge(this);
//
//      if (nodeToMergeWith == null) {
//        // cannot find node to merge with, need to rebalance instead
//        BTreeNode<K> nodeToRebalanceWith = findNodeToRebalanceWith(this);
//        if (nodeToRebalanceWith == null) {
//          throw new RuntimeException("Tree in invalid state");
//        }
//        return reBalanceLeafNode(this, nodeToRebalanceWith);
//      }
//
//      return mergeLeafNodes(this, nodeToMergeWith);
//    }
//    return Optional.empty();
//  }

  @SuppressWarnings("unchecked")
  public static <K extends Comparable<K>> BTreeNode<K> deserialize(
      ByteBuffer byteBuffer
  ) {
    BTreeNode<K> treeNode;
    byte typeSignal = byteBuffer.get();
    byte isLeafByte = byteBuffer.get();
    if (isLeafByte == 0x01) {
      treeNode = BTreeNode.createLeafNode(typeSignal);
    } else {
      treeNode = BTreeNode.createInternalNode(typeSignal);
    }

    byte hasParent = byteBuffer.get();

    long parentOffset = byteBuffer.getLong();
    if (hasParent != 0x00) {
      treeNode.setParentOffset(LongField.fromValue(parentOffset));
    }

    long offset = byteBuffer.getLong();
    treeNode.setOffset(offset);

    int numRecordsOrPointers = byteBuffer.getInt();

    if (treeNode.isLeaf) {
      IntStream.range(0, numRecordsOrPointers)
          .forEach(i -> treeNode.getRecordOffsets().add(byteBuffer.getLong()));
    } else {
      IntStream.range(0, numRecordsOrPointers)
          .forEach(i -> treeNode.getPointerOffsets().add(byteBuffer.getLong()));
    }


    // skip the padding
    for (int i = numRecordsOrPointers; i < FANOUT; i++) {
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
                  treeNode.getKeys().add(
                      (SortableField<K>) Field.fromValue(StringField.deserialize(byteBuffer, byteBuffer.position())));
                }
            );
      }
    }

    return treeNode;
  }

  /**
   * Immutable template to calculate leaf node size.
   * <p>
   * private static final Map<String, Integer> leafNodeSizes = Map.of(
   * "keyType", 1
   * "isLeaf", BOOL_SIZE_BYTES,
   * "numKeys", INT_SIZE_BYTES,
   * "keys", 0,  //to be overridden
   * "hasParent", BOOL_SIZE_BYTES,
   * "parentOffset", POINTER_SIZE_BYTES,
   * "numRecords", INT_SIZE_BYTES,
   * "recordsOffset", 0 //to be overridden
   * );
   * <p>
   * <p>
   * Immutable template to calculate internal node size.
   * <p>
   * private static final Map<String, Integer> internalNodeSizes = Map.of(
   * "keyType", 1
   * "isLeaf", BOOL_SIZE_BYTES,
   * "numKeys", INT_SIZE_BYTES,
   * "keys", 0,  //to be overridden
   * "hasParent", BOOL_SIZE_BYTES,
   * "parentOffset", POINTER_SIZE_BYTES,
   * "numPointers", INT_SIZE_BYTES,
   * "pointersOffset", POINTER_SIZE_BYTES   //to be overridden
   * );
   */
  @Override
  public ByteBuffer serialize() {
    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(PAGE_SIZE_BYTES);
    byteBuffer.put(keyTypeSignal);

    byteBuffer.put((byte) (this.getIsLeaf() ? 1 : 0));

    if (this.getParentOffset() == null) {
      byteBuffer.put((byte) 0x00);
      byteBuffer.putLong(0);
    } else {
      byteBuffer.put((byte) 0x01);
      byteBuffer.put(this.getParentOffset().serialize());
    }

    byteBuffer.putLong(this.offset != null? this.offset : 0);

    if (this.getIsLeaf()) {
      // records
      byteBuffer.putInt(this.getRecordOffsets().size());
      this.getRecordOffsets().forEach(byteBuffer::putLong);

      // add padding to make the size constant
      for (int i = this.getRecordOffsets().size(); i < FANOUT; i++) {
        byteBuffer.putLong(0);
      }
    } else {
      // pointers
      byteBuffer.putInt(this.getPointerOffsets().size());
      this.getPointerOffsets().forEach(byteBuffer::putLong);

      // add padding to make the size constant
      for (int i = this.getPointerOffsets().size(); i < FANOUT; i++) {
        byteBuffer.putLong(0);
      }
    }

    byteBuffer.putInt(this.getKeys().size());

    this.getKeys().forEach(key -> byteBuffer.put(key.serialize()));
    while (byteBuffer.position() < PAGE_SIZE_BYTES) {
      byteBuffer.put((byte) 0);
    }

    byteBuffer.position(PAGE_SIZE_BYTES);
    byteBuffer.flip();
    return byteBuffer;
  }

//  private Optional<BTreeNode<K>> reBalanceLeafNode(
//      @NonNull BTreeNode<K> underflowNode,
//      @NonNull BTreeNode<K> nodeToRebalanceWith
//  ) {
//    SiblingPosition positionOfNodeToRebalance = determineSiblingPosition(
//        underflowNode,
//        nodeToRebalanceWith
//    );
//    int parentKeyIndexToChange;
//    Record<K> recordToMove;
//
//    if (SiblingPosition.TO_THE_LEFT.equals(positionOfNodeToRebalance)) {
//      parentKeyIndexToChange = underflowNode.parent.pointers.indexOf(nodeToRebalanceWith);
//      recordToMove = nodeToRebalanceWith.records.removeLast();
//      nodeToRebalanceWith.keys.removeLast();
//      underflowNode.records.addFirst(recordToMove);
//      underflowNode.keys.addFirst(recordToMove.getKey());
//      underflowNode.parent.keys.set(parentKeyIndexToChange, recordToMove.getKey());
//    } else {
//      parentKeyIndexToChange = underflowNode.parent.pointers.indexOf(underflowNode);
//      recordToMove = nodeToRebalanceWith.records.removeFirst();
//      nodeToRebalanceWith.keys.removeFirst();
//      var keyToPromote = nodeToRebalanceWith.records.getFirst().getKey();
//      underflowNode.records.addLast(recordToMove);
//      underflowNode.keys.addLast(recordToMove.getKey());
//      underflowNode.parent.keys.set(parentKeyIndexToChange, keyToPromote);
//    }
//
//    return Optional.empty();
//  }
//
//  private Optional<BTreeNode<K>> mergeLeafNodes(
//      @NonNull BTreeNode<K> underFlowNode,
//      @NonNull BTreeNode<K> nodeToMergeWith
//  ) {
//    SiblingPosition positionOfNodeToMerge = determineSiblingPosition(
//        underFlowNode,
//        nodeToMergeWith
//    );
//
//    int parentPointerIndexToDelete;
//    BTreeNode<K> nodeToPossiblyBeRoot;
//    if (SiblingPosition.TO_THE_LEFT.equals(positionOfNodeToMerge)) {
//      // Merge into the sibling
//      nodeToPossiblyBeRoot = nodeToMergeWith;
//      nodeToMergeWith.records.addAll(underFlowNode.records);
//      nodeToMergeWith.keys.addAll(underFlowNode.keys);
//      parentPointerIndexToDelete = this.parent.pointers.indexOf(this);
//    } else {
//      // Merge into the sibling
//      nodeToPossiblyBeRoot = underFlowNode;
//      underFlowNode.records.addAll(nodeToMergeWith.records);
//      underFlowNode.keys.addAll(nodeToMergeWith.keys);
//      parentPointerIndexToDelete = underFlowNode.parent.pointers.indexOf(nodeToMergeWith);
//    }
//
//    underFlowNode.parent.pointers.remove(parentPointerIndexToDelete);
//    underFlowNode.parent.keys.remove(parentPointerIndexToDelete - 1);
//    if (underFlowNode.parent.parent == null && underFlowNode.parent.keys.isEmpty()) {
//      underFlowNode.parent = null;
//      return Optional.of(nodeToPossiblyBeRoot);
//    }
//
//    return this.parent.mergeOrRebalance();
//  }

//  /**
//   * Merge or rebalance a non-leaf node.
//   *
//   * @return a node that will be a new root, null if root does not change
//   */
//  private Optional<BTreeNode<K>> mergeOrRebalance() {
//    if (isLeaf) {
//      throw new InvalidMethodInvocationException("Cannot call mergeOrRebalance on leaf node");
//    }
//
//    if (this.parent == null) {
//      return Optional.empty();
//    }
//
//    // in case this is not root
//    boolean isUnderFlow = this.pointers.size() < Math.ceil(((double) FANOUT) / 2);
//
//    if (isUnderFlow) {
//      // needs to merge/rebalance
//      BTreeNode<K> nodeToMergeWith = findNodeToMerge(this);
//
//      if (nodeToMergeWith == null) {
//        // cannot find node to merge with, need to rebalance instead
//        BTreeNode<K> nodeToRebalanceWith = findNodeToRebalanceWith(this);
//        if (nodeToRebalanceWith == null) {
//          throw new RuntimeException("Tree in invalid state");
//        }
//        return rebalanceInternalNode(this, nodeToRebalanceWith);
//      }
//
//      return mergeInternalNodes(this, nodeToMergeWith);
//    }
//
//    return Optional.empty();
//  }

//  private Optional<BTreeNode<K>> mergeInternalNodes(
//      @NonNull BTreeNode<K> underflowNode,
//      @NonNull BTreeNode<K> nodeToMergeWith
//  ) {
//    SiblingPosition positionOfNodeToMerge = determineSiblingPosition(
//        underflowNode,
//        nodeToMergeWith
//    );
//
//    if (SiblingPosition.TO_THE_LEFT.equals(positionOfNodeToMerge)) {
//      // Merge into the sibling
//      nodeToMergeWith.pointers.addAll(underflowNode.pointers);
//      for (BTreeNode<K> pointer : underflowNode.pointers) {
//        pointer.setParent(nodeToMergeWith);
//      }
//      int parentPointerIndexToDelete = underflowNode.parent.pointers.indexOf(underflowNode);
//      SortableField<K> keyToDemoteFromParent = underflowNode.parent.keys.get(parentPointerIndexToDelete - 1);
//      nodeToMergeWith.keys.add(keyToDemoteFromParent);
//      nodeToMergeWith.keys.addAll(underflowNode.keys);
//      underflowNode.parent.pointers.remove(parentPointerIndexToDelete);
//      underflowNode.parent.keys.remove(keyToDemoteFromParent);
//      if (underflowNode.parent.parent == null && underflowNode.parent.keys.isEmpty()) {
//        nodeToMergeWith.parent = null;
//        return Optional.of(nodeToMergeWith);
//      }
//    } else {
//      // Merge into the current node
//      underflowNode.pointers.addAll(nodeToMergeWith.pointers);
//      for (BTreeNode<K> pointer : nodeToMergeWith.pointers) {
//        pointer.setParent(underflowNode);
//      }
//      int parentPointerIndexToDelete = underflowNode.parent.pointers.indexOf(nodeToMergeWith);
//      SortableField<K> keyToDemoteFromParent = underflowNode.parent.keys.get(parentPointerIndexToDelete - 1);
//      underflowNode.keys.add(keyToDemoteFromParent);
//      underflowNode.keys.addAll(nodeToMergeWith.keys);
//      underflowNode.parent.pointers.remove(parentPointerIndexToDelete);
//      underflowNode.parent.keys.remove(keyToDemoteFromParent);
//      if (underflowNode.parent.parent == null && underflowNode.parent.keys.isEmpty()) {
//        underflowNode.parent = null;
//        return Optional.of(underflowNode);
//      }
//    }
//    return underflowNode.parent.mergeOrRebalance();
//  }

//  /**
//   * Rebalance internal node.
//   */
//  private Optional<BTreeNode<K>> rebalanceInternalNode(
//      @NonNull BTreeNode<K> underflowNode,
//      @NonNull BTreeNode<K> nodeToRebalanceWith
//  ) {
//    SiblingPosition positionOfNodeToRebalance = determineSiblingPosition(
//        underflowNode,
//        nodeToRebalanceWith
//    );
//    int parentKeyIndexToChange;
//    SortableField<K> keyToMove;
//    if (SiblingPosition.TO_THE_LEFT.equals(positionOfNodeToRebalance)) {
//      parentKeyIndexToChange = underflowNode.parent.pointers.indexOf(nodeToRebalanceWith);
//      final var keyToPromote = nodeToRebalanceWith.keys.getLast();
//      keyToMove = parent.keys.get(parentKeyIndexToChange);
//      underflowNode.keys.addFirst(keyToMove);
//      var pointerToMove = nodeToRebalanceWith.pointers.getLast();
//      pointerToMove.setParent(underflowNode);
//      underflowNode.pointers.addFirst(pointerToMove);
//      nodeToRebalanceWith.keys.removeLast();
//      nodeToRebalanceWith.pointers.removeLast();
//      underflowNode.parent.keys.set(parentKeyIndexToChange, keyToPromote);
//    } else {
//      parentKeyIndexToChange = underflowNode.parent.pointers.indexOf(underflowNode);
//      keyToMove = nodeToRebalanceWith.keys.getFirst();
//
//      var pointerToMove = nodeToRebalanceWith.pointers.getFirst();
//      pointerToMove.setParent(underflowNode);
//      underflowNode.pointers.addLast(pointerToMove);
//      underflowNode.keys.addLast(underflowNode.parent.keys.get(parentKeyIndexToChange));
//      nodeToRebalanceWith.keys.removeFirst();
//      nodeToRebalanceWith.pointers.removeFirst();
//      underflowNode.parent.keys.set(parentKeyIndexToChange, keyToMove);
//    }
//
//    return Optional.empty();
//  }

//  private BTreeNode<K> findNodeToRebalanceWith(@NonNull BTreeNode<K> node) {
//    BTreeNode<K> parent = node.getParent();
//    List<BTreeNode<K>> parentPointers = parent.getPointers();
//    BTreeNode<K> leftSibling;
//    BTreeNode<K> rightSibling;
//
//    if (parentPointers.getFirst().equals(node)) {
//      leftSibling = null;
//    } else {
//      leftSibling = parentPointers.get(parentPointers.indexOf(node) - 1);
//    }
//
//    if (parentPointers.getLast().equals(node)) {
//      rightSibling = null;
//    } else {
//      rightSibling = parentPointers.get(parentPointers.indexOf(node) + 1);
//    }
//
//    if (node.isLeaf) {
//      if (leftSibling != null && rightSibling != null) {
//        if (rightSibling.records.size() > Math.ceil((double) FANOUT / 2)) {
//          return rightSibling;
//        }
//        if (leftSibling.records.size() > Math.ceil((double) FANOUT / 2)) {
//          return leftSibling;
//        }
//      } else if (rightSibling != null) {
//        if (rightSibling.records.size() > Math.ceil((double) FANOUT / 2)) {
//          return rightSibling;
//        }
//      } else if (leftSibling != null) {
//        if (leftSibling.records.size() > Math.ceil((double) FANOUT / 2)) {
//          return leftSibling;
//        }
//      }
//    } else {
//      if (leftSibling != null && rightSibling != null) {
//        if (rightSibling.pointers.size() >= Math.ceil((double) (FANOUT + 1) / 2)) {
//          return rightSibling;
//        }
//        if (leftSibling.pointers.size() >= Math.ceil((double) (FANOUT + 1) / 2)) {
//          return leftSibling;
//        }
//      } else if (rightSibling != null) {
//        if (rightSibling.pointers.size() >= Math.ceil((double) (FANOUT + 1) / 2)) {
//          return rightSibling;
//        }
//      } else if (leftSibling != null) {
//        if (leftSibling.pointers.size() >= Math.ceil((double) (FANOUT + 1) / 2)) {
//          return leftSibling;
//        }
//      }
//    }
//
//    return null;
//  }

//  /**
//   * Find a node that the input node can be merged with.
//   *
//   * @return sibling node that will be used to merge
//   */
//  private BTreeNode<K> findNodeToMerge(@NonNull BTreeNode<K> node) {
//    BTreeNode<K> parent = node.getParent();
//    List<BTreeNode<K>> parentPointers = parent.getPointers();
//    BTreeNode<K> leftSibling;
//    BTreeNode<K> rightSibling;
//
//    if (parentPointers.getFirst().equals(node)) {
//      leftSibling = null;
//    } else {
//      leftSibling = parentPointers.get(parentPointers.indexOf(node) - 1);
//    }
//
//    if (parentPointers.getLast().equals(node)) {
//      rightSibling = null;
//    } else {
//      rightSibling = parentPointers.get(parentPointers.indexOf(node) + 1);
//    }
//
//    if (node.isLeaf) {
//      if (leftSibling != null && rightSibling != null) {
//        if (rightSibling.records.size() + node.records.size() <= FANOUT) {
//          return rightSibling;
//        }
//        if (leftSibling.records.size() + node.records.size() <= FANOUT) {
//          return leftSibling;
//        }
//      } else if (rightSibling != null) {
//        if (rightSibling.records.size() + node.records.size() <= FANOUT) {
//          return rightSibling;
//        }
//      } else if (leftSibling != null) {
//        if (leftSibling.records.size() + node.records.size() <= FANOUT) {
//          return leftSibling;
//        }
//      }
//    } else {
//      if (leftSibling != null && rightSibling != null) {
//        if (rightSibling.pointers.size() + node.pointers.size() <= FANOUT) {
//          return rightSibling;
//        }
//        if (leftSibling.pointers.size() + node.pointers.size() <= FANOUT) {
//          return leftSibling;
//        }
//      } else if (rightSibling != null) {
//        if (rightSibling.pointers.size() + node.pointers.size() <= FANOUT) {
//          return rightSibling;
//        }
//      } else if (leftSibling != null) {
//        if (leftSibling.pointers.size() + node.pointers.size() <= FANOUT) {
//          return leftSibling;
//        }
//      }
//    }
//
//    return null;
//  }
//
//  private @NonNull SiblingPosition determineSiblingPosition(
//      BTreeNode<K> original,
//      BTreeNode<K> sibling
//  ) {
//    BTreeNode<K> parent = original.parent;
//    int originalNodePosition = parent.pointers.indexOf(original);
//    int siblingNodePosition = parent.pointers.indexOf(sibling);
//
//    if (originalNodePosition == -1 || siblingNodePosition == -1) {
//      throw new IllegalArgumentException("Argument node not found in parent's pointers");
//    }
//
//    if (Math.abs(originalNodePosition - siblingNodePosition) != 1) {
//      throw new IllegalArgumentException("Two nodes are not siblings");
//    }
//
//    if (originalNodePosition > siblingNodePosition) {
//      return SiblingPosition.TO_THE_LEFT;
//    }
//
//    return SiblingPosition.TO_THE_RIGHT;
//  }
//
//  private BTreeNode<K> getRoot() {
//    BTreeNode<K> root = this;
//    BTreeNode<K> currentNode = this;
//    while (currentNode.parent != null) {
//      root = currentNode.parent;
//      currentNode = currentNode.parent;
//    }
//
//    return root;
//  }

  private BTreeNode<K> getNode(Long offset) throws IOException {
    return BTreeNode.deserialize(Store.INDEX_FILE.read(offset));
  }


  private void updateNode(Long offset, BTreeNode<K> newNode) throws IOException {
    Store.INDEX_FILE.update(newNode.serialize(), offset);
  }

  private Record<K> getRecord(Long offset) throws IOException {
    return Record.deserialize(Store.RECORD_FILE.read(offset));
  }

  private enum SiblingPosition {
    TO_THE_LEFT,
    TO_THE_RIGHT
  }

  // TODO: The problem here is that node does not have offset until they are persisted....
  private Long appendNode(BTreeNode<K> node) throws IOException {
    return Store.INDEX_FILE.append(node.serialize());
  }
}
