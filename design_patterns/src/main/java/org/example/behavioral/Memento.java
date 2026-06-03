package org.example.behavioral;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Memento — captures and externalises an object's internal state so it can be
 * restored later, without violating encapsulation.
 * Example: undo history for a canvas drawing tool.
 */
public class Memento {

    static class Canvas {
        private String state;

        public void draw(String shape) {
            state = (state == null ? "" : state + ", ") + shape;
            System.out.println("Canvas: " + state);
        }

        public Snapshot save()             { return new Snapshot(state); }
        public void restore(Snapshot s)    { state = s.state(); }

        record Snapshot(String state) {}
    }

    static class History {
        private final Deque<Canvas.Snapshot> snapshots = new ArrayDeque<>();

        public void push(Canvas.Snapshot s) { snapshots.push(s); }
        public Canvas.Snapshot pop()        { return snapshots.isEmpty() ? null : snapshots.pop(); }
    }

    public static void main(String[] args) {
        Canvas canvas  = new Canvas();
        History history = new History();

        canvas.draw("Circle");
        history.push(canvas.save());

        canvas.draw("Square");
        history.push(canvas.save());

        canvas.draw("Triangle");

        System.out.println("-- undo --");
        canvas.restore(history.pop());
        System.out.println("Canvas after undo: " + canvas.save().state());

        System.out.println("-- undo --");
        canvas.restore(history.pop());
        System.out.println("Canvas after undo: " + canvas.save().state());
    }
}
