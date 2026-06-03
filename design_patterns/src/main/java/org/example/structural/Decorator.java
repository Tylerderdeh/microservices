package org.example.structural;

/**
 * Decorator — attaches additional responsibilities to an object dynamically.
 * Example: adding compression and encryption to a data stream.
 */
public class Decorator {

    interface DataWriter {
        void write(String data);
    }

    static class FileWriter implements DataWriter {
        public void write(String data) {
            System.out.println("Writing to file: " + data);
        }
    }

    static abstract class DataWriterDecorator implements DataWriter {
        protected final DataWriter wrapped;
        DataWriterDecorator(DataWriter wrapped) { this.wrapped = wrapped; }
    }

    static class CompressionDecorator extends DataWriterDecorator {
        CompressionDecorator(DataWriter wrapped) { super(wrapped); }
        public void write(String data) {
            String compressed = "COMPRESSED[" + data + "]";
            wrapped.write(compressed);
        }
    }

    static class EncryptionDecorator extends DataWriterDecorator {
        EncryptionDecorator(DataWriter wrapped) { super(wrapped); }
        public void write(String data) {
            String encrypted = "ENCRYPTED[" + data + "]";
            wrapped.write(encrypted);
        }
    }

    public static void main(String[] args) {
        DataWriter writer = new EncryptionDecorator(
                                new CompressionDecorator(
                                    new FileWriter()));
        writer.write("user_data");
        // Writing to file: ENCRYPTED[COMPRESSED[user_data]]
    }
}
