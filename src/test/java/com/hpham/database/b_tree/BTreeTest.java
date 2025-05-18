package com.hpham.database.b_tree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

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
        for (int i = 150; i >= 0 ; i--) {
            int randomKey = new Random().nextInt();
            Record<Integer, ?> entry = new Record<>(randomKey, "Hello");
            bTree.insert(entry);
            System.out.println(randomKey);
            assertThat(bTree.findRecord(randomKey)).isEqualTo(entry);
        }
    }
}
