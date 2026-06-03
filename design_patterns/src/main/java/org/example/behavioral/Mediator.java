package org.example.behavioral;

import java.util.ArrayList;
import java.util.List;

/**
 * Mediator — defines an object that encapsulates how a set of objects interact,
 * reducing direct dependencies.
 * Example: chat room where users communicate through the room (mediator).
 */
public class Mediator {

    interface ChatMediator {
        void sendMessage(String message, User sender);
        void addUser(User user);
    }

    static class ChatRoom implements ChatMediator {
        private final List<User> users = new ArrayList<>();

        public void addUser(User user) { users.add(user); }

        public void sendMessage(String message, User sender) {
            users.stream()
                 .filter(u -> u != sender)
                 .forEach(u -> u.receive(sender.getName() + ": " + message));
        }
    }

    static class User {
        private final String name;
        private final ChatMediator mediator;

        User(String name, ChatMediator mediator) {
            this.name     = name;
            this.mediator = mediator;
            mediator.addUser(this);
        }

        public String getName() { return name; }
        public void send(String message)   { mediator.sendMessage(message, this); }
        public void receive(String message) { System.out.println("[" + name + "] received: " + message); }
    }

    public static void main(String[] args) {
        ChatRoom room = new ChatRoom();

        User alice = new User("Alice", room);
        User bob   = new User("Bob",   room);
        User carol = new User("Carol", room);

        alice.send("Hello everyone!");
        bob.send("Hey Alice!");
    }
}
