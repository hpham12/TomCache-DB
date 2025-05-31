package com.hpham.database.b_tree;

import com.hpham.database.b_tree.exceptions.RecordNotFoundException;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BTree<K extends Comparable<K>> {
    public static final Integer FANOUT = 4;
    private BTreeNode<K> root;

    public BTree() {
        root = new BTreeNode<>(Boolean.TRUE);
    }

    public Record<K, ?> insert(Record<K, ?> record) throws IllegalAccessException {
        K key = record.getKey();
        BTreeNode<K> targetLeafNode = findTargetLeafNode(key);

        BTreeNode<K> newRoot = targetLeafNode.addNewRecord(record);

        if (newRoot != null) {
            this.root = newRoot;
        }
        return record;
    }

    public void delete(K key) throws RecordNotFoundException, IllegalAccessException {
        BTreeNode<K> targetLeafNode = findTargetLeafNode(key);

        BTreeNode<K> newRoot = targetLeafNode.deleteRecord(key);

        if (newRoot != null) {
            this.root = newRoot;
        }
    }

    public Record<K, ?> findRecord(K key) {
        BTreeNode<K> targetLeafNode = findTargetLeafNode(key);
        int recordIndex = SearchUtil.searchForIndex(key, targetLeafNode.getRecords().stream().map(Record::getKey).toList());

        if (recordIndex == -1) {
            return null;
        }

        return targetLeafNode.getRecords().get(recordIndex);
    }

    /**
     * Find the leaf node that possibly contain a record with key "key"
     * */
    private BTreeNode<K> findTargetLeafNode(K key) {
        BTreeNode<K> currentNode = root;

        while (!currentNode.getIsLeaf()) {
            int pointerIndex = SearchUtil.findFirstLargerIndex(key, currentNode.getKeys());
            currentNode = currentNode.getPointers().get(pointerIndex);
        }

        return currentNode;
    }
}
