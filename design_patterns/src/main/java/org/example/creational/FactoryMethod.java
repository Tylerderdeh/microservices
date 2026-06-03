package org.example.creational;

/**
 * Factory Method — defines an interface for creating objects, but lets
 * subclasses decide which class to instantiate.
 * Example: different notification senders (Email, SMS, Push).
 */
public class FactoryMethod {

    interface Notification {
        void send(String message);
    }

    static class EmailNotification implements Notification {
        public void send(String message) {
            System.out.println("Email: " + message);
        }
    }

    static class SmsNotification implements Notification {
        public void send(String message) {
            System.out.println("SMS: " + message);
        }
    }

    static abstract class NotificationFactory {
        abstract Notification createNotification();

        public void notify(String message) {
            createNotification().send(message);
        }
    }

    static class EmailFactory extends NotificationFactory {
        public Notification createNotification() { return new EmailNotification(); }
    }

    static class SmsFactory extends NotificationFactory {
        public Notification createNotification() { return new SmsNotification(); }
    }

    public static void main(String[] args) {
        NotificationFactory factory = new EmailFactory();
        factory.notify("Your order shipped!");

        factory = new SmsFactory();
        factory.notify("Your order shipped!");
    }
}
