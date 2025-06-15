package com.hpham.database.btree;

import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Class representing a B-Tree.
 * */
@Getter
@Setter
public class BTree<K extends Comparable<K>> {
  public static final Integer FANOUT = 5;
  private BTreeNode<K> root;

  /**
   * Initialize a b-tree.
   * */
  public BTree() {
    root = BTreeNode.createLeafNode();
  }

  /**
   * Insert a record into a b-tree.
   *
   * @param record Record to be added
   * @return added record
   */
  public Record<K, Object> insert(@NonNull Record<K, Object> record) {
    K key = record.getKey();
    BTreeNode<K> targetLeafNode = findTargetLeafNode(key);

    Optional<BTreeNode<K>> newRootOptional = targetLeafNode.addNewRecord(record);

    newRootOptional.ifPresent(newRoot -> this.root = newRoot);

    return record;
  }

  /**
   * Update a record.
   *
   * @param record new record to update
   * @return updated record
   */
  public Record<K, Object> update(@NonNull Record<K, Object> record) {
    K key = record.getKey();
    BTreeNode<K> targetLeafNode = findTargetLeafNode(key);

    return targetLeafNode.updateRecord(record);
  }

  /**
   * Delete a record, given a key.
   *
   * @param key key of the record to delete
   */
  public void delete(@NonNull K key) {
    BTreeNode<K> targetLeafNode = findTargetLeafNode(key);

    Optional<BTreeNode<K>> newRootOptional = targetLeafNode.deleteRecord(key);

    newRootOptional.ifPresent(newRoot -> {
      boolean isNewRootEmpty = newRoot.getIsLeaf()
          ? newRoot.getRecords().isEmpty() : newRoot.getKeys().isEmpty();

      if (isNewRootEmpty) {
        this.root = null;
      } else {
        this.root = newRoot;
        this.root.setParent(null);
      }
    });
  }

  /**
   * Find a record in the tree, given a {@code key}.
   *
   * @param key the search key
   * */
  public Record<K, Object> findRecord(K key) {
    if (this.root == null) {
      return null;
    }
    BTreeNode<K> targetLeafNode = findTargetLeafNode(key);
    int recordIndex = SearchUtil.searchForIndex(
        key,
        targetLeafNode.getRecords().stream().map(Record::getKey).toList()
    );

    if (recordIndex == -1) {
      return null;
    }

    return targetLeafNode.getRecords().get(recordIndex);
  }

  /**
   * Find the leaf node that possibly contain a record with key {@code key}.
   *
   * @param key the search key
   */
  private BTreeNode<K> findTargetLeafNode(K key) {
    BTreeNode<K> currentNode = root;

    while (!currentNode.getIsLeaf()) {
      int pointerIndex = SearchUtil.findFirstLargerIndex(key, currentNode.getKeys());
      currentNode = currentNode.getPointers().get(pointerIndex);
    }

    return currentNode;
  }
}
