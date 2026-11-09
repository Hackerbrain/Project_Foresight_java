# Project Foresight 🚀
### Smart Inventory and Supply Chain Management System with Data Science & ML Forecasting
**WeIntern Major Project 01 | Team 01 (Assigned Member / Lead: Anubhab Sharma)**

[![Spring Boot 3.3.2](https://img.shields.io/badge/Spring%20Boot-3.3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java 17+](https://img.shields.io/badge/Java-17%20%7C%2021-orange.svg)](https://www.oracle.com/java/)
[![Spring Security 6](https://img.shields.io/badge/Security-Spring%20Security%206-blue.svg)](https://spring.io/projects/spring-security)
[![Database](https://img.shields.io/badge/DB-H2%20%2F%20PostgreSQL%20%2F%20MySQL-purple.svg)](https://www.h2database.com)

---

## 👥 Team 01 Allocation & Contribution Matrix

| No. | Member Name | Role & Core Responsibilities |
|---|---|---|
| **01** | **ANUBHAB SHARMA** *(Lead)* | System Architecture, Spring Boot 3 Backend, ML Analytics Services, Full Stack Integration & Deployment |
| **02** | **Prerna Raut** | Database Schema Design, JPA Entities (`Product`, `StockMovement`), ORM Relationships |
| **03** | **Sanket Abhang** | Inventory Ledger, Stock Adjustments, Negative Balance Safeguards & Reorder Engine |
| **04** | **Sudhanshu Raj** | Purchase Order Management, Vendor Workflows & Atomic Partial Receipt Logic |
| **05** | **Purvika N** | Smart Blue/White UI/UX Engineering, Chart.js Visualizations & Responsive Layouts |

---

## 🌟 Comprehensive Navigation Windows

### 1. 📊 Executive Dashboard
- **Stock Flow Dynamics**: Multi-timeframe bar & line charts with dynamic toggles for **Daily (24h)**, **Weekly (7d)**, **Monthly (4w)**, and **Yearly (12m)**.
- **KPI Metrics**: Real-time Total Valuation ($), Active SKUs, Low Stock Critical Alerts, and Pending Purchase Orders.
- **Category Share**: Donut chart breakdown.

### 2. 🔍 Exploratory Data Analysis (EDA)
- **Category Revenue**: Breakdown across Electronics, Industrial, Raw Materials, Logistics, Sensors.
- **City Revenue**: Geographical metropolitan performance (New York, San Francisco, Austin, Seattle, Chicago, Dallas, Boston).
- **Store Type Revenue**: Polar area chart across Central CDC, Regional Hubs, Express Depots, B2B Direct, and Retail.
- **Seasonal Revenue**: Quarterly cyclical patterns and surges.
- **Holiday vs Regular Revenue**: Holiday surge analysis.
- **Promotion vs Non-Promotion Distribution**: Promo campaign impact (+34.2% lift).
- **Sales Volume Distribution**: Frequency histogram across order volume tiers.

### 3. ⚙️ Feature Engineering
- **Interactive Correlation Matrix**: 7x7 Pearson correlation heatmap (Demand, Price, Discount, Lead Time, Stock Level, Seasonality, Stockout Risk).
- **Price vs Demand Elasticity Curve**: Scatter-line response curve indicating **Price Elasticity of Demand ($E_d = -1.42$)**.
- **Feature Importance Ranking**: Tree-ensemble contribution bars (Lag-7 demand, 30d rolling mean, promo flag, lead-time variance).

### 4. 📈 AI Demand Forecasting
- **Weekly Revenue Trend**: Actual historical revenue (Wk 35-44) plotted alongside forward AI projections (Wk 45-48).
- **Prediction Envelope**: 95% upper and lower statistical confidence bounds.

### 5. 🎯 Model Evaluation Process
- **Leaderboard Comparison**: Out-of-sample benchmark comparing **XGBoost (Champion)**, **LSTM Neural Net (Challenger)**, **Prophet (Benchmark)**, and **SARIMAX (Baseline)**.
- **Metrics**: Mean Absolute Error (MAE), Root Mean Squared Error (RMSE), Mean Absolute Percentage Error (MAPE), and $R^2$ Score.
- **Residual Error Distribution**: Q-Q error bell curve.

### 6. ⚡ Inventory Optimization
- **Inventory Health in Monthly Revenue Trend**: Dual-axis chart correlating Gross Monthly Revenue ($) with the Inventory Health Index (%).
- **Interactive Service Level Simulator**: Slider dynamically adjusting target service level (80% to 99%), computing Z-score, required safety buffer, and capital allocation.

### 7. 🧠 Smart Foresight AI
- Automated calculation of **Average Daily Demand**, **Days of Runway Remaining**, and risk categorization (`CRITICAL`, `HIGH`, `MODERATE`, `HEALTHY`, `OVERSTOCKED`) with 1-click reorder actions.

### 8. 📦 Product Catalogue & CSV Upload
- Dynamic product creation, real-time detail and quantity editing, search, category filtering, and bulk CSV spreadsheet import.

### 9. 📑 Purchase Orders with Partial Receipt
- Full order lifecycle: `DRAFT` ➔ `ORDERED` ➔ `PARTIALLY_RECEIVED` ➔ `RECEIVED` / `CANCELLED`.
- Atomic partial receipt updating stock on hand immediately while keeping remaining units open.

### 10. 🏢 Multi-Warehouse Network & 🚚 Approved Suppliers
- Multi-facility logistics (Central CDC, West Coast Hub, Metro Depot) with inter-warehouse stock transfer workflows.
- Vendor directory with lead times and ratings.

### 11. 📜 Traceable Movement Audit Ledger
- Immutable audit log capturing user, timestamp, delta, and reference number for every stock change.

---

## 🔑 Demo Sign-In Accounts

### 📧 Gmail Login (via Password)
| Name / Role | Gmail Address | Password | Permissions |
|---|---|---|---|
| **Anubhab Sharma (Admin)** | `anubhab.foresight@gmail.com` | `gmail123` | Full administrative control |
| **Inventory Specialist** | `staff.foresight@gmail.com` | `gmail123` | Stock operations & transfers |

### 🏢 Corporate Accounts
| Role | Email | Password | Permissions |
|---|---|---|---|
| **Admin** | `admin@foresight.com` | `admin123` | Full administrative control |
| **Inventory Staff** | `inventory@foresight.com` | `staff123` | Stock in/out, adjustments, audits |
| **Purchasing Staff** | `purchasing@foresight.com` | `purchase123` | Supplier POs & partial receipts |

---

## 🚀 How to Run

1. Open PowerShell or Command Prompt in the workspace directory:
   ```powershell
   cd "c:\Users\ANUBHAB SHARMA\OneDrive\Desktop\Project_Forsight_Java"
   mvn spring-boot:run
   ```
2. Navigate in your browser to:
   * **Application**: `http://localhost:8080`
   * **H2 Console**: `http://localhost:8080/h2-console`
