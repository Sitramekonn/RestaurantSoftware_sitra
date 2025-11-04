# Use Case Name: Cancel Order

**Primary Actor:** Restaurant Staff

**Stakeholders and Interests:**
- **Restaurant Staff** – need a fast, reliable way to cancel orders that cannot be fulfilled.
- **Kitchen Staff** – avoid preparing meals that will not be completed.
- **Restaurant Manager** – maintain operational accuracy, reduce waste, and ensure proper documentation.
- **Customers** – receive accurate and timely responses regarding their orders.

---

## Preconditions:
- Restaurant Management System is running and active.
- The order exists in the system and is assigned to this restaurant.
- The user is authenticated and authorized to cancel orders.
- GUI interface is open and responsive.

## Success Conditions:
Order is marked **Canceled**, removed from further processing, and visually updated in the user interface.

## Frequency of Occurrence:
As needed during restaurant operating hours.

---

## Main Success Scenario:

| User Actions | System Actions |
|-------------|----------------|
| **1.** Restaurant staff selects an existing order. | **1.1** System displays order details (ID, items, status, etc.). |
| | **1.2** System enables the **Cancel Order** option if cancelable. |
| **2.** Staff clicks **Cancel Order**. | **2.1** System prompts for cancellation confirmation (optional: reason field). |
| **3.** Staff confirms cancellation. | **2.2** System verifies that the order is not already **Completed**. |
| | **2.3** System updates order status to **Canceled**. |
| | **2.4** System records an audit entry (user, timestamp, order ID, optional reason). |
| **4.** Staff views updated order list. | **3.1** Canceled order is reflected in the observable order list. |
| | **3.2** JavaFX `Platform.runLater()` ensures safe UI refresh. |
| | **3.3** System visually marks the order as **Canceled** and disables further cancellation actions. |

---

## Alternate Scenarios:

### Scenario 1: Order Already Completed (Cancellation Blocked)

| User Actions | System Actions |
|-------------|----------------|
| **1.1** Staff attempts to cancel an order already in **Completed** state. | **1.1** System detects the order is not cancelable. |
| | **1.2** System displays an error: *"This order has already been completed and cannot be canceled."* |
| | **1.3** No state change occurs; audit may record the attempt. |

---

### Scenario 2: Order Already Canceled (Idempotent)

| User Actions | System Actions |
|-------------|----------------|
| **2.1** Staff clicks **Cancel Order** again on a canceled order. | **2.1** System detects status **Canceled** and performs no state change. |
| | **2.2** System optionally informs staff: *"Order is already canceled."* |

---

### Scenario 3: Order Not Found or Not Assigned

| User Actions | System Actions |
|-------------|----------------|
| **3.1** Staff attempts to cancel an order that has been removed or reassigned. | **3.1** System verifies that the order is not available or permitted. |
| | **3.2** System displays: *"Order not found or not assigned to this location."* |
| | **3.3** No state change occurs. |

---

## Functional Requirements:

1. **The software shall allow an order to be canceled only if it is not in the Completed state.**
2. **The software shall update the order status to Canceled upon a valid cancellation.**
3. **The software shall record cancellation events in an audit log containing actor, timestamp, and order ID information.**
4. **The software shall treat repeated cancel attempts on a Canceled order as a no-operation (idempotent).**
5. **The software shall refresh the GUI order list immediately after a successful cancellation.**
6. **The software shall display an informative error message when a cancellation request is blocked or invalid.**
