package com.hpham.database.b_tree;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
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

    public BTreeNode<K> addNewRecord(Record<K, ?> newRecord) throws IllegalAccessException{
        if (!isLeaf) {
            throw new IllegalAccessException("Cannot call addNewRecord on non-leaf node");
        }

        if (records.size() == FANOUT) {
            // The leaf node is full, so we need to split
            BTreeNode<K> newNode = new BTreeNode<>(true);
            List<Record<K, ?>> combinedRecords = this.records;
            this.records = new ArrayList<>();
            combinedRecords.add(newRecord);
            combinedRecords.sort(Comparator.comparing(Record::getKey));

            // split records into 2 halves
            for (int i = 0; i <= FANOUT; i++) {
                if (i <= FANOUT/2) {
                    this.records.add(combinedRecords.get(i));
                } else {
                    newNode.records.add(combinedRecords.get(i));
                }
            }

            // bubble the key up to parent node
            K keyBubbledUp = combinedRecords.get(FANOUT/2).getKey();

            if (parent == null) {
                // parent == null implies that "this" is the root,
                // thus we need to create a new parent node
                BTreeNode<K> newParent = new BTreeNode<>(false);
                this.parent = newParent;
                newNode.parent = newParent;

                parent.pointers.add(this);
                parent.pointers.add(newNode);
                parent.keys.add(keyBubbledUp);

                return parent;
            }

            newNode.parent = this.parent;
            return parent.addNewKey(keyBubbledUp, newNode);
        } else {
            records.add(newRecord);
            records.sort(Comparator.comparing(Record::getKey));

            return null;
        }
    }

    private BTreeNode<K> addNewKey(K key, BTreeNode<K> newChildNode) throws IllegalAccessException {
        if (isLeaf) {
            throw new IllegalAccessException("Cannot call addNewKey on leaf node");
        }

        if (keys.size() == FANOUT - 1) {
            // The internal node is full, so we need to split
            BTreeNode<K> newNode = new BTreeNode<>(false);
            List<K> combinedKeys = this.keys;
            List<BTreeNode<K>> combinedPointers = this.pointers;

            this.keys = new ArrayList<>();
            this.pointers = new ArrayList<>();

            int newKeyIndex = SearchUtil.findFirstLargerIndex(key, combinedKeys);
            combinedKeys.add(newKeyIndex, key);

            // split records into 2 halves
            for (int i = 0; i < FANOUT; i++) {
                if (i < FANOUT/2) {
                    this.keys.add(combinedKeys.get(i));
                } else {
                    newNode.keys.add(combinedKeys.get(i));
                }
            }

            combinedPointers.add(newKeyIndex + 1, newChildNode);

            for (int i = 0; i <= FANOUT; i++) {
                if (i <= FANOUT/2) {
                    this.pointers.add(combinedPointers.get(i));
                    combinedPointers.get(i).setParent(this);
                }

                if (i >= FANOUT/2) {
                    newNode.pointers.add(combinedPointers.get(i));
                    combinedPointers.get(i).setParent(newNode);
                }
            }

            // bubble the key up to parent node
            K keyBubbledUp = combinedKeys.get((FANOUT - 1)/2);

            if (parent == null) {
                // parent == null implies that "this" is the root,
                // thus we need to create a new parent node
                BTreeNode<K> newParent = new BTreeNode<>(false);
                this.parent = newParent;
                newNode.parent = newParent;

                parent.pointers.add(this);
                parent.pointers.add(newNode);
                parent.keys.add(keyBubbledUp);

                return parent;
            } else {
                return parent.addNewKey(keyBubbledUp, newNode);
            }
        } else {
            int newKeyIndex = SearchUtil.findFirstLargerIndex(key, keys);
            keys.add(newKeyIndex, key);
            pointers.add(newKeyIndex + 1, newChildNode);
            return null;
        }
    }
}
