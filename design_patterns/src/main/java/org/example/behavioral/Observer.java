package org.example.behavioral;

import java.util.ArrayList;
import java.util.List;

/**
 * Observer — defines a one-to-many dependency so that when one object changes
 * state, all its dependents are notified automatically.
 * Example: event bus for stock price updates.
 */
public class Observer {

    interface StockObserver {
        void onPriceChange(String ticker, double newPrice);
    }

    static class StockMarket {
        private final List<StockObserver> observers = new ArrayList<>();

        public void subscribe(StockObserver o)   { observers.add(o); }
        public void unsubscribe(StockObserver o) { observers.remove(o); }

        public void setPrice(String ticker, double price) {
            System.out.println("Market: " + ticker + " = $" + price);
            observers.forEach(o -> o.onPriceChange(ticker, price));
        }
    }

    static class AlertService implements StockObserver {
        private final double threshold;
        AlertService(double threshold) { this.threshold = threshold; }

        public void onPriceChange(String ticker, double price) {
            if (price > threshold)
                System.out.println("Alert! " + ticker + " above $" + threshold);
        }
    }

    static class Portfolio implements StockObserver {
        public void onPriceChange(String ticker, double price) {
            System.out.println("Portfolio updated: " + ticker + " → $" + price);
        }
    }

    public static void main(String[] args) {
        StockMarket market = new StockMarket();
        market.subscribe(new AlertService(150.0));
        market.subscribe(new Portfolio());

        market.setPrice("AAPL", 145.0);
        market.setPrice("AAPL", 160.0);
    }
}
