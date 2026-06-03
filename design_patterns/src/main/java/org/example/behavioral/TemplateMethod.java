package org.example.behavioral;

/**
 * Template Method — defines the skeleton of an algorithm in a base class,
 * deferring some steps to subclasses.
 * Example: data importer that reads → parse → save, with format-specific steps.
 */
public class TemplateMethod {

    static abstract class DataImporter {
        // Template method — fixed algorithm skeleton
        public final void importData(String source) {
            String raw      = readData(source);
            String parsed   = parseData(raw);
            saveData(parsed);
        }

        protected abstract String readData(String source);
        protected abstract String parseData(String raw);

        protected void saveData(String data) {
            System.out.println("Saving: " + data);
        }
    }

    static class CsvImporter extends DataImporter {
        protected String readData(String source) {
            return "name,age\nAlice,30\nBob,25";
        }
        protected String parseData(String raw) {
            return raw.replace(",", " | ").replace("\n", "  /  ");
        }
    }

    static class JsonImporter extends DataImporter {
        protected String readData(String source) {
            return "[{\"name\":\"Alice\"},{\"name\":\"Bob\"}]";
        }
        protected String parseData(String raw) {
            return raw.replaceAll("[\\[\\]{}\":]", "").trim();
        }
    }

    public static void main(String[] args) {
        new CsvImporter().importData("data.csv");
        new JsonImporter().importData("data.json");
    }
}
