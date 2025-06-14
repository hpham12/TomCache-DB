package com.hpham.database.b_tree;

import com.hpham.database.b_tree.exceptions.InvalidMethodInvocationException;
import com.hpham.database.b_tree.exceptions.RecordAlreadyExistException;
import com.hpham.database.b_tree.exceptions.RecordNotFoundException;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.*;

import static com.hpham.database.b_tree.BTree.FANOUT;

/**
 * Class representing B-Tree node.
 * <br>
 * If a node is a leaf node, pointers and keys will be {@code null}, and records will contain the actual record
 *
 * @author Hieu Pham
 * */
@Getter
@Setter
public class BTreeNode<K extends Comparable<K>> {
    private Boolean isLeaf;
    private List<BTreeNode<K>> pointers;
    private List<K> keys;
    private List<Record<K, Object>> records;
    private BTreeNode<K> parent;

    private BTreeNode(Boolean isLeaf) {
        this.isLeaf = isLeaf;
        if (isLeaf) {
            records = new ArrayList<>();
        } else {
            pointers = new ArrayList<>();
            keys = new ArrayList<>();
        }
    }

    /**
     * Create a leaf node.
     * */
    static <K extends Comparable<K>> BTreeNode<K> createLeafNode() {
        return new BTreeNode<>(true);
    }

    /**
     * Create an internal node.
     * */
    static <K extends Comparable<K>> BTreeNode<K> createInternalNode() {
        return new BTreeNode<>(false);
    }

    /**
     * Update an existing record
     *
     * @throws InvalidMethodInvocationException if the record key is not found
     * */
    Record<K, Object> updateRecord(@NonNull Record<K, Object> newRecord) {
        if (!this.isLeaf) {
            throw new InvalidMethodInvocationException("Cannot update record in an internal node");
        }
        
        Optional<Record<K, Object>> existingRecordOptional = this.records.stream()
                .filter(record -> record.getKey().equals(newRecord.getKey()))
                .findAny();

        if (existingRecordOptional.isEmpty()) {
            throw new RecordNotFoundException(newRecord.getKey());
        }
        
        return existingRecordOptional.map(r -> {
            r.setValue(newRecord.getValue());
            return r;
        }).get();
    }

    /**
     * Add new record to the current leaf node.
     *
     * @param newRecord new record to add
     * @return New root of the B-Tree, {@code null} if the root does not change
     * @throws InvalidMethodInvocationException if this is not a leaf node
     * */
    Optional<BTreeNode<K>> addNewRecord(@NonNull Record<K, Object> newRecord) {
        if (!this.isLeaf) {
            throw new InvalidMethodInvocationException("Cannot add new record to an internal node");
        }

        // check for duplicate
        boolean hasDuplicate = this.records.stream().anyMatch(record -> record.getKey().equals(newRecord.getKey()));

        if (hasDuplicate) {
            throw new RecordAlreadyExistException(newRecord.getKey());
        }

        if (this.records.size() == FANOUT) {
            // The leaf node is full, so we need to split
            return splitLeafNode(this, newRecord);
        } else {
            this.records.add(newRecord);
            this.records.sort(Comparator.comparing(Record::getKey));

            return Optional.empty();
        }
    }

    /**
     * Split the overflowed node, then add new record
     * */
    private Optional<BTreeNode<K>> splitLeafNode(@NonNull BTreeNode<K> nodeToSplit, @NonNull Record<K , Object> newRecord) {
        BTreeNode<K> newLeafNode = createLeafNode();
        List<Record<K , Object>> combinedRecords = nodeToSplit.records;
        nodeToSplit.records = new ArrayList<>();
        combinedRecords.add(newRecord);
        combinedRecords.sort(Comparator.comparing(Record::getKey));

        // split records into 2 halves
        for (int i = 0; i <= FANOUT; i++) {
            if (i < FANOUT/2) {
                nodeToSplit.records.add(combinedRecords.get(i));
            } else {
                newLeafNode.records.add(combinedRecords.get(i));
            }
        }

        // bubble the key up to parent node
        K keyBubbledUp = combinedRecords.get(FANOUT/2).getKey();

        if (nodeToSplit.parent == null) {
            // parent == null implies that nodeToSplit is the root,
            // thus we need to create a new parent node
            BTreeNode<K> newParent = createInternalNode();
            nodeToSplit.parent = newParent;
            newLeafNode.parent = newParent;

            nodeToSplit.parent.pointers.add(nodeToSplit);
            nodeToSplit.parent.pointers.add(newLeafNode);
            nodeToSplit.parent.keys.add(keyBubbledUp);

            return Optional.ofNullable(nodeToSplit.parent);
        }

        newLeafNode.parent = nodeToSplit.parent;
        return nodeToSplit.parent.addNewKey(keyBubbledUp, newLeafNode);
    }

    public Boolean isRootNode() {
        return this.parent == null;
    }

    /**
     * Add new key to the current internal node.
     *
     * @param newKey new key to add.
     * @param newChildNode new child node bubbling up.
     * @return New root of the B-Tree, {@code null} if the root does not change
     * @throws InvalidMethodInvocationException if the current node is not an internal node
     * */
    private Optional<BTreeNode<K>> addNewKey(@NonNull K newKey, @NonNull BTreeNode<K> newChildNode) {
        if (isLeaf) {
            throw new InvalidMethodInvocationException("Cannot call addNewKey on an internal node");
        }

        if (pointers.size() == FANOUT) {
            // The internal node is full, so we need to split
            return splitInternalNodeAndAddNewKey(this, newKey, newChildNode);
        } else {
            // No need to split as the current node can still contain more key(s) and pointer(s)
            return addNewKeyWithoutSplitting(this, newKey, newChildNode);
        }
    }

    private Optional<BTreeNode<K>> addNewKeyWithoutSplitting(BTreeNode<K> node, K newKey, BTreeNode<K> newChildNode) {
        int newKeyIndex = SearchUtil.findFirstLargerIndex(newKey, keys);
        node.keys.add(newKeyIndex, newKey);
        newChildNode.setParent(node);
        node.pointers.add(newKeyIndex + 1, newChildNode);
        return Optional.empty();
    }

    /**
     * Split an internal node
     * */
    private Optional<BTreeNode<K>> splitInternalNodeAndAddNewKey(@NonNull BTreeNode<K> nodeToSplit, @NonNull K newKey, @NonNull BTreeNode<K> newChildNode) {
        BTreeNode<K> newNode = createInternalNode();
        List<K> combinedKeys = nodeToSplit.keys;
        List<BTreeNode<K>> combinedPointers = nodeToSplit.pointers;

        nodeToSplit.keys = new ArrayList<>();
        nodeToSplit.pointers = new ArrayList<>();

        int newKeyIndex = SearchUtil.findFirstLargerIndex(newKey, combinedKeys);
        combinedKeys.add(newKeyIndex, newKey);

        // split keys into 2 halves
        for (int i = 0; i < FANOUT; i++) {
            if (i < FANOUT/2) {
                nodeToSplit.keys.add(combinedKeys.get(i));
            } else if (i > FANOUT/2) {
                newNode.keys.add(combinedKeys.get(i));
            }
        }

        combinedPointers.add(newKeyIndex + 1, newChildNode);

        // Split pointers
        for (int i = 0; i <= FANOUT; i++) {
            if (i <= FANOUT/2) {
                nodeToSplit.pointers.add(combinedPointers.get(i));
                combinedPointers.get(i).setParent(this);
            } else {
                newNode.pointers.add(combinedPointers.get(i));
                combinedPointers.get(i).setParent(newNode);
            }
        }

        // bubble the key up to parent node
        K keyBubbledUp = combinedKeys.get(FANOUT/2);

        if (parent == null) {
            // parent == null implies that nodeToSplit is the root,
            // thus we need to create a new parent node
            BTreeNode<K> newParent = createInternalNode();
            nodeToSplit.parent = newParent;
            newNode.parent = newParent;

            nodeToSplit.parent.pointers.add(this);
            nodeToSplit.parent.pointers.add(newNode);
            nodeToSplit.parent.keys.add(keyBubbledUp);

            return Optional.of(nodeToSplit.parent);
        } else {
            return nodeToSplit.parent.addNewKey(keyBubbledUp, newNode);
        }
    }

    /**
     * Delete a record, given a {@code key}.
     *
     * @param key key of the record to be deleted
     * @return New root of the B-Tree, {@code null} if the root does not change
     * @throws InvalidMethodInvocationException if the current node is not a leaf node
     * @throws RecordNotFoundException if there is no record associated with the given {@code key}
     * */
    Optional<BTreeNode<K>> deleteRecord(@NonNull K key) {
        if (!isLeaf) {
            throw new InvalidMethodInvocationException("Cannot call deleteRecord on an internal node");
        }

        Record<K , Object> recordToDelete = records
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
            return Optional.of(this);
        }

        if (this.records.size() < Math.ceil(((double) FANOUT)/2)) {
            // needs to merge/rebalance
            BTreeNode<K> nodeToMergeWith = findNodeToMerge(this);

            if (nodeToMergeWith == null) {
                // cannot find node to merge with, need to rebalance instead
                BTreeNode<K> nodeToRebalanceWith = findNodeToRebalanceWith(this);
                if (nodeToRebalanceWith == null) {
                    throw new RuntimeException("Tree in invalid state");
                }
                return reBalanceLeafNode(this, nodeToRebalanceWith);
            }

            return mergeLeafNodes(this, nodeToMergeWith);
        }
        return Optional.empty();
    }

    private Optional<BTreeNode<K>> reBalanceLeafNode(@NonNull BTreeNode<K> underflowNode, @NonNull BTreeNode<K> nodeToRebalanceWith) {
        SiblingPosition positionOfNodeToRebalance = determineSiblingPosition(underflowNode, nodeToRebalanceWith);
        int parentKeyIndexToChange;
        Record<K , Object> recordToMove;

        if (SiblingPosition.TO_THE_LEFT.equals(positionOfNodeToRebalance)) {
            parentKeyIndexToChange = underflowNode.parent.pointers.indexOf(nodeToRebalanceWith);
            recordToMove = nodeToRebalanceWith.records.removeLast();
            underflowNode.records.addFirst(recordToMove);
            underflowNode.parent.keys.set(parentKeyIndexToChange, recordToMove.getKey());
        } else {
            parentKeyIndexToChange = underflowNode.parent.pointers.indexOf(underflowNode);
            recordToMove = nodeToRebalanceWith.records.removeFirst();
            var keyToPromote = nodeToRebalanceWith.records.getFirst().getKey();
            underflowNode.records.addLast(recordToMove);
            underflowNode.parent.keys.set(parentKeyIndexToChange, keyToPromote);
        }

        return Optional.empty();
    }

    private Optional<BTreeNode<K>> mergeLeafNodes(@NonNull BTreeNode<K> underFlowNode, @NonNull BTreeNode<K> nodeToMergeWith) {
        SiblingPosition positionOfNodeToMerge = determineSiblingPosition(underFlowNode, nodeToMergeWith);

        int parentPointerIndexToDelete;
        BTreeNode<K> nodeToPossiblyBeRoot;
        if (SiblingPosition.TO_THE_LEFT.equals(positionOfNodeToMerge)) {
            // Merge into the sibling
            nodeToPossiblyBeRoot = nodeToMergeWith;
            nodeToMergeWith.records.addAll(underFlowNode.records);
            parentPointerIndexToDelete = this.parent.pointers.indexOf(this);
        } else {
            // Merge into the sibling
            nodeToPossiblyBeRoot = underFlowNode;
            underFlowNode.records.addAll(nodeToMergeWith.records);
            parentPointerIndexToDelete = underFlowNode.parent.pointers.indexOf(nodeToMergeWith);
        }

        underFlowNode.parent.pointers.remove(parentPointerIndexToDelete);
        underFlowNode.parent.keys.remove(parentPointerIndexToDelete - 1);
        if (underFlowNode.parent.parent == null && underFlowNode.parent.keys.isEmpty()) {
            underFlowNode.parent = null;
            return Optional.of(nodeToPossiblyBeRoot);
        }

        return this.parent.mergeOrRebalance();
    }

    /**
     * Merge or rebalance a non-leaf node
     *
     * @return a node that will be a new root, null if root does not change
     * */
    private Optional<BTreeNode<K>> mergeOrRebalance() {
        if (isLeaf) {
            throw new InvalidMethodInvocationException("Cannot call mergeOrRebalance on leaf node");
        }

        if (this.parent == null) {
            return Optional.empty();
        }

        // in case this is not root
        boolean isUnderFlow = this.pointers.size() < Math.ceil(((double) FANOUT)/2);

        if (isUnderFlow) {
            // needs to merge/rebalance
            BTreeNode<K> nodeToMergeWith = findNodeToMerge(this);

            if (nodeToMergeWith == null) {
                // cannot find node to merge with, need to rebalance instead
                BTreeNode<K> nodeToRebalanceWith = findNodeToRebalanceWith(this);
                if (nodeToRebalanceWith == null) {
                    throw new RuntimeException("Tree in invalid state");
                }
                return rebalanceInternalNode(this, nodeToRebalanceWith);
            }

            return mergeInternalNodes(this, nodeToMergeWith);
        }

        return Optional.empty();
    }

    private Optional<BTreeNode<K>> mergeInternalNodes(@NonNull BTreeNode<K> underflowNode, @NonNull BTreeNode<K> nodeToMergeWith) {
        SiblingPosition positionOfNodeToMerge = determineSiblingPosition(underflowNode, nodeToMergeWith);

        if (SiblingPosition.TO_THE_LEFT.equals(positionOfNodeToMerge)) {
            // Merge into the sibling
            nodeToMergeWith.pointers.addAll(underflowNode.pointers);
            for (BTreeNode<K> pointer : underflowNode.pointers) {
                pointer.setParent(nodeToMergeWith);
            }
            int parentPointerIndexToDelete = underflowNode.parent.pointers.indexOf(underflowNode);
            K keyToDemoteFromParent = underflowNode.parent.keys.get(parentPointerIndexToDelete - 1);
            nodeToMergeWith.keys.add(keyToDemoteFromParent);
            nodeToMergeWith.keys.addAll(underflowNode.keys);
            underflowNode.parent.pointers.remove(parentPointerIndexToDelete);
            underflowNode.parent.keys.remove(keyToDemoteFromParent);
            if (underflowNode.parent.parent == null && underflowNode.parent.keys.isEmpty()) {
                nodeToMergeWith.parent = null;
                return Optional.of(nodeToMergeWith);
            }
        } else {
            // Merge into the current node
            underflowNode.pointers.addAll(nodeToMergeWith.pointers);
            for (BTreeNode<K> pointer : nodeToMergeWith.pointers) {
                pointer.setParent(underflowNode);
            }
            int parentPointerIndexToDelete = underflowNode.parent.pointers.indexOf(nodeToMergeWith);
            K keyToDemoteFromParent = underflowNode.parent.keys.get(parentPointerIndexToDelete - 1);
            underflowNode.keys.add(keyToDemoteFromParent);
            underflowNode.keys.addAll(nodeToMergeWith.keys);
            underflowNode.parent.pointers.remove(parentPointerIndexToDelete);
            underflowNode.parent.keys.remove(keyToDemoteFromParent);
            if (underflowNode.parent.parent == null && underflowNode.parent.keys.isEmpty()) {
                underflowNode.parent = null;
                return Optional.of(underflowNode);
            }
        }
        return underflowNode.parent.mergeOrRebalance();
    }

    /**
     * Rebalance internal node
     * */
    private Optional<BTreeNode<K>> rebalanceInternalNode(@NonNull BTreeNode<K> underflowNode, @NonNull BTreeNode<K> nodeToRebalanceWith) {
        SiblingPosition positionOfNodeToRebalance = determineSiblingPosition(underflowNode, nodeToRebalanceWith);
        int parentKeyIndexToChange;
        K keyToMove;
        if (SiblingPosition.TO_THE_LEFT.equals(positionOfNodeToRebalance)) {
            parentKeyIndexToChange = underflowNode.parent.pointers.indexOf(nodeToRebalanceWith);
            var keyToPromote = nodeToRebalanceWith.keys.getLast();
            keyToMove = parent.keys.get(parentKeyIndexToChange);
            underflowNode.keys.addFirst(keyToMove);
            var pointerToMove = nodeToRebalanceWith.pointers.getLast();
            pointerToMove.setParent(underflowNode);
            underflowNode.pointers.addFirst(pointerToMove);
            nodeToRebalanceWith.keys.removeLast();
            nodeToRebalanceWith.pointers.removeLast();
            underflowNode.parent.keys.set(parentKeyIndexToChange, keyToPromote);
        } else {
            parentKeyIndexToChange = underflowNode.parent.pointers.indexOf(underflowNode);
            keyToMove = nodeToRebalanceWith.keys.getFirst();

            var pointerToMove = nodeToRebalanceWith.pointers.getFirst();
            pointerToMove.setParent(underflowNode);
            underflowNode.pointers.addLast(pointerToMove);
            underflowNode.keys.addLast(underflowNode.parent.keys.get(parentKeyIndexToChange));
            nodeToRebalanceWith.keys.removeFirst();
            nodeToRebalanceWith.pointers.removeFirst();
            underflowNode.parent.keys.set(parentKeyIndexToChange, keyToMove);
        }

        return Optional.empty();
    }

    private BTreeNode<K> findNodeToRebalanceWith(@NonNull BTreeNode<K> node) {
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
    private BTreeNode<K> findNodeToMerge(@NonNull BTreeNode<K> node) {
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
