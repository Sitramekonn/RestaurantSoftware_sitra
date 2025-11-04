package edu.metrostate.ics372_phase2;

import javafx.application.Application;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;

public class MainApp extends Application {

    private final MainController controller = new MainController();

    // FXML-injected controls
    @FXML private FlowPane ordersContainer;
    @FXML private Button btnExport;
    @FXML private Label statsLabel;
    @FXML private Label revenueLabel;
    @FXML private Button btnFilterAll;
    @FXML private Button btnFilterIncoming;
    @FXML private Button btnFilterStarted;
    @FXML private Button btnFilterCompleted;
    @FXML private Button btnFilterCancelled;
    @FXML private Button btnFilterSource;

    private Stage primaryStage;
    private OrderStatus currentFilter = null; // null means show all
    private boolean filterBySource = false;

    private static final String PREF_X = "window.x";
    private static final String PREF_Y = "window.y";
    private static final String PREF_W = "window.w";
    private static final String PREF_H = "window.h";
    private static final String PREF_MAX = "window.max";

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("Restaurant Order Management");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/main-view.fxml"));
            loader.setController(this);
            Parent root = loader.load();
            Scene scene = new Scene(root, 900, 500);
            // Apply stylesheet programmatically to avoid FXML URL parsing issues
            String css = getClass().getResource("/ui/styles.css").toExternalForm();
            scene.getStylesheets().add(css);
            stage.setScene(scene);
            restoreWindowState(stage);
            stage.show();
            stage.setOnCloseRequest(e -> {
                saveWindowState(stage);
                controller.shutdown(); // Cleanup and save state
            });

            // Initialize controller (loads saved state and starts directory watching)
            controller.initialize();
        } catch (IOException e) {
            e.printStackTrace();
            // Fallback minimal UI if FXML fails
            BorderPane fallback = new BorderPane(new Label("Failed to load UI"));
            stage.setScene(new Scene(fallback, 800, 400));
            stage.show();
        }
    }

    @Override
    public void stop() {
        if (primaryStage != null) {
            saveWindowState(primaryStage);
        }
    }

    @FXML
    private void initialize() {
        if (ordersContainer == null) {
            return;
        }

        // Set up the orders container and listen for changes
        controller.getOrderList().addListener((ListChangeListener<Order>) change -> {
            refreshOrderCards();
            updateStatistics();
        });

        // Set up export button
        if (btnExport != null) {
            btnExport.setOnAction(e -> showExportDialog());
        }

        // Set up filter buttons
        setupFilterButtons();

        // Initial refresh of order cards and statistics
        refreshOrderCards();
        updateStatistics();
    }

    private void setupFilterButtons() {
        if (btnFilterAll != null) {
            btnFilterAll.setOnAction(e -> {
                currentFilter = null;
                filterBySource = false;
                updateFilterButtonStyles();
                refreshOrderCards();
            });
        }

        if (btnFilterIncoming != null) {
            btnFilterIncoming.setOnAction(e -> {
                currentFilter = OrderStatus.INCOMING;
                filterBySource = false;
                updateFilterButtonStyles();
                refreshOrderCards();
            });
        }

        if (btnFilterStarted != null) {
            btnFilterStarted.setOnAction(e -> {
                currentFilter = OrderStatus.STARTED;
                filterBySource = false;
                updateFilterButtonStyles();
                refreshOrderCards();
            });
        }

        if (btnFilterCompleted != null) {
            btnFilterCompleted.setOnAction(e -> {
                currentFilter = OrderStatus.COMPLETED;
                filterBySource = false;
                updateFilterButtonStyles();
                refreshOrderCards();
            });
        }

        if (btnFilterCancelled != null) {
            btnFilterCancelled.setOnAction(e -> {
                currentFilter = OrderStatus.CANCELLED;
                filterBySource = false;
                updateFilterButtonStyles();
                refreshOrderCards();
            });
        }

        if (btnFilterSource != null) {
            btnFilterSource.setOnAction(e -> {
                currentFilter = null;
                filterBySource = !filterBySource;
                updateFilterButtonStyles();
                refreshOrderCards();
            });
        }
    }

    private void updateFilterButtonStyles() {
        // Remove active class from all buttons
        if (btnFilterAll != null) btnFilterAll.getStyleClass().remove("filter-button-active");
        if (btnFilterIncoming != null) btnFilterIncoming.getStyleClass().remove("filter-button-active");
        if (btnFilterStarted != null) btnFilterStarted.getStyleClass().remove("filter-button-active");
        if (btnFilterCompleted != null) btnFilterCompleted.getStyleClass().remove("filter-button-active");
        if (btnFilterCancelled != null) btnFilterCancelled.getStyleClass().remove("filter-button-active");
        if (btnFilterSource != null) btnFilterSource.getStyleClass().remove("filter-button-active");

        // Add active class to current filter
        if (filterBySource && btnFilterSource != null) {
            btnFilterSource.getStyleClass().add("filter-button-active");
        } else if (currentFilter == null && btnFilterAll != null) {
            btnFilterAll.getStyleClass().add("filter-button-active");
        } else if (currentFilter == OrderStatus.INCOMING && btnFilterIncoming != null) {
            btnFilterIncoming.getStyleClass().add("filter-button-active");
        } else if (currentFilter == OrderStatus.STARTED && btnFilterStarted != null) {
            btnFilterStarted.getStyleClass().add("filter-button-active");
        } else if (currentFilter == OrderStatus.COMPLETED && btnFilterCompleted != null) {
            btnFilterCompleted.getStyleClass().add("filter-button-active");
        } else if (currentFilter == OrderStatus.CANCELLED && btnFilterCancelled != null) {
            btnFilterCancelled.getStyleClass().add("filter-button-active");
        }
    }

    private void updateStatistics() {
        var orders = controller.getOrderList();
        long total = orders.size();
        long incoming = orders.stream().filter(o -> o.getStatus() == OrderStatus.INCOMING).count();
        long started = orders.stream().filter(o -> o.getStatus() == OrderStatus.STARTED).count();
        long completed = orders.stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).count();
        long cancelled = orders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();
        double totalRevenue = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .mapToDouble(Order::getTotalPrice)
                .sum();

        if (statsLabel != null) {
            statsLabel.setText(String.format(
                    "Orders: %d Total | %d Incoming | %d Started | %d Completed | %d Cancelled",
                    total, incoming, started, completed, cancelled));
        }

        if (revenueLabel != null) {
            revenueLabel.setText(String.format("Total Revenue: $%.2f", totalRevenue));
        }
    }

    private void refreshOrderCards() {
        if (ordersContainer == null) return;

        ordersContainer.getChildren().clear();
        // Apply filtering
        List<Order> filteredOrders = controller.getOrderList().stream()
                .filter(order -> {
                    // Hide cancelled orders by default (unless specifically filtering for them)
                    if (order.getStatus() == OrderStatus.CANCELLED && currentFilter != OrderStatus.CANCELLED) {
                        return false;
                    }
                    // Apply status filter
                    if (currentFilter != null && order.getStatus() != currentFilter) {
                        return false;
                    }
                    return true;
                })
                .sorted((o1, o2) -> {
                    if (filterBySource) {
                        // Group by source first, then by time
                        int sourceCompare = o1.getSource().compareTo(o2.getSource());
                        if (sourceCompare != 0) return sourceCompare;
                    }
                    // Null-safe comparison for order times
                    if (o1.getOrderTime() == null && o2.getOrderTime() == null) return 0;
                    if (o1.getOrderTime() == null) return 1; // null comes last
                    if (o2.getOrderTime() == null) return -1; // null comes last
                    return o2.getOrderTime().compareTo(o1.getOrderTime()); // Newest first
                })
                .toList();

        // Group by source if filtering by source
        if (filterBySource) {
            String currentSource = null;
            for (Order order : filteredOrders) {
                // Add source header if this is a new source
                if (!order.getSource().equals(currentSource)) {
                    currentSource = order.getSource();
                    Label sourceHeader = new Label("Source: " + currentSource);
                    sourceHeader.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 10 0 5 0;");
                    ordersContainer.getChildren().add(sourceHeader);
                }

                VBox orderCard = createOrderCard(order);
                ordersContainer.getChildren().add(orderCard);
            }
        } else {
            // Normal card display
            for (Order order : filteredOrders) {
                VBox orderCard = createOrderCard(order);
                ordersContainer.getChildren().add(orderCard);
            }
        }
    }

    private VBox createOrderCard(Order order) {
        VBox card = new VBox(8);
        card.getStyleClass().add("order-card");
        card.setAlignment(Pos.TOP_LEFT);

        // Add order-type specific styling
        String orderTypeClass = "order-type-" + order.getOrderType().toString().toLowerCase();
        card.getStyleClass().add(orderTypeClass);

        // Header with Order ID and type icon
        HBox headerBox = new HBox(8);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label headerLabel = new Label("Order #" + order.getOrderId());
        headerLabel.getStyleClass().add("order-card-header");

        // Add visual indicator for order type
        Label typeIndicator = new Label(getOrderTypeIcon(order.getOrderType()));
        typeIndicator.getStyleClass().addAll("order-type-indicator", "type-" + order.getOrderType().toString().toLowerCase());
        typeIndicator.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        headerBox.getChildren().addAll(headerLabel, typeIndicator);

        // Status badge
        Label statusLabel = new Label(order.getStatus().toString());
        statusLabel.getStyleClass().addAll("order-card-status", "status-" + order.getStatus().toString().toLowerCase());

        // Order details with null-safe time formatting
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        String timeText = order.getOrderTime() != null ?
                order.getOrderTime().format(timeFormatter) :
                "Time not specified";
        Label timeLabel = new Label("Time: " + timeText);
        timeLabel.getStyleClass().add("order-card-details");

        // Enhanced type label with description
        String typeDescription = getOrderTypeDescription(order.getOrderType());
        Label typeLabel = new Label("Type: " + order.getOrderType().toString() + " " + typeDescription);
        typeLabel.getStyleClass().addAll("order-card-details", "order-type-label");

        // Items summary
        int totalItems = order.getItems().stream().mapToInt(OrderItem::quantity).sum();
        Label itemsLabel = new Label("Items: " + totalItems + " item(s)");
        itemsLabel.getStyleClass().add("order-card-details");

        // Total price
        Label totalLabel = new Label(String.format("Total: $%.2f", order.getTotalPrice()));
        totalLabel.getStyleClass().add("order-card-details");

        // Source information
        Label sourceLabel = new Label("Source: " + order.getSource());
        sourceLabel.getStyleClass().add("order-card-source");

        card.getChildren().addAll(headerBox, statusLabel, timeLabel, typeLabel, itemsLabel, totalLabel, sourceLabel);

        // Add click handler
        card.setOnMouseClicked(e -> showOrderActionDialog(order));

        return card;
    }

    /**
     * Returns an icon/emoji for the order type for visual identification.
     */
    private String getOrderTypeIcon(OrderType orderType) {
        switch (orderType) {
            case TOGO:
                return "🥡"; // takeout box
            case PICKUP:
                return "🏪"; // convenience store
            case DELIVERY:
                return "🚗"; // car for delivery
            default:
                return "📦"; // package
        }
    }

    /**
     * Returns a description for the order type.
     */
    private String getOrderTypeDescription(OrderType orderType) {
        switch (orderType) {
            case TOGO:
                return "(To Go)";
            case PICKUP:
                return "(Pickup)";
            case DELIVERY:
                return "(Delivery)";
            default:
                return "";
        }
    }

    private void showOrderActionDialog(Order order) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Order Actions");
        dialog.setHeaderText("Order #" + order.getOrderId() + " - " + order.getStatus());

        // Content
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        Label orderInfo = new Label(String.format(
                "Type: %s\nTime: %s\nItems: %d\nTotal: $%.2f",
                order.getOrderType(),
                order.getOrderTime() == null ? "Time not specified"
                        : order.getOrderTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                order.getItems().stream().mapToInt(OrderItem::quantity).sum(),
                order.getTotalPrice()
        ));
        content.getChildren().add(orderInfo);

        // Items
        Label itemsHeader = new Label("Items:");
        itemsHeader.setStyle("-fx-font-weight: bold;");
        content.getChildren().add(itemsHeader);

        for (OrderItem item : order.getItems()) {
            Label itemLabel = new Label(String.format("• %s (x%d) - $%.2f",
                    item.name(), item.quantity(), item.price() * item.quantity()));
            content.getChildren().add(itemLabel);
        }

        content.getChildren().add(new Separator());

        // ---- Your added UI: Priority + Customer Notes ----
        Label prLabel = new Label("Priority:");
        ChoiceBox<String> priorityBox = new ChoiceBox<>();
        priorityBox.getItems().addAll("High", "Normal", "Low");
        priorityBox.setValue(order.getPriority() == null ? "Normal" : order.getPriority());

        Label notesLabel = new Label("Customer Notes:");
        TextArea notesArea = new TextArea(order.getCustomerNotes());
        notesArea.setPromptText("Add special instructions (e.g., no onions, extra cheese)...");
        notesArea.setPrefRowCount(3);

        content.getChildren().addAll(prLabel, priorityBox, notesLabel, notesArea);
        // --------------------------------------------------

        dialog.getDialogPane().setContent(content);

        // Buttons by status (team behavior + your Save/Receipt)
        switch (order.getStatus()) {
            case INCOMING: {
                ButtonType startButton        = new ButtonType("Start Order");
                ButtonType cancelButton       = new ButtonType("Cancel Order");
                ButtonType saveButton         = new ButtonType("Save");
                ButtonType receiptButton      = new ButtonType("Save Receipt");

                dialog.getDialogPane().getButtonTypes().addAll(
                        saveButton, receiptButton, startButton, cancelButton, ButtonType.CLOSE
                );

                Optional<ButtonType> result = dialog.showAndWait();
                if (result.isPresent()) {
                    if (result.get() == saveButton) {
                        order.setPriority(priorityBox.getValue());
                        order.setCustomerNotes(notesArea.getText());
                        StateManager.saveState(controller.getOrderList());
                        refreshOrderCards();
                        updateStatistics();
                    } else if (result.get() == receiptButton) {
                        saveReceipt(order);
                    } else if (result.get() == startButton) {
                        if (controller.startOrder(order.getOrderId())) {
                            showInfo("Order started successfully!");
                        } else {
                            showInfo("Failed to start order!");
                        }
                    } else if (result.get() == cancelButton) {
                        if (controller.cancel(order.getOrderId())) {
                            showInfo("Order canceled successfully!");
                            refreshOrderCards();
                        } else {
                            showInfo("Failed to cancel order!");
                        }
                    }
                }
                break;
            }

            case STARTED: {
                ButtonType completeButton     = new ButtonType("Complete Order");
                ButtonType cancelStartedButton= new ButtonType("Cancel Order");
                ButtonType saveButton         = new ButtonType("Save");
                ButtonType receiptButton      = new ButtonType("Save Receipt");

                dialog.getDialogPane().getButtonTypes().addAll(
                        saveButton, receiptButton, completeButton, cancelStartedButton, ButtonType.CLOSE
                );

                Optional<ButtonType> result = dialog.showAndWait();
                if (result.isPresent()) {
                    if (result.get() == saveButton) {
                        order.setPriority(priorityBox.getValue());
                        order.setCustomerNotes(notesArea.getText());
                        StateManager.saveState(controller.getOrderList());
                        refreshOrderCards();
                        updateStatistics();
                    } else if (result.get() == receiptButton) {
                        saveReceipt(order);
                    } else if (result.get() == completeButton) {
                        if (controller.completeOrder(order.getOrderId())) {
                            showInfo("Order completed successfully!");
                        } else {
                            showInfo("Failed to complete order!");
                        }
                    } else if (result.get() == cancelStartedButton) {
                        if (controller.cancel(order.getOrderId())) {
                            showInfo("Order canceled successfully!");
                            refreshOrderCards();
                        } else {
                            showInfo("Failed to cancel order!");
                        }
                    }
                }
                break;
            }

            case COMPLETED: {
                ButtonType saveButton    = new ButtonType("Save");
                ButtonType receiptButton = new ButtonType("Save Receipt");
                dialog.getDialogPane().getButtonTypes().addAll(saveButton, receiptButton, ButtonType.CLOSE);
                Optional<ButtonType> res = dialog.showAndWait();
                if (res.isPresent()) {
                    if (res.get() == saveButton) {
                        order.setPriority(priorityBox.getValue());
                        order.setCustomerNotes(notesArea.getText());
                        StateManager.saveState(controller.getOrderList());
                        refreshOrderCards();
                        updateStatistics();
                    } else if (res.get() == receiptButton) {
                        saveReceipt(order);
                    }
                }
                break;
            }

            case CANCELLED: {
                dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
                dialog.showAndWait();
                break;
            }
        }
    }

    // Write a TXT receipt for a single order (your feature)
    private void saveReceipt(Order order) {
        try {
            java.nio.file.Path dir = java.nio.file.Path.of("Receipts");
            java.nio.file.Files.createDirectories(dir);
            var file = dir.resolve("Order_" + order.getOrderId() + ".txt");
            java.nio.file.Files.writeString(file, order.toReceiptText());
            showInfo("Receipt saved: " + file.toAbsolutePath());
        } catch (Exception ex) {
            showInfo("Failed to save receipt: " + ex.getMessage());
        }
    }

    private void showExportDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Export Orders");
        dialog.setHeaderText("Choose export format and filename");

        // Create content
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        // Format selection
        Label formatLabel = new Label("Export Format:");
        formatLabel.setStyle("-fx-font-weight: bold;");

        ToggleGroup formatGroup = new ToggleGroup();
        RadioButton jsonRadio = new RadioButton("JSON (.json)");
        RadioButton xmlRadio = new RadioButton("XML (.xml)");
        jsonRadio.setToggleGroup(formatGroup);
        xmlRadio.setToggleGroup(formatGroup);
        jsonRadio.setSelected(true); // Default to JSON

        // Filename input
        Label nameLabel = new Label("Filename:");
        nameLabel.setStyle("-fx-font-weight: bold;");
        TextField nameField = new TextField("allOrders");
        nameField.setPrefWidth(200);

        // Info label
        Label infoLabel = new Label("Files will be saved to: ICS372_Group3_ProjectPhase2/Exported Orders");
        infoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

        content.getChildren().addAll(
                formatLabel, jsonRadio, xmlRadio,
                new Separator(),
                nameLabel, nameField,
                new Separator(),
                infoLabel
        );

        dialog.getDialogPane().setContent(content);

        ButtonType exportButton = new ButtonType("Export");
        dialog.getDialogPane().getButtonTypes().addAll(exportButton, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == exportButton) {
            String filename = nameField.getText().trim();
            if (filename.isEmpty()) {
                showInfo("Please enter a filename.");
                return;
            }

            // Add appropriate extension
            if (xmlRadio.isSelected()) {
                if (!filename.toLowerCase().endsWith(".xml")) {
                    filename += ".xml";
                }
            } else {
                if (!filename.toLowerCase().endsWith(".json")) {
                    filename += ".json";
                }
            }

            try {
                controller.exportAll(filename);
                String format = xmlRadio.isSelected() ? "XML" : "JSON";
                showInfo(String.format("Successfully exported orders to %s in %s format!", filename, format));
            } catch (IOException ex) {
                showInfo("Export failed: " + ex.getMessage());
            }
        }
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void saveWindowState(Stage stage) {
        Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
        if (!stage.isMaximized()) {
            prefs.putDouble(PREF_X, stage.getX());
            prefs.putDouble(PREF_Y, stage.getY());
            prefs.putDouble(PREF_W, stage.getWidth());
            prefs.putDouble(PREF_H, stage.getHeight());
        }
        prefs.putBoolean(PREF_MAX, stage.isMaximized());
    }

    private void restoreWindowState(Stage stage) {
        Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
        boolean maximized = prefs.getBoolean(PREF_MAX, false);
        double w = prefs.getDouble(PREF_W, 900);
        double h = prefs.getDouble(PREF_H, 500);
        double x = prefs.getDouble(PREF_X, Double.NaN);
        double y = prefs.getDouble(PREF_Y, Double.NaN);

        stage.setWidth(w);
        stage.setHeight(h);
        if (!Double.isNaN(x) && !Double.isNaN(y)) {
            stage.setX(x);
            stage.setY(y);
        }
        stage.setMaximized(maximized);
    }
}
