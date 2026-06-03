package org.example.structural;

/**
 * Adapter — converts one interface into another that a client expects.
 * Example: wrapping a legacy XML logger behind a modern JSON logger interface.
 */
public class Adapter {

    interface JsonLogger {
        void log(String json);
    }

    // Legacy class we cannot modify
    static class XmlLogger {
        public void logXml(String xml) {
            System.out.println("XML Logger: " + xml);
        }
    }

    static class XmlToJsonAdapter implements JsonLogger {
        private final XmlLogger xmlLogger;

        XmlToJsonAdapter(XmlLogger xmlLogger) {
            this.xmlLogger = xmlLogger;
        }

        public void log(String json) {
            // Minimal conversion for illustration
            String xml = "<log>" + json.replaceAll("\"", "") + "</log>";
            xmlLogger.logXml(xml);
        }
    }

    public static void main(String[] args) {
        JsonLogger logger = new XmlToJsonAdapter(new XmlLogger());
        logger.log("{\"level\":\"INFO\",\"msg\":\"Service started\"}");
    }
}
