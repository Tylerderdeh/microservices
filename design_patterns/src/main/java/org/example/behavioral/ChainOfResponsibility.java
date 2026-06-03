package org.example.behavioral;

/**
 * Chain of Responsibility — passes a request along a chain of handlers until
 * one handles it.
 * Example: HTTP middleware chain (auth → rate-limit → logging).
 */
public class ChainOfResponsibility {

    static abstract class Handler {
        protected Handler next;

        public Handler setNext(Handler next) { this.next = next; return next; }

        public abstract void handle(Request request);
    }

    record Request(String user, int rateCount, String path) {}

    static class AuthHandler extends Handler {
        public void handle(Request req) {
            if (req.user() == null) {
                System.out.println("AuthHandler: rejected — no user");
                return;
            }
            System.out.println("AuthHandler: passed");
            if (next != null) next.handle(req);
        }
    }

    static class RateLimitHandler extends Handler {
        public void handle(Request req) {
            if (req.rateCount() > 100) {
                System.out.println("RateLimitHandler: rejected — too many requests");
                return;
            }
            System.out.println("RateLimitHandler: passed");
            if (next != null) next.handle(req);
        }
    }

    static class LoggingHandler extends Handler {
        public void handle(Request req) {
            System.out.println("LoggingHandler: " + req.user() + " → " + req.path());
            if (next != null) next.handle(req);
        }
    }

    public static void main(String[] args) {
        Handler auth = new AuthHandler();
        auth.setNext(new RateLimitHandler()).setNext(new LoggingHandler());

        System.out.println("-- Valid request --");
        auth.handle(new Request("alice", 5, "/api/data"));

        System.out.println("-- Unauthenticated --");
        auth.handle(new Request(null, 5, "/api/data"));
    }
}
