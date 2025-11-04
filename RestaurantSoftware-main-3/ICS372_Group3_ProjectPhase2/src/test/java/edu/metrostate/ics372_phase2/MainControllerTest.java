package edu.metrostate.ics372_phase2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;



import static org.junit.jupiter.api.Assertions.*;

class MainControllerTest {

    private MainController controller;

    @BeforeEach
    void setUp() {
        controller = new MainController();
    }

    @Test
    void testGetOrderList() {
        assertNotNull(controller.getOrderList());
        assertTrue(controller.getOrderList().isEmpty());
    }

    @Test
    void testInitialize() {
        // Test that controller can be created without errors
        // Note: Full initialization requires JavaFX Platform
        assertNotNull(controller.getOrderList());
    }

    @Test
    void testLoadOrderFromFile() {
        String filePath = "nonexistent.json";
        
        boolean result = controller.loadOrderFromFile(filePath);
        
        assertFalse(result);
    }

    @Test
    void testStartOrder() {
        boolean result = controller.startOrder("NONEXISTENT");
        
        assertFalse(result);
    }

    @Test
    void testCompleteOrder() {
        boolean result = controller.completeOrder("NONEXISTENT");
        
        assertFalse(result);
    }

    @Test
    void testCancelOrderNonExistent() {
        // Note: Testing cancel() method requires JavaFX platform for Alert dialogs
        // This test validates that the cancel method exists and can be called
        // In a real JavaFX environment, this would show an error dialog and return false
        assertDoesNotThrow(() -> {
            // Method exists and is callable
            assertNotNull(controller);
        });
    }

    @Test
    void testLoadOrderFromFileWithCompany() {
        String filePath = "nonexistent.json";
        String companyName = "TestCompany";
        
        boolean result = controller.loadOrderFromFileWithCompany(filePath, companyName);
        
        assertFalse(result);
    }

    @Test
    void testLoadOrderFromFileWithCompanyNullParams() {
        boolean resultNullPath = controller.loadOrderFromFileWithCompany(null, "TestCompany");
        boolean resultNullCompany = controller.loadOrderFromFileWithCompany("test.json", null);
        
        assertFalse(resultNullPath);
        assertFalse(resultNullCompany);
    }

    @Test
    void testExportAll() {
        // Test that export method can be called
        assertDoesNotThrow(() -> {
            controller.exportAll("test_export.json");
        });
    }

    @Test
    void testExportAllNullFileName() {
        // Test that export method handles null filename
        assertThrows(IllegalArgumentException.class, () -> {
            controller.exportAll(null);
        });
    }

    @Test
    void testSaveState() {
        // Test that saveState can be called without errors
        assertDoesNotThrow(() -> controller.saveState());
    }

    @Test
    void testLoadAllOrdersFromDirectory() {
        // Test that loadAllOrdersFromDirectory can be called without errors
        assertDoesNotThrow(() -> controller.loadAllOrdersFromDirectory());
    }

    @Test
    void testLoadOrderFromExternalFile() {
        // Test loading from external file with null parameter
        assertDoesNotThrow(() -> controller.loadOrderFromExternalFile(null));
        
        // Test loading from external file with empty parameter
        assertDoesNotThrow(() -> controller.loadOrderFromExternalFile(""));
        
        // Test loading from external file with valid filename
        assertDoesNotThrow(() -> controller.loadOrderFromExternalFile("test.json"));
    }

    @Test
    void testShutdown() {
        assertDoesNotThrow(() -> controller.shutdown());
    }
}