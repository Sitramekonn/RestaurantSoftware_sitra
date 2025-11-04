package edu.metrostate.ics372_phase2;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * Main controller responsible for coordinating order management operations.
 * Follows Single Responsibility Principle by delegating specific tasks to appropriate methods.
 *
 * Responsibilities:
 * - Order lifecycle management
 * - File system monitoring coordination
 * - State persistence coordination
 * - UI synchronization
 */
public class MainController {

    // ====================== CONSTANTS & CONFIGURATION ======================
    private static final Logger logger = Logger.getLogger(MainController.class.getName());
    private static final String INCOMING_ORDERS_DIR = "ICS372_Group3_ProjectPhase2/Incoming Orders";
    private static final long REFRESH_INTERVAL_MS = 30000; // 30 seconds

    // ====================== CORE DEPENDENCIES ======================
    private final OrderTracker tracker = new OrderTracker();
    private final ObservableList<Order> orderList = FXCollections.observableArrayList();

    // ====================== MONITORING COMPONENTS ======================
    private final List<DirectoryWatcher> subdirectoryWatchers = new ArrayList<>();
    private Timer refreshTimer;

    // ====================== PUBLIC API ======================

    /**
     * Returns the observable list of orders for UI binding.
     * Encapsulates the internal order list while providing read access.
     */
    public ObservableList<Order> getOrderList() {
        return orderList;
    }

    /**
     * Initializes the controller with all required components.
     * Orchestrates the startup sequence following proper initialization order.
     */
    public void initialize() {
        initializeDataLayer();
        initializeMonitoring();
        validateDataIntegrity();
    }

    /**
     * Gracefully shuts down all controller components.
     * Ensures proper resource cleanup and state persistence.
     */
    public void shutdown() {
        shutdownMonitoring();
        persistCurrentState();
        logger.info("MainController shutdown completed");
    }

    // ====================== ORDER LIFECYCLE MANAGEMENT ======================

    /**
     * Starts processing of an order by ID.
     * Encapsulates business logic and ensures state consistency.
     */
    public boolean startOrder(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            logger.warning("Cannot start order: invalid order ID");
            return false;
        }

        boolean success = tracker.startOrder(orderId);
        if (success) {
            refreshOrders();
            persistCurrentState();
            logger.info("Successfully started order: " + orderId);
        } else {
            logger.warning("Failed to start order: " + orderId);
        }
        return success;
    }

    /**
     * Completes an order by ID.
     * Follows the same pattern as startOrder for consistency.
     */
    public boolean completeOrder(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            logger.warning("Cannot complete order: invalid order ID");
            return false;
        }

        boolean success = tracker.completeOrder(orderId);
        if (success) {
            refreshOrders();
            persistCurrentState();
            logger.info("Successfully completed order: " + orderId);
        } else {
            logger.warning("Failed to complete order: " + orderId);
        }
        return success;
    }

    /**
     * Cancels an order by ID with proper error handling.
     * Demonstrates proper exception handling and user feedback.
     */
    public boolean cancel(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            showError("Cannot cancel order: invalid order ID");
            return false;
        }

        try {
            tracker.cancelOrder(orderId);
            refreshOrders();
            persistCurrentState();
            logger.info("Successfully cancelled order: " + orderId);
            return true;
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
            logger.warning("Failed to cancel order: " + orderId + " - " + ex.getMessage());
            return false;
        }
    }

    // -------------------- customer comment --------------------
    public boolean addCustomerComment(String orderId, String notes) {
        if (orderId == null || orderId.trim().isEmpty()) {
            showError("Cannot save note: invalid order ID");
            return false;
        }
        Order order = tracker.getOrder(orderId);
        if (order == null) {
            showError("Order not found: " + orderId);
            return false;
        }
        // If your Order uses a different setter/name, change the next line only.
        order.setCustomerNotes(notes == null ? "" : notes.trim()); // <-- CHANGE HERE if your Order uses a different setter
        refreshOrders();
        persistCurrentState();
        logger.info("Saved customer note for order " + orderId);
        return true;
    }

    // -------------------- order priority --------------------
    public boolean setOrderPriority(String orderId, String priority) {
        if (orderId == null || orderId.trim().isEmpty()) {
            showError("Cannot set priority: invalid order ID");
            return false;
        }
        Order order = tracker.getOrder(orderId);
        if (order == null) {
            showError("Order not found: " + orderId);
            return false;
        }
        String p = (priority == null ? "Normal" : priority.trim());
        // If your Order uses a different setter/type (enum), change the next line only.
        order.setPriority(p); // <-- CHANGE HERE if your Order uses a different setter or an enum
        refreshOrders();
        persistCurrentState();
        logger.info("Set priority '" + p + "' for order " + orderId);
        return true;
    }

    // ====================== FILE OPERATIONS ======================

    /**
     * Loads a single order from a file path.
     * Demonstrates strategy pattern by delegating to appropriate parser.
     */
    public boolean loadOrderFromFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            logger.warning("Cannot load order: invalid file path");
            return false;
        }

        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            logger.warning("Cannot load order: file does not exist - " + filePath);
            return false;
        }

        Order order = parseOrderFile(file);
        if (order != null) {
            order.setSource(file.getName());
            tracker.addOrder(order);
            refreshOrders();
            persistCurrentState();
            logger.info("Successfully loaded order from: " + filePath);
            return true;
        }

        logger.warning("Failed to parse order file: " + filePath);
        return false;
    }

    /**
     * Loads an order from a file with explicit company source.
     * Follows Open/Closed Principle by extending base functionality.
     */
    public boolean loadOrderFromFileWithCompany(String filePath, String companyName) {
        if (filePath == null || companyName == null) {
            logger.warning("Cannot load order: invalid parameters");
            return false;
        }

        File file = new File(filePath);
        Order order = parseOrderFile(file);

        if (order != null) {
            order.setSource(companyName);
            tracker.addOrder(order);
            refreshOrders();
            persistCurrentState();
            logger.info("Successfully loaded order from " + companyName + ": " + file.getName());
            return true;
        }

        return false;
    }

    /**
     * Exports all orders to a specified file format.
     * Demonstrates strategy pattern for different export formats.
     */
    public void exportAll(String fileName) throws IOException {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("Export filename cannot be null or empty");
        }

        if (fileName.toLowerCase().endsWith(".xml")) {
            XMLParser.exportAllOrders(tracker.exportAllOrders(), fileName);
            logger.info("Exported orders to XML: " + fileName);
        } else {
            // Default to JSON export
            JSONParser.exportAllOrders(tracker.exportAllOrders(), fileName);
            logger.info("Exported orders to JSON: " + fileName);
        }
    }

    /**
     * Adds an order directly to the tracker.
     * This method is primarily for testing purposes.
     * @param order The order to add
     */
    public void addOrder(Order order) {
        tracker.addOrder(order);
        try {
            Platform.runLater(this::refreshOrders);
        } catch (IllegalStateException e) {
            // JavaFX not initialized, likely in test mode
            refreshOrders();
        }
    }

    /**
     * Saves a receipt for a specific order to a text file.
     * @param orderId The ID of the order to generate receipt for
     * @return The file path where the receipt was saved
     * @throws IOException If the receipt cannot be saved
     * @throws IllegalArgumentException If the order is not found
     */
    public String saveReceipt(String orderId) throws IOException {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }

        Order order = tracker.getOrder(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }

        // Create receipts directory if it doesn't exist
        File receiptsDir = new File("ICS372_Group3_ProjectPhase2/Receipts");
        if (!receiptsDir.exists()) {
            receiptsDir.mkdirs();
        }

        // Generate receipt filename
        String filename = String.format("receipt_%s_%s.txt",
                orderId,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

        File receiptFile = new File(receiptsDir, filename);

        // Generate and save receipt
        String receiptText = order.toReceiptText();
        java.nio.file.Files.write(receiptFile.toPath(), receiptText.getBytes());

        logger.info("Receipt saved for order " + orderId + " to: " + receiptFile.getAbsolutePath());
        return receiptFile.getAbsolutePath();
    }

    // ====================== PRIVATE INITIALIZATION METHODS ======================

    /**
     * Initializes the data layer by loading saved state.
     * Separated for clarity and testability.
     */
    private void initializeDataLayer() {
        loadSavedState();
        ensureDirectoryStructure();
    }

    /**
     * Initializes monitoring components.
     * Separated to follow Single Responsibility Principle.
     */
    private void initializeMonitoring() {
        startDirectoryWatching();
        startPeriodicRefresh();
    }

    /**
     * Validates data integrity after initialization.
     * Ensures system is in a consistent state.
     */
    private void validateDataIntegrity() {
        checkForNewOrdersInDirectory();
        if (orderList.isEmpty()) {
            loadAllOrdersFromDirectory();
        }
    }

    // ====================== PRIVATE STATE MANAGEMENT ======================


    /**
     * Loads saved state from previous sessions.
     * Follows Single Responsibility Principle by focusing only on state loading.
     */
    private void loadSavedState() {
        try {
            List<Order> savedOrders = StateManager.loadState();
            if (!savedOrders.isEmpty()) {
                savedOrders.forEach(tracker::addOrder);
                refreshOrders();
                logger.info("Successfully loaded " + savedOrders.size() + " orders from saved state");
            } else {
                logger.info("No saved state found, will load from directory");
            }
        } catch (Exception e) {
            logger.warning("Failed to load saved state: " + e.getMessage());
        }
    }

    /**
     * Persists current state to storage.
     * Encapsulates state management responsibility.
     */
    private void persistCurrentState() {
        try {
            StateManager.saveState(tracker.getAll());
            logger.fine("State persisted successfully");
        } catch (Exception e) {
            logger.warning("Failed to persist state: " + e.getMessage());
        }
    }

    /**
     * Legacy method for external state saving.
     * Delegates to private method following DRY principle.
     */
    public void saveState() {
        persistCurrentState();
    }

    /**
     * Refreshes the observable order list for UI synchronization.
     * Encapsulates UI update logic and ensures thread safety.
     */
    private void refreshOrders() {
        Platform.runLater(() -> orderList.setAll(tracker.getAll()));
    }

    // ====================== DIRECTORY STRUCTURE MANAGEMENT ======================

    /**
     * Ensures proper directory structure exists.
     * Separated for clarity and reusability.
     */
    private void ensureDirectoryStructure() {
        File orderDirectory = new File(INCOMING_ORDERS_DIR);
        if (!orderDirectory.exists()) {
            if (orderDirectory.mkdirs()) {
                createSampleCompanyDirectories();
                logger.info("Created directory structure: " + orderDirectory.getAbsolutePath());
            } else {
                logger.warning("Failed to create directory structure");
            }
        }
    }

    /**
     * Creates sample company directories for demonstration.
     * Follows Open/Closed Principle - easy to extend with new companies.
     */
    private void createSampleCompanyDirectories() {
        String[] companies = {"DoorDash", "GrubHub", "UberEats", "Direct"};
        File baseDir = new File(INCOMING_ORDERS_DIR);

        for (String company : companies) {
            File companyDir = new File(baseDir, company);
            if (companyDir.mkdirs()) {
                logger.info("Created company directory: " + company);
            }
        }
    }

    // ====================== FILE PARSING STRATEGY ======================

    /**
     * Parses an order file using appropriate strategy based on file type.
     * Demonstrates Strategy Pattern and Open/Closed Principle.
     */
    private Order parseOrderFile(File file) {
        if (file == null || !file.exists()) {
            return null;
        }

        String fileName = file.getName().toLowerCase();
        String filePath = file.getAbsolutePath();

        try {
            if (fileName.endsWith(".json")) {
                return JSONParser.readOrderFromExternalFile(filePath);
            } else if (fileName.endsWith(".xml")) {
                return XMLParser.readOrderFromExternalFile(filePath);
            } else {
                logger.warning("Unsupported file format: " + fileName);
                return null;
            }
        } catch (Exception e) {
            logger.warning("Failed to parse file " + fileName + ": " + e.getMessage());
            return null;
        }
    }

    // ====================== MONITORING SYSTEM ======================

    /**
     * Starts periodic refresh monitoring.
     * Encapsulates timer management with proper error handling.
     */
    private void startPeriodicRefresh() {
        try {
            refreshTimer = new Timer("OrderRefreshTimer", true);
            refreshTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        checkForNewOrdersInDirectory();
                        logger.fine("Periodic refresh completed");
                    });
                }
            }, REFRESH_INTERVAL_MS, REFRESH_INTERVAL_MS);

            logger.info("Started periodic refresh timer (every " + (REFRESH_INTERVAL_MS / 1000) + " seconds)");
        } catch (Exception e) {
            logger.warning("Failed to start periodic refresh: " + e.getMessage());
        }
    }

    /**
     * Starts directory watching for real-time file detection.
     * Encapsulates directory watcher management.
     */
    private void startDirectoryWatching() {
        Platform.runLater(() -> {
            try {
                Thread.sleep(1000); // Allow JavaFX initialization

                File mainDir = new File(INCOMING_ORDERS_DIR);
                if (mainDir.exists() && mainDir.isDirectory()) {
                    File[] subdirs = mainDir.listFiles(File::isDirectory);
                    if (subdirs != null) {
                        for (File subdir : subdirs) {
                            createDirectoryWatcher(subdir);
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warning("Directory watcher startup interrupted");
            } catch (Exception e) {
                logger.warning("Failed to start directory watching: " + e.getMessage());
            }
        });
    }

    /**
     * Creates and configures a directory watcher for a specific subdirectory.
     * Follows Single Responsibility Principle by separating watcher creation.
     */
    private void createDirectoryWatcher(File subdir) {
        try {
            String subdirPath = subdir.getPath();
            String companyName = subdir.getName();

            DirectoryWatcher watcher = new DirectoryWatcher(subdirPath,
                    fileName -> handleFileDetected(fileName, companyName));

            watcher.startWatching();
            subdirectoryWatchers.add(watcher);
            logger.info("Started watching subdirectory: " + subdirPath);
        } catch (Exception e) {
            logger.warning("Failed to create directory watcher for " + subdir.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Handles file detection events from directory watchers.
     * Encapsulates real-time file processing logic.
     */
    private void handleFileDetected(String fileName, String companyName) {
        try {
            String fullPath = INCOMING_ORDERS_DIR + File.separator + companyName + File.separator + fileName;
            File orderFile = new File(fullPath);

            if (orderFile.exists() && !isOrderAlreadyLoaded(fileName, companyName)) {
                boolean loadSuccess = loadOrderFromFileWithCompany(orderFile.getAbsolutePath(), companyName);

                if (loadSuccess) {
                    logger.info("Real-time detected and loaded: " + fileName + " from " + companyName);
                    Platform.runLater(this::refreshOrders);
                }
            }
        } catch (Exception e) {
            logger.warning("Failed to handle detected file " + fileName + ": " + e.getMessage());
        }
    }

    /**
     * Checks if an order is already loaded to prevent duplicates.
     * Encapsulates duplicate detection logic.
     */
    private boolean isOrderAlreadyLoaded(String fileName, String companyName) {
        String fileNameWithoutExt = fileName.replaceFirst("\\.[^.]+$", "");
        return tracker.getAll().stream()
                .anyMatch(order -> companyName.equals(order.getSource()) &&
                        order.getOrderId().equals(fileNameWithoutExt));
    }

    /**
     * Stops all monitoring components gracefully.
     * Ensures proper resource cleanup.
     */
    private void shutdownMonitoring() {
        // Stop directory watchers
        subdirectoryWatchers.forEach(DirectoryWatcher::stopWatching);
        subdirectoryWatchers.clear();

        // Stop refresh timer
        if (refreshTimer != null) {
            refreshTimer.cancel();
            refreshTimer = null;
            logger.info("Stopped monitoring components");
        }
    }

    // ====================== DIRECTORY SCANNING ======================

    /**
     * Scans directories for new orders that haven't been loaded yet.
     * Encapsulates directory scanning logic with error handling.
     */
    private void checkForNewOrdersInDirectory() {
        try {
            File orderDirectory = new File(INCOMING_ORDERS_DIR);

            if (!orderDirectory.exists() || !orderDirectory.isDirectory()) {
                logger.fine("Incoming Orders directory doesn't exist, skipping scan");
                return;
            }

            File[] companyFolders = orderDirectory.listFiles(File::isDirectory);
            if (companyFolders == null || companyFolders.length == 0) {
                logger.fine("No company subdirectories found");
                return;
            }

            int newOrdersCount = scanCompanyFolders(companyFolders);

            if (newOrdersCount > 0) {
                logger.info("Found and loaded " + newOrdersCount + " new orders");
                persistCurrentState();
            }
        } catch (Exception e) {
            logger.warning("Error during directory scan: " + e.getMessage());
        }
    }

    /**
     * Scans individual company folders for new order files.
     * Follows Single Responsibility Principle by focusing on folder scanning.
     */
    private int scanCompanyFolders(File[] companyFolders) {
        int newOrdersCount = 0;

        for (File companyFolder : companyFolders) {
            String companyName = companyFolder.getName();
            File[] orderFiles = companyFolder.listFiles();

            if (orderFiles != null) {
                newOrdersCount += scanOrderFiles(orderFiles, companyName);
            }
        }

        return newOrdersCount;
    }

    /**
     * Scans order files within a company folder.
     * Encapsulates file-level scanning logic.
     */
    private int scanOrderFiles(File[] orderFiles, String companyName) {
        int count = 0;

        for (File orderFile : orderFiles) {
            if (isValidOrderFile(orderFile) && !isFileAlreadyLoaded(orderFile, companyName)) {
                if (loadOrderFromFileWithCompany(orderFile.getAbsolutePath(), companyName)) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Validates if a file is a supported order file.
     * Encapsulates file validation logic.
     */
    private boolean isValidOrderFile(File file) {
        if (file == null || !file.isFile()) {
            return false;
        }

        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".json") || fileName.endsWith(".xml");
    }

    /**
     * Checks if a file has already been loaded to prevent duplicates.
     * More robust duplicate detection than the simple version.
     */
    private boolean isFileAlreadyLoaded(File orderFile, String companyName) {
        return tracker.getAll().stream()
                .anyMatch(order -> companyName.equals(order.getSource()) &&
                        orderFile.getAbsolutePath().contains(orderFile.getName()));
    }

    /**
     * Loads all orders from the directory structure.
     * Provides comprehensive directory loading for initialization.
     */
    public void loadAllOrdersFromDirectory() {
        try {
            File orderDirectory = new File(INCOMING_ORDERS_DIR);

            if (!orderDirectory.exists()) {
                ensureDirectoryStructure();
                return;
            }

            checkForNewOrdersInDirectory();
            logger.info("Completed loading all orders from directory");
        } catch (Exception e) {
            logger.warning("Failed to load orders from directory: " + e.getMessage());
        }
    }

    // ====================== LEGACY/EXTERNAL METHODS ======================

    /**
     * Legacy method for external file loading.
     * Maintained for backward compatibility but delegates to improved implementation.
     */
    public void loadOrderFromExternalFile(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            logger.warning("Cannot load external file: invalid filename");
            return;
        }

        String fullPath = INCOMING_ORDERS_DIR + "/" + fileName;
        File file = new File(fullPath);

        // Determine company from parent directory
        String companyName = determineCompanyFromPath(file);

        Order order = parseOrderFile(file);
        if (order != null) {
            order.setSource(companyName + " (auto-imported)");

            Platform.runLater(() -> {
                tracker.addOrder(order);
                refreshOrders();
                persistCurrentState();
                logger.info("Auto-imported order from: " + companyName);
            });
        }
    }

    /**
     * Determines company name from file path.
     * Encapsulates path analysis logic.
     */
    private String determineCompanyFromPath(File file) {
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.getName().equals(INCOMING_ORDERS_DIR)) {
            return parentDir.getName();
        }
        return "Unknown";
    }

    // ====================== ERROR HANDLING ======================

    /**
     * Shows error dialog to user.
     * Encapsulates error presentation logic.
     */
    private void showError(String message) {
        try {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Operation Failed");
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            // Fallback for environments where JavaFX dialogs aren't available
            logger.severe("Error (dialog failed to show): " + message);
        }
    }
}
