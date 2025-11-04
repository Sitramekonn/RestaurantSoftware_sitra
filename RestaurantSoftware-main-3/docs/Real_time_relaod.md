**Use Case Name**: Reload Orders in Real-Time

**Primary Actor**: Restaurant Staff

**Stakeholders and Interests:**
- **Restaurant Staff** – ensuring new orders are detected automatically without manual intervention
- **Kitchen Staff** – getting real-time order updates for efficient meal preparation  
- **Restaurant Manager** – ensuring automated operations to reduce labor costs and improve efficiency
- **Customers** – expect prompt order acknowledgment and accurate delivery times

**Preconditions:**
- Restaurant Management System is running and active.
- External order directories are configured and accessible.
- Order file monitoring service is initialized.
- GUI interface is displayed and responsive.

**Success Conditions:** Restaurant staff has real-time visibility of new orders automatically loaded into the system without manual refresh.

**Frequency of Occurrence:** Continuous during restaurant operating hours

**Main Success Scenario:**

|  User Actions                                     |  System Actions                                                        |
|-----------------------------------------------------|--------------------------------------------------------------------------|
| **1.** Restaurant staff operates the system normally | **1.1** System starts DirectoryWatcher service for each configured directory |
|                                                     | **1.2** File monitoring begins for JSON and XML order files               |
|                                                     | **1.3** GUI initializes with current order list display                  |
| ═══════════════════════════════════════════════════ | ════════════════════════════════════════════════════════════════════════ |
| **2.** External delivery platform places new order | **2.1** System detects new order file creation within 1 second           |
|                                                     | **2.2** System validates file format and accessibility                   |
|                                                     | **2.3** Jackson parser reads and deserializes order data from JSON/XML   |
|                                                     | **2.4** System validates order completeness and business rules           |
| ═══════════════════════════════════════════════════ | ════════════════════════════════════════════════════════════════════════ |
| **3.** Staff views updated order list              | **3.1** New order is added to ObservableList data structure              |
|                                                     | **3.2** JavaFX Platform.runLater() ensures thread-safe GUI updates       |
|                                                     | **3.3** Order list refreshes automatically without user intervention     |
|                                                     | **3.4** System shows new order in the queue with visual confirmation     |

**Alternate Scenarios:**

###  **Scenario 1: Corrupted File Handling**

|  User Actions                                       |  System Actions                                                      |
|-----------------------------------------------------|------------------------------------------------------------------------|
| **1.1** External delivery platform creates corrupted file | **1.1** System detects file processing error                           |
|                                                     | **1.2** System logs parsing error with file details                   |
|                                                     | **1.3** Error notification displayed to staff                         |
| ═══════════════════════════════════════════════════ | ══════════════════════════════════════════════════════════════════════ |
| **1.2** Staff acknowledges error notification       | **1.4** Corrupted file moved to error directory for manual review     |
|                                                     | **1.5** System continues monitoring for valid orders                  |

###  **Scenario 2: Duplicate Order Detection**

|  User Actions                                       |  System Actions                                                      |
|-----------------------------------------------------|------------------------------------------------------------------------|
| **2.1** External delivery platform sends duplicate order | **2.1** System compares order ID against existing orders               |
|                                                     | **2.2** Duplicate order is rejected and logged                        |
|                                                     | **2.3** No GUI update occurs for duplicate                            |
|                                                     | **2.4** Original order data remains unchanged                         |

**Functional Requirements:**

1. **The software shall detect new order files created in monitored directories within 1 second of file creation.**

2. **The software shall support both JSON and XML file formats for order processing using Jackson parser libraries.**

3. **The software shall prevent duplicate orders by comparing order IDs against existing orders in the system.**

4. **The software shall automatically refresh the GUI order list without requiring manual user intervention when new valid orders are detected.**

5. **The software shall move corrupted or unreadable order files to an error directory and continue monitoring for valid orders.**

**The software shall automatically detect and process new order files without requiring manual user intervention.**