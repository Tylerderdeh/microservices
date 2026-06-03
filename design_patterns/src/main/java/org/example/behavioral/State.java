package org.example.behavioral;

/**
 * State — allows an object to alter its behaviour when its internal state
 * changes.
 * Example: vending machine with IDLE / HAS_MONEY / DISPENSING states.
 */
public class State {

    interface VendingState {
        void insertCoin(VendingMachine machine);
        void selectProduct(VendingMachine machine);
        void dispense(VendingMachine machine);
    }

    static class IdleState implements VendingState {
        public void insertCoin(VendingMachine m)   { System.out.println("Coin inserted"); m.setState(m.getHasMoneyState()); }
        public void selectProduct(VendingMachine m){ System.out.println("Insert coin first"); }
        public void dispense(VendingMachine m)     { System.out.println("No money inserted"); }
    }

    static class HasMoneyState implements VendingState {
        public void insertCoin(VendingMachine m)   { System.out.println("Coin already inserted"); }
        public void selectProduct(VendingMachine m){ System.out.println("Product selected"); m.setState(m.getDispensingState()); }
        public void dispense(VendingMachine m)     { System.out.println("Select product first"); }
    }

    static class DispensingState implements VendingState {
        public void insertCoin(VendingMachine m)   { System.out.println("Please wait, dispensing"); }
        public void selectProduct(VendingMachine m){ System.out.println("Already dispensing"); }
        public void dispense(VendingMachine m)     { System.out.println("Dispensing product!"); m.setState(m.getIdleState()); }
    }

    static class VendingMachine {
        private final VendingState idleState      = new IdleState();
        private final VendingState hasMoneyState  = new HasMoneyState();
        private final VendingState dispensingState = new DispensingState();
        private VendingState current = idleState;

        public void setState(VendingState s) { current = s; }
        public VendingState getIdleState()       { return idleState; }
        public VendingState getHasMoneyState()   { return hasMoneyState; }
        public VendingState getDispensingState() { return dispensingState; }

        public void insertCoin()   { current.insertCoin(this);   }
        public void selectProduct(){ current.selectProduct(this); }
        public void dispense()     { current.dispense(this);     }
    }

    public static void main(String[] args) {
        VendingMachine machine = new VendingMachine();
        machine.selectProduct();   // Insert coin first
        machine.insertCoin();      // Coin inserted
        machine.selectProduct();   // Product selected
        machine.dispense();        // Dispensing product!
        machine.dispense();        // No money inserted
    }
}
