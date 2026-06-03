package org.example.behavioral;

/**
 * Iterator — provides a way to traverse a collection without exposing its
 * underlying structure.
 * Example: iterating over a fixed-size circular buffer.
 */
public class Iterator {

    interface MyIterator<T> {
        boolean hasNext();
        T next();
    }

    static class CircularBuffer<T> {
        private final Object[] data;
        private int size = 0;
        private int head = 0;

        CircularBuffer(int capacity) { data = new Object[capacity]; }

        public void add(T item) {
            data[(head + size) % data.length] = item;
            if (size < data.length) size++;
            else head = (head + 1) % data.length; // overwrite oldest
        }

        public MyIterator<T> iterator() {
            return new MyIterator<>() {
                int index = 0;

                public boolean hasNext() { return index < size; }

                @SuppressWarnings("unchecked")
                public T next() {
                    return (T) data[(head + index++) % data.length];
                }
            };
        }
    }

    public static void main(String[] args) {
        CircularBuffer<Integer> buf = new CircularBuffer<>(3);
        buf.add(1); buf.add(2); buf.add(3); buf.add(4); // 4 overwrites 1

        MyIterator<Integer> it = buf.iterator();
        while (it.hasNext()) System.out.print(it.next() + " "); // 2 3 4
        System.out.println();
    }
}
