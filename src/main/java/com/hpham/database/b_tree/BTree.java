package com.hpham.database.b_tree;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

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
     * Insert a record into a b-tree.
     *
     * @param record Record to be added
     * @return added record
     * */
    public Record<K, ?> insert(@NonNull Record<K, ?> record) {
        // TODO: Handle duplicate
        K key = record.getKey();
        BTreeNode<K> targetLeafNode = findTargetLeafNode(key);

        Optional<BTreeNode<K>> newRootOptional = targetLeafNode.addNewRecord(record);

        newRootOptional.ifPresent(newRoot -> this.root = newRoot);

        return record;
    }

    /**
     * Delete a record, given a key.
     *
     * @param key key of the record to delete
     * */
    public void delete(@NonNull K key) {
        BTreeNode<K> targetLeafNode = findTargetLeafNode(key);

        Optional<BTreeNode<K>> newRootOptional = targetLeafNode.deleteRecord(key);

        newRootOptional.ifPresent(newRoot -> {
            this.root = newRoot;
            this.root.setParent(null);
        });
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
