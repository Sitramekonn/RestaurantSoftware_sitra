package edu.metrostate.ics372_phase2;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JSONParserTest {

    @TempDir
    File tempDir;

    private File createValidJSONFile() throws IOException {
        File jsonFile = new File(tempDir, "test-order.json");
        try (FileWriter writer = new FileWriter(jsonFile)) {
            writer.write("""
                {
                  "order": {
                    "type": "TOGO",
                    "order_date": 1699027200000,
                    "items": [
                      {
                        "name": "Coffee",
                        "quantity": 2,
                        "price": 5.99
                      },
                      {
                        "name": "Sandwich",
                        "quantity": 1,
                        "price": 8.50
                      }
                    ]
                  }
                }
                """);
        }
        return jsonFile;
    }

    private File createInvalidJSONFile() throws IOException {
        File jsonFile = new File(tempDir, "invalid.json");
        try (FileWriter writer = new FileWriter(jsonFile)) {
            writer.write("{ invalid json content }");
        }
        return jsonFile;
    }

    @Test
    void testReadOrderFromExternalFileValid() throws IOException {
        File jsonFile = createValidJSONFile();
        
        Order order = JSONParser.readOrderFromExternalFile(jsonFile.getAbsolutePath());
        
        assertNotNull(order);
        assertEquals(OrderType.TOGO, order.getOrderType());
        assertEquals(2, order.getItems().size());
        assertEquals("Coffee", order.getItems().get(0).name());
        assertEquals(2, order.getItems().get(0).quantity());
        assertEquals(5.99, order.getItems().get(0).price());
    }

    @Test
    void testReadOrderFromExternalFileNotFound() {
        Order order = JSONParser.readOrderFromExternalFile("nonexistent-file.json");
        
        assertNull(order);
    }

    @Test
    void testReadOrderFromExternalFileInvalid() throws IOException {
        File invalidFile = createInvalidJSONFile();
        
        Order order = JSONParser.readOrderFromExternalFile(invalidFile.getAbsolutePath());
        
        assertNull(order);
    }

    @Test
    void testExportAllOrders() throws IOException {
        List<Order> orders = new ArrayList<>();
        
        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem("Test Item", 1, 10.0));
        
        Order order = new Order("TEST001", null, items, OrderType.TOGO);
        orders.add(order);

        String filename = "test-export";
        
        assertDoesNotThrow(() -> JSONParser.exportAllOrders(orders, filename));
        
        File exportedFile = new File("ICS372_Group3_ProjectPhase2/Exported Orders/test-export.json");
        assertTrue(exportedFile.exists());
    }

    @Test
    void testExportAllOrdersAddsJsonExtension() throws IOException {
        List<Order> orders = new ArrayList<>();
        String filename = "test-without-extension";
        
        assertDoesNotThrow(() -> JSONParser.exportAllOrders(orders, filename));
        
        File exportedFile = new File("ICS372_Group3_ProjectPhase2/Exported Orders/test-without-extension.json");
        assertTrue(exportedFile.exists());
    }
}