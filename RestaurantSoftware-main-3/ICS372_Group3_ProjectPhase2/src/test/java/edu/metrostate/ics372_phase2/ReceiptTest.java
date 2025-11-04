package edu.metrostate.ics372_phase2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

class ReceiptTest {

    private Order testOrder;
    private OrderTracker tracker;
    
    @TempDir
    File tempDir;

    @BeforeEach
    void setUp() {
        tracker = new OrderTracker();
        
        // Create test order
        List<OrderItem> items = Arrays.asList(
            new OrderItem("Burger", 2, 12.99),
            new OrderItem("Fries", 1, 4.99)
        );
        
        testOrder = new Order("TEST001", LocalDateTime.now(), items, OrderType.TOGO);
        tracker.addOrder(testOrder);
    }

    @Test
    void testOrderReceiptTextGeneration() {
        String receiptText = testOrder.toReceiptText();
        
        assertNotNull(receiptText);
        assertTrue(receiptText.contains("RESTAURANT RECEIPT"));
        assertTrue(receiptText.contains("Order ID: TEST001"));
        assertTrue(receiptText.contains("Burger"));
        assertTrue(receiptText.contains("Fries"));
        assertTrue(receiptText.contains("TOTAL:"));
        assertTrue(receiptText.contains("Thank you for your business!"));
    }

    @Test
    void testReceiptContainsCorrectTotal() {
        String receiptText = testOrder.toReceiptText();
        double expectedTotal = (12.99 * 2) + (4.99 * 1); // 30.97
        
        assertTrue(receiptText.contains(String.format("$%.2f", expectedTotal)));
    }

    @Test
    void testReceiptContainsAllOrderDetails() {
        String receiptText = testOrder.toReceiptText();
        
        assertTrue(receiptText.contains("Order Type: TOGO"));
        assertTrue(receiptText.contains("Status: INCOMING"));
        assertTrue(receiptText.contains("Burger"));
        assertTrue(receiptText.contains("x2"));
        assertTrue(receiptText.contains("Fries"));
        assertTrue(receiptText.contains("x1"));
    }

    @Test
    void testOrderTrackerGetOrderMethod() {
        Order retrievedOrder = tracker.getOrder("TEST001");
        assertNotNull(retrievedOrder);
        assertEquals("TEST001", retrievedOrder.getOrderId());
        
        Order nonExistentOrder = tracker.getOrder("NONEXISTENT");
        assertNull(nonExistentOrder);
    }

    @Test
    void testReceiptFormatStructure() {
        String receiptText = testOrder.toReceiptText();
        
        // Test that the receipt has the proper sections
        assertTrue(receiptText.contains("================================="));
        assertTrue(receiptText.contains("RESTAURANT RECEIPT"));
        assertTrue(receiptText.contains("ITEMS:"));
        assertTrue(receiptText.contains("---------------------------------"));
        assertTrue(receiptText.startsWith("================================="));
        assertTrue(receiptText.endsWith("Thank you for your business!\n"));
    }

    @Test
    void testReceiptWithDifferentOrderTypes() {
        // Test PICKUP order
        Order pickupOrder = new Order("PICKUP001", LocalDateTime.now(), 
            Arrays.asList(new OrderItem("Pizza", 1, 15.99)), OrderType.PICKUP);
        String pickupReceipt = pickupOrder.toReceiptText();
        assertTrue(pickupReceipt.contains("Order Type: PICKUP"));
        
        // Test DELIVERY order
        Order deliveryOrder = new Order("DELIVERY001", LocalDateTime.now(), 
            Arrays.asList(new OrderItem("Pasta", 1, 12.50)), OrderType.DELIVERY);
        String deliveryReceipt = deliveryOrder.toReceiptText();
        assertTrue(deliveryReceipt.contains("Order Type: DELIVERY"));
    }

    @Test
    void testReceiptWithDifferentOrderStatuses() {
        // Test STARTED status
        testOrder.start();
        String startedReceipt = testOrder.toReceiptText();
        assertTrue(startedReceipt.contains("Status: STARTED"));
        
        // Test COMPLETED status
        testOrder.complete();
        String completedReceipt = testOrder.toReceiptText();
        assertTrue(completedReceipt.contains("Status: COMPLETED"));
    }

    @Test
    void testReceiptCalculatesItemTotalsCorrectly() {
        String receiptText = testOrder.toReceiptText();
        
        // Check individual item totals
        double burgerTotal = 12.99 * 2; // 25.98
        double friesTotal = 4.99 * 1;   // 4.99
        
        assertTrue(receiptText.contains(String.format("$%.2f", burgerTotal)));
        assertTrue(receiptText.contains(String.format("$%.2f", friesTotal)));
    }
}