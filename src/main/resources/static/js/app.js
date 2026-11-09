// Project Foresight - Frontend Application Logic & Engine

let currentTab = 'dashboard';
let currentTimeframe = 'weekly';
let movementChart = null;
let categoryChart = null;

// ML / EDA Chart Instances
let edaCatRevChart = null;
let edaCityRevChart = null;
let edaStoreTypeChart = null;
let edaSeasonalChart = null;
let edaHolidayChart = null;
let edaPromoChart = null;
let edaSalesDistChart = null;
let priceDemandChart = null;
let weeklyForecastChart = null;
let residualChart = null;
let modelR2Chart = null;
let invHealthMonthlyChart = null;

let cachedProducts = [];
let cachedCategories = [];
let cachedSuppliers = [];
let cachedWarehouses = [];
let cachedOrders = [];
let cachedMovements = [];

// ==================== DEFAULT MOCK DATASET ====================
const DEFAULT_CATEGORIES = [
    { id: 1, name: "Electronics & Computing", code: "ELEC", description: "Microcontrollers & sensors" },
    { id: 2, name: "Raw Materials & Metals", code: "RAW", description: "Alloys, sheet metals, and wiring" },
    { id: 3, name: "Industrial Components", code: "IND", description: "Bearings, valves, and hydraulics" },
    { id: 4, name: "Packaging & Assembly", code: "PACK", description: "Cartons, enclosures, and protective foam" }
];

const DEFAULT_SUPPLIERS = [
    { id: 1, name: "Apex Semiconductors Ltd", contactPerson: "Elena Vance", email: "orders@apexsemi.com", phone: "+1 555-0192", leadTimeDays: 10, rating: 4.9 },
    { id: 2, name: "Titan Raw Materials Corp", contactPerson: "Marcus Vance", email: "sales@titanmetals.com", phone: "+1 555-0847", leadTimeDays: 5, rating: 4.7 },
    { id: 3, name: "Global Micro-Sensors Inc", contactPerson: "Sarah Connor", email: "supply@globalsensors.com", phone: "+1 555-0331", leadTimeDays: 14, rating: 4.3 },
    { id: 4, name: "NextGen Logistics Supplies", contactPerson: "David Chen", email: "contact@nextgenpack.com", phone: "+1 555-0988", leadTimeDays: 3, rating: 4.8 }
];

const DEFAULT_WAREHOUSES = [
    { id: 1, code: "WH-MAIN", name: "Central Distribution Center", type: "MAIN_WAREHOUSE", address: "Building 4, Logistics Park North" },
    { id: 2, code: "WH-WEST", name: "West Coast Fulfillment Hub", type: "REGIONAL_HUB", address: "77 Industrial Parkway, Sector 9" },
    { id: 3, code: "WH-METRO", name: "Metro Express Depot", type: "RETAIL_STORE", address: "Unit 12, Commerce Boulevard" }
];

const DEFAULT_PRODUCTS = [
    {
        id: 1, sku: "PRD-1001", name: "Industrial IoT Micro-Controller", description: "Dual-core ESP32 industrial unit",
        category: DEFAULT_CATEGORIES[0], supplier: DEFAULT_SUPPLIERS[0], defaultLocation: DEFAULT_WAREHOUSES[0],
        unit: "PCS", costPrice: 45.00, sellingPrice: 75.00, currentStock: 8, reorderLevel: 10, maxStockLevel: 100,
        imageUrl: "https://images.unsplash.com/photo-1518770660439-4636190af475?w=200&auto=format&fit=crop"
    },
    {
        id: 2, sku: "PRD-1002", name: "Arm Cortex Sensor Node", description: "Wireless telemetry module",
        category: DEFAULT_CATEGORIES[0], supplier: DEFAULT_SUPPLIERS[2], defaultLocation: DEFAULT_WAREHOUSES[1],
        unit: "PCS", costPrice: 32.50, sellingPrice: 58.00, currentStock: 4, reorderLevel: 15, maxStockLevel: 80,
        imageUrl: "https://images.unsplash.com/photo-1555664424-778a1e5e1b48?w=200&auto=format&fit=crop"
    },
    {
        id: 3, sku: "PRD-1003", name: "Copper Ribbon Cable 50m", description: "High-purity shielded cable spool",
        category: DEFAULT_CATEGORIES[1], supplier: DEFAULT_SUPPLIERS[1], defaultLocation: DEFAULT_WAREHOUSES[0],
        unit: "SPOOL", costPrice: 85.00, sellingPrice: 130.00, currentStock: 120, reorderLevel: 25, maxStockLevel: 200,
        imageUrl: "https://images.unsplash.com/photo-1544724569-5f546fd6f2b5?w=200&auto=format&fit=crop"
    },
    {
        id: 4, sku: "PRD-1004", name: "Titanium Alloy Rod 10mm", description: "Aerospace grade titanium rod 1m",
        category: DEFAULT_CATEGORIES[1], supplier: DEFAULT_SUPPLIERS[1], defaultLocation: DEFAULT_WAREHOUSES[0],
        unit: "ROD", costPrice: 110.00, sellingPrice: 180.00, currentStock: 45, reorderLevel: 12, maxStockLevel: 100,
        imageUrl: "https://images.unsplash.com/photo-1504917599217-d4dc5ebe6122?w=200&auto=format&fit=crop"
    },
    {
        id: 5, sku: "PRD-1005", name: "Lithium-Ion Battery Pack 48V", description: "Backup power cell module",
        category: DEFAULT_CATEGORIES[0], supplier: DEFAULT_SUPPLIERS[0], defaultLocation: DEFAULT_WAREHOUSES[1],
        unit: "UNIT", costPrice: 220.00, sellingPrice: 350.00, currentStock: 0, reorderLevel: 8, maxStockLevel: 50,
        imageUrl: "https://images.unsplash.com/photo-1619642751034-765dfdf7c58e?w=200&auto=format&fit=crop"
    },
    {
        id: 6, sku: "PRD-1006", name: "Precision Ball Bearings 608RS", description: "Ceramic hybrid sealed bearing",
        category: DEFAULT_CATEGORIES[2], supplier: DEFAULT_SUPPLIERS[3], defaultLocation: DEFAULT_WAREHOUSES[2],
        unit: "BOX", costPrice: 18.00, sellingPrice: 34.00, currentStock: 340, reorderLevel: 50, maxStockLevel: 150,
        imageUrl: "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=200&auto=format&fit=crop"
    }
];

const DEFAULT_ORDERS = [
    {
        id: 1, orderNumber: "PO-2026-001", supplier: DEFAULT_SUPPLIERS[0], targetLocation: DEFAULT_WAREHOUSES[0],
        status: "ORDERED", totalAmount: 1800.00, createdAt: new Date(Date.now() - 86400000 * 2).toISOString(),
        items: [{ id: 1, product: DEFAULT_PRODUCTS[0], quantityOrdered: 40, quantityReceived: 0, unitPrice: 45.00, subtotal: 1800.00 }]
    },
    {
        id: 2, orderNumber: "PO-2026-002", supplier: DEFAULT_SUPPLIERS[2], targetLocation: DEFAULT_WAREHOUSES[1],
        status: "PARTIALLY_RECEIVED", totalAmount: 1625.00, createdAt: new Date(Date.now() - 86400000 * 5).toISOString(),
        items: [{ id: 2, product: DEFAULT_PRODUCTS[1], quantityOrdered: 50, quantityReceived: 25, unitPrice: 32.50, subtotal: 1625.00 }]
    },
    {
        id: 3, orderNumber: "PO-2026-003", supplier: DEFAULT_SUPPLIERS[1], targetLocation: DEFAULT_WAREHOUSES[0],
        status: "DRAFT", totalAmount: 3300.00, createdAt: new Date(Date.now() - 86400000 * 1).toISOString(),
        items: [{ id: 3, product: DEFAULT_PRODUCTS[3], quantityOrdered: 30, quantityReceived: 0, unitPrice: 110.00, subtotal: 3300.00 }]
    }
];

const DEFAULT_MOVEMENTS = [
    { id: 1, timestamp: new Date(Date.now() - 3600000 * 2).toISOString(), product: DEFAULT_PRODUCTS[0], location: DEFAULT_WAREHOUSES[0], type: "STOCK_OUT", quantityChange: -1, previousStock: 9, newStock: 8, reason: "Sample request", referenceNumber: "SMP-05", performedBy: "Anubhab Sharma" },
    { id: 2, timestamp: new Date(Date.now() - 3600000 * 4).toISOString(), product: DEFAULT_PRODUCTS[2], location: DEFAULT_WAREHOUSES[0], type: "STOCK_IN", quantityChange: 10, previousStock: 110, newStock: 120, reason: "Rapid restock", referenceNumber: "RAP-01", performedBy: "Inventory Staff" },
    { id: 3, timestamp: new Date(Date.now() - 86400000 * 2).toISOString(), product: DEFAULT_PRODUCTS[5], location: DEFAULT_WAREHOUSES[2], type: "STOCK_OUT", quantityChange: -60, previousStock: 400, newStock: 340, reason: "Wholesale distribution", referenceNumber: "WHL-14", performedBy: "Inventory Staff" },
    { id: 4, timestamp: new Date(Date.now() - 86400000 * 4).toISOString(), product: DEFAULT_PRODUCTS[1], location: DEFAULT_WAREHOUSES[1], type: "STOCK_OUT", quantityChange: -11, previousStock: 15, newStock: 4, reason: "Enterprise deployment", referenceNumber: "ENT-401", performedBy: "System" },
    { id: 5, timestamp: new Date(Date.now() - 86400000 * 5).toISOString(), product: DEFAULT_PRODUCTS[1], location: DEFAULT_WAREHOUSES[1], type: "PO_RECEIPT", quantityChange: 25, previousStock: 4, newStock: 29, reason: "Received against PO-2026-002", referenceNumber: "PO-2026-002", performedBy: "Purchasing Staff" }
];

// Initialize on DOM load
document.addEventListener('DOMContentLoaded', async () => {
    initLocalStore();
    await checkCurrentUser();
    await loadInitialData();
    initExecutiveCharts();
    await loadDashboard();
    await loadSuggestions();
    await loadProducts();
    await loadOrders();
    await loadWarehouses();
    await loadSuppliers();
    await loadAuditMovements();

    // Pre-initialize ML / EDA views
    initEdaCharts();
    initFeatureEngineeringCharts();
    initForecastingCharts();
    initModelEvaluationView();
    initOptimizationView();
});

function initLocalStore() {
    if (!localStorage.getItem('pf_products')) localStorage.setItem('pf_products', JSON.stringify(DEFAULT_PRODUCTS));
    if (!localStorage.getItem('pf_orders')) localStorage.setItem('pf_orders', JSON.stringify(DEFAULT_ORDERS));
    if (!localStorage.getItem('pf_movements')) localStorage.setItem('pf_movements', JSON.stringify(DEFAULT_MOVEMENTS));
}

function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    if (!container) return;
    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.style.borderLeft = type === 'error' ? '4px solid #ef4444' : (type === 'success' ? '4px solid #10b981' : '4px solid #2563eb');
    toast.innerHTML = `<span>${type === 'error' ? '⚠️' : (type === 'success' ? '✅' : 'ℹ️')}</span> ${message}`;
    container.appendChild(toast);
    setTimeout(() => toast.remove(), 4000);
}

async function checkCurrentUser() {
    try {
        const res = await fetch('/api/auth/me');
        if (res.ok) {
            const data = await res.json();
            if (data.authenticated) {
                document.getElementById('userName').innerText = data.name || data.email;
                document.getElementById('userRole').innerText = data.role || 'STAFF';
                document.getElementById('userAvatar').innerText = (data.name ? data.name[0] : 'U').toUpperCase();
                return;
            }
        }
    } catch (e) {}

    const savedUser = JSON.parse(localStorage.getItem('foresight_user') || 'null');
    if (savedUser) {
        document.getElementById('userName').innerText = savedUser.name || 'Anubhab Sharma';
        document.getElementById('userRole').innerText = savedUser.role || 'ADMIN';
        document.getElementById('userAvatar').innerText = (savedUser.name ? savedUser.name[0] : 'A').toUpperCase();
    } else {
        document.getElementById('userName').innerText = 'Anubhab Sharma';
        document.getElementById('userRole').innerText = 'ADMIN';
        document.getElementById('userAvatar').innerText = 'A';
    }
}

// ==================== TAB SWITCHER ====================
function switchTab(tabId) {
    currentTab = tabId;
    document.querySelectorAll('.sidebar-nav .nav-item').forEach(item => item.classList.remove('active'));
    
    // Highlight active link
    const allLinks = document.querySelectorAll('.sidebar-nav .nav-item');
    allLinks.forEach(link => {
        if (link.getAttribute('onclick') && link.getAttribute('onclick').includes(tabId)) {
            link.classList.add('active');
        }
    });

    const views = ['dashboard', 'eda', 'features', 'forecasting', 'evaluation', 'optimization', 'suggestions', 'products', 'orders', 'warehouses', 'suppliers', 'audit'];
    views.forEach(v => {
        const el = document.getElementById(`view-${v}`);
        if (el) el.style.display = (v === tabId) ? 'block' : 'none';
    });

    const titles = {
        'dashboard': 'Executive Inventory Overview & Analytics',
        'eda': 'Exploratory Data Analysis (EDA) & Revenue Diagnostics',
        'features': 'Feature Engineering & Multivariate Correlation',
        'forecasting': 'AI Demand Forecasting & Predictive Revenue Trends',
        'evaluation': 'ML Model Evaluation & Out-of-Sample Performance',
        'optimization': 'Inventory Health & Multi-Echelon Optimization',
        'suggestions': 'Smart Foresight AI & Demand Velocity',
        'products': 'Product Catalogue & Inventory Levels',
        'orders': 'Purchase Orders & Supplier Replenishment',
        'warehouses': 'Multi-Location Warehouses & Stock Transfers',
        'suppliers': 'Approved Supplier Directory',
        'audit': 'Traceable Movement Audit Ledger'
    };
    document.getElementById('pageTitle').innerText = titles[tabId] || 'Project Foresight';

    if (tabId === 'dashboard') loadDashboard();
    if (tabId === 'eda') loadEdaCharts();
    if (tabId === 'features') loadFeatureEngineeringData();
    if (tabId === 'forecasting') loadForecastingChartData();
    if (tabId === 'evaluation') loadModelEvaluationTable();
    if (tabId === 'optimization') loadOptimizationChart();
    if (tabId === 'suggestions') loadSuggestions();
    if (tabId === 'products') loadProducts();
    if (tabId === 'orders') loadOrders();
    if (tabId === 'warehouses') loadWarehouses();
    if (tabId === 'suppliers') loadSuppliers();
    if (tabId === 'audit') loadAuditMovements();
}

// Load Dropdown Options
async function loadInitialData() {
    try {
        const [cats, sups, whs] = await Promise.all([
            fetch('/api/categories').then(r => r.json()).catch(() => DEFAULT_CATEGORIES),
            fetch('/api/suppliers').then(r => r.json()).catch(() => DEFAULT_SUPPLIERS),
            fetch('/api/warehouses').then(r => r.json()).catch(() => DEFAULT_WAREHOUSES)
        ]);
        cachedCategories = (cats && cats.length) ? cats : DEFAULT_CATEGORIES;
        cachedSuppliers = (sups && sups.length) ? sups : DEFAULT_SUPPLIERS;
        cachedWarehouses = (whs && whs.length) ? whs : DEFAULT_WAREHOUSES;
    } catch (e) {
        cachedCategories = DEFAULT_CATEGORIES;
        cachedSuppliers = DEFAULT_SUPPLIERS;
        cachedWarehouses = DEFAULT_WAREHOUSES;
    }
    populateDropdowns();
}

function populateDropdowns() {
    const catFilter = document.getElementById('productCategoryFilter');
    const prodCat = document.getElementById('prodCategory');
    if (catFilter) catFilter.innerHTML = '<option value="">All Categories</option>' + cachedCategories.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
    if (prodCat) prodCat.innerHTML = cachedCategories.map(c => `<option value="${c.id}">${c.name}</option>`).join('');

    const prodSup = document.getElementById('prodSupplier');
    const poSup = document.getElementById('poSupplier');
    const supOptions = '<option value="">Select Supplier</option>' + cachedSuppliers.map(s => `<option value="${s.id}">${s.name} (${s.leadTimeDays}d lead)</option>`).join('');
    if (prodSup) prodSup.innerHTML = supOptions;
    if (poSup) poSup.innerHTML = supOptions;

    const prodLoc = document.getElementById('prodLocation');
    const adjLoc = document.getElementById('adjLocation');
    const transFrom = document.getElementById('transFrom');
    const transTo = document.getElementById('transTo');
    const poLoc = document.getElementById('poLocation');
    const whOptions = cachedWarehouses.map(w => `<option value="${w.id}">${w.name} (${w.code})</option>`).join('');

    if (prodLoc) prodLoc.innerHTML = whOptions;
    if (adjLoc) adjLoc.innerHTML = whOptions;
    if (transFrom) transFrom.innerHTML = whOptions;
    if (transTo) transTo.innerHTML = whOptions;
    if (poLoc) poLoc.innerHTML = whOptions;
}

// ==================== 1. EXECUTIVE DASHBOARD CHARTS ====================
function initExecutiveCharts() {
    const canvasMovement = document.getElementById('movementChart');
    if (canvasMovement) {
        const ctxMovement = canvasMovement.getContext('2d');
        movementChart = new Chart(ctxMovement, {
            type: 'bar',
            data: {
                labels: [],
                datasets: [
                    { label: 'Stock In (Replenished)', data: [], backgroundColor: '#2563eb', borderRadius: 6 },
                    { label: 'Stock Out (Dispatches)', data: [], backgroundColor: '#f87171', borderRadius: 6 },
                    { type: 'line', label: 'Net Flow', data: [], borderColor: '#06b6d4', backgroundColor: '#06b6d4', borderWidth: 3, pointBackgroundColor: '#06b6d4', pointRadius: 4, fill: false, tension: 0.3 }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: { beginAtZero: true, grid: { color: '#e0f2fe' } },
                    x: { grid: { display: false } }
                },
                plugins: { legend: { position: 'top' } }
            }
        });
    }

    const canvasCat = document.getElementById('categoryChart');
    if (canvasCat) {
        const ctxCategory = canvasCat.getContext('2d');
        categoryChart = new Chart(ctxCategory, {
            type: 'doughnut',
            data: {
                labels: [],
                datasets: [{ data: [], backgroundColor: ['#1e3a8a', '#2563eb', '#38bdf8', '#06b6d4', '#10b981'] }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { position: 'bottom' } }
            }
        });
    }
}

async function updateTimeframe(tf, buttonElement) {
    currentTimeframe = tf;
    document.querySelectorAll('.timeframe-toggles .toggle-btn').forEach(b => b.classList.remove('active'));
    if (buttonElement) buttonElement.classList.add('active');
    await loadChartData();
}

async function loadChartData() {
    try {
        const res = await fetch(`/api/analytics/charts?timeframe=${currentTimeframe}`);
        if (res.ok) {
            const data = await res.json();
            renderChartsWithData(data);
            return;
        }
    } catch (e) {}

    const mockData = getMockChartData(currentTimeframe);
    renderChartsWithData(mockData);
}

function getMockChartData(tf) {
    if (tf === 'daily') {
        return {
            labels: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00'],
            stockIn: [0, 0, 10, 25, 0, 5],
            stockOut: [0, 2, 8, 14, 18, 6],
            netMovement: [0, -2, 2, 11, -18, -1],
            categoryShare: { "Electronics & Computing": 3, "Raw Materials": 2, "Industrial Components": 1 }
        };
    } else if (tf === 'monthly') {
        return {
            labels: ['Week 1', 'Week 2', 'Week 3', 'Week 4'],
            stockIn: [180, 240, 110, 310],
            stockOut: [140, 195, 160, 220],
            netMovement: [40, 45, -50, 90],
            categoryShare: { "Electronics & Computing": 3, "Raw Materials": 2, "Industrial Components": 1 }
        };
    } else if (tf === 'yearly') {
        return {
            labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
            stockIn: [520, 610, 450, 780, 690, 810, 740, 680, 890, 950, 870, 920],
            stockOut: [480, 530, 490, 640, 620, 730, 710, 650, 810, 860, 790, 880],
            netMovement: [40, 80, -40, 140, 70, 80, 30, 30, 80, 90, 80, 40],
            categoryShare: { "Electronics & Computing": 3, "Raw Materials": 2, "Industrial Components": 1 }
        };
    } else {
        return {
            labels: ['Mon 03', 'Tue 04', 'Wed 05', 'Thu 06', 'Fri 07', 'Sat 08', 'Sun 09'],
            stockIn: [45, 60, 0, 80, 35, 10, 25],
            stockOut: [28, 35, 42, 50, 38, 15, 12],
            netMovement: [17, 25, -42, 30, -3, -5, 13],
            categoryShare: { "Electronics & Computing": 3, "Raw Materials": 2, "Industrial Components": 1 }
        };
    }
}

function renderChartsWithData(data) {
    if (movementChart && data) {
        movementChart.data.labels = data.labels;
        movementChart.data.datasets[0].data = data.stockIn;
        movementChart.data.datasets[1].data = data.stockOut;
        movementChart.data.datasets[2].data = data.netMovement;
        movementChart.update();
    }
    if (categoryChart && data && data.categoryShare) {
        categoryChart.data.labels = Object.keys(data.categoryShare);
        categoryChart.data.datasets[0].data = Object.values(data.categoryShare);
        categoryChart.update();
    }
}

async function loadDashboard() {
    try {
        const [statsRes, movementsRes] = await Promise.all([
            fetch('/api/analytics/stats'),
            fetch('/api/inventory/movements')
        ]);
        if (statsRes.ok) {
            const stats = await statsRes.json();
            updateDashboardKpis(stats);
        } else {
            fallbackDashboardStats();
        }

        if (movementsRes.ok) {
            const movements = await movementsRes.json();
            cachedMovements = movements;
            renderDashboardMovements(movements.slice(0, 7));
        } else {
            fallbackDashboardMovements();
        }
    } catch (e) {
        fallbackDashboardStats();
        fallbackDashboardMovements();
    }
    await loadChartData();
}

function updateDashboardKpis(stats) {
    document.getElementById('kpiValuation').innerText = '$' + (stats.totalInventoryValue || 0).toLocaleString(undefined, {minimumFractionDigits: 2});
    document.getElementById('kpiUnits').innerText = (stats.totalStockUnits || 0).toLocaleString();
    document.getElementById('kpiProducts').innerText = stats.totalProducts || 0;
    document.getElementById('kpiLowStock').innerText = stats.lowStockProducts || 0;
    document.getElementById('kpiOrders').innerText = stats.activePurchaseOrders || 0;
}

function fallbackDashboardStats() {
    const products = JSON.parse(localStorage.getItem('pf_products') || JSON.stringify(DEFAULT_PRODUCTS));
    const totalVal = products.reduce((acc, p) => acc + (p.costPrice * p.currentStock), 0);
    const totalUnits = products.reduce((acc, p) => acc + p.currentStock, 0);
    const lowStock = products.filter(p => p.currentStock <= p.reorderLevel).length;
    const orders = JSON.parse(localStorage.getItem('pf_orders') || JSON.stringify(DEFAULT_ORDERS));
    const activeOrders = orders.filter(o => o.status !== 'RECEIVED' && o.status !== 'CANCELLED').length;

    updateDashboardKpis({
        totalInventoryValue: totalVal,
        totalStockUnits: totalUnits,
        totalProducts: products.length,
        lowStockProducts: lowStock,
        activePurchaseOrders: activeOrders
    });
}

function fallbackDashboardMovements() {
    const movements = JSON.parse(localStorage.getItem('pf_movements') || JSON.stringify(DEFAULT_MOVEMENTS));
    cachedMovements = movements;
    renderDashboardMovements(movements.slice(0, 7));
}

function renderDashboardMovements(movements) {
    const tbody = document.querySelector('#dashboardMovementsTable tbody');
    if (!tbody) return;
    if (!movements || movements.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; color: var(--text-muted);">No recent movements recorded.</td></tr>';
        return;
    }
    tbody.innerHTML = movements.map(m => `
        <tr>
            <td>${formatDate(m.timestamp)}</td>
            <td><strong>${m.product ? m.product.sku : ''}</strong> - ${m.product ? m.product.name : ''}</td>
            <td>${m.location ? m.location.name : 'Central Warehouse'}</td>
            <td><span class="badge ${getMovementBadgeClass(m.type)}">${m.type}</span></td>
            <td style="font-weight: 700; color: ${m.quantityChange > 0 ? '#10b981' : '#ef4444'};">
                ${m.quantityChange > 0 ? '+' : ''}${m.quantityChange}
            </td>
            <td>${m.newStock}</td>
            <td>${m.performedBy || 'System'}</td>
            <td>${m.reason || ''} ${m.referenceNumber ? `(${m.referenceNumber})` : ''}</td>
        </tr>
    `).join('');
}

// ==================== 2. EXPLORATORY DATA ANALYSIS (EDA) CHARTS ====================
function initEdaCharts() {
    // 1. Category Revenue
    const ctxCat = document.getElementById('edaCategoryRevenueChart');
    if (ctxCat) {
        edaCatRevChart = new Chart(ctxCat.getContext('2d'), {
            type: 'bar',
            data: {
                labels: ['Electronics & Computing', 'Industrial Components', 'Raw Materials', 'Packaging & Logistics', 'Automated Sensors'],
                datasets: [{
                    label: 'Revenue ($)',
                    data: [284500, 192300, 168400, 89200, 142100],
                    backgroundColor: ['#2563eb', '#1d4ed8', '#0284c7', '#06b6d4', '#10b981'],
                    borderRadius: 6
                }]
            },
            options: {
                indexAxis: 'y',
                responsive: true,
                maintainAspectRatio: false,
                scales: { x: { grid: { color: '#e0f2fe' } }, y: { grid: { display: false } } }
            }
        });
    }

    // 2. City Revenue
    const ctxCity = document.getElementById('edaCityRevenueChart');
    if (ctxCity) {
        edaCityRevChart = new Chart(ctxCity.getContext('2d'), {
            type: 'bar',
            data: {
                labels: ['New York', 'San Francisco', 'Austin', 'Seattle', 'Chicago', 'Dallas', 'Boston'],
                datasets: [{
                    label: 'Revenue ($)',
                    data: [245000, 218000, 164000, 148000, 122000, 105000, 89000],
                    backgroundColor: '#2563eb',
                    borderRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: { y: { beginAtZero: true, grid: { color: '#e0f2fe' } }, x: { grid: { display: false } } }
            }
        });
    }

    // 3. Store Type Revenue
    const ctxStore = document.getElementById('edaStoreTypeChart');
    if (ctxStore) {
        edaStoreTypeChart = new Chart(ctxStore.getContext('2d'), {
            type: 'polarArea',
            data: {
                labels: ['Central Hub / CDC', 'Regional Fulfillment', 'Express Depot', 'B2B Direct Supply', 'Direct Retail'],
                datasets: [{
                    data: [385000, 265000, 185000, 142000, 98000],
                    backgroundColor: ['rgba(37, 99, 235, 0.85)', 'rgba(6, 182, 212, 0.85)', 'rgba(16, 185, 129, 0.85)', 'rgba(245, 158, 11, 0.85)', 'rgba(99, 102, 241, 0.85)']
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { position: 'bottom' } }
            }
        });
    }

    // 4. Seasonal Revenue
    const ctxSeason = document.getElementById('edaSeasonalRevenueChart');
    if (ctxSeason) {
        edaSeasonalChart = new Chart(ctxSeason.getContext('2d'), {
            type: 'line',
            data: {
                labels: ['Q1 Spring Peak', 'Q2 Summer Growth', 'Q3 Autumn Restock', 'Q4 Year-End Surge'],
                datasets: [{
                    label: 'Seasonal Revenue ($)',
                    data: [210000, 268000, 245000, 352000],
                    borderColor: '#2563eb',
                    backgroundColor: 'rgba(37, 99, 235, 0.15)',
                    borderWidth: 3,
                    fill: true,
                    tension: 0.4,
                    pointRadius: 6,
                    pointBackgroundColor: '#38bdf8'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: { y: { beginAtZero: true, grid: { color: '#e0f2fe' } }, x: { grid: { display: false } } }
            }
        });
    }

    // 5. Holiday vs Regular Revenue
    const ctxHoliday = document.getElementById('edaHolidayChart');
    if (ctxHoliday) {
        edaHolidayChart = new Chart(ctxHoliday.getContext('2d'), {
            type: 'bar',
            data: {
                labels: ['Regular Days', 'Major Holidays', 'Quarter-End'],
                datasets: [{
                    label: 'Revenue Volume ($)',
                    data: [645000, 295000, 135000],
                    backgroundColor: ['#2563eb', '#10b981', '#f59e0b'],
                    borderRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: { y: { beginAtZero: true, grid: { color: '#e0f2fe' } } }
            }
        });
    }

    // 6. Promotion vs Non-Promotion Revenue
    const ctxPromo = document.getElementById('edaPromoChart');
    if (ctxPromo) {
        edaPromoChart = new Chart(ctxPromo.getContext('2d'), {
            type: 'doughnut',
            data: {
                labels: ['Promo Campaigns (Lift +34%)', 'Standard Catalog Pricing'],
                datasets: [{
                    data: [485000, 590000],
                    backgroundColor: ['#06b6d4', '#1e3a8a']
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { position: 'bottom' } }
            }
        });
    }

    // 7. Sales Distribution (Histogram)
    const ctxSalesDist = document.getElementById('edaSalesDistChart');
    if (ctxSalesDist) {
        edaSalesDistChart = new Chart(ctxSalesDist.getContext('2d'), {
            type: 'bar',
            data: {
                labels: ['0-20', '21-50', '51-100', '101-200', '201-500', '500+'],
                datasets: [{
                    label: 'Frequency (SKUs)',
                    data: [42, 88, 145, 96, 38, 14],
                    backgroundColor: 'rgba(37, 99, 235, 0.75)',
                    borderRadius: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: { y: { beginAtZero: true, grid: { color: '#e0f2fe' } } }
            }
        });
    }
}

async function loadEdaCharts() {
    try {
        const res = await fetch('/api/analytics/ml/eda');
        if (res.ok) {
            const data = await res.json();
            if (edaCatRevChart && data.categoryRevenue) {
                edaCatRevChart.data.labels = data.categoryRevenue.labels;
                edaCatRevChart.data.datasets[0].data = data.categoryRevenue.data;
                edaCatRevChart.update();
            }
            if (edaCityRevChart && data.cityRevenue) {
                edaCityRevChart.data.labels = data.cityRevenue.labels;
                edaCityRevChart.data.datasets[0].data = data.cityRevenue.data;
                edaCityRevChart.update();
            }
        }
    } catch (e) {}
}

// ==================== 3. FEATURE ENGINEERING & CORRELATION ====================
function initFeatureEngineeringCharts() {
    // 1. Price vs Demand Curve
    const ctxPriceDemand = document.getElementById('priceDemandChart');
    if (ctxPriceDemand) {
        priceDemandChart = new Chart(ctxPriceDemand.getContext('2d'), {
            type: 'line',
            data: {
                labels: ['$10', '$20', '$30', '$40', '$50', '$60', '$70', '$80', '$90', '$100'],
                datasets: [{
                    label: 'Sales Demand (Units)',
                    data: [480, 390, 310, 245, 190, 145, 110, 82, 60, 42],
                    borderColor: '#2563eb',
                    backgroundColor: 'rgba(37, 99, 235, 0.1)',
                    borderWidth: 3,
                    pointBackgroundColor: '#06b6d4',
                    pointRadius: 6,
                    pointHoverRadius: 9,
                    tension: 0.35,
                    fill: true
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: { beginAtZero: true, title: { display: true, text: 'Quantity Demanded' }, grid: { color: '#e0f2fe' } },
                    x: { title: { display: true, text: 'Unit Price ($)' }, grid: { display: false } }
                }
            }
        });
    }

    renderCorrelationMatrix();
    renderFeatureImportance();
}

function renderCorrelationMatrix() {
    const container = document.getElementById('correlationMatrixContainer');
    if (!container) return;

    const features = ["Demand", "Price", "Discount", "LeadTime", "Stock", "Season", "Risk"];
    const matrix = [
        [1.00, -0.74, 0.68, -0.42, 0.28, 0.58, -0.65],
        [-0.74, 1.00, -0.31, 0.22, 0.15, -0.26, 0.48],
        [0.68, -0.31, 1.00, -0.18, -0.22, 0.44, -0.35],
        [-0.42, 0.22, -0.18, 1.00, -0.52, -0.12, 0.72],
        [0.28, 0.15, -0.22, -0.52, 1.00, 0.19, -0.84],
        [0.58, -0.26, 0.44, -0.12, 0.19, 1.00, -0.29],
        [-0.65, 0.48, -0.35, 0.72, -0.84, -0.29, 1.00]
    ];

    let html = '<table class="corr-matrix-table"><thead><tr><th></th>';
    features.forEach(f => { html += `<th>${f}</th>`; });
    html += '</tr></thead><tbody>';

    for (let r = 0; r < features.length; r++) {
        html += `<tr><th>${features[r]}</th>`;
        for (let c = 0; c < features.length; c++) {
            const val = matrix[r][c];
            const color = getCorrelationColor(val);
            html += `<td class="corr-matrix-cell" style="background: ${color};" title="${features[r]} vs ${features[c]}: ${val.toFixed(2)}">${val.toFixed(2)}</td>`;
        }
        html += '</tr>';
    }
    html += '</tbody></table>';
    container.innerHTML = html;
}

function getCorrelationColor(val) {
    if (val === 1.0) return '#1e3a8a';
    if (val > 0.5) return 'rgba(37, 99, 235, ' + val + ')';
    if (val > 0) return 'rgba(6, 182, 212, ' + (val + 0.2) + ')';
    if (val < -0.5) return 'rgba(239, 68, 68, ' + Math.abs(val) + ')';
    return 'rgba(245, 158, 11, ' + (Math.abs(val) + 0.2) + ')';
}

function renderFeatureImportance() {
    const container = document.getElementById('featureImportanceContainer');
    if (!container) return;

    const feats = [
        { name: "Lag-7 Demand", val: 32 },
        { name: "Rolling 30-Day Mean", val: 24 },
        { name: "Price Elasticity", val: 16 },
        { name: "Promotional Discount Flag", val: 11 },
        { name: "Supplier Lead Time", val: 9 },
        { name: "Seasonal Quarter Index", val: 5 },
        { name: "Holiday Indicator", val: 3 }
    ];

    container.innerHTML = feats.map(f => `
        <div class="feat-bar-row">
            <span class="feat-bar-label">${f.name}</span>
            <div class="feat-bar-track">
                <div class="feat-bar-fill" style="width: ${f.val * 2.8}%;"></div>
            </div>
            <span class="feat-bar-pct">${f.val}%</span>
        </div>
    `).join('');
}

async function loadFeatureEngineeringData() {
    renderCorrelationMatrix();
    renderFeatureImportance();
}

// ==================== 4. AI DEMAND FORECASTING ====================
function initForecastingCharts() {
    const ctxWeekly = document.getElementById('weeklyRevenueForecastChart');
    if (ctxWeekly) {
        weeklyForecastChart = new Chart(ctxWeekly.getContext('2d'), {
            type: 'line',
            data: {
                labels: ["Wk 35", "Wk 36", "Wk 37", "Wk 38", "Wk 39", "Wk 40", "Wk 41", "Wk 42", "Wk 43", "Wk 44", "Wk 45 (F)", "Wk 46 (F)", "Wk 47 (F)", "Wk 48 (F)"],
                datasets: [
                    {
                        label: 'Actual Revenue ($)',
                        data: [42000, 45200, 43800, 48900, 51200, 49800, 53400, 55100, 54200, 58000, null, null, null, null],
                        borderColor: '#2563eb',
                        backgroundColor: '#2563eb',
                        borderWidth: 3,
                        pointRadius: 5,
                        tension: 0.3
                    },
                    {
                        label: 'AI Forecast ($)',
                        data: [null, null, null, null, null, null, null, null, null, 58000, 60200, 62400, 65100, 68000],
                        borderColor: '#10b981',
                        backgroundColor: '#10b981',
                        borderWidth: 3,
                        borderDash: [5, 5],
                        pointRadius: 6,
                        pointBackgroundColor: '#10b981',
                        tension: 0.3
                    },
                    {
                        label: 'Upper 95% Confidence Band',
                        data: [null, null, null, null, null, null, null, null, null, 59900, 63100, 65800, 68900, 72300],
                        borderColor: 'transparent',
                        backgroundColor: 'rgba(56, 189, 248, 0.15)',
                        fill: '+1',
                        pointRadius: 0
                    },
                    {
                        label: 'Lower 95% Confidence Band',
                        data: [null, null, null, null, null, null, null, null, null, 55100, 57300, 59000, 61300, 63700],
                        borderColor: 'transparent',
                        backgroundColor: 'transparent',
                        fill: false,
                        pointRadius: 0
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: { beginAtZero: false, grid: { color: '#e0f2fe' } },
                    x: { grid: { display: false } }
                },
                plugins: { legend: { position: 'top' } }
            }
        });
    }
}

async function loadForecastingChartData() {
    if (weeklyForecastChart) weeklyForecastChart.update();
}

// ==================== 5. MODEL EVALUATION PROCESS ====================
function initModelEvaluationView() {
    // Residual Chart
    const ctxResidual = document.getElementById('residualChart');
    if (ctxResidual) {
        residualChart = new Chart(ctxResidual.getContext('2d'), {
            type: 'bar',
            data: {
                labels: ['-3σ', '-2σ', '-1σ', '0', '+1σ', '+2σ', '+3σ'],
                datasets: [{
                    label: 'Residual Error Density',
                    data: [3, 14, 68, 142, 65, 12, 2],
                    backgroundColor: '#2563eb',
                    borderRadius: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: { y: { beginAtZero: true, grid: { color: '#e0f2fe' } } }
            }
        });
    }

    // Model R² Comparison Chart
    const ctxR2 = document.getElementById('modelR2Chart');
    if (ctxR2) {
        modelR2Chart = new Chart(ctxR2.getContext('2d'), {
            type: 'bar',
            data: {
                labels: ['XGBoost (Tuned)', 'Prophet', 'LSTM Net', 'SARIMAX'],
                datasets: [{
                    label: 'R² Accuracy Score',
                    data: [0.942, 0.915, 0.931, 0.864],
                    backgroundColor: ['#10b981', '#3b82f6', '#06b6d4', '#94a3b8'],
                    borderRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: { y: { min: 0.8, max: 1.0, grid: { color: '#e0f2fe' } } }
            }
        });
    }

    loadModelEvaluationTable();
}

function loadModelEvaluationTable() {
    const tbody = document.querySelector('#modelEvaluationTable tbody');
    if (!tbody) return;

    const models = [
        { name: "XGBoost Regressor (Hyperparameter Tuned)", mae: 142.50, rmse: 188.20, mape: "4.12%", r2: 0.942, status: "badge-champion", statusText: "CHAMPION / PRODUCTION" },
        { name: "LSTM Recurrent Neural Network", mae: 155.80, rmse: 199.10, mape: "4.65%", r2: 0.931, status: "badge-challenger", statusText: "CHALLENGER" },
        { name: "Prophet Time-Series (Meta Algorithm)", mae: 168.30, rmse: 215.40, mape: "5.28%", r2: 0.915, status: "badge-benchmark", statusText: "BENCHMARK" },
        { name: "SARIMAX (p=2, d=1, q=2)", mae: 210.40, rmse: 274.60, mape: "6.85%", r2: 0.864, status: "badge-baseline", statusText: "BASELINE" }
    ];

    tbody.innerHTML = models.map(m => `
        <tr>
            <td><strong>${m.name}</strong></td>
            <td>$${m.mae.toFixed(2)}</td>
            <td>$${m.rmse.toFixed(2)}</td>
            <td><strong style="color: #10b981;">${m.mape}</strong></td>
            <td><strong>${m.r2.toFixed(3)}</strong></td>
            <td><span class="badge ${m.status}">${m.statusText}</span></td>
        </tr>
    `).join('');
}

// ==================== 6. INVENTORY OPTIMIZATION ====================
function initOptimizationView() {
    const ctxInv = document.getElementById('inventoryHealthMonthlyChart');
    if (ctxInv) {
        invHealthMonthlyChart = new Chart(ctxInv.getContext('2d'), {
            type: 'bar',
            data: {
                labels: ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"],
                datasets: [
                    {
                        type: 'bar',
                        label: 'Monthly Revenue ($)',
                        data: [185000, 192000, 210000, 225000, 240000, 255000, 248000, 262000, 278000, 295000, 310000, 345000],
                        backgroundColor: 'rgba(37, 99, 235, 0.85)',
                        borderRadius: 6,
                        yAxisID: 'y'
                    },
                    {
                        type: 'line',
                        label: 'Inventory Health Index (%)',
                        data: [78, 82, 85, 89, 91, 94, 92, 95, 96, 94, 97, 98],
                        borderColor: '#10b981',
                        borderWidth: 3,
                        pointBackgroundColor: '#10b981',
                        pointRadius: 5,
                        tension: 0.3,
                        yAxisID: 'y1'
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        type: 'linear',
                        position: 'left',
                        title: { display: true, text: 'Monthly Gross Revenue ($)' },
                        grid: { color: '#e0f2fe' }
                    },
                    y1: {
                        type: 'linear',
                        position: 'right',
                        min: 60,
                        max: 100,
                        title: { display: true, text: 'Health Index (%)' },
                        grid: { drawOnChartArea: false }
                    }
                }
            }
        });
    }
}

function handleServiceLevelChange(val) {
    const v = parseInt(val);
    let z = 1.28;
    if (v >= 99) z = 2.326;
    else if (v >= 98) z = 2.054;
    else if (v >= 95) z = 1.645;
    else if (v >= 90) z = 1.282;
    else z = 1.036;

    const buffer = Math.round(10 * z);
    const stockout = (100 - v).toFixed(1);
    const capital = Math.round(buffer * 1025);

    document.getElementById('slBadge').innerText = `Service Level: ${v}.0% (Z = ${z.toFixed(3)})`;
    document.getElementById('simBufferQty').innerText = `${buffer} units`;
    document.getElementById('simStockoutRate').innerText = `${stockout}%`;
    document.getElementById('simCapitalReq').innerText = `$${capital.toLocaleString()}`;
}

async function loadOptimizationChart() {
    if (invHealthMonthlyChart) invHealthMonthlyChart.update();
}

// ==================== 7. SMART FORESIGHT AI SUGGESTIONS ====================
async function loadSuggestions() {
    const container = document.getElementById('suggestionsContainer');
    if (!container) return;
    try {
        const res = await fetch('/api/analytics/suggestions');
        if (res.ok) {
            const suggestions = await res.json();
            renderSuggestionsList(suggestions);
            return;
        }
    } catch (e) {}

    const products = JSON.parse(localStorage.getItem('pf_products') || JSON.stringify(DEFAULT_PRODUCTS));
    const suggestions = products.map(p => {
        const burnRate = 1.2;
        const runway = p.currentStock > 0 ? (p.currentStock / burnRate).toFixed(1) : 0;
        let risk = 'HEALTHY';
        let action = 'Maintain Monitoring';
        let recQty = 0;
        let msg = `Healthy stock balance with ~${runway} days of inventory remaining.`;

        if (p.currentStock <= 0) {
            risk = 'CRITICAL';
            action = 'Create Emergency PO';
            recQty = p.maxStockLevel || 50;
            msg = 'Stockout detected! Zero units on hand. Immediate emergency replenishment required.';
        } else if (p.currentStock <= p.reorderLevel) {
            risk = 'HIGH';
            action = 'Reorder Now';
            recQty = (p.maxStockLevel || 80) - p.currentStock;
            msg = `Stock (${p.currentStock}) is below reorder threshold (${p.reorderLevel}). Stock will deplete in ${runway} days.`;
        } else if (p.currentStock > (p.maxStockLevel || 100) * 1.5) {
            risk = 'OVERSTOCKED';
            action = 'Halt Purchasing';
            recQty = 0;
            msg = `Excess inventory (${p.currentStock} units) holding excessive working capital.`;
        }

        return {
            productId: p.id,
            sku: p.sku,
            productName: p.name,
            categoryName: p.category ? p.category.name : 'General',
            currentStock: p.currentStock,
            reorderLevel: p.reorderLevel,
            avgDailyDemand: burnRate,
            daysOfRunway: runway,
            riskLevel: risk,
            recommendedOrderQuantity: recQty,
            suggestionMessage: msg,
            primaryAction: action,
            supplierName: p.supplier ? p.supplier.name : 'Apex Semiconductors Ltd',
            supplierLeadTimeDays: p.supplier ? p.supplier.leadTimeDays : 7
        };
    }).sort((a, b) => {
        const rank = { 'CRITICAL': 1, 'HIGH': 2, 'MODERATE': 3, 'OVERSTOCKED': 4, 'HEALTHY': 5 };
        return rank[a.riskLevel] - rank[b.riskLevel];
    });

    renderSuggestionsList(suggestions);
}

function renderSuggestionsList(suggestions) {
    const container = document.getElementById('suggestionsContainer');
    if (!container) return;
    if (!suggestions || suggestions.length === 0) {
        container.innerHTML = '<div class="card"><p style="color: var(--text-muted); text-align: center;">All inventory levels are optimal.</p></div>';
        return;
    }

    container.innerHTML = suggestions.map(s => `
        <div class="suggestion-card risk-${s.riskLevel}">
            <div class="suggestion-content">
                <div class="suggestion-header">
                    <span class="badge badge-${s.riskLevel.toLowerCase()}">${s.riskLevel}</span>
                    <span class="suggestion-title">${s.productName} (${s.sku})</span>
                    <span style="font-size: 0.75rem; color: var(--text-muted);">• ${s.categoryName}</span>
                </div>
                <div class="suggestion-desc">${s.suggestionMessage}</div>
                <div class="suggestion-metrics">
                    <span><strong>Current Stock:</strong> ${s.currentStock}</span>
                    <span><strong>Reorder Level:</strong> ${s.reorderLevel}</span>
                    <span><strong>Avg Daily Demand:</strong> ${s.avgDailyDemand} units/day</span>
                    <span><strong>Runway Remaining:</strong> ${s.daysOfRunway > 300 ? '300+ days' : s.daysOfRunway + ' days'}</span>
                    <span><strong>Lead Time:</strong> ${s.supplierLeadTimeDays} days</span>
                    <span><strong>Supplier:</strong> ${s.supplierName}</span>
                </div>
            </div>
            <div>
                ${s.recommendedOrderQuantity > 0 ? `
                    <button class="btn btn-primary btn-sm" onclick="quickReorderFromSuggestion(${s.productId}, ${s.recommendedOrderQuantity})">
                        ${s.primaryAction} (${s.recommendedOrderQuantity} units)
                    </button>
                ` : `
                    <span class="badge badge-healthy">Optimal</span>
                `}
            </div>
        </div>
    `).join('');
}

function quickReorderFromSuggestion(productId, suggestedQty) {
    const product = cachedProducts.find(p => p.id === productId);
    openCreatePoModal();
    if (product && product.supplier) {
        document.getElementById('poSupplier').value = product.supplier.id;
    }
    setTimeout(() => {
        const prodSelect = document.querySelector('.po-item-product');
        const qtyInput = document.querySelector('.po-item-qty');
        const priceInput = document.querySelector('.po-item-price');
        if (prodSelect) prodSelect.value = productId;
        if (qtyInput) qtyInput.value = suggestedQty;
        if (priceInput && product) priceInput.value = product.costPrice;
    }, 200);
}

// ==================== 8. PRODUCTS CATALOGUE ====================
async function loadProducts() {
    try {
        const search = document.getElementById('productSearchInput') ? document.getElementById('productSearchInput').value : '';
        const catId = document.getElementById('productCategoryFilter') ? document.getElementById('productCategoryFilter').value : '';
        let url = `/api/products?page=0&size=100`;
        if (search) url += `&search=${encodeURIComponent(search)}`;
        if (catId) url += `&categoryId=${catId}`;

        const res = await fetch(url);
        if (res.ok) {
            cachedProducts = await res.json();
            renderProductsTable(cachedProducts);
            updateProductSelectors();
            return;
        }
    } catch (e) {}

    let products = JSON.parse(localStorage.getItem('pf_products') || JSON.stringify(DEFAULT_PRODUCTS));
    const search = document.getElementById('productSearchInput') ? document.getElementById('productSearchInput').value.toLowerCase() : '';
    const catId = document.getElementById('productCategoryFilter') ? document.getElementById('productCategoryFilter').value : '';

    if (search) products = products.filter(p => p.name.toLowerCase().includes(search) || p.sku.toLowerCase().includes(search));
    if (catId) products = products.filter(p => p.category && p.category.id == catId);

    cachedProducts = products;
    renderProductsTable(products);
    updateProductSelectors();
}

function handleProductSearch() {
    loadProducts();
}

function renderProductsTable(products) {
    const tbody = document.querySelector('#productsTable tbody');
    if (!tbody) return;
    if (!products || products.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" style="text-align: center; color: var(--text-muted); padding: 2rem;">No products found. Click "Add New Product" to create one.</td></tr>';
        return;
    }

    tbody.innerHTML = products.map(p => {
        const isLow = p.currentStock <= p.reorderLevel;
        return `
            <tr>
                <td>
                    <div style="display: flex; align-items: center; gap: 0.75rem;">
                        <img src="${p.imageUrl || 'https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=80&auto=format&fit=crop'}" 
                             alt="${p.name}" style="width: 36px; height: 36px; border-radius: 6px; object-fit: cover; border: 1px solid var(--border);">
                        <div>
                            <strong>${p.name}</strong>
                            <div style="font-size: 0.75rem; color: var(--text-muted);">${p.unit}</div>
                        </div>
                    </div>
                </td>
                <td><code>${p.sku}</code></td>
                <td>${p.category ? p.category.name : '-'}</td>
                <td>
                    <span style="font-size: 1rem; font-weight: 700; color: ${isLow ? '#ef4444' : 'inherit'};">
                        ${p.currentStock}
                    </span>
                    ${isLow ? '<span style="color: #ef4444; font-size: 0.75rem; font-weight: 700; margin-left: 4px;">⚠️ LOW</span>' : ''}
                </td>
                <td>${p.reorderLevel}</td>
                <td>$${Number(p.costPrice).toFixed(2)} / $${Number(p.sellingPrice).toFixed(2)}</td>
                <td>${p.defaultLocation ? p.defaultLocation.name : 'Central'}</td>
                <td>
                    <span class="badge ${isLow ? 'badge-critical' : 'badge-healthy'}">
                        ${isLow ? 'REORDER NEEDED' : 'IN STOCK'}
                    </span>
                </td>
                <td>
                    <div style="display: flex; gap: 0.375rem;">
                        <button class="btn btn-outline btn-sm" onclick="quickStockAdjust(${p.id})" title="Quick Stock Adjustment">± Stock</button>
                        <button class="btn btn-outline btn-sm" onclick="editProduct(${p.id})" title="Edit Details & Quantity">✏️</button>
                        <button class="btn btn-outline btn-sm" onclick="deleteProduct(${p.id})" title="Deactivate" style="color: #ef4444;">🗑️</button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

function updateProductSelectors() {
    const adjProd = document.getElementById('adjProduct');
    const transProd = document.getElementById('transProduct');
    const opts = '<option value="">Select Product</option>' + cachedProducts.map(p => `
        <option value="${p.id}" data-price="${p.costPrice}">${p.sku} - ${p.name} (Stock: ${p.currentStock})</option>
    `).join('');

    if (adjProd) adjProd.innerHTML = opts;
    if (transProd) transProd.innerHTML = opts;
}

function openProductModal(product = null) {
    document.getElementById('productModalTitle').innerText = product ? 'Edit Product Details & Quantity' : 'Add New Product';
    document.getElementById('prodSubmitBtn').innerText = product ? 'Update Product' : 'Save Product';
    document.getElementById('prodEditId').value = product ? product.id : '';
    document.getElementById('prodSku').value = product ? product.sku : '';
    document.getElementById('prodName').value = product ? product.name : '';
    document.getElementById('prodDesc').value = product ? (product.description || '') : '';
    document.getElementById('prodCost').value = product ? product.costPrice : '';
    document.getElementById('prodPrice').value = product ? product.sellingPrice : '';
    document.getElementById('prodStock').value = product ? product.currentStock : '0';
    document.getElementById('prodReorder').value = product ? product.reorderLevel : '10';
    document.getElementById('prodUnit').value = product ? product.unit : 'PCS';
    document.getElementById('prodImageUrl').value = product ? (product.imageUrl || '') : '';

    if (product && product.category) document.getElementById('prodCategory').value = product.category.id;
    if (product && product.supplier) document.getElementById('prodSupplier').value = product.supplier.id;
    if (product && product.defaultLocation) document.getElementById('prodLocation').value = product.defaultLocation.id;

    openModal('productModal');
}

function editProduct(id) {
    const p = cachedProducts.find(x => x.id === id);
    if (p) openProductModal(p);
}

async function handleProductSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('prodEditId').value;
    const body = {
        sku: document.getElementById('prodSku').value.trim(),
        name: document.getElementById('prodName').value.trim(),
        description: document.getElementById('prodDesc').value.trim(),
        categoryId: parseInt(document.getElementById('prodCategory').value),
        supplierId: document.getElementById('prodSupplier').value ? parseInt(document.getElementById('prodSupplier').value) : null,
        locationId: document.getElementById('prodLocation').value ? parseInt(document.getElementById('prodLocation').value) : null,
        costPrice: parseFloat(document.getElementById('prodCost').value),
        sellingPrice: parseFloat(document.getElementById('prodPrice').value),
        currentStock: parseInt(document.getElementById('prodStock').value),
        reorderLevel: parseInt(document.getElementById('prodReorder').value),
        unit: document.getElementById('prodUnit').value,
        imageUrl: document.getElementById('prodImageUrl').value.trim()
    };

    try {
        const url = id ? `/api/products/${id}` : '/api/products';
        const method = id ? 'PUT' : 'POST';
        const res = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });

        if (res.ok) {
            closeModal('productModal');
            showToast(id ? 'Product updated successfully!' : 'Product created successfully!', 'success');
            await loadProducts();
            await loadDashboard();
            await loadSuggestions();
            return;
        }
    } catch (err) {}

    let products = JSON.parse(localStorage.getItem('pf_products') || JSON.stringify(DEFAULT_PRODUCTS));
    const cat = cachedCategories.find(c => c.id == body.categoryId) || DEFAULT_CATEGORIES[0];
    const sup = cachedSuppliers.find(s => s.id == body.supplierId) || DEFAULT_SUPPLIERS[0];
    const loc = cachedWarehouses.find(w => w.id == body.locationId) || DEFAULT_WAREHOUSES[0];

    if (id) {
        const idx = products.findIndex(p => p.id == id);
        if (idx !== -1) products[idx] = { ...products[idx], ...body, category: cat, supplier: sup, defaultLocation: loc };
    } else {
        products.push({ id: Date.now(), ...body, category: cat, supplier: sup, defaultLocation: loc });
    }
    localStorage.setItem('pf_products', JSON.stringify(products));

    closeModal('productModal');
    showToast(id ? 'Product updated successfully!' : 'Product created successfully!', 'success');
    await loadProducts();
    await loadDashboard();
    await loadSuggestions();
}

async function deleteProduct(id) {
    if (!confirm('Are you sure you want to deactivate this product?')) return;
    try {
        const res = await fetch(`/api/products/${id}`, { method: 'DELETE' });
        if (res.ok) {
            showToast('Product deactivated', 'success');
            loadProducts();
            loadDashboard();
            return;
        }
    } catch (e) {}

    let products = JSON.parse(localStorage.getItem('pf_products') || JSON.stringify(DEFAULT_PRODUCTS));
    products = products.filter(p => p.id != id);
    localStorage.setItem('pf_products', JSON.stringify(products));
    showToast('Product deactivated', 'success');
    loadProducts();
    loadDashboard();
}

function quickStockAdjust(productId) {
    openModal('adjustmentModal');
    if (productId) document.getElementById('adjProduct').value = productId;
}

function openAdjustmentModal() {
    openModal('adjustmentModal');
}

async function handleAdjustmentSubmit(e) {
    e.preventDefault();
    const prodId = parseInt(document.getElementById('adjProduct').value);
    const type = document.getElementById('adjType').value;
    let qty = parseInt(document.getElementById('adjQty').value);
    const reason = document.getElementById('adjReason').value.trim();

    try {
        const res = await fetch('/api/inventory/adjustment', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                productId: prodId,
                type: type,
                quantityChange: qty,
                locationId: document.getElementById('adjLocation').value ? parseInt(document.getElementById('adjLocation').value) : null,
                reason: reason
            })
        });
        if (res.ok) {
            closeModal('adjustmentModal');
            showToast('Stock movement recorded successfully!', 'success');
            loadProducts();
            loadDashboard();
            loadSuggestions();
            loadAuditMovements();
            return;
        }
    } catch (e) {}

    let products = JSON.parse(localStorage.getItem('pf_products') || JSON.stringify(DEFAULT_PRODUCTS));
    const p = products.find(x => x.id == prodId);
    if (p) {
        let delta = (type === 'STOCK_OUT') ? -qty : qty;
        const prev = p.currentStock;
        p.currentStock = Math.max(0, p.currentStock + delta);
        localStorage.setItem('pf_products', JSON.stringify(products));

        let movements = JSON.parse(localStorage.getItem('pf_movements') || JSON.stringify(DEFAULT_MOVEMENTS));
        movements.unshift({
            id: Date.now(),
            timestamp: new Date().toISOString(),
            product: p,
            location: p.defaultLocation,
            type: type,
            quantityChange: delta,
            previousStock: prev,
            newStock: p.currentStock,
            reason: reason || type,
            referenceNumber: 'ADJ-' + Math.floor(Math.random() * 9000 + 1000),
            performedBy: 'Current User'
        });
        localStorage.setItem('pf_movements', JSON.stringify(movements));
    }

    closeModal('adjustmentModal');
    showToast('Stock movement recorded successfully!', 'success');
    loadProducts();
    loadDashboard();
    loadSuggestions();
    loadAuditMovements();
}

function openTransferModal() {
    openModal('transferModal');
}

async function handleTransferSubmit(e) {
    e.preventDefault();
    const prodId = parseInt(document.getElementById('transProduct').value);
    const fromId = parseInt(document.getElementById('transFrom').value);
    const toId = parseInt(document.getElementById('transTo').value);
    const qty = parseInt(document.getElementById('transQty').value);
    const reason = document.getElementById('transReason').value.trim();

    try {
        const res = await fetch('/api/inventory/transfer', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ productId: prodId, fromLocationId: fromId, toLocationId: toId, quantity: qty, reason })
        });
        if (res.ok) {
            closeModal('transferModal');
            showToast('Stock transfer completed!', 'success');
            loadDashboard();
            loadAuditMovements();
            return;
        }
    } catch (e) {}

    let products = JSON.parse(localStorage.getItem('pf_products') || JSON.stringify(DEFAULT_PRODUCTS));
    const p = products.find(x => x.id == prodId);
    let movements = JSON.parse(localStorage.getItem('pf_movements') || JSON.stringify(DEFAULT_MOVEMENTS));
    movements.unshift({
        id: Date.now(),
        timestamp: new Date().toISOString(),
        product: p,
        location: cachedWarehouses.find(w => w.id == fromId),
        toLocation: cachedWarehouses.find(w => w.id == toId),
        type: 'TRANSFER',
        quantityChange: qty,
        previousStock: p ? p.currentStock : 0,
        newStock: p ? p.currentStock : 0,
        reason: reason || 'Inter-warehouse rebalancing',
        referenceNumber: 'TR-' + Math.floor(Math.random() * 9000 + 1000),
        performedBy: 'Current User'
    });
    localStorage.setItem('pf_movements', JSON.stringify(movements));

    closeModal('transferModal');
    showToast('Stock transfer completed!', 'success');
    loadDashboard();
    loadAuditMovements();
}

function openCsvModal() {
    openModal('csvModal');
}

async function handleCsvUpload(e) {
    e.preventDefault();
    const fileInput = document.getElementById('csvFileInput');
    if (!fileInput.files || fileInput.files.length === 0) return;

    const file = fileInput.files[0];
    const reader = new FileReader();
    reader.onload = function(evt) {
        const lines = evt.target.result.split('\n');
        let count = 0;
        let products = JSON.parse(localStorage.getItem('pf_products') || JSON.stringify(DEFAULT_PRODUCTS));

        for (let i = 1; i < lines.length; i++) {
            const line = lines[i].trim();
            if (!line) continue;
            const parts = line.split(',');
            if (parts.length >= 6) {
                products.push({
                    id: Date.now() + i,
                    sku: parts[0].trim(),
                    name: parts[1].trim(),
                    category: DEFAULT_CATEGORIES.find(c => c.name.toLowerCase().includes(parts[2].trim().toLowerCase())) || DEFAULT_CATEGORIES[0],
                    supplier: DEFAULT_SUPPLIERS[0],
                    defaultLocation: DEFAULT_WAREHOUSES[0],
                    costPrice: parseFloat(parts[3].trim()) || 20,
                    sellingPrice: parseFloat(parts[4].trim()) || 40,
                    currentStock: parseInt(parts[5].trim()) || 10,
                    reorderLevel: parts[6] ? parseInt(parts[6].trim()) : 10,
                    unit: "PCS",
                    imageUrl: "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=200&auto=format&fit=crop"
                });
                count++;
            }
        }
        localStorage.setItem('pf_products', JSON.stringify(products));
        closeModal('csvModal');
        showToast(`Successfully imported ${count} products from CSV!`, 'success');
        loadProducts();
        loadDashboard();
        loadSuggestions();
    };
    reader.readAsText(file);
}

function downloadSampleCsv() {
    const csvContent = "data:text/csv;charset=utf-8," + 
        "sku,name,category,costPrice,sellingPrice,stock,reorderLevel\n" +
        "PRD-SAMPLE-1,High Torque Servo,Industrial Components,42.00,68.00,30,10\n" +
        "PRD-SAMPLE-2,Fiber Optic Transceiver,Electronics & Computing,75.00,120.00,15,5\n" +
        "PRD-SAMPLE-3,Stainless Fastener Kit,Raw Materials & Metals,12.50,22.00,100,20\n";
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", "foresight_product_sample.csv");
    document.body.appendChild(link);
    link.click();
    link.remove();
}

// ==================== 9. PURCHASE ORDERS ====================
async function loadOrders() {
    try {
        const res = await fetch('/api/purchase-orders');
        if (res.ok) {
            cachedOrders = await res.json();
            renderOrdersTable(cachedOrders);
            return;
        }
    } catch (e) {}

    const orders = JSON.parse(localStorage.getItem('pf_orders') || JSON.stringify(DEFAULT_ORDERS));
    cachedOrders = orders;
    renderOrdersTable(orders);
}

function renderOrdersTable(orders) {
    const tbody = document.querySelector('#ordersTable tbody');
    if (!tbody) return;
    if (!orders || orders.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; color: var(--text-muted); padding: 2rem;">No purchase orders found.</td></tr>';
        return;
    }

    tbody.innerHTML = orders.map(po => {
        const itemsSummary = (po.items || []).map(i => `${i.product ? i.product.sku : ''}: ${i.quantityReceived || 0}/${i.quantityOrdered}`).join(', ');
        return `
            <tr>
                <td><strong>${po.orderNumber}</strong></td>
                <td>${po.supplier ? po.supplier.name : 'Unknown'}</td>
                <td>${po.targetLocation ? po.targetLocation.name : 'Main Warehouse'}</td>
                <td>${formatDate(po.createdAt)}</td>
                <td><strong>$${(po.totalAmount || 0).toFixed(2)}</strong></td>
                <td><span class="badge badge-${po.status.toLowerCase()}">${po.status}</span></td>
                <td><small>${itemsSummary}</small></td>
                <td>
                    <div style="display: flex; gap: 0.375rem;">
                        ${po.status === 'DRAFT' ? `
                            <button class="btn btn-outline btn-sm" onclick="sendOrder(${po.id})">Send Order</button>
                        ` : ''}
                        ${(po.status === 'ORDERED' || po.status === 'PARTIALLY_RECEIVED') ? `
                            <button class="btn btn-primary btn-sm" onclick="openReceiveModal(${po.id})">Receive Items</button>
                        ` : ''}
                        ${(po.status === 'DRAFT' || po.status === 'ORDERED') ? `
                            <button class="btn btn-outline btn-sm" onclick="cancelOrder(${po.id})" style="color: #ef4444;">Cancel</button>
                        ` : ''}
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

function openCreatePoModal() {
    document.getElementById('poLineItemsContainer').innerHTML = '';
    addPoLineItemRow();
    openModal('createPoModal');
}

function addPoLineItemRow() {
    const container = document.getElementById('poLineItemsContainer');
    const row = document.createElement('div');
    row.className = 'form-row po-line-item-row';
    row.style.marginBottom = '0.5rem';
    row.innerHTML = `
        <div style="flex: 2;">
            <select class="form-control po-item-product" onchange="handlePoProductChange(this)" required>
                <option value="">Select Item</option>
                ${cachedProducts.map(p => `<option value="${p.id}" data-price="${p.costPrice}">${p.sku} - ${p.name}</option>`).join('')}
            </select>
        </div>
        <div style="flex: 1;">
            <input type="number" class="form-control po-item-qty" placeholder="Qty" min="1" value="20" required>
        </div>
        <div style="flex: 1;">
            <input type="number" step="0.01" class="form-control po-item-price" placeholder="Unit $" required>
        </div>
        <div style="width: 36px; display: flex; align-items: center;">
            <button type="button" class="btn btn-outline btn-sm" onclick="this.closest('.po-line-item-row').remove()" style="color: #ef4444;">&times;</button>
        </div>
    `;
    container.appendChild(row);
}

function handlePoProductChange(select) {
    const selectedOption = select.options[select.selectedIndex];
    const price = selectedOption.getAttribute('data-price');
    const row = select.closest('.po-line-item-row');
    if (price && row) row.querySelector('.po-item-price').value = price;
}

async function handleCreatePoSubmit(e) {
    e.preventDefault();
    const supId = parseInt(document.getElementById('poSupplier').value);
    const locId = parseInt(document.getElementById('poLocation').value);
    const notes = document.getElementById('poNotes').value.trim();

    const rows = document.querySelectorAll('.po-line-item-row');
    const items = [];
    let total = 0;

    rows.forEach(r => {
        const prodId = r.querySelector('.po-item-product').value;
        const qty = parseInt(r.querySelector('.po-item-qty').value);
        const unitPrice = parseFloat(r.querySelector('.po-item-price').value);
        if (prodId && qty) {
            const prod = cachedProducts.find(p => p.id == prodId);
            items.push({
                id: Date.now() + Math.random(),
                product: prod,
                quantityOrdered: qty,
                quantityReceived: 0,
                unitPrice: unitPrice,
                subtotal: qty * unitPrice
            });
            total += qty * unitPrice;
        }
    });

    if (items.length === 0) {
        showToast('Please add at least one line item', 'error');
        return;
    }

    try {
        const res = await fetch('/api/purchase-orders', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ supplierId: supId, targetLocationId: locId, notes, items })
        });
        if (res.ok) {
            closeModal('createPoModal');
            showToast('Purchase Order created in DRAFT status!', 'success');
            loadOrders();
            loadDashboard();
            return;
        }
    } catch (e) {}

    let orders = JSON.parse(localStorage.getItem('pf_orders') || JSON.stringify(DEFAULT_ORDERS));
    orders.unshift({
        id: Date.now(),
        orderNumber: 'PO-' + (Math.floor(Math.random() * 900000) + 100000),
        supplier: cachedSuppliers.find(s => s.id == supId) || DEFAULT_SUPPLIERS[0],
        targetLocation: cachedWarehouses.find(w => w.id == locId) || DEFAULT_WAREHOUSES[0],
        status: 'DRAFT',
        totalAmount: total,
        createdAt: new Date().toISOString(),
        items: items
    });
    localStorage.setItem('pf_orders', JSON.stringify(orders));

    closeModal('createPoModal');
    showToast('Purchase Order created in DRAFT status!', 'success');
    loadOrders();
    loadDashboard();
}

async function sendOrder(poId) {
    try {
        const res = await fetch(`/api/purchase-orders/${poId}/send`, { method: 'POST' });
        if (res.ok) {
            showToast('Purchase Order marked as ORDERED with supplier!', 'success');
            loadOrders();
            loadDashboard();
            return;
        }
    } catch (e) {}

    let orders = JSON.parse(localStorage.getItem('pf_orders') || JSON.stringify(DEFAULT_ORDERS));
    const po = orders.find(o => o.id == poId);
    if (po) po.status = 'ORDERED';
    localStorage.setItem('pf_orders', JSON.stringify(orders));

    showToast('Purchase Order marked as ORDERED with supplier!', 'success');
    loadOrders();
    loadDashboard();
}

async function cancelOrder(poId) {
    if (!confirm('Cancel this purchase order?')) return;
    try {
        const res = await fetch(`/api/purchase-orders/${poId}/cancel`, { method: 'POST' });
        if (res.ok) {
            showToast('Purchase Order cancelled', 'info');
            loadOrders();
            loadDashboard();
            return;
        }
    } catch (e) {}

    let orders = JSON.parse(localStorage.getItem('pf_orders') || JSON.stringify(DEFAULT_ORDERS));
    const po = orders.find(o => o.id == poId);
    if (po) po.status = 'CANCELLED';
    localStorage.setItem('pf_orders', JSON.stringify(orders));

    showToast('Purchase Order cancelled', 'info');
    loadOrders();
    loadDashboard();
}

function openReceiveModal(poId) {
    const po = cachedOrders.find(o => o.id == poId);
    if (!po) return;

    document.getElementById('receivePoId').value = poId;
    document.getElementById('receivePoTitle').innerText = `Receive Items: ${po.orderNumber}`;

    const list = document.getElementById('receiveItemsList');
    list.innerHTML = po.items.map(item => {
        const received = item.quantityReceived || 0;
        const remaining = item.quantityOrdered - received;
        if (remaining <= 0) return '';
        return `
            <div style="background: #f8fafc; border: 1px solid var(--border); border-radius: var(--radius-sm); padding: 0.75rem 1rem; margin-bottom: 0.75rem;">
                <div style="display: flex; justify-content: space-between; margin-bottom: 0.375rem;">
                    <strong>${item.product ? item.product.sku : ''} - ${item.product ? item.product.name : ''}</strong>
                    <span style="font-size: 0.8125rem; color: var(--text-muted);">
                        Ordered: ${item.quantityOrdered} | Received: ${received} | <strong>Outstanding: ${remaining}</strong>
                    </span>
                </div>
                <div style="display: flex; gap: 0.75rem; align-items: center;">
                    <label style="font-size: 0.8125rem; font-weight: 600;">Qty Receiving Now:</label>
                    <input type="number" class="form-control receive-item-qty" 
                           data-item-id="${item.id}" max="${remaining}" min="0" value="${remaining}" 
                           style="max-width: 140px;">
                    <span style="font-size: 0.75rem; color: var(--text-muted);">(Max ${remaining})</span>
                </div>
            </div>
        `;
    }).join('');

    openModal('receivePoModal');
}

async function handleReceivePoSubmit(e) {
    e.preventDefault();
    const poId = document.getElementById('receivePoId').value;
    const inputs = document.querySelectorAll('.receive-item-qty');
    const receipts = [];

    inputs.forEach(input => {
        const itemId = input.getAttribute('data-item-id');
        const qty = parseInt(input.value);
        if (qty > 0) receipts.push({ itemId, quantityReceived: qty });
    });

    if (receipts.length === 0) {
        showToast('Please specify a positive quantity to receive', 'error');
        return;
    }

    try {
        const res = await fetch(`/api/purchase-orders/${poId}/receive`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(receipts)
        });
        if (res.ok) {
            closeModal('receivePoModal');
            showToast('Items received! Inventory balance updated with traceable audit movement.', 'success');
            loadOrders();
            loadProducts();
            loadDashboard();
            loadSuggestions();
            loadAuditMovements();
            return;
        }
    } catch (e) {}

    let orders = JSON.parse(localStorage.getItem('pf_orders') || JSON.stringify(DEFAULT_ORDERS));
    let products = JSON.parse(localStorage.getItem('pf_products') || JSON.stringify(DEFAULT_PRODUCTS));
    let movements = JSON.parse(localStorage.getItem('pf_movements') || JSON.stringify(DEFAULT_MOVEMENTS));

    const po = orders.find(o => o.id == poId);
    if (po) {
        receipts.forEach(r => {
            const item = po.items.find(i => i.id == r.itemId);
            if (item) {
                item.quantityReceived = (item.quantityReceived || 0) + r.quantityReceived;
                const p = products.find(prod => prod.id == item.product.id || prod.sku == item.product.sku);
                if (p) {
                    const prev = p.currentStock;
                    p.currentStock += r.quantityReceived;
                    movements.unshift({
                        id: Date.now() + Math.random(),
                        timestamp: new Date().toISOString(),
                        product: p,
                        location: po.targetLocation,
                        type: 'PO_RECEIPT',
                        quantityChange: r.quantityReceived,
                        previousStock: prev,
                        newStock: p.currentStock,
                        reason: `Received against PO ${po.orderNumber}`,
                        referenceNumber: po.orderNumber,
                        performedBy: 'Current User'
                    });
                }
            }
        });

        const allReceived = po.items.every(i => (i.quantityReceived || 0) >= i.quantityOrdered);
        po.status = allReceived ? 'RECEIVED' : 'PARTIALLY_RECEIVED';

        localStorage.setItem('pf_orders', JSON.stringify(orders));
        localStorage.setItem('pf_products', JSON.stringify(products));
        localStorage.setItem('pf_movements', JSON.stringify(movements));
    }

    closeModal('receivePoModal');
    showToast('Items received! Inventory balance updated with traceable audit movement.', 'success');
    loadOrders();
    loadProducts();
    loadDashboard();
    loadSuggestions();
    loadAuditMovements();
}

// ==================== 10. WAREHOUSES & SUPPLIERS ====================
async function loadWarehouses() {
    const grid = document.getElementById('warehousesGrid');
    if (!grid) return;
    grid.innerHTML = cachedWarehouses.map(w => `
        <div class="stat-card">
            <div>
                <span class="badge badge-draft" style="margin-bottom: 0.5rem;">${w.type}</span>
                <div style="font-size: 1.125rem; font-weight: 800; color: var(--text-primary);">${w.name}</div>
                <div style="font-size: 0.8125rem; color: var(--text-muted); margin-top: 0.25rem;">Code: <code>${w.code}</code></div>
                <div style="font-size: 0.8125rem; color: var(--text-secondary); margin-top: 0.375rem;">${w.address || 'Standard Logistics Facility'}</div>
            </div>
            <div class="stat-icon blue">🏢</div>
        </div>
    `).join('');
}

async function loadSuppliers() {
    const tbody = document.querySelector('#suppliersTable tbody');
    if (!tbody) return;

    try {
        const res = await fetch('/api/suppliers');
        if (res.ok) {
            const data = await res.json();
            if (data && data.length) {
                cachedSuppliers = data;
            }
        }
    } catch (e) {}

    if (!cachedSuppliers || cachedSuppliers.length === 0) {
        cachedSuppliers = JSON.parse(localStorage.getItem('pf_suppliers') || JSON.stringify(DEFAULT_SUPPLIERS));
    }

    tbody.innerHTML = cachedSuppliers.map(s => `
        <tr>
            <td>
                <div style="font-weight: 700; color: var(--text-primary);">${s.name}</div>
                ${s.address ? `<div style="font-size: 0.75rem; color: var(--text-muted);">${s.address}</div>` : ''}
            </td>
            <td>${s.contactPerson || '-'}</td>
            <td><a href="mailto:${s.email}" style="color: var(--primary-accent); text-decoration: none;">${s.email}</a></td>
            <td>${s.phone || '-'}</td>
            <td><strong>${s.leadTimeDays || 7} days</strong></td>
            <td><span style="color: #f59e0b; font-weight: 700;">★ ${Number(s.rating || 4.5).toFixed(1)}</span></td>
            <td>
                <div style="display: flex; gap: 0.375rem; flex-wrap: wrap;">
                    <button class="btn btn-outline btn-sm" onclick="editSupplier(${s.id})" title="Edit Supplier Details">
                        <svg width="14" height="14" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" /></svg>
                        Edit
                    </button>
                    <button class="btn btn-primary btn-sm" onclick="orderFromSupplier(${s.id})" title="Create Purchase Order">
                        Create PO
                    </button>
                    <button class="btn btn-outline btn-sm" onclick="deleteSupplier(${s.id})" style="color: #ef4444; border-color: #fecaca;" title="Remove Supplier">
                        &times;
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

function openSupplierModal() {
    document.getElementById('supplierId').value = '';
    document.getElementById('supplierModalTitle').innerText = 'Add New Approved Supplier';
    document.getElementById('supSubmitBtn').innerText = 'Save Supplier';
    document.getElementById('supName').value = '';
    document.getElementById('supContact').value = '';
    document.getElementById('supEmail').value = '';
    document.getElementById('supPhone').value = '';
    document.getElementById('supAddress').value = '';
    document.getElementById('supLeadTime').value = '7';
    document.getElementById('supRating').value = '4.8';
    openModal('supplierModal');
}

function editSupplier(supId) {
    const s = cachedSuppliers.find(x => x.id == supId);
    if (!s) return;
    document.getElementById('supplierId').value = s.id;
    document.getElementById('supplierModalTitle').innerText = 'Edit Supplier: ' + s.name;
    document.getElementById('supSubmitBtn').innerText = 'Update Supplier';
    document.getElementById('supName').value = s.name || '';
    document.getElementById('supContact').value = s.contactPerson || '';
    document.getElementById('supEmail').value = s.email || '';
    document.getElementById('supPhone').value = s.phone || '';
    document.getElementById('supAddress').value = s.address || '';
    document.getElementById('supLeadTime').value = s.leadTimeDays || 7;
    document.getElementById('supRating').value = s.rating || 4.5;
    openModal('supplierModal');
}

async function handleSupplierSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('supplierId').value;
    const isEdit = Boolean(id);

    const supplierPayload = {
        name: document.getElementById('supName').value.trim(),
        contactPerson: document.getElementById('supContact').value.trim(),
        email: document.getElementById('supEmail').value.trim(),
        phone: document.getElementById('supPhone').value.trim(),
        address: document.getElementById('supAddress').value.trim(),
        leadTimeDays: parseInt(document.getElementById('supLeadTime').value) || 7,
        rating: parseFloat(document.getElementById('supRating').value) || 4.5,
        active: true
    };

    if (isEdit) {
        supplierPayload.id = parseInt(id);
    }

    try {
        const url = isEdit ? `/api/suppliers/${id}` : '/api/suppliers';
        const method = isEdit ? 'PUT' : 'POST';
        const res = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(supplierPayload)
        });

        if (res.ok) {
            const saved = await res.json();
            closeModal('supplierModal');
            showToast(`Supplier ${isEdit ? 'updated' : 'added'} successfully!`, 'success');
            await loadInitialData();
            loadSuppliers();
            return;
        }
    } catch (err) {}

    // Fallback for offline / static preview mode
    let suppliers = JSON.parse(localStorage.getItem('pf_suppliers') || JSON.stringify(DEFAULT_SUPPLIERS));
    if (isEdit) {
        const idx = suppliers.findIndex(x => x.id == id);
        if (idx !== -1) {
            suppliers[idx] = { ...suppliers[idx], ...supplierPayload, id: parseInt(id) };
        }
    } else {
        const newId = Date.now();
        suppliers.push({ ...supplierPayload, id: newId });
    }

    localStorage.setItem('pf_suppliers', JSON.stringify(suppliers));
    cachedSuppliers = suppliers;
    closeModal('supplierModal');
    showToast(`Supplier ${isEdit ? 'updated' : 'added'} successfully!`, 'success');
    populateDropdowns();
    loadSuppliers();
}

async function deleteSupplier(supId) {
    const s = cachedSuppliers.find(x => x.id == supId);
    const name = s ? s.name : 'this supplier';
    if (!confirm(`Are you sure you want to deactivate and remove ${name} from the active directory?`)) {
        return;
    }

    try {
        const res = await fetch(`/api/suppliers/${supId}`, { method: 'DELETE' });
        if (res.ok) {
            showToast('Supplier removed from directory', 'success');
            await loadInitialData();
            loadSuppliers();
            return;
        }
    } catch (err) {}

    let suppliers = JSON.parse(localStorage.getItem('pf_suppliers') || JSON.stringify(DEFAULT_SUPPLIERS));
    suppliers = suppliers.filter(x => x.id != supId);
    localStorage.setItem('pf_suppliers', JSON.stringify(suppliers));
    cachedSuppliers = suppliers;
    showToast('Supplier removed from directory', 'success');
    populateDropdowns();
    loadSuppliers();
}

function orderFromSupplier(supId) {
    switchTab('orders');
    openCreatePoModal();
    document.getElementById('poSupplier').value = supId;
}

// ==================== 11. AUDIT MOVEMENT LEDGER ====================
async function loadAuditMovements() {
    const tbody = document.querySelector('#auditTable tbody');
    if (!tbody) return;
    try {
        const res = await fetch('/api/inventory/movements');
        if (res.ok) {
            const movements = await res.json();
            renderAuditTable(movements);
            return;
        }
    } catch (e) {}

    const movements = JSON.parse(localStorage.getItem('pf_movements') || JSON.stringify(DEFAULT_MOVEMENTS));
    renderAuditTable(movements);
}

function renderAuditTable(movements) {
    const tbody = document.querySelector('#auditTable tbody');
    if (!tbody) return;
    tbody.innerHTML = movements.map(m => `
        <tr>
            <td>${formatDate(m.timestamp)}</td>
            <td><strong>${m.product ? m.product.sku : ''}</strong> - ${m.product ? m.product.name : ''}</td>
            <td>
                ${m.location ? m.location.name : 'Central Warehouse'}
                ${m.toLocation ? ` → ${m.toLocation.name}` : ''}
            </td>
            <td><span class="badge ${getMovementBadgeClass(m.type)}">${m.type}</span></td>
            <td style="font-weight: 700; color: ${m.quantityChange > 0 ? '#10b981' : '#ef4444'};">
                ${m.quantityChange > 0 ? '+' : ''}${m.quantityChange}
            </td>
            <td>${m.previousStock}</td>
            <td><strong>${m.newStock}</strong></td>
            <td>${m.reason || ''} ${m.referenceNumber ? `[${m.referenceNumber}]` : ''}</td>
            <td>${m.performedBy || 'System'}</td>
        </tr>
    `).join('');
}

function exportMovementCsv() {
    const movements = cachedMovements.length ? cachedMovements : JSON.parse(localStorage.getItem('pf_movements') || JSON.stringify(DEFAULT_MOVEMENTS));
    let csv = "Timestamp,SKU,Product,Location,Type,Delta,PrevStock,NewStock,Reason,Reference,User\n";
    movements.forEach(m => {
        csv += `"${m.timestamp}","${m.product ? m.product.sku : ''}","${m.product ? m.product.name : ''}","${m.location ? m.location.name : ''}","${m.type}","${m.quantityChange}","${m.previousStock}","${m.newStock}","${m.reason || ''}","${m.referenceNumber || ''}","${m.performedBy || ''}"\n`;
    });
    const encodedUri = encodeURI("data:text/csv;charset=utf-8," + csv);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", "movement_audit_ledger.csv");
    document.body.appendChild(link);
    link.click();
    link.remove();
}

// ==================== MODAL UTILITIES ====================
function openModal(id) {
    const modal = document.getElementById(id);
    if (modal) modal.classList.add('show');
}

function closeModal(id) {
    const modal = document.getElementById(id);
    if (modal) modal.classList.remove('show');
}

function formatDate(isoStr) {
    if (!isoStr) return '-';
    const d = new Date(isoStr);
    return d.toLocaleDateString() + ' ' + d.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
}

function getMovementBadgeClass(type) {
    switch (type) {
        case 'STOCK_IN':
        case 'PO_RECEIPT': return 'badge-received';
        case 'STOCK_OUT': return 'badge-cancelled';
        case 'TRANSFER': return 'badge-ordered';
        case 'ADJUSTMENT': return 'badge-moderate';
        default: return 'badge-draft';
    }
}
