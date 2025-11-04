package edu.metrostate.ics372_phase2;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Monitors a directory for new order files and automatically imports them.
 * Supports both JSON and XML file formats.
 */
public class DirectoryWatcher {
    
    private static final Logger logger = Logger.getLogger(DirectoryWatcher.class.getName());
    private final ExecutorService executor;
    private final Consumer<String> fileProcessor;
    private final Path watchDirectory;
    private WatchService watchService;
    private volatile boolean running = false;
    
    public DirectoryWatcher(String directoryPath, Consumer<String> fileProcessor) {
        this.watchDirectory = Paths.get(directoryPath);
        this.fileProcessor = fileProcessor;
        // Create executor with custom thread factory to ensure non-daemon threads
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "DirectoryWatcher-" + directoryPath);
            t.setDaemon(false); // Ensure this is not a daemon thread
            return t;
        });
    }
    
    /**
     * Starts monitoring the directory for new files.
     */
    public void startWatching() {
        if (running) {
            return;
        }
        
        try {
            // Create directory if it doesn't exist
            if (!Files.exists(watchDirectory)) {
                Files.createDirectories(watchDirectory);
                logger.info("Created watch directory: " + watchDirectory);
            } else {
                logger.info("Watch directory exists: " + watchDirectory);
            }
            
            watchService = FileSystems.getDefault().newWatchService();
            watchDirectory.register(watchService, 
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY);
            
            running = true;
            logger.info("Watch service initialized successfully for: " + watchDirectory);
            
            executor.submit(() -> {
                logger.info("Started watching directory: " + watchDirectory);
                while (running) {
                    try {
                        WatchKey key = watchService.take();
                        
                        for (WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind<?> kind = event.kind();
                            
                            if (kind == StandardWatchEventKinds.OVERFLOW) {
                                continue;
                            }
                            
                            @SuppressWarnings("unchecked")
                            WatchEvent<Path> ev = (WatchEvent<Path>) event;
                            Path filename = ev.context();
                            String fileName = filename.toString();
                            
                            // Only process JSON and XML files
                            if (fileName.toLowerCase().endsWith(".json") || 
                                fileName.toLowerCase().endsWith(".xml")) {
                                
                                logger.info("Detected new order file: " + fileName);
                                
                                // Add a small delay to ensure file is fully written
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    break;
                                }
                                
                                // Process the file
                                try {
                                    fileProcessor.accept(fileName);
                                } catch (Exception e) {
                                    logger.log(Level.WARNING, "Failed to process file: " + fileName, e);
                                }
                            }
                        }
                        
                        boolean valid = key.reset();
                        if (!valid) {
                            break;
                        }
                        
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (java.nio.file.ClosedWatchServiceException e) {
                        logger.log(Level.WARNING, "Watch service was closed unexpectedly", e);
                        break;
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Error in directory watcher", e);
                    }
                }
                logger.info("Directory watcher stopped");
            });
            
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to start directory watcher", e);
        }
    }
    
    /**
     * Stops monitoring the directory.
     */
    public void stopWatching() {
        running = false;
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error closing watch service", e);
            }
        }
        executor.shutdown();
    }
    
    /**
     * Checks if the watcher is currently running.
     */
    public boolean isRunning() {
        return running;
    }
}
