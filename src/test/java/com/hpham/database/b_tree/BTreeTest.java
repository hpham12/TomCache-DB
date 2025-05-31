package com.hpham.database.b_tree;

import com.hpham.database.b_tree.exceptions.RecordNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.hpham.database.b_tree.BTree.FANOUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BTreeTest {
    private BTree<Integer> bTree;

    @BeforeEach
    void setup() {
        bTree = new BTree<>();
    }

    @Test
    void testAdd() throws IllegalAccessException{
        for (int i = 0; i < 115; i++) {
            Record<Integer, ?> entry = new Record<>(i, "Hello");
            bTree.insert(entry);
            assertThat(bTree.findRecord(i)).isEqualTo(entry);
        }
    }

    @Test
    void testAddVer2() throws IllegalAccessException{
        for (int i = 150; i >= 0 ; i--) {
            Record<Integer, ?> entry = new Record<>(i, "Hello");
            bTree.insert(entry);
            assertThat(bTree.findRecord(i)).isEqualTo(entry);
        }
    }

    @Test
    void testAddVer3() throws IllegalAccessException{
        for (int i = 0; i < 150 ; i++) {
            int randomKey = new Random().nextInt();
            Record<Integer, ?> entry = new Record<>(randomKey, "Hello" + randomKey);
            bTree.insert(entry);
            assertThat(bTree.findRecord(randomKey)).isEqualTo(entry);
        }
    }

    @Test
    void testTreeIntegrity() throws IllegalAccessException {
        for (int i = 0; i < 150 ; i++) {
            int randomKey = new Random().nextInt();
            Record<Integer, ?> entry = new Record<>(randomKey, "Hello");
            bTree.insert(entry);
        }

        Queue<BTreeNode<Integer>> queue = new LinkedList<>();
        queue.offer(bTree.getRoot());

        while (!queue.isEmpty()) {
            // check for ordering, occupancies, and parent-child relationship
            BTreeNode<Integer> currentNode = queue.poll();
            if (currentNode.getIsLeaf()) {
                var records = currentNode.getRecords();
                for (int i = 1; i < records.size(); i++) {
                    assertThat(records.get(i).getKey()).isGreaterThan(records.get(i - 1).getKey());
                }
                assertThat(records.size())
                        .isGreaterThanOrEqualTo(FANOUT/2)
                        .isLessThanOrEqualTo(FANOUT);
            } else {
                var keys = currentNode.getKeys();
                for (int i = 1; i < keys.size(); i++) {
                    assertThat(keys.get(i)).isGreaterThan(keys.get(i - 1));
                }
                var pointers = currentNode.getPointers();
                assertThat(pointers.size())
                        .isGreaterThanOrEqualTo((int) Math.ceil(((double) FANOUT)/2));

                assertThat(pointers.size()).isEqualTo(keys.size() + 1);

                for (var pointer : pointers) {
                    assertThat(pointer.getParent()).isEqualTo(currentNode);
                }
            }

            if (!currentNode.getIsLeaf()) {
                for (var pointer : currentNode.getPointers()) {
                    queue.offer(pointer);
                }
            }
        }
    }

    @Test
    void testDelete() throws IllegalAccessException, RecordNotFoundException {
        for (int i = 0; i < 1500 ; i++) {
            Record<Integer, ?> entry = new Record<>(i, "Hello");
            bTree.insert(entry);
        }

        for (int i = 0; i < 1500 ; i++) {
            System.out.println(i);
            bTree.delete(i);
            assertThat(bTree.findRecord(i)).isNull();
        }
    }
}
