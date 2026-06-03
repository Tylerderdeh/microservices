package org.example.structural;

import java.util.ArrayList;
import java.util.List;

/**
 * Composite — composes objects into tree structures to represent part-whole
 * hierarchies. Clients treat individual objects and compositions uniformly.
 * Example: file system with files and directories.
 */
public class Composite {

    interface FileSystemNode {
        void print(String indent);
        long size();
    }

    static class File implements FileSystemNode {
        private final String name;
        private final long bytes;

        File(String name, long bytes) { this.name = name; this.bytes = bytes; }

        public void print(String indent) {
            System.out.println(indent + name + " (" + bytes + "B)");
        }

        public long size() { return bytes; }
    }

    static class Directory implements FileSystemNode {
        private final String name;
        private final List<FileSystemNode> children = new ArrayList<>();

        Directory(String name) { this.name = name; }

        public void add(FileSystemNode node) { children.add(node); }

        public void print(String indent) {
            System.out.println(indent + "[" + name + "] (" + size() + "B)");
            children.forEach(c -> c.print(indent + "  "));
        }

        public long size() { return children.stream().mapToLong(FileSystemNode::size).sum(); }
    }

    public static void main(String[] args) {
        Directory root = new Directory("root");
        root.add(new File("readme.txt", 200));

        Directory src = new Directory("src");
        src.add(new File("Main.java", 1500));
        src.add(new File("Utils.java", 800));
        root.add(src);

        root.print("");
    }
}
