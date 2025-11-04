# Use Case: Import Orders from XML

## Use Case Name
Import Orders from XML

## Primary Actor
Restaurant Staff

## Preconditions
- Application is running
- XML files exist in "Incoming Orders/" directory
- DirectoryWatcher is monitoring subdirectories

## Success Conditions
Order is imported, stored in OrderTracker, displayed in UI, and persisted to orders_state.json.

## Main Success Scenario

| User Actions | System Actions |
|--------------|----------------|
| 1. Place XML file in company folder | |
| | 2. DirectoryWatcher detects file |
| | 3. XMLParser reads and parses XML |
| | 4. Extract order data (ID, type, items) |
| | 5. Create Order object |
| | 6. Set source to company name |
| | 7. Add order to OrderTracker |
| | 8. StateManager.saveState(orders) |
| | 9. Platform.runLater() refreshOrders() |
| 10. View order in dashboard | |

## Alternate Scenario: Malformed XML (Automatic Import)

| User Actions | System Actions |
|--------------|----------------|
| 1. Place malformed XML file | |
| | 2. Parser encounters errors |
| | 3. ErrorHandler logs warnings |
| | 4. Apply defaults (TOGO, qty=1, price=0.0) |
| | 5. Skip invalid items with try-catch |
| | 6. Create Order with recovered data |
| | 7. order.setSource(companyName) |
| | 8. OrderTracker.addOrder(order) |
| | 9. Platform.runLater() refreshOrders() |
| | 10. Log: "Auto-imported order from [Company]" |
| 11. View order in dashboard (no dialog shown) | |

## Alternate Scenario: Fatal Error

| User Actions | System Actions |
|--------------|----------------|
| 1. Place corrupted XML file | |
| | 2. Parser fails completely |
| | 3. Logger.severe("XML Fatal Error") |
| | 4. Return null |
| | 5. Logger.warning("Failed to parse file") |
| | 6. No order created or displayed |

## Functional Requirements

**1**: Support XML format import

**2**: Store data source

**3**: Handle malformed data gracefully

**4**: Auto-detect XML files in monitored directories

**5**: Prevent duplicate imports