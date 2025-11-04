package edu.metrostate.ics372_phase2;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StateManagerTest {

    @TempDir
    File tempDir;

    private List<Order> createTestOrders() {
        List<Order> orders = new ArrayList<>();
        
        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem("Coffee", 2, 5.99));
        items.add(new OrderItem("Sandwich", 1, 8.50));
        
        Order order1 = new Order("TEST001", null, items, OrderType.TOGO);
        order1.setStatus(OrderStatus.INCOMING);
        
        Order order2 = new Order("TEST002", null, items, OrderType.PICKUP);
        order2.setStatus(OrderStatus.STARTED);
        
        orders.add(order1);
        orders.add(order2);
        
        return orders;
    }

    @Test
    void testSaveState() {
        List<Order> orders = createTestOrders();
        
        assertDoesNotThrow(() -> StateManager.saveState(orders));
        
        File stateFile = new File("ICS372_Group3_ProjectPhase2/AppState/orders_state.json");
        assertTrue(stateFile.exists());
    }

    @Test
    void testLoadStateWhenFileExists() {
        List<Order> originalOrders = createTestOrders();
        StateManager.saveState(originalOrders);
        
        List<Order> loadedOrders = StateManager.loadState();
        
        assertNotNull(loadedOrders);
        assertEquals(2, loadedOrders.size());
        assertEquals("TEST001", loadedOrders.get(0).getOrderId());
        assertEquals("TEST002", loadedOrders.get(1).getOrderId());
    }

    @Test
    void testLoadStateWhenFileNotExists() {
        StateManager.clearState();
        
        List<Order> loadedOrders = StateManager.loadState();
        
        assertNotNull(loadedOrders);
        assertTrue(loadedOrders.isEmpty());
    }

    @Test
    void testClearState() {
        List<Order> orders = createTestOrders();
        StateManager.saveState(orders);
        
        StateManager.clearState();
        
        File stateFile = new File("ICS372_Group3_ProjectPhase2/AppState/orders_state.json");
        assertFalse(stateFile.exists());
    }

    @Test
    void testSaveEmptyOrderList() {
        List<Order> emptyOrders = new ArrayList<>();
        
        assertDoesNotThrow(() -> StateManager.saveState(emptyOrders));
        
        List<Order> loadedOrders = StateManager.loadState();
        assertNotNull(loadedOrders);
        assertTrue(loadedOrders.isEmpty());
    }
}