package org.example.behavioral;

import java.util.Arrays;

/**
 * Strategy — defines a family of algorithms, encapsulates each one, and makes
 * them interchangeable.
 * Example: pluggable sorting strategies.
 */
public class Strategy {

    interface SortStrategy {
        void sort(int[] arr);
    }

    static class BubbleSort implements SortStrategy {
        public void sort(int[] arr) {
            int n = arr.length;
            for (int i = 0; i < n - 1; i++)
                for (int j = 0; j < n - i - 1; j++)
                    if (arr[j] > arr[j + 1]) { int t = arr[j]; arr[j] = arr[j + 1]; arr[j + 1] = t; }
            System.out.println("BubbleSort: " + Arrays.toString(arr));
        }
    }

    static class InsertionSort implements SortStrategy {
        public void sort(int[] arr) {
            for (int i = 1; i < arr.length; i++) {
                int key = arr[i], j = i - 1;
                while (j >= 0 && arr[j] > key) { arr[j + 1] = arr[j]; j--; }
                arr[j + 1] = key;
            }
            System.out.println("InsertionSort: " + Arrays.toString(arr));
        }
    }

    static class Sorter {
        private SortStrategy strategy;

        public void setStrategy(SortStrategy strategy) { this.strategy = strategy; }
        public void sort(int[] arr) { strategy.sort(arr); }
    }

    public static void main(String[] args) {
        Sorter sorter = new Sorter();

        sorter.setStrategy(new BubbleSort());
        sorter.sort(new int[]{5, 3, 1, 4, 2});

        sorter.setStrategy(new InsertionSort());
        sorter.sort(new int[]{5, 3, 1, 4, 2});
    }
}
