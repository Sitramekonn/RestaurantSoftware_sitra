package edu.metrostate.ics372_phase2;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import java.lang.reflect.*;

import java.nio.file.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

class XMLImportTest {

    @TempDir
    Path tempDir;

    private Path createFile(String filename, String content) throws IOException {
        Path file = tempDir.resolve(filename);
        Files.writeString(file, content);
        return file;
    }

    @Test
    @DisplayName("Support XML format import - Valid XML")
    void testFR31_ValidXMLImport() throws IOException {
        String xml = """
            <?xml version="1.0"?>
            <Order id="ORD-001">
                <OrderType>DELIVERY</OrderType>
                <Item type="Burger"><Price>12.99</Price><Quantity>2</Quantity></Item>
            </Order>
            """;

        Order order = XMLParser.readOrderFromExternalFile(createFile("order.xml", xml).toString());

        assertNotNull(order, "Should parse valid XML");
        assertEquals("ORD-001", order.getOrderId());
        assertEquals(OrderType.DELIVERY, order.getOrderType());
        assertEquals(1, order.getItems().size());
        assertEquals(12.99, order.getItems().get(0).price(), 0.01);
        assertEquals(2, order.getItems().get(0).quantity());
    }

    @Test
    @DisplayName("Store data source - Company name for directory imports")
    void testFR32_StoreDataSource() throws IOException {
        Order order = XMLParser.readOrderFromExternalFile(
                createFile("order.xml", "<Order id=\"S-001\"></Order>").toString()
        );
        order.setSource("DoorDash");
        assertEquals("DoorDash", order.getSource());
    }

    @Test
    @DisplayName("Handle malformed data - Invalid values, defaults applied")
    void testFR33_GracefulErrorHandling() throws IOException {
        String xml = """
            <?xml version="1.0"?>
            <Order>
                <OrderType>INVALID</OrderType>
                <Item type="Pizza"><Price>ABC</Price><Quantity>XYZ</Quantity></Item>
            </Order>
            """;

        Order order = XMLParser.readOrderFromExternalFile(createFile("bad.xml", xml).toString());
        assertNotNull(order);
        assertEquals(OrderType.TOGO, order.getOrderType());
        assertEquals(0.0, order.getItems().get(0).price(), 0.01);
        assertEquals(1, order.getItems().get(0).quantity());
    }

    @Test
    @DisplayName("Fatal error returns null")
    void testFR33_FatalError() throws IOException {
        Order order = XMLParser.readOrderFromExternalFile(
                createFile("broken.xml", "<Bad></Bad>").toString()
        );
        assertNull(order);
    }

    @Test
    @DisplayName("Auto-detect XML in company subdirectory with 500ms delay")
    void testFR34_AutoDetectWithDelay() throws IOException, InterruptedException {
        Path companyDir = tempDir.resolve("DoorDash");
        Files.createDirectories(companyDir);

        List<String> detected = new CopyOnWriteArrayList<>();
        AtomicLong processTime = new AtomicLong();

        DirectoryWatcher watcher = new DirectoryWatcher(companyDir.toString(), fileName -> {
            detected.add(fileName);
            processTime.set(System.currentTimeMillis());
        });
        watcher.startWatching();

        Thread.sleep(800);
        long writeTime = System.currentTimeMillis();
        Files.writeString(companyDir.resolve("auto.xml"), "<Order id=\"A\"></Order>");

        Thread.sleep(1000);

        assertTrue(detected.contains("auto.xml"), "Should detect file in subdirectory");
        long delay = processTime.get() - writeTime;
        assertTrue(delay >= 450, "Delay was: " + delay + "ms");

        watcher.stopWatching();
    }

    @Test
    @DisplayName("Prevent duplicate imports using isOrderAlreadyLoaded()")
    void testFR35_PreventDuplicates() throws IOException, ReflectiveOperationException {
        MainController controller = new MainController();

        // Access private tracker
        Field trackerField = MainController.class.getDeclaredField("tracker");
        trackerField.setAccessible(true);
        OrderTracker tracker = (OrderTracker) trackerField.get(controller);

        // Add order
        Order order = new Order("DUP-001", null, null, null);
        order.setSource("DoorDash");
        tracker.addOrder(order);

        // Call private method
        Method method = MainController.class.getDeclaredMethod("isOrderAlreadyLoaded", String.class, String.class);
        method.setAccessible(true);

        assertTrue((boolean) method.invoke(controller, "DUP-001.xml", "DoorDash"));
        assertFalse((boolean) method.invoke(controller, "NEW-001.xml", "DoorDash"));
    }
}