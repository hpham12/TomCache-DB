package com.hpham.database.b_tree;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.Optional;

@Getter
@Setter
public class BTree<K extends Comparable<K>> {
    public static final Integer FANOUT = 5;
    private BTreeNode<K> root;

    public BTree() {
        root = BTreeNode.createLeafNode();
    }

    /**
     *
     * */
    public Record<K, ?> insert(Record<K, ?> record) {
        Objects.requireNonNull(record, "record must not be null");
        K key = record.getKey();
        BTreeNode<K> targetLeafNode = findTargetLeafNode(key);

        Optional<BTreeNode<K>> newRootOptional = targetLeafNode.addNewRecord(record);

        newRootOptional.ifPresent(newRoot -> this.root = newRoot);

        return record;
    }

    public void delete(K key) {
        BTreeNode<K> targetLeafNode = findTargetLeafNode(key);

        BTreeNode<K> newRoot = targetLeafNode.deleteRecord(key);

        if (newRoot != null) {
            this.root = newRoot;
            this.root.setParent(null);
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
     * Find the leaf node that possibly contain a record with key {@code key}
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
