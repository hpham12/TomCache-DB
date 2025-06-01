package com.hpham.database.b_tree;

import com.hpham.database.b_tree.exceptions.RecordNotFoundException;
import lombok.Getter;
import lombok.NonNull;
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
                if (i < FANOUT/2) {
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

            // split keys into 2 halves
            for (int i = 0; i < FANOUT; i++) {
                if (i < FANOUT/2) {
                    this.keys.add(combinedKeys.get(i));
                } else if (i > FANOUT/2) {
                    newNode.keys.add(combinedKeys.get(i));
                }
            }

            combinedPointers.add(newKeyIndex + 1, newChildNode);

            // Split pointers
            for (int i = 0; i <= FANOUT; i++) {
                if (i <= FANOUT/2) {
                    this.pointers.add(combinedPointers.get(i));
                    combinedPointers.get(i).setParent(this);
                } else {
                    newNode.pointers.add(combinedPointers.get(i));
                    combinedPointers.get(i).setParent(newNode);
                }
            }

            // bubble the key up to parent node
            K keyBubbledUp = combinedKeys.get(FANOUT/2);

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
            newChildNode.setParent(this);
            pointers.add(newKeyIndex + 1, newChildNode);
            return null;
        }
    }

    BTreeNode<K> deleteRecord(K key) throws IllegalAccessException, RecordNotFoundException {
        if (!isLeaf) {
            throw new IllegalAccessException("Cannot call deleteRecord on non-left node");
        }

        Record<K, ?> recordToDelete = records
                .stream()
                .filter(r -> r.getKey().equals(key))
                .findAny()
                .orElse(null);

        if (recordToDelete == null) {
            throw new RecordNotFoundException(key);
        }

        this.records.remove(recordToDelete);

        // This is the case where the current leaf node is also a root
        if (this.parent == null) {
            return null;
        }

        if (this.records.size() < Math.ceil(((double) FANOUT)/2)) {
            // needs to merge/rebalance
            BTreeNode<K> nodeToMerge = findNodeToMerge(this);

            if (nodeToMerge == null) {
                System.out.printf("Reshuffling, key = %s\n", key);
                // cannot find node to merge with, need to rebalance instead
                BTreeNode<K> nodeToRebalanceWith = findNodeToRebalanceWith(this);
                if (nodeToRebalanceWith == null) {
                    throw new RuntimeException("Tree in invalid state");
                }
                SiblingPosition positionOfNodeToRebalance = determineSiblingPosition(this, nodeToRebalanceWith);
                int parentKeyIndexToChange;
                Record<K, ?> recordToMove;
                if (SiblingPosition.TO_THE_LEFT.equals(positionOfNodeToRebalance)) {
                    // TODO: fix this
                    parentKeyIndexToChange = this.parent.pointers.indexOf(nodeToRebalanceWith);
                    recordToMove = nodeToRebalanceWith.records.getLast();
                    nodeToRebalanceWith.records.removeLast();
                    this.records.addFirst(recordToMove);
                    this.parent.keys.set(parentKeyIndexToChange, recordToMove.getKey());
                } else {
                    parentKeyIndexToChange = this.parent.pointers.indexOf(this);
                    recordToMove = nodeToRebalanceWith.records.getFirst();
                    var keyToPromote = nodeToRebalanceWith.records.get(1).getKey();
                    nodeToRebalanceWith.records.removeFirst();
                    this.records.addLast(recordToMove);
                    this.parent.keys.set(parentKeyIndexToChange, keyToPromote);
                }
                if (this.parent.parent == null && this.parent.keys.isEmpty()) {
                    this.parent = null;
                    return this;
                }

                return null;
            }

            SiblingPosition positionOfNodeToMerge = determineSiblingPosition(this, nodeToMerge);

            if (SiblingPosition.TO_THE_LEFT.equals(positionOfNodeToMerge)) {
                // Merge into the sibling
                nodeToMerge.records.addAll(this.records);
                int parentPointerIndexToDelete = this.parent.pointers.indexOf(this);
                this.parent.pointers.remove(parentPointerIndexToDelete);
                this.parent.keys.remove(parentPointerIndexToDelete - 1);
                if (this.parent.parent == null && this.parent.keys.isEmpty()) {
                    this.parent = null;
                    return nodeToMerge;
                }
            } else {
                // Merge into the sibling
                this.records.addAll(nodeToMerge.records);
                int parentPointerIndexToDelete = this.parent.pointers.indexOf(nodeToMerge);
                this.parent.pointers.remove(parentPointerIndexToDelete);
                this.parent.keys.remove(parentPointerIndexToDelete - 1);
                if (this.parent.parent == null && this.parent.keys.isEmpty()) {
                    this.parent = null;
                    return this;
                }
            }

            return this.parent.mergeOrRebalance();
        }
        return null;
    }

    /**
     * Merge or rebalance a non-leaf node
     *
     * @return a node that will be a new root, null if root does not change
     * */
    private BTreeNode<K> mergeOrRebalance() throws IllegalAccessException {
        if (isLeaf) {
            throw new IllegalAccessException("Cannot call mergeOrRebalance on leaf node");
        }

        if (this.parent == null) {
            return null;
        }

        // in case this is not root
        boolean isUnderFlow = this.pointers.size() < Math.ceil(((double) FANOUT)/2);

        if (isUnderFlow) {
            // needs to merge/rebalance
            BTreeNode<K> nodeToMergeWith = findNodeToMerge(this);

            if (nodeToMergeWith == null) {
                System.out.println("Reshuffling");
                // cannot find node to merge with, need to rebalance instead
                BTreeNode<K> nodeToRebalanceWith = findNodeToRebalanceWith(this);
                if (nodeToRebalanceWith == null) {
                    throw new RuntimeException("Tree in invalid state");
                }
                SiblingPosition positionOfNodeToRebalance = determineSiblingPosition(this, nodeToRebalanceWith);
                int parentKeyIndexToChange;
                K keyToMove;
                if (SiblingPosition.TO_THE_LEFT.equals(positionOfNodeToRebalance)) {
                    parentKeyIndexToChange = this.parent.pointers.indexOf(nodeToRebalanceWith);
                    var keyToPromote = nodeToRebalanceWith.keys.getLast();
                    keyToMove = parent.keys.get(parentKeyIndexToChange);
                    this.keys.addFirst(keyToMove);
                    var pointerToMove = nodeToRebalanceWith.pointers.getLast();
                    pointerToMove.setParent(this);
                    this.pointers.addFirst(pointerToMove);
                    nodeToRebalanceWith.keys.removeLast();
                    nodeToRebalanceWith.pointers.removeLast();
                    this.parent.keys.set(parentKeyIndexToChange, keyToPromote);
                } else {
                    parentKeyIndexToChange = this.parent.pointers.indexOf(this);
                    keyToMove = nodeToRebalanceWith.keys.getFirst();

                    var pointerToMove = nodeToRebalanceWith.pointers.getFirst();
                    pointerToMove.setParent(this);
                    this.pointers.addLast(pointerToMove);
                    this.keys.addLast(this.parent.keys.get(parentKeyIndexToChange));
                    nodeToRebalanceWith.keys.removeFirst();
                    nodeToRebalanceWith.pointers.removeFirst();
                    this.parent.keys.set(parentKeyIndexToChange, keyToMove);
                }

                return null;
            }

            SiblingPosition positionOfNodeToMerge = determineSiblingPosition(this, nodeToMergeWith);

            if (SiblingPosition.TO_THE_LEFT.equals(positionOfNodeToMerge)) {
                // Merge into the sibling
                nodeToMergeWith.pointers.addAll(this.pointers);
                for (BTreeNode<K> pointer : this.pointers) {
                    pointer.setParent(nodeToMergeWith);
                }
                int parentPointerIndexToDelete = this.parent.pointers.indexOf(this);
                K keyToDemoteFromParent = this.parent.keys.get(parentPointerIndexToDelete - 1);
                nodeToMergeWith.keys.add(keyToDemoteFromParent);
                nodeToMergeWith.keys.addAll(this.keys);
                this.parent.pointers.remove(parentPointerIndexToDelete);
                this.parent.keys.remove(keyToDemoteFromParent);
                if (this.parent.parent == null && this.parent.keys.isEmpty()) {
                    nodeToMergeWith.parent = null;
                    return nodeToMergeWith;
                }
            } else {
                // Merge into the current node
                this.pointers.addAll(nodeToMergeWith.pointers);
                for (BTreeNode<K> pointer : nodeToMergeWith.pointers) {
                    pointer.setParent(this);
                }
                int parentPointerIndexToDelete = this.parent.pointers.indexOf(nodeToMergeWith);
                K keyToDemoteFromParent = this.parent.keys.get(parentPointerIndexToDelete - 1);
                this.keys.add(keyToDemoteFromParent);
                this.keys.addAll(nodeToMergeWith.keys);
                this.parent.pointers.remove(parentPointerIndexToDelete);
                this.parent.keys.remove(keyToDemoteFromParent);
                if (this.parent.parent == null && this.parent.keys.isEmpty()) {
                    this.parent = null;
                    return this;
                }
            }
            return this.parent.mergeOrRebalance();
        }

        return null;
    }

    private BTreeNode<K> findNodeToRebalanceWith(BTreeNode<K> node) {
        BTreeNode<K> parent = node.getParent();
        List<BTreeNode<K>> parentPointers = parent.getPointers();
        BTreeNode<K> leftSibling;
        BTreeNode<K> rightSibling;

        if (parentPointers.getFirst().equals(node)) {
            leftSibling = null;
        } else {
            leftSibling = parentPointers.get(parentPointers.indexOf(node) - 1);
        }

        if (parentPointers.getLast().equals(node)) {
            rightSibling = null;
        } else {
            rightSibling = parentPointers.get(parentPointers.indexOf(node) + 1);
        }

        if (node.isLeaf) {
            if (leftSibling != null && rightSibling != null) {
                if (rightSibling.records.size() > Math.ceil((double) FANOUT/2)) {
                    return rightSibling;
                }
                if (leftSibling.records.size() > Math.ceil((double) FANOUT/2)) {
                    return leftSibling;
                }
            } else if (rightSibling != null) {
                if (rightSibling.records.size() > Math.ceil((double) FANOUT/2)) {
                    return rightSibling;
                }
            } else if (leftSibling != null) {
                if (leftSibling.records.size() > Math.ceil((double) FANOUT/2)) {
                    return leftSibling;
                }
            }
        } else {
            if (leftSibling != null && rightSibling != null) {
                if (rightSibling.pointers.size() >= Math.ceil((double) (FANOUT + 1)/2)) {
                    return rightSibling;
                }
                if (leftSibling.pointers.size() >= Math.ceil((double) (FANOUT + 1)/2)) {
                    return leftSibling;
                }
            } else if (rightSibling != null) {
                if (rightSibling.pointers.size() >= Math.ceil((double) (FANOUT + 1)/2)) {
                    return rightSibling;
                }
            } else if (leftSibling != null) {
                if (leftSibling.pointers.size() >= Math.ceil((double) (FANOUT + 1)/2)) {
                    return leftSibling;
                }
            }
        }

        return null;
    }

    /**
     * Find a node that the input node can be merged with
     *
     * @return sibling node that will be used to merge
     * */
    private BTreeNode<K> findNodeToMerge(BTreeNode<K> node) {
        BTreeNode<K> parent = node.getParent();
        List<BTreeNode<K>> parentPointers = parent.getPointers();
        BTreeNode<K> leftSibling;
        BTreeNode<K> rightSibling;

        if (parentPointers.getFirst().equals(node)) {
            leftSibling = null;
        } else {
            leftSibling = parentPointers.get(parentPointers.indexOf(node) - 1);
        }

        if (parentPointers.getLast().equals(node)) {
            rightSibling = null;
        } else {
            rightSibling = parentPointers.get(parentPointers.indexOf(node) + 1);
        }

        if (node.isLeaf) {
            if (leftSibling != null && rightSibling != null) {
                if (rightSibling.records.size() + node.records.size() <= FANOUT) {
                    return rightSibling;
                }
                if (leftSibling.records.size() + node.records.size() <= FANOUT) {
                    return leftSibling;
                }
            } else if (rightSibling != null) {
                if (rightSibling.records.size() + node.records.size() <= FANOUT) {
                    return rightSibling;
                }
            } else if (leftSibling != null) {
                if (leftSibling.records.size() + node.records.size() <= FANOUT) {
                    return leftSibling;
                }
            }
        } else {
            if (leftSibling != null && rightSibling != null) {
                if (rightSibling.pointers.size() + node.pointers.size() <= FANOUT) {
                    return rightSibling;
                }
                if (leftSibling.pointers.size() + node.pointers.size() <= FANOUT) {
                    return leftSibling;
                }
            } else if (rightSibling != null) {
                if (rightSibling.pointers.size() + node.pointers.size() <= FANOUT) {
                    return rightSibling;
                }
            } else if (leftSibling != null) {
                if (leftSibling.pointers.size() + node.pointers.size() <= FANOUT) {
                    return leftSibling;
                }
            }
        }

        return null;
    }

    private @NonNull SiblingPosition determineSiblingPosition(BTreeNode<K> original, BTreeNode<K> sibling) {
        BTreeNode<K> parent = original.parent;
        int originalNodePosition = parent.pointers.indexOf(original);
        int siblingNodePosition = parent.pointers.indexOf(sibling);

        if (originalNodePosition == -1 || siblingNodePosition == -1) {
            throw new IllegalArgumentException("Argument node not found in parent's pointers");
        }

        if (Math.abs(originalNodePosition - siblingNodePosition) != 1) {
            throw new IllegalArgumentException("Two nodes are not siblings");
        }

        if (originalNodePosition > siblingNodePosition) {
            return SiblingPosition.TO_THE_LEFT;
        }

        return SiblingPosition.TO_THE_RIGHT;
    }

    private enum SiblingPosition {
        TO_THE_LEFT,
        TO_THE_RIGHT
    }

    private BTreeNode<K> getRoot() {
        BTreeNode<K> root = this;
        BTreeNode<K> currentNode = this;
        while (currentNode.parent != null) {
            root = currentNode.parent;
            currentNode = currentNode.parent;
        }

        return root;
    }
}
