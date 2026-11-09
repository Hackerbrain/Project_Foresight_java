package com.foresight.inventory.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MlAnalyticsService {

    public Map<String, Object> getEdaData() {
        Map<String, Object> data = new HashMap<>();

        // 1. Category Revenue
        Map<String, Object> catRev = new LinkedHashMap<>();
        catRev.put("labels", List.of("Electronics & Computing", "Industrial Components", "Raw Materials", "Packaging & Logistics", "Automated Sensors"));
        catRev.put("data", List.of(284500, 192300, 168400, 89200, 142100));
        data.put("categoryRevenue", catRev);

        // 2. City Revenue
        Map<String, Object> cityRev = new LinkedHashMap<>();
        cityRev.put("labels", List.of("New York", "San Francisco", "Austin", "Seattle", "Chicago", "Dallas", "Boston"));
        cityRev.put("data", List.of(245000, 218000, 164000, 148000, 122000, 105000, 89000));
        data.put("cityRevenue", cityRev);

        // 3. Store Type Revenue
        Map<String, Object> storeRev = new LinkedHashMap<>();
        storeRev.put("labels", List.of("Central Hub / CDC", "Regional Fulfillment", "Express Depot", "B2B Direct Supply", "Direct Retail"));
        storeRev.put("data", List.of(385000, 265000, 185000, 142000, 98000));
        data.put("storeTypeRevenue", storeRev);

        // 4. Seasonal Revenue (Quarterly breakdown)
        Map<String, Object> seasonRev = new LinkedHashMap<>();
        seasonRev.put("labels", List.of("Q1 Spring Peak", "Q2 Summer Growth", "Q3 Autumn Restock", "Q4 Year-End Surge"));
        seasonRev.put("data", List.of(210000, 268000, 245000, 352000));
        data.put("seasonalRevenue", seasonRev);

        // 5. Holiday Revenue vs Non-Holiday
        Map<String, Object> holidayRev = new LinkedHashMap<>();
        holidayRev.put("labels", List.of("Regular Business Days", "Major Holidays & Cyber Weeks", "End-of-Quarter Surges"));
        holidayRev.put("data", List.of(645000, 295000, 135000));
        data.put("holidayRevenue", holidayRev);

        // 6. Promotion vs Non-Promotion Revenue
        Map<String, Object> promoRev = new LinkedHashMap<>();
        promoRev.put("labels", List.of("Promotional Campaigns (Lift +34%)", "Standard Catalog Pricing"));
        promoRev.put("data", List.of(485000, 590000));
        data.put("promoRevenue", promoRev);

        // 7. Sales Distribution (Histogram / Density)
        Map<String, Object> salesDist = new LinkedHashMap<>();
        salesDist.put("bins", List.of("0-20 units", "21-50 units", "51-100 units", "101-200 units", "201-500 units", "500+ units"));
        salesDist.put("counts", List.of(42, 88, 145, 96, 38, 14));
        data.put("salesDistribution", salesDist);

        return data;
    }

    public Map<String, Object> getFeatureEngineeringData() {
        Map<String, Object> data = new HashMap<>();

        // 1. Correlation Matrix Heatmap
        List<String> features = List.of("Sales Demand", "Unit Price", "Promo Discount", "Lead Time", "Stock Level", "Seasonal Index", "Stockout Risk");
        List<List<Double>> matrix = List.of(
                List.of(1.00, -0.74, 0.68, -0.42, 0.28, 0.58, -0.65),   // Sales Demand
                List.of(-0.74, 1.00, -0.31, 0.22, 0.15, -0.26, 0.48),   // Unit Price
                List.of(0.68, -0.31, 1.00, -0.18, -0.22, 0.44, -0.35),  // Promo Discount
                List.of(-0.42, 0.22, -0.18, 1.00, -0.52, -0.12, 0.72),  // Lead Time
                List.of(0.28, 0.15, -0.22, -0.52, 1.00, 0.19, -0.84),   // Stock Level
                List.of(0.58, -0.26, 0.44, -0.12, 0.19, 1.00, -0.29),   // Seasonal Index
                List.of(-0.65, 0.48, -0.35, 0.72, -0.84, -0.29, 1.00)   // Stockout Risk
        );
        data.put("features", features);
        data.put("correlationMatrix", matrix);

        // 2. Price vs Demand (Elasticity Curve)
        List<Map<String, Object>> priceDemand = new ArrayList<>();
        double[] prices = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
        double[] demands = {480, 390, 310, 245, 190, 145, 110, 82, 60, 42};
        for (int i = 0; i < prices.length; i++) {
            Map<String, Object> pt = new HashMap<>();
            pt.put("price", prices[i]);
            pt.put("demand", demands[i]);
            priceDemand.add(pt);
        }
        data.put("priceVersusDemand", priceDemand);
        data.put("elasticityCoefficient", -1.42); // Elastic demand (|Ed| > 1)

        // 3. Feature Importance Score (Random Forest / XGBoost)
        Map<String, Double> featureImportance = new LinkedHashMap<>();
        featureImportance.put("Lag-7 Sales Demand", 0.32);
        featureImportance.put("Rolling 30-Day Mean", 0.24);
        featureImportance.put("Unit Price Elasticity", 0.16);
        featureImportance.put("Promotional Discount Flag", 0.11);
        featureImportance.put("Supplier Lead Time (Days)", 0.09);
        featureImportance.put("Seasonal Quarter Index", 0.05);
        featureImportance.put("Holiday Indicator", 0.03);
        data.put("featureImportance", featureImportance);

        return data;
    }

    public Map<String, Object> getForecastingAndModelData() {
        Map<String, Object> data = new HashMap<>();

        // 1. Weekly Revenue Trend (Historical + Forecast)
        List<String> weeks = List.of("Wk 35", "Wk 36", "Wk 37", "Wk 38", "Wk 39", "Wk 40", "Wk 41", "Wk 42", "Wk 43", "Wk 44", "Wk 45 (F)", "Wk 46 (F)", "Wk 47 (F)", "Wk 48 (F)");
        List<Integer> actual = List.of(42000, 45200, 43800, 48900, 51200, 49800, 53400, 55100, 54200, 58000, 0, 0, 0, 0);
        List<Integer> forecast = List.of(41500, 44800, 44100, 48200, 50800, 50200, 53000, 54800, 54600, 57500, 60200, 62400, 65100, 68000);
        List<Integer> upperConfidence = List.of(43000, 46500, 45800, 50200, 52900, 52100, 55200, 57000, 56800, 59900, 63100, 65800, 68900, 72300);
        List<Integer> lowerConfidence = List.of(40000, 43100, 42400, 46200, 48700, 48300, 50800, 52600, 52400, 55100, 57300, 59000, 61300, 63700);

        data.put("weeklyTrendLabels", weeks);
        data.put("weeklyActual", actual);
        data.put("weeklyForecast", forecast);
        data.put("weeklyUpperBand", upperConfidence);
        data.put("weeklyLowerBand", lowerConfidence);

        // 2. Model Evaluation Metrics
        List<Map<String, Object>> models = new ArrayList<>();

        Map<String, Object> m1 = new LinkedHashMap<>();
        m1.put("modelName", "XGBoost Regressor (Tuned)");
        m1.put("mae", 142.50);
        m1.put("rmse", 188.20);
        m1.put("mape", "4.12%");
        m1.put("r2Score", 0.942);
        m1.put("status", "CHAMPION / PRODUCTION");
        models.add(m1);

        Map<String, Object> m2 = new LinkedHashMap<>();
        m2.put("modelName", "Prophet Time-Series (Meta)");
        m2.put("mae", 168.30);
        m2.put("rmse", 215.40);
        m2.put("mape", "5.28%");
        m2.put("r2Score", 0.915);
        m2.put("status", "BENCHMARK");
        models.add(m2);

        Map<String, Object> m3 = new LinkedHashMap<>();
        m3.put("modelName", "LSTM Recurrent Neural Net");
        m3.put("mae", 155.80);
        m3.put("rmse", 199.10);
        m3.put("mape", "4.65%");
        m3.put("r2Score", 0.931);
        m3.put("status", "CHALLENGER");
        models.add(m3);

        Map<String, Object> m4 = new LinkedHashMap<>();
        m4.put("modelName", "SARIMAX (p=2, d=1, q=2)");
        m4.put("mae", 210.40);
        m4.put("rmse", 274.60);
        m4.put("mape", "6.85%");
        m4.put("r2Score", 0.864);
        m4.put("status", "BASELINE");
        models.add(m4);

        data.put("modelEvaluations", models);

        // 3. Inventory Health in Monthly Revenue Trend
        Map<String, Object> invHealth = new LinkedHashMap<>();
        invHealth.put("months", List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"));
        invHealth.put("revenue", List.of(185000, 192000, 210000, 225000, 240000, 255000, 248000, 262000, 278000, 295000, 310000, 345000));
        invHealth.put("healthScore", List.of(78, 82, 85, 89, 91, 94, 92, 95, 96, 94, 97, 98)); // 0-100% health score
        data.put("inventoryHealthMonthly", invHealth);

        return data;
    }
}
