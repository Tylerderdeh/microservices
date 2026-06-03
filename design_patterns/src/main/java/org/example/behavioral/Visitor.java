package org.example.behavioral;

/**
 * Visitor — lets you add new operations to an object structure without
 * modifying the classes of the elements.
 * Example: computing tax and rendering for different expense types.
 */
public class Visitor {

    interface ExpenseVisitor {
        void visit(HotelExpense e);
        void visit(FlightExpense e);
        void visit(MealExpense e);
    }

    interface Expense {
        void accept(ExpenseVisitor visitor);
        double amount();
    }

    record HotelExpense(double amount) implements Expense {
        public void accept(ExpenseVisitor v) { v.visit(this); }
    }

    record FlightExpense(double amount) implements Expense {
        public void accept(ExpenseVisitor v) { v.visit(this); }
    }

    record MealExpense(double amount) implements Expense {
        public void accept(ExpenseVisitor v) { v.visit(this); }
    }

    static class TaxCalculator implements ExpenseVisitor {
        private double totalTax = 0;

        public void visit(HotelExpense  e) { totalTax += e.amount() * 0.15; }
        public void visit(FlightExpense e) { totalTax += e.amount() * 0.07; }
        public void visit(MealExpense   e) { totalTax += e.amount() * 0.10; }

        public double getTotalTax() { return totalTax; }
    }

    static class ExpenseReport implements ExpenseVisitor {
        public void visit(HotelExpense  e) { System.out.printf("Hotel:  $%.2f%n", e.amount()); }
        public void visit(FlightExpense e) { System.out.printf("Flight: $%.2f%n", e.amount()); }
        public void visit(MealExpense   e) { System.out.printf("Meal:   $%.2f%n", e.amount()); }
    }

    public static void main(String[] args) {
        Expense[] expenses = {
            new HotelExpense(200), new FlightExpense(350), new MealExpense(45)
        };

        ExpenseReport report = new ExpenseReport();
        TaxCalculator tax    = new TaxCalculator();

        for (Expense e : expenses) { e.accept(report); e.accept(tax); }
        System.out.printf("Total tax: $%.2f%n", tax.getTotalTax());
    }
}
