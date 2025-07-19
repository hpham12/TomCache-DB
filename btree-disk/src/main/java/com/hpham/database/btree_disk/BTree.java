package com.hpham.database.btree_disk;

import java.io.IOException;
import java.util.Optional;

import com.hpham.database.btree_disk.data_types.SortableField;
import com.hpham.database.btree_disk.file_formats.Store;
import com.hpham.database.btree_disk.file_formats.index.IndexFile;
import com.hpham.database.btree_disk.file_formats.record.RecordFile;
import com.hpham.database.btree_disk.util.SearchUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;

/**
 * Class representing a B-Tree.
 * */
@Getter
@Setter
public class BTree<K extends Comparable<K>> {
  public static final Integer FANOUT = 5;
  private BTreeNode<K> root;
  private byte keyTypeSinal;

  /**
   * Initialize a b-tree.
   * */
  public BTree(byte keyTypeSinal) throws IOException {
    root = BTreeNode.createLeafNode(keyTypeSinal);
    this.keyTypeSinal = keyTypeSinal;
  }

  public BTreeNode<K> getRoot() throws IOException {
    return getNode(Store.INDEX_FILE.getRootOffset());
  }

  /**
   * Insert a record into a b-tree.
   *
   * @param record Record to be added
   * @return added record
   */
  public Record<K> insert(@NonNull Record<K> record) throws IOException {
    SortableField<K> key = record.getKey();
    BTreeNode<K> targetLeafNode = findTargetLeafNode(key);

    Optional<BTreeNode<K>> newRootOptional = targetLeafNode.addNewRecord(record);

    newRootOptional.ifPresent(newRoot -> this.root = newRoot);

    return record;
  }

//  /**
//   * Update a record.
//   *
//   * @param record new record to update
//   * @return updated record
//   */
//  public Record<K> update(@NonNull Record<K> record) {
//    SortableField<K> key = record.getKey();
//    BTreeNode<K> targetLeafNode = findTargetLeafNode(key);
//
//    return targetLeafNode.updateRecord(record);
//  }
//
//  /**
//   * Delete a record, given a key.
//   *
//   * @param key key of the record to delete
//   */
//  public void delete(@NonNull SortableField<K> key) {
//    BTreeNode<K> targetLeafNode = findTargetLeafNode(key);
//
//    Optional<BTreeNode<K>> newRootOptional = targetLeafNode.deleteRecord(key);
//
//    newRootOptional.ifPresent(newRoot -> {
//      boolean isNewRootEmpty = newRoot.getIsLeaf()
//          ? newRoot.getRecords().isEmpty() : newRoot.getKeys().isEmpty();
//
//      if (isNewRootEmpty) {
//        this.root = null;
//      } else {
//        this.root = newRoot;
//        this.root.setParent(null);
//      }
//    });
//  }

  /**
   * Find a record in the tree, given a {@code key}.
   *
   * @param key the search key
   * */
  public Record<K> findRecord(SortableField<K> key) throws IOException{
    if (this.root == null) {
      return null;
    }
    BTreeNode<K> targetLeafNode = findTargetLeafNode(key);
    int recordIndex = SearchUtil.searchForIndex(
        key,
        targetLeafNode.getRecordOffsets()
            .stream()
            .map(offset -> {
              try {
                return getRecord(offset);
              } catch (IOException e) {
                throw new RuntimeException();
              }
            })
            .map(Record::getKey)
            .toList()
    );

    if (recordIndex == -1) {
      return null;
    }

    return getRecord(targetLeafNode.getRecordOffsets().get(recordIndex));
  }

  /**
   * Find the leaf node that possibly contain a record with key {@code key}.
   *
   * @param key the search key
   */
  private BTreeNode<K> findTargetLeafNode(SortableField<K> key) throws IOException {
    BTreeNode<K> currentNode = root;

    while (!currentNode.getIsLeaf()) {
      int pointerIndex = SearchUtil.findFirstLargerIndex(key, currentNode.getKeys());
      currentNode = getNode(currentNode.getPointerOffsets().get(pointerIndex));
    }

    return currentNode;
  }


  private BTreeNode<K> getNode(Long offset) throws IOException {
    return BTreeNode.deserialize(Store.INDEX_FILE.read(offset));
  }

  private Record<K> getRecord(Long offset) throws IOException {
    return Record.deserialize(Store.RECORD_FILE.read(offset));
  }
}
