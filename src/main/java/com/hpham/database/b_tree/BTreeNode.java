package com.hpham.database.b_tree;

import com.hpham.database.model.Entry;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.hpham.database.b_tree.BTree.FAN_OUT;

/**
 * Class representing a B-Tree Node.
 * If a node is a leaf node (isLeat == true), then separators is null, and entries contain
 * pointers to the actual entry.
 * If a node is not a leaf node, then entries is null, and separators contain pointers to
 * child nodes
 *
 * @author Hieu Pham
 */
@Getter
@Setter
public class BTreeNode<K extends Comparable<K>> {
    private List<K> keys;
    private List<Separator<BTreeNode<K>>> separators;
    private Boolean isLeaf;
    private List<Entry<K, ?>> entries;

    public BTreeNode(Boolean isLeaf) {
        this.isLeaf = isLeaf;

        if (isLeaf) {
            entries = new ArrayList<>(FAN_OUT);
        } else {
            keys = new ArrayList<>(FAN_OUT - 1);
            separators = new ArrayList<>(FAN_OUT);
        }
    }

    /**
     * Add an entry into a node.
     * If a node is a leaf node, then add entry into entries in sorted order.
     * If a node is not a lead node, add the key to keys in sorted order
     */
    public Entry<K, ?> addEntry(Entry<K, ?> entry) {
        if (isLeaf) {
            entries.add(entry);
            entries.sort(Comparator.comparing(Entry::getKey));
            return entry;
        } else {
            K key = entry.getKey();
            keys.add(key);
            Collections.sort(keys);
            return entry;
        }
    }

    public void setSeparator(int index, BTreeNode<K> childNode) {
        if (separators.get(index) == null) {
            separators.set(index, new Separator<>());
        }
        separators.get(index).setNext(childNode);
    }
}
