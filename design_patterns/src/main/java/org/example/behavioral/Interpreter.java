package org.example.behavioral;

import java.util.Map;
import java.util.Stack;

/**
 * Interpreter — defines a grammar for a language and provides an interpreter
 * to deal with that grammar.
 * Example: simple arithmetic expression evaluator (RPN — Reverse Polish Notation).
 */
public class Interpreter {

    interface Expression {
        int interpret(Map<String, Integer> context);
    }

    record NumberExpression(int value) implements Expression {
        public int interpret(Map<String, Integer> ctx) { return value; }
    }

    record VariableExpression(String name) implements Expression {
        public int interpret(Map<String, Integer> ctx) {
            return ctx.getOrDefault(name, 0);
        }
    }

    record AddExpression(Expression left, Expression right) implements Expression {
        public int interpret(Map<String, Integer> ctx) {
            return left.interpret(ctx) + right.interpret(ctx);
        }
    }

    record MultiplyExpression(Expression left, Expression right) implements Expression {
        public int interpret(Map<String, Integer> ctx) {
            return left.interpret(ctx) * right.interpret(ctx);
        }
    }

    // Parse RPN string like "3 x + 2 *"
    static Expression parse(String rpn) {
        Stack<Expression> stack = new Stack<>();
        for (String token : rpn.split("\\s+")) {
            switch (token) {
                case "+" -> { Expression r = stack.pop(); stack.push(new AddExpression(stack.pop(), r)); }
                case "*" -> { Expression r = stack.pop(); stack.push(new MultiplyExpression(stack.pop(), r)); }
                default  -> {
                    try { stack.push(new NumberExpression(Integer.parseInt(token))); }
                    catch (NumberFormatException e) { stack.push(new VariableExpression(token)); }
                }
            }
        }
        return stack.pop();
    }

    public static void main(String[] args) {
        // (3 + x) * 2   in RPN: 3 x + 2 *
        Expression expr = parse("3 x + 2 *");

        Map<String, Integer> ctx = Map.of("x", 7);
        System.out.println("(3 + 7) * 2 = " + expr.interpret(ctx)); // 20
    }
}
