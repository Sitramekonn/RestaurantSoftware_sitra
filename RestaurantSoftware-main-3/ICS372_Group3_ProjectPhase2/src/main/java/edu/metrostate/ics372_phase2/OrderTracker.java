package edu.metrostate.ics372_phase2;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

public class OrderTracker {
    private final Map<String, Order> orders = new HashMap<>();
    private static int nextOrderId = 1;

    public void addOrder(Order order) {
        if (order.getOrderId() == null || order.getOrderId().isEmpty()) {
            order.setOrderId(String.valueOf(nextOrderId++));
        }
        orders.put(order.getOrderId(), order);
    }

    public boolean startOrder(String orderId) {
        Order order = orders.get(orderId);
        if (order != null) {
            return order.start();
        }
        return false;
    }

    public boolean completeOrder(String orderId) {
        Order order = orders.get(orderId);
        if (order != null) {
            return order.complete();
        }
        return false;
    }

    public Collection<Order> exportAllOrders() {
        return orders.values();
    }



    public List<Order> getAll() {
        return new ArrayList<>(orders.values());
    }

    public Order getOrder(String orderId) {
        return orders.get(orderId);
    }

    //Put cancel Order method here

    public void cancelOrder(String orderId){
        Order order = orders.get(orderId);
        if (order == null){
            throw new IllegalArgumentException("Order #" + orderId +  "not found");
        }
        if (order.getStatus() == OrderStatus.CANCELLED){
            return;
        }
        if(order.getStatus() == OrderStatus.COMPLETED){
            throw new IllegalArgumentException("Order " + orderId + " has already been completed.");
        }
        order.setStatus(OrderStatus.CANCELLED);
    }

}
