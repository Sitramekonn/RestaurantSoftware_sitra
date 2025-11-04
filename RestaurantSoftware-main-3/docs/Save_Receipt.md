# Use Case: Save Receipt

**Primary Actor:** Restaurant Staff

## Stakeholders and Interests
- **Restaurant Staff** – Generate and save receipts easily for order records
- **Restaurant Manager** – Maintain consistent records for audits and disputes
- **Customers** – Provide accurate, printable receipts when requested
- **System** – Maintain order transaction records

## Preconditions
- Order exists in the system (any status: INCOMING, STARTED, COMPLETED, CANCELLED)
- Application is running and responsive
- User has access to order management interface

## Success Conditions
A formatted `.txt` receipt is generated and saved in the `ICS372_Group3_ProjectPhase2/Receipts/` directory with confirmation showing the saved path.

## Frequency of Occurrence
As needed (typically once per order when requested, or for completed orders)

## Main Success Scenario

| **User Actions** | **System Actions** |
|---|---|
| 1. Staff clicks on an order card | 1.1 System opens the order action dialog and displays order details |
| 2. Staff clicks **Save Receipt** | 1.2 System calls MainController.saveReceipt(orderId) |
|  | 1.3 System retrieves Order object from OrderTracker |
|  | 1.4 System calls Order.toReceiptText() to generate formatted receipt |
|  | 1.5 System creates Receipts directory if it doesn't exist |
|  | 1.6 System generates timestamped filename (receipt_[orderId]_[timestamp].txt) |
|  | 1.7 System writes receipt text to file |
|  | 1.8 System displays success confirmation with saved file path |

## Alternative Scenarios

### Scenario 2: Order Not Found
| **User Actions** | **System Actions** |
|---|---|
| 1. Staff clicks **Save Receipt** for invalid order | 1.1 System throws IllegalArgumentException |
|  | 1.2 System displays error: "Order not found: [orderId]" |

### Scenario 3: File System Error
| **User Actions** | **System Actions** |
|---|---|
| 1. Staff clicks **Save Receipt** | 1.1 I/O error occurs while creating directory or writing file |
|  | 1.2 System catches IOException |
|  | 1.3 System displays error: "Failed to save receipt: [error message]" |
|  | 1.4 Error is logged for system monitoring |

### Scenario 4: Receipt for Different Order Statuses
| **User Actions** | **System Actions** |
|---|---|
| 1. Staff generates receipt for INCOMING order | 1.1 System generates receipt showing current status |
| 2. Staff generates receipt for STARTED order | 2.1 System generates receipt showing in-progress status |
| 3. Staff generates receipt for COMPLETED order | 3.1 System generates receipt showing final completed status |
| 4. Staff generates receipt for CANCELLED order | 4.1 System generates receipt showing cancelled status |

## Functional Requirements
1. **Receipt Generation**: Allow saving order receipts in formatted `.txt` format
2. **Status Flexibility**: Support receipt generation for orders in any status
3. **File Management**: Automatically create receipts directory if needed
4. **Unique Naming**: Generate timestamped filenames to prevent conflicts
5. **Success Confirmation**: Display saved file path upon successful save
6. **Error Handling**: Provide clear error messages for failures
7. **Order Validation**: Verify order exists before attempting receipt generation

## Non-Functional Requirements
1. **Performance**: Receipt generation and save should complete within 2 seconds
2. **Reliability**: File operations should be atomic and handle system errors gracefully
3. **Usability**: Clear success/error messages with actionable information
4. **Maintainability**: Receipt format should be easily modifiable
5. **Storage**: Receipt files should be human-readable text format

## Receipt Format Specification
```
=================================
       RESTAURANT RECEIPT
=================================

Order ID: [ORDER_ID]
Order Type: [TOGO|PICKUP|DELIVERY]
Order Time: [YYYY-MM-DD HH:MM:SS]
Status: [INCOMING|STARTED|COMPLETED|CANCELLED]
Source: [SOURCE_FILE_NAME]

ITEMS:
---------------------------------
[ITEM_NAME]         x[QTY]  $[TOTAL]
[ITEM_NAME]         x[QTY]  $[TOTAL]
...
---------------------------------
TOTAL: $[GRAND_TOTAL]
=================================
Thank you for your business!
```

## Dependencies
- **MainController**: Coordinates receipt save operation
- **OrderTracker**: Provides order retrieval functionality
- **Order**: Generates formatted receipt text
- **MainApp**: Handles UI interaction and user feedback
- **File System**: Directory creation and file writing operations