package org.example.creational;

/**
 * Prototype — creates new objects by copying an existing instance.
 * Example: cloning a pre-configured document template.
 */
public class Prototype {

    static class Document implements Cloneable {
        private String title;
        private String content;

        public Document(String title, String content) {
            this.title = title;
            this.content = content;
        }

        @Override
        public Document clone() {
            try {
                return (Document) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        public void setTitle(String title)     { this.title = title; }
        public String getTitle()               { return title; }
        public String getContent()             { return content; }
    }

    public static void main(String[] args) {
        Document template = new Document("Report Template", "Introduction:\n...");

        Document q1Report = template.clone();
        q1Report.setTitle("Q1 Report");

        Document q2Report = template.clone();
        q2Report.setTitle("Q2 Report");

        System.out.println(template.getTitle()); // Report Template
        System.out.println(q1Report.getTitle()); // Q1 Report
        System.out.println(q2Report.getTitle()); // Q2 Report
    }
}
