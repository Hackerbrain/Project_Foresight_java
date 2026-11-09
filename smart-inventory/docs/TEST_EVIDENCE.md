# Test Evidence

## 1. Automated tests (`mvn test`)

| Test class | What it proves |
|---|---|
| `InventoryServiceTest` | Stock in raises quantity and writes a ledger row; stock out below zero is rejected and records nothing; 25 -> 8 flags low stock; adjustments record the signed difference; zero/negative quantities, blank reasons and archived products are rejected; low stock at/below reorder level |
| `PurchaseOrderServiceTest` | Opening stock creates a ledger entry; **8 -> 33 partial receipt with 15 outstanding**, then full receipt; over-receipt rejected with stock unchanged; empty receipt rejected; draft cannot be received; empty draft cannot be placed; duplicate product lines merge; cancellation rules |
| `SecurityAccessTest` | Anonymous users redirected to login; role restrictions for purchasing, stock and user pages; CSRF token required on POST |
| `PageLinksTest` | Pager links keep filters and encode special characters |

**Result of last run:** _paste the `mvn test` summary here after running, e.g. "Tests run: 24, Failures: 0, Errors: 0"_.

## 2. Manual test script

Run on a fresh start (delete `./data` first). Mark each row Pass/Fail and attach a screenshot to your report.

| ID | Steps | Expected result | Result |
|---|---|---|---|
| M-01 | Follow README quick start | App starts; login page opens at `localhost:8080` | |
| M-02 | Sign in as `inventory`; Products -> Add product with a new SKU and opening stock 5 | Product created; ledger shows "Opening stock +5" | |
| M-03 | Add a product with an existing SKU | Clear error: SKU already used | |
| M-04 | Stock out more units than on hand | Error shows units on hand vs requested; stock unchanged | |
| M-05 | Adjust a product to a counted quantity with a reason | Ledger shows signed difference, user and time | |
| M-06 | Sign in as `purchasing`; add a supplier linked to two products | Supplier appears in list with products | |
| M-07 | Create a purchase order, add items, place it | Status moves Draft -> Ordered | |
| M-08 | As `purchasing`, open `/inventory/transaction` | "Access denied" page (403) | |
| M-09 | Receive 25 of 40 on PO-00001 (scanner at 8) | Stock 8 -> 33; status Partially received; 15 outstanding | |
| M-10 | Try to receive 20 when 15 remain | Error naming the outstanding quantity; nothing changes | |
| M-11 | Receive the remaining 15 | Status Received; ledger has two PO receipt rows | |
| M-12 | Dashboard | Low-stock list, order counts and recent movements match the data | |
| M-13 | Products -> tick "Low stock only" | Only products at or below reorder level are listed | |
| M-14 | Sign in as `admin`; Users -> add user; disable that user | New user can sign in, then cannot after disabling | |
| M-15 | Enter text in a number field / negative quantity | Field-level validation message, no server error page | |
| M-16 | Open the app on a phone-width window | Layout stacks; tables scroll; forms usable | |

## 3. Defects log

| ID | Description | Severity | Status |
|---|---|---|---|
| | | | |
