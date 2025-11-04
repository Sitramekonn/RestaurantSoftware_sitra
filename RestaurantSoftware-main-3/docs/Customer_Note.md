# Use Case: Add Customer Note
**Primary Actor:** Restaurant Staff

## Stakeholders and Interests
- **Restaurant Staff** – record special requests quickly
- **Kitchen Staff** – see notes to prepare accurately
- **Restaurant Manager** – improve customer satisfaction and reduce mistakes

## Preconditions
- An order is selected or being created
- Order dialog is open with editable fields

## Success Conditions
The note is saved with the order and displayed in the UI.

## Frequency of Occurrence
Common (whenever special instructions are received)

## Main Success Scenario

| **User Actions** | **System Actions** |
|---|---|
| 1. Staff opens an order | 1.1 System shows order details and a **Customer Note** field |
| 2. Staff types a note (e.g., *No onions*) | 1.2 System stores the pending note |
| 3. Staff clicks **Save** | 1.3 System persists the note with the order |
|  | 1.4 UI refreshes to display the note under order details |

## Scenario 2: Blank Note Handling

| **User Actions** | **System Actions** |
|---|---|
| 1. Staff leaves the field blank and clicks **Save** | 1.1 System detects blank input |
|  | 1.2 No change is written; prior note (if any) is retained |

## Functional Requirements
1. Provide an editable **Customer Note** field in the order dialog
2. Store and retrieve notes with the order data
3. Blank notes must not overwrite existing notes
