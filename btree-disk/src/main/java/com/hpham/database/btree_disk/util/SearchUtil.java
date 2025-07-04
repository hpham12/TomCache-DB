package com.hpham.database.btree_disk.util;

import java.util.List;

/**
 * Util class providing search functionalities.
 * */
public class SearchUtil {

  /**
   * Find index of a key in an array of keys.
   *
   * @return Index of the found key, -1 if not found
   */
  public static <K extends Comparable<K>> int searchForIndex(K key, List<K> keyList) {
    int start = 0;
    int end = keyList.size() - 1;

    while (start <= end) {
      int mid = (start + end) / 2;
      K midKey = keyList.get(mid);

      if (midKey.compareTo(key) == 0) {
        return mid;
      }
      if (midKey.compareTo(key) < 0) {
        start = mid + 1;
      } else {
        end = mid - 1;
      }
    }

    return -1;
  }


  /**
   * Find the index of the first key that is larger than the search key,
   * using binary search. Returns <code>-1</code> if there is no key in the list larger
   * than the input key
   */
  public static <K extends Comparable<K>> int findFirstLargerIndex(K key, List<K> keyList) {
    int start = 0;
    int end = keyList.size() - 1;

    if (key.compareTo(keyList.getFirst()) < 0) {
      return 0;
    }

    if (key.compareTo(keyList.getLast()) >= 0) {
      return keyList.size();
    }

    while (start < end) {
      int mid = (start + end) / 2;
      K midKey = keyList.get(mid);

      if (midKey.compareTo(key) <= 0) {
        start = mid + 1;
      } else {
        end = mid;
      }
    }

    return start;
  }
}
