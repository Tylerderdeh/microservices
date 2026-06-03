package org.example.structural;

/**
 * Proxy — provides a surrogate that controls access to another object.
 * Example: caching proxy for an expensive database query.
 */
public class Proxy {

    interface UserRepository {
        String findById(int id);
    }

    static class DatabaseUserRepository implements UserRepository {
        public String findById(int id) {
            System.out.println("DB query for id=" + id);
            return "User#" + id;
        }
    }

    static class CachingUserRepositoryProxy implements UserRepository {
        private final UserRepository real;
        private final java.util.Map<Integer, String> cache = new java.util.HashMap<>();

        CachingUserRepositoryProxy(UserRepository real) { this.real = real; }

        public String findById(int id) {
            return cache.computeIfAbsent(id, key -> {
                System.out.println("Cache miss for id=" + key);
                return real.findById(key);
            });
        }
    }

    public static void main(String[] args) {
        UserRepository repo = new CachingUserRepositoryProxy(new DatabaseUserRepository());

        System.out.println(repo.findById(1)); // cache miss → DB query
        System.out.println(repo.findById(1)); // cache hit
        System.out.println(repo.findById(2)); // cache miss → DB query
    }
}
