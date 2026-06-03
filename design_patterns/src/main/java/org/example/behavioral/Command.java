package org.example.behavioral;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Command — encapsulates a request as an object, enabling undo/redo and
 * queuing.
 * Example: text editor with undo stack.
 */
public class Command {

    interface TextCommand {
        void execute();
        void undo();
    }

    static class TextEditor {
        private final StringBuilder text = new StringBuilder();

        public void append(String s) { text.append(s); }
        public void deleteLast(int n) { text.delete(text.length() - n, text.length()); }
        public String getText() { return text.toString(); }
    }

    static class AppendCommand implements TextCommand {
        private final TextEditor editor;
        private final String text;

        AppendCommand(TextEditor editor, String text) {
            this.editor = editor;
            this.text   = text;
        }

        public void execute() { editor.append(text); }
        public void undo()    { editor.deleteLast(text.length()); }
    }

    static class CommandHistory {
        private final Deque<TextCommand> history = new ArrayDeque<>();

        public void executeCommand(TextCommand cmd) {
            cmd.execute();
            history.push(cmd);
        }

        public void undo() {
            if (!history.isEmpty()) history.pop().undo();
        }
    }

    public static void main(String[] args) {
        TextEditor editor = new TextEditor();
        CommandHistory history = new CommandHistory();

        history.executeCommand(new AppendCommand(editor, "Hello"));
        history.executeCommand(new AppendCommand(editor, ", World"));
        System.out.println(editor.getText()); // Hello, World

        history.undo();
        System.out.println(editor.getText()); // Hello

        history.undo();
        System.out.println(editor.getText()); // (empty)
    }
}
