package org.example.structural;

/**
 * Bridge — decouples an abstraction from its implementation so both can vary
 * independently.
 * Example: shapes that can be rendered by different renderers (SVG / Canvas).
 */
public class Bridge {

    interface Renderer {
        void renderCircle(double radius);
        void renderSquare(double side);
    }

    static class SvgRenderer implements Renderer {
        public void renderCircle(double r) { System.out.println("SVG <circle r=\"" + r + "\"/>"); }
        public void renderSquare(double s) { System.out.println("SVG <rect w=\"" + s + "\" h=\"" + s + "\"/>"); }
    }

    static class CanvasRenderer implements Renderer {
        public void renderCircle(double r) { System.out.println("Canvas arc(0,0," + r + ")"); }
        public void renderSquare(double s) { System.out.println("Canvas fillRect(0,0," + s + "," + s + ")"); }
    }

    static abstract class Shape {
        protected final Renderer renderer;
        Shape(Renderer renderer) { this.renderer = renderer; }
        abstract void draw();
    }

    static class Circle extends Shape {
        private final double radius;
        Circle(double radius, Renderer renderer) { super(renderer); this.radius = radius; }
        public void draw() { renderer.renderCircle(radius); }
    }

    static class Square extends Shape {
        private final double side;
        Square(double side, Renderer renderer) { super(renderer); this.side = side; }
        public void draw() { renderer.renderSquare(side); }
    }

    public static void main(String[] args) {
        Renderer svg    = new SvgRenderer();
        Renderer canvas = new CanvasRenderer();

        new Circle(5, svg).draw();
        new Circle(5, canvas).draw();
        new Square(4, svg).draw();
    }
}
