package edu.metrostate.ics372_phase2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DirectoryWatcher class.
 * Tests the real-time file monitoring functionality.
 */
public class DirectoryWatcherTest {

    @TempDir
    Path tempDir;
    
    private DirectoryWatcher directoryWatcher;
    private AtomicReference<String> processedFile;
    private CountDownLatch fileLatch;

    @BeforeEach
    void setUp() {
        processedFile = new AtomicReference<>();
        fileLatch = new CountDownLatch(1);
        
        // Create DirectoryWatcher with test callback
        directoryWatcher = new DirectoryWatcher(tempDir.toString(), fileName -> {
            processedFile.set(fileName);
            fileLatch.countDown();
        });
    }

    @AfterEach
    void tearDown() {
        if (directoryWatcher != null) {
            directoryWatcher.stopWatching();
        }
    }

    @Test
    @Timeout(10)
    void testDirectoryWatcherInitialization() {
        assertNotNull(directoryWatcher);
        assertFalse(directoryWatcher.isRunning());
    }

    @Test
    @Timeout(10)
    void testStartWatching() {
        directoryWatcher.startWatching();
        
        // Give some time for the watcher to start
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        assertTrue(directoryWatcher.isRunning());
    }

    @Test
    @Timeout(15)
    void testDetectJsonFileCreation() throws IOException, InterruptedException {
        directoryWatcher.startWatching();
        
        // Wait for watcher to initialize
        Thread.sleep(1000);
        
        // Create a JSON file
        Path jsonFile = tempDir.resolve("test-order.json");
        String jsonContent = """
            {
                "orderId": "test-001",
                "customerName": "Test Customer",
                "items": [
                    {
                        "name": "Test Item",
                        "price": 10.99,
                        "quantity": 1
                    }
                ],
                "total": 10.99
            }
            """;
        
        Files.write(jsonFile, jsonContent.getBytes());
        
        // Wait for file to be processed
        assertTrue(fileLatch.await(5, TimeUnit.SECONDS), "File should be detected within 5 seconds");
        assertEquals("test-order.json", processedFile.get());
    }

    @Test
    @Timeout(10)
    void testStopWatching() throws InterruptedException {
        directoryWatcher.startWatching();
        Thread.sleep(500); // Let it start
        
        assertTrue(directoryWatcher.isRunning());
        
        directoryWatcher.stopWatching();
        Thread.sleep(500); // Let it stop
        
        assertFalse(directoryWatcher.isRunning());
    }

    @Test
    @Timeout(10)
    void testWatcherWithNonExistentDirectory() {
        Path nonExistentDir = tempDir.resolve("nonexistent");
        DirectoryWatcher watcher = new DirectoryWatcher(nonExistentDir.toString(), fileName -> {});
        
        // Should not throw exception and should create directory
        assertDoesNotThrow(() -> watcher.startWatching());
        
        try {
            Thread.sleep(500);
            assertTrue(Files.exists(nonExistentDir), "Directory should be created");
            assertTrue(watcher.isRunning());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            watcher.stopWatching();
        }
    }
}