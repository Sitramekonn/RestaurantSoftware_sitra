**Primary Actor:** Staff  
**Scope:** Restaurant Order Management System  
**Level:** User goal  
**Related Components:** `HelloController`, `OrderService`, `Orders` (collection), `Order`, `TypeLabel`

---

## Stakeholders and Interests
- **Staff** — Completes orders and expects clear confirmation including the order type label.
- **Customers** — Receive accurate status updates and correct handling by type (Pickup / To-Go / Delivery).
- **Manager** — Wants consistent completion workflow and error handling to avoid bad data.

---

## Preconditions
- `Orders` collection is loaded and accessible.
- The target order exists and is not already **COMPLETED** or **CANCELLED**.
- The order has a non-null `orderType`.

## Success Guarantee (Postconditions)
- The order’s status is set to **COMPLETED**.
- The UI shows a type label string (e.g., `[Pickup]`, `[To-Go]`, `[Delivery]`) returned by `OrderService`.

## Minimal Guarantee
- If completion fails (missing order, invalid state, or missing type), the system surfaces a clear error and leaves data unchanged.

## Frequency of Occurrence
Frequent (whenever an order is finalized at the counter).

---

## Main Success Scenario

| **User Actions** | **System Actions** |
|---|---|
| 1. Staff clicks **Complete** on an order (by `orderId`). | 1.1 `HelloController` calls `OrderService.completeOrder(orderId, orders)` and activates the service. |
|  | 1.2 `OrderService` queries `Orders.stream().findFirst(id)` and obtains `Optional<Order>`. |
|  | 1.3 `OrderService` checks `order.getStatus()` and confirms it is neither **COMPLETED** nor **CANCELLED**. |
|  | 1.4 `OrderService` verifies `order.getOrderType()` is not null. |
|  | 1.5 `OrderService` sets `order.setStatus(COMPLETED)`. |
|  | 1.6 `OrderService` maps the type to a label via `TypeLabel.of(orderType)` → `"[Pickup]" / "[To-Go]" / "[Delivery]"`. |
|  | 1.7 `OrderService` returns the label string to `HelloController`. |
| 2. Staff sees the label on the UI (e.g., **[Pickup]**). | 2.1 `HelloController` displays the returned label to the Staff. |

---

## Extensions (Alternate / Error Flows)

### E1 — Order not found
- **Condition:** `Orders.findFirst(id)` returns empty.
- **System:** `OrderService` throws `IllegalArgumentException("Order not found")`; controller shows error; service deactivates; no state change.

### E2 — Order already COMPLETED or CANCELLED
- **Condition:** `order.getStatus()` is **COMPLETED** or **CANCELLED**.
- **System:** `OrderService` throws `IllegalStateException("Order is already ...")`; controller shows error; no state change.

### E3 — Order type is null
- **Condition:** `order.getOrderType()` is `null`.
- **System:** `OrderService` throws `IllegalStateException("Order type is required")`; controller shows error; no state change.

---

## Business Rules
1. Only orders with a defined `orderType` may be completed.
2. Completed orders must display a type label derived by `TypeLabel.of(orderType)`.
3. Orders already **COMPLETED**/**CANCELLED** cannot be re-completed.

---

## Functional Requirements
1. The system shall provide a **Complete** action per order in the UI.
2. The controller shall call `OrderService.completeOrder(orderId, orders)` to finalize completion.
3. The service shall:
    - Locate the order by `orderId` in `Orders`.
    - Validate status (not **COMPLETED**/**CANCELLED**) and `orderType != null`.
    - Set status to **COMPLETED** and compute a label via `TypeLabel.of(orderType)`.
    - Return the label string to be displayed.
4. The UI shall display the returned label adjacent to the completed order.
5. On validation failures, the system shall throw an appropriate exception and surface a readable message in the UI without changing data.

---

## Traceability
- **Sequence Diagram:** `src/Sequence_ShowOrderType.puml`
- **Methods:** `OrderService.completeOrder(...)`, `Order.getStatus()`, `Order.setStatus(...)`, `Order.getOrderType()`, `TypeLabel.of(...)`
