package org.example.creational;

/**
 * Builder — constructs complex objects step by step.
 * Example: building an HTTP request with optional fields.
 */
public class Builder {

    static class HttpRequest {
        private final String method;
        private final String url;
        private final String body;
        private final String authToken;
        private final int timeoutMs;

        private HttpRequest(BuilderInner b) {
            this.method    = b.method;
            this.url       = b.url;
            this.body      = b.body;
            this.authToken = b.authToken;
            this.timeoutMs = b.timeoutMs;
        }

        @Override
        public String toString() {
            return method + " " + url +
                   (authToken != null ? " [auth]" : "") +
                   (body != null ? " body=" + body : "") +
                   " timeout=" + timeoutMs + "ms";
        }

        static class BuilderInner {
            private final String method;
            private final String url;
            private String body;
            private String authToken;
            private int timeoutMs = 5000;

            public BuilderInner(String method, String url) {
                this.method = method;
                this.url    = url;
            }

            public BuilderInner body(String body)           { this.body = body;           return this; }
            public BuilderInner authToken(String token)     { this.authToken = token;     return this; }
            public BuilderInner timeoutMs(int ms)           { this.timeoutMs = ms;        return this; }
            public HttpRequest build()                      { return new HttpRequest(this); }
        }
    }

    public static void main(String[] args) {
        HttpRequest get = new HttpRequest.BuilderInner("GET", "https://api.example.com/users")
                .authToken("Bearer xyz")
                .timeoutMs(3000)
                .build();

        HttpRequest post = new HttpRequest.BuilderInner("POST", "https://api.example.com/users")
                .authToken("Bearer xyz")
                .body("{\"name\":\"Alice\"}")
                .build();

        System.out.println(get);
        System.out.println(post);
    }
}
