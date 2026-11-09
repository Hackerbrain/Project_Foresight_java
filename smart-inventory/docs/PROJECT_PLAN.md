# Project Plan and Team Allocation

## Milestones (from the handbook)

| Stage | Result | Done when |
|---|---|---|
| 1. Discover | Problem statement, users, requirements, risks, task split | `docs/REQUIREMENTS.md` reviewed by all five members |
| 2. Design | Architecture, ER diagram, API/route plan | `docs/ARCHITECTURE.md` agreed; repository and branches created |
| 3. Build | Core workflow end to end with validation and persistence | A user can create a product, stock it, order it and receive it |
| 4. Verify | Test evidence, defects fixed, usability improvements | `mvn test` green; manual script in `docs/TEST_EVIDENCE.md` completed |
| 5. Release | Working demo, README, report, presentation | Fresh clone runs from the README; demo rehearsed |

## Suggested ownership (adjust as a team)

Every member should be able to explain the whole flow, but each owns one area so contributions are clear.

| Member | Primary area | Main files |
|---|---|---|
| Anubhab Sharma | Security and roles, user management, deployment, repository hygiene | `SecurityConfig`, `AppUserService`, `UserController`, `README.md` |
| Prerna Raut | Product catalogue and categories, search/filter/pagination | `Product*`, `Category*`, `templates/products`, `templates/categories` |
| Sanket Abhang | Suppliers and purchase-order workflow including receiving | `Supplier*`, `PurchaseOrder*`, `templates/purchase-orders` |
| Sudhanshu Raj | Inventory ledger, dashboard and reports, concurrency handling | `InventoryService`, `DashboardService`, `templates/inventory`, `dashboard.html` |
| Purvika N | Testing, validation messages, sample data, documentation, report | `src/test`, `DataInitializer`, `docs/` |

## Git workflow
- One shared repository; `main` is always runnable.
- One short-lived branch per feature (`feature/purchase-orders`), merged by pull request reviewed by another member.
- Meaningful commit messages ("Validate receipt quantity against outstanding"), never commit `.env`, `data/` or passwords.

## Final submission checklist
- [ ] Source code in the shared repository
- [ ] README with setup steps and demo accounts
- [ ] ER diagram and architecture overview (`docs/ARCHITECTURE.md`)
- [ ] Test evidence with real results and screenshots
- [ ] Sample data (loaded automatically by `DataInitializer`)
- [ ] Concise technical report (5-8 pages: problem, design, key decisions, testing, limitations, individual contributions)
- [ ] Demo video or live demo of the handbook scenario
- [ ] Each member ready to explain their contribution
