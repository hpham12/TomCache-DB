package com.hpham.database.b_tree;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.hpham.database.b_tree.BTree.FANOUT;

/**
 * Class representing B-Tree node.
 *
 * If a node is a leaf node, pointers and keys will be null, and records will contain the actual record
 *
 * @author hpham12
 * */
@Getter
@Setter
public class BTreeNode<K extends Comparable<K>> {
    private Boolean isLeaf;
    private List<BTreeNode<K>> pointers;
    private List<K> keys;
    private List<Record<K, ?>> records;
    private BTreeNode<K> parent;

    public BTreeNode(Boolean isLeaf) {
        this.isLeaf = isLeaf;
        if (isLeaf) {
            records = new ArrayList<>();
        } else {
            pointers = new ArrayList<>();
            keys = new ArrayList<>();
        }
    }

    public void addNewRecord(Record<K, ?> newRecord) throws IllegalAccessException{
        if (!isLeaf) {
            throw new IllegalAccessException("Cannot call addNewRecord on non-leaf node");
        }
        K key = newRecord.getKey();

        if (records.size() == FANOUT + 1) {
            // Full, need to split

        } else {
            records.add(newRecord);
            records.sort(Comparator.comparing(Record::getKey));
        }
    }
}
