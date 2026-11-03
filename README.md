# 🐓 Smart Indian Poultry Farm & Chicken Business Management System

[![Open Live Application](https://img.shields.io/badge/%F0%9F%8C%90_OPEN_LIVE_WEB_APP-Click_Here_to_Launch-198754?style=for-the-badge)](https://smart-indian-poultry-system.onrender.com/)
[![Login Portal](https://img.shields.io/badge/%F0%9F%94%90_Login_Portal-Open-0d6efd?style=for-the-badge)](https://smart-indian-poultry-system.onrender.com/login.html)
[![Swagger API](https://img.shields.io/badge/%F0%9F%93%96_Swagger_API_Docs-Open-fd7e14?style=for-the-badge)](https://smart-indian-poultry-system.onrender.com/swagger-ui.html)

> ### 🚀 **Click Here to Open Live Project:** [https://smart-indian-poultry-system.onrender.com/](https://smart-indian-poultry-system.onrender.com/)
> *(Hosted on Render Free Tier + MongoDB Atlas Cloud. Note: If nobody opened the site in the last 15 minutes, the first click takes ~30–50 seconds for the Java server to wake up.)*

A complete, farmer-friendly, multilingual web application built with **Java 21 (LTS)**, **Spring Boot 3.3.4**, **Spring Security (BCrypt)**, **Spring Data MongoDB**, **Bootstrap 5**, **Chart.js**, **Web Speech API**, and **OpenPDF**.

Designed specifically for **Indian poultry farmers, indigenous (Desi) breeders, broiler & layer farm managers, wholesalers, egg/bird retailers, and hygienic chicken cutting shops**.

---

## 🌐 Live Cloud Links (Open Directly in Browser)

- **🏠 Live Home Page**: https://smart-indian-poultry-system.onrender.com/
- **🔐 Live Login Screen**: https://smart-indian-poultry-system.onrender.com/login.html
- **📝 New Registration**: https://smart-indian-poultry-system.onrender.com/register.html
- **📖 Swagger API Explorer**: https://smart-indian-poultry-system.onrender.com/swagger-ui.html
- **💚 Health Check API**: https://smart-indian-poultry-system.onrender.com/api/health

*(Why doesn't GitHub Pages open this directly? GitHub only stores source code files — because this project uses a real **Java 21 Spring Boot backend + MongoDB Atlas database**, the live running website is hosted at the Render link above!)*

---

## 🗺️ Phased Development Roadmap

- **✅ Phase 1 (Completed & Live)**:
  - Spring Boot 3.3.4 + MongoDB Atlas setup (`smart_indian_poultry_db`)
  - BCrypt Authentication + Stateful Session & HTTP Basic REST Security
  - 6 Role-Based Access Levels (`SUPER_ADMIN`, `FARM_ADMIN`, `FARM_MANAGER`, `FARMER`, `RETAILER`, `CHICKEN_SHOP_OWNER`)
  - Strict **Farm-Level Data Isolation** (`FarmAccessGuard`) + Security `audit_logs`
  - Multiple Farms (`farms`) & Poultry Houses / Sheds (`poultry_houses`)
  - Configurable **Indian Chicken Breed Master** (`breeds`) pre-seeded with **16 breeds**:
    - **Indigenous (8)**: *Aseel, Kadaknath, Ghagus, Nicobari, Ankaleshwar, Miri, Hansli, Kalasthi*
    - **Improved Rural (4)**: *Vanaraja, Gramapriya, Giriraja, Srinidhi*
    - **Commercial (4)**: *Ross 308, Cobb 500, Hubbard, BV-300 Layer*
  - Farmer-friendly mobile-first UI in **11 Indian Languages** (*English, Marathi, Hindi, Gujarati, Punjabi, Bengali, Telugu, Kannada, Tamil, Malayalam, Odia*) with **Voice Input (🎤)**, **Audio Readout (🔊)**, and **Offline Draft Saving (`localStorage`)**.
- **✅ Phase 2 (Completed & Live)**: Batch Lifecycle Management, Daily Poultry Entry, Broiler FCR/Growth Engine, Layer Hen-Day Egg Engine.
- **✅ Phase 3 (Completed & Live)**: Feed, Medicine, Configurable Vaccination Schedules, Suppliers & Automatic Inventory Reduction.
- **✅ Phase 4 (Completed & Live)**: Wholesale, Retail, Chicken Cutting Shop (actual dressed meat yield & by-products), Sales, Purchases, Udhaar Ledgers & P&L Finance.
- **⏳ Phase 5 (Next)**: 15 Interactive Chart.js Analytics & Deep Filtering across all 31 pages.
- **⏳ Phase 6**: Automatic Batch Completion Unicode PDF Report (`OpenPDF`), Security Hardening, MongoDB Backup Utility.

---

## 🛠️ Prerequisites & MongoDB Setup (Windows)

1. **Java JDK 17 or 21 LTS** (Installed on this machine: Temurin JDK 21).
2. **MongoDB Community Server 7.x / 8.x**:
   - Download from: https://www.mongodb.com/try/download/community (select Windows x64 MSI, check *"Install MongoDB as a Service"* and *"Install MongoDB Compass"*).
   - Default connection URI in `src/main/resources/application.yml`:
     ```yaml
     spring:
       data:
         mongodb:
           uri: mongodb://localhost:27017/smart_indian_poultry_db
     ```
   - Verify in **Windows CMD (Run as Administrator)**:
     ```cmd
     net start MongoDB
     ```

---

## 🚀 How to Run the Application

### Option A: One-Click Startup Script (Recommended on Windows)
Double-click `run.bat` or run in PowerShell / CMD:
```cmd
cd C:\Users\sawan\.gemini\antigravity\scratch\smart-poultry-backend
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

### Option B: Windows Command Prompt (CMD) with Maven
```cmd
cd C:\Users\sawan\.gemini\antigravity\scratch\smart-poultry-backend
"C:\Program Files\JetBrains\IntelliJ IDEA 2026.1.2\plugins\maven\lib\maven3\bin\mvn.cmd" spring-boot:run
```
*(Or simply `mvn spring-boot:run` if Maven is added to your Windows `PATH`.)*

### Option C: Run in IntelliJ IDEA
1. Open **IntelliJ IDEA** -> **File -> Open...** -> select `C:\Users\sawan\.gemini\antigravity\scratch\smart-poultry-backend`.
2. Wait 10–20 seconds for IntelliJ to import `pom.xml` as a Maven project.
3. Ensure **Project SDK** (`File -> Project Structure -> Project`) is set to **Java 21**.
4. Open `src/main/java/com/poultry/smartfarm/SmartPoultryApplication.java`.
5. Click the green **Run ▶** icon next to `public static void main`.

---

## 🌐 Open in Browser

- **Landing Page**: http://localhost:8080/index.html
- **Login Screen**: http://localhost:8080/login.html
- **New Registration**: http://localhost:8080/register.html
- **Swagger API Explorer**: http://localhost:8080/swagger-ui.html

---

## 🔐 Default Development Credentials

*(Override in production via environment variables `APP_ADMIN_USERNAME`, `APP_ADMIN_PASSWORD`, and `APP_SEED_DEMO_USERS=false`.)*

| Role | Username | Mobile Login | Password | Assigned Farm Scope |
| :--- | :--- | :--- | :--- | :--- |
| **1. Super Admin** | `superadmin` | `9800000001` | `Admin@12345` | All Farms & System Settings |
| **2. Farm Admin (MH)** | `farmadmin1` | `9800000002` | `Farm@12345` | `FARM-MH-001` (Nashik, MH) |
| **2b. Farm Admin (MP)** | `farmadmin2` | `9800000007` | `Farm@12345` | `FARM-MP-002` (Jhabua, MP - Isolated!) |
| **3. Farm Manager** | `manager1` | `9800000003` | `Manager@123` | `FARM-MH-001` |
| **4. Farmer** | `farmer1` | `9800000004` | `Farmer@123` | `FARM-MH-001` |
| **5. Retailer** | `retailer1` | `9800000005` | `Retail@123` | Retail Stock & Sales |
| **6. Chicken Shop Owner** | `shopowner1` | `9800000006` | `Shop@12345` | Meat Processing & Shop P&L |
