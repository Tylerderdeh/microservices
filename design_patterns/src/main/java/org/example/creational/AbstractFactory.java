package org.example.creational;

/**
 * Abstract Factory — produces families of related objects without specifying
 * their concrete classes.
 * Example: UI component factory for different OS themes (Light / Dark).
 */
public class AbstractFactory {

    interface Button   { void render(); }
    interface Checkbox { void render(); }

    static class LightButton   implements Button   { public void render() { System.out.println("Light Button");   } }
    static class LightCheckbox implements Checkbox { public void render() { System.out.println("Light Checkbox"); } }
    static class DarkButton    implements Button   { public void render() { System.out.println("Dark Button");    } }
    static class DarkCheckbox  implements Checkbox { public void render() { System.out.println("Dark Checkbox");  } }

    interface UIFactory {
        Button   createButton();
        Checkbox createCheckbox();
    }

    static class LightThemeFactory implements UIFactory {
        public Button   createButton()   { return new LightButton();   }
        public Checkbox createCheckbox() { return new LightCheckbox(); }
    }

    static class DarkThemeFactory implements UIFactory {
        public Button   createButton()   { return new DarkButton();   }
        public Checkbox createCheckbox() { return new DarkCheckbox(); }
    }

    static void renderUI(UIFactory factory) {
        factory.createButton().render();
        factory.createCheckbox().render();
    }

    public static void main(String[] args) {
        renderUI(new LightThemeFactory());
        renderUI(new DarkThemeFactory());
    }
}
