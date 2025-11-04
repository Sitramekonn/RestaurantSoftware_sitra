# Use Case: Set Order Priority
**Primary Actor:** Restaurant Staff

## Stakeholders and Interests
- **Restaurant Staff** – quickly flag urgent orders
- **Kitchen Staff** – prepare urgent orders first with clear visual cues
- **Restaurant Manager** – ensure efficient handling of high-priority orders

## Preconditions
- Order list is loaded and visible in the GUI
- Selected order exists in the system

## Success Conditions
Order is marked **High Priority**, the list is re-sorted, and the card shows a visual indicator.

## Frequency of Occurrence
Occasional (whenever certain orders must be prioritized)

## Main Success Scenario

| **User Actions** | **System Actions** |
|---|---|
| 1. Staff selects an order | 1.1 System highlights the selected order |
| 2. Staff marks the order as **High Priority** | 1.2 System updates the order’s `priority` attribute |
|  | 1.3 System persists the updated order |

## Scenario 2: Equal Priority Orders

| **User Actions** | **System Actions** |
|---|---|
| 1. Multiple orders share the same priority | 1.1 System applies a stable secondary sort (e.g., created time) |
|  | 1.2 UI order appears consistent without flicker |

## Functional Requirements
1. Allow assigning and updating an order’s priority (e.g., `LOW`, `NORMAL`, `HIGH`)
2. Persist priority changes with the order data
3. Automatically re-sort and refresh the UI after a priority change
4. Display a clear visual indicator for high-priority orders
