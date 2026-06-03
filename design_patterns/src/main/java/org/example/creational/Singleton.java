package org.example.creational;

/**
 * Singleton — ensures a class has only one instance.
 * Example: application-wide configuration store.
 */
public class Singleton {

    private static volatile Singleton instance;
    private String configValue;

    private Singleton() {
        configValue = "default";
    }

    public static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }

    public String getConfigValue() { return configValue; }
    public void setConfigValue(String value) { this.configValue = value; }

    public static void main(String[] args) {
        Singleton a = Singleton.getInstance();
        Singleton b = Singleton.getInstance();
        a.setConfigValue("production");

        System.out.println(b.getConfigValue()); // production
        System.out.println(a == b);             // true
    }
}
