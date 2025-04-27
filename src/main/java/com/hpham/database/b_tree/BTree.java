package com.hpham.database.b_tree;

import com.hpham.database.model.Entry;
import lombok.Getter;
import lombok.Setter;

import static com.hpham.database.b_tree.SearchUtil.findFirstLargerIndex;
import static com.hpham.database.b_tree.SearchUtil.searchForIndex;

/**
 * Class representing B-tree structure.
 *
 * @author Hieu Pham
 * */
@Getter
@Setter
public class BTree<K extends Comparable<K>> {
    private BTreeNode<K> root;
    public static final Integer FAN_OUT = 4;

    /**
     * Find a BTreeNode, given a key
     *
     * @return the entry that has the kay value being queried
     * */
    public Entry<K, ?> findEntry(K key) {
        BTreeNode<K> targetLeafNode = findLeafNode(key);
        int index = searchForIndex(key, targetLeafNode.getKeys());
        return findLeafNode(key).getEntries().get(index);
    }

    public BTreeNode<K> findLeafNode(K key) {
        if (root == null) {
            return null;
        }

        BTreeNode<K> currentNode = root;

        while (!currentNode.getIsLeaf()) {
            int indexOfFirstLarger = findFirstLargerIndex(key, currentNode.getKeys());
            int separatorIndex = indexOfFirstLarger + 1;

            currentNode = currentNode.getSeparators().get(separatorIndex).getNext();
        }

        int index = searchForIndex(key, currentNode.getKeys());

        if (index == -1) {
            return null;
        }

        return currentNode;
    }

    /**
     * Insert an entry and update B-tree accordingly
     * */
    public Entry<K, ?> insert(Entry<K, ?> entry) {
        K key = entry.getKey();
        if (root == null) {
            root = new BTreeNode<>(false);
            BTreeNode<K> leafNode = new BTreeNode<>(true);
            leafNode.addEntry(entry);
            root.setSeparator(1, leafNode);
        } else {
            BTreeNode<K> targetLeafNode = findLeafNode(key);
            targetLeafNode.addEntry(entry);
        }

        return entry;
    }
}
