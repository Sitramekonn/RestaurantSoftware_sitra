package edu.metrostate.ics372_phase2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class XMLParserTest {

    @TempDir
    File tempDir;

    private File createValidXMLFile() throws IOException {
        File xmlFile = new File(tempDir, "test-order.xml");
        try (FileWriter writer = new FileWriter(xmlFile)) {
            writer.write("""
                <?xml version="1.0" encoding="UTF-8"?>
                <Order id="TEST001">
                    <OrderType>TOGO</OrderType>
                    <Item type="Coffee">
                        <Price>5.99</Price>
                        <Quantity>2</Quantity>
                    </Item>
                    <Item type="Sandwich">
                        <Price>8.50</Price>
                        <Quantity>1</Quantity>
                    </Item>
                </Order>
                """);
        }
        return xmlFile;
    }

    private File createInvalidXMLFile() throws IOException {
        File xmlFile = new File(tempDir, "invalid.xml");
        try (FileWriter writer = new FileWriter(xmlFile)) {
            writer.write("<invalid xml content>");
        }
        return xmlFile;
    }

    @Test
    void testReadOrderFromExternalFileValid() throws IOException {
        File xmlFile = createValidXMLFile();
        
        Order order = XMLParser.readOrderFromExternalFile(xmlFile.getAbsolutePath());
        
        assertNotNull(order);
        assertEquals(OrderType.TOGO, order.getOrderType());
        assertEquals("TEST001", order.getOrderId());
        assertEquals(2, order.getItems().size());
        assertEquals("Coffee", order.getItems().get(0).name());
        assertEquals(2, order.getItems().get(0).quantity());
        assertEquals(5.99, order.getItems().get(0).price());
    }

    @Test
    void testReadOrderFromExternalFileNotFound() {
        Order order = XMLParser.readOrderFromExternalFile("nonexistent-file.xml");
        
        assertNull(order);
    }

    @Test
    void testReadOrderFromExternalFileInvalid() throws IOException {
        File invalidFile = createInvalidXMLFile();
        
        Order order = XMLParser.readOrderFromExternalFile(invalidFile.getAbsolutePath());
        
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
        
        assertDoesNotThrow(() -> XMLParser.exportAllOrders(orders, filename));
        
        File exportedFile = new File("ICS372_Group3_ProjectPhase2/Exported Orders/test-export.xml");
        assertTrue(exportedFile.exists());
    }

    @Test
    void testReadOrderFromFolderMethod() {
        // Test the readOrderFromFolder method returns null for non-existent file
        Order order = XMLParser.readOrderFromFolder("nonexistent.xml");
        
        assertNull(order);
    }

    @Test
    void testReadOrderFromResourceFolderMethod() {
        // Test the readOrderFromResourceFolder method returns null for non-existent file
        Order order = XMLParser.readOrderFromResourceFolder("nonexistent.xml");
        
        assertNull(order);
    }
}