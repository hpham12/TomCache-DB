package com.hpham.database.b_tree;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BTree<K extends Comparable<K>> {
    public static final Integer FANOUT = 4;
    BTreeNode<K> root;

    public BTree() {
        root = new BTreeNode<>(Boolean.TRUE);
    }

    public Record<K, ?> insert(Record<K, ?> record) throws IllegalAccessException {
        K key = record.getKey();
        BTreeNode<K> targetLeafNode = findTargetLeafNode(key);

        // TODO: handle the case when targetLeafNode is full
        targetLeafNode.addNewRecord(record);
        return record;
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
