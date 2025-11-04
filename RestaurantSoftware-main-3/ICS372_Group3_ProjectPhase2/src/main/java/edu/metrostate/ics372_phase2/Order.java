package edu.metrostate.ics372_phase2;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "orderId", "orderType", "orderTime", "status", "items", "source" })
public class Order {
    private String orderId;
    private LocalDateTime orderTime;
    private List<OrderItem> items;
    private OrderStatus status;
    private OrderType orderType;
    private String source; // Track which file this order came from

    private String customerNotes; // optional note shown/edited in dialog
    private String priority;      // "High", "Normal", "Low"

    // Default constructor for Jackson deserialization
    public Order() {
        this.status = OrderStatus.INCOMING;
        this.source = "unknown";
        // defaults for new fields
        this.customerNotes = "";
        this.priority = "Normal";
    }

    public Order(String orderId, LocalDateTime orderTime, List<OrderItem> items, OrderType orderType) {
        this.orderId = orderId;
        this.orderTime = orderTime;
        this.items = items;
        this.status = OrderStatus.INCOMING;
        this.orderType = orderType;
        this.source = "unknown";
        // defaults for new fields
        this.customerNotes = "";
        this.priority = "Normal";
    }

    // Constructor with source
    public Order(String orderId, LocalDateTime orderTime, List<OrderItem> items, OrderType orderType, String source) {
        this.orderId = orderId;
        this.orderTime = orderTime;
        this.items = items;
        this.status = OrderStatus.INCOMING;
        this.orderType = orderType;
        this.source = source;
        // defaults for new fields
        this.customerNotes = "";
        this.priority = "Normal";
    }

    public boolean start() {
        if (status == OrderStatus.INCOMING) {
            status = OrderStatus.STARTED;
            return true;
        }
        return false;
    }

    public boolean complete() {
        if (status == OrderStatus.STARTED) {
            status = OrderStatus.COMPLETED;
            return true;
        }
        return false;
    }

    @JsonIgnore
    public double getTotalPrice() {
        return items.stream()
                .mapToDouble(item -> item.price() * item.quantity())
                .sum();
    }

    /**
     * Generates a formatted receipt text for this order.
     * @return Formatted receipt as a string
     */
    @JsonIgnore
    public String toReceiptText() {
        StringBuilder receipt = new StringBuilder();

        receipt.append("=================================\n");
        receipt.append("       RESTAURANT RECEIPT\n");
        receipt.append("=================================\n\n");

        receipt.append("Order ID: ").append(orderId).append("\n");
        receipt.append("Order Type: ").append(orderType).append("\n");
        receipt.append("Order Time: ").append(orderTime).append("\n");
        receipt.append("Status: ").append(status).append("\n");
        receipt.append("Source: ").append(source).append("\n\n");

        receipt.append("ITEMS:\n");
        receipt.append("---------------------------------\n");

        for (OrderItem item : items) {
            receipt.append(String.format("%-20s x%d  $%.2f\n",
                    item.name(), item.quantity(), item.price() * item.quantity()));
        }

        receipt.append("---------------------------------\n");
        receipt.append(String.format("TOTAL: $%.2f\n", getTotalPrice()));
        receipt.append("=================================\n");
        receipt.append("Thank you for your business!\n");

        return receipt.toString();
    }

    public String getOrderId() {return orderId;}
    public LocalDateTime getOrderTime() {return orderTime;}
    public List<OrderItem> getItems() {return items;}
    public OrderStatus getStatus() {return status;}
    public OrderType getOrderType() {return orderType;}
    public String getSource() {return source;}

    public void setOrderId(String orderId) {this.orderId = orderId;}
    public void setOrderTime(LocalDateTime orderTime) {this.orderTime = orderTime;}
    public void setItems(List<OrderItem> items) {this.items = items;}
    public void setStatus(OrderStatus status) {this.status = status;}
    public void setOrderType(OrderType orderType) {this.orderType = orderType;}
    public void setSource(String source) {this.source = source;}

    public String getCustomerNotes() { return customerNotes; }
    public void setCustomerNotes(String customerNotes) { this.customerNotes = customerNotes; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    // ------------------------------------------
}
