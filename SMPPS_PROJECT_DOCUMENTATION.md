# Smart Manufacturing & Production Planning System (SMPPS) - Project Documentation

**Project location:** `C:\Users\2503807\Downloads\final_smpps_project`  
**Microservices root:** `C:\Users\2503807\Downloads\final_smpps_project\smpps-microservices`  
**Current gateway URL:** `http://localhost:8889`  
**Document date:** June 28, 2026

---

## 1. Executive Summary

The Smart Manufacturing & Production Planning System (SMPPS) is a Spring Boot microservices-based application converted from a monolith. It supports core manufacturing operations such as product master management, production order planning, machine monitoring, quality inspection, maintenance work orders, dashboard reporting, and role-based access control.

The application is accessed through a single **API Gateway** that serves both:

1. The frontend UI pages.
2. Secured backend API routes to the individual microservices.

A user logs in through the gateway, sees only the pages allowed for their role, and performs operations such as creating products, creating production orders, logging machine runtime/downtime, recording inspections, and creating maintenance work orders.

---

## 2. Technology Stack

| Area | Technology |
|---|---|
| Language | Java 21 |
| Backend Framework | Spring Boot 3.3.2 |
| Cloud Framework | Spring Cloud 2023.0.1 |
| API Gateway | Spring Cloud Gateway |
| Service Discovery | Netflix Eureka |
| Central Config | Spring Cloud Config Server |
| Security | Spring Security WebFlux at API Gateway |
| Data Access | Spring Data JPA / Hibernate |
| Database | MySQL |
| Frontend | Static HTML, CSS, JavaScript, Bootstrap |
| Build Tool | Maven Wrapper (`mvnw.cmd`) |
| IDE | IntelliJ IDEA / JetBrains IDE |

---

## 3. High-Level Architecture

```text
Browser
  |
  | http://localhost:8889
  v
API Gateway / Frontend / Security
  |
  | Routes API requests
  |
  +--> Product Service             -> smpps_product_db
  +--> Production Order Service    -> smpps_order_db
  +--> Machine Service             -> smpps_machine_db
  +--> Quality Service             -> smpps_quality_db
  +--> Maintenance Service         -> smpps_maintenance_db

Supporting services:
  - Eureka Server: service registry
  - Config Server: centralized configuration source
  - Config Repo: property files for services
```

The user should normally access only the gateway:

```text
http://localhost:8889/login
```

The gateway internally calls or routes to the business services.

---

## 4. Project Folder Structure

```text
final_smpps_project/
  compile-smpps.cmd
  run-smpps.cmd
  open-correct-project-folder.cmd
  SMPPS_PROJECT_DOCUMENTATION.md
  smpps-microservices/
    pom.xml
    mvnw.cmd
    README.md
    config-repo/
    eureka-server/
    config-server/
    api-gateway/
    product-service/
    production-order-service/
    machine-service/
    quality-service/
    maintenance-service/
```

### Important folders

| Folder | Purpose |
|---|---|
| `smpps-microservices` | Main Maven multi-module project root |
| `api-gateway` | UI, login, security, dashboard aggregation, API routing |
| `eureka-server` | Service discovery registry |
| `config-server` | Centralized config service |
| `config-repo` | Property files read by Config Server |
| `product-service` | Product master and BOM-related operations |
| `production-order-service` | Production order lifecycle operations |
| `machine-service` | Runtime/downtime and OEE-related machine logs |
| `quality-service` | Quality inspections and approval/rejection |
| `maintenance-service` | Maintenance work orders, technician assignment, spares, closure |

---

## 5. Services, Ports, and Databases

| Service | Port | Database | Main Responsibility |
|---|---:|---|---|
| Eureka Server | `8761` | N/A | Service registry |
| Config Server | `8888` | N/A | Centralized config server |
| API Gateway | `8889` | N/A | UI, login, role security, routing, dashboard |
| Product Service | `8101` | `smpps_product_db` | Products and product structure |
| Production Order Service | `8102` | `smpps_order_db` | Production orders and order lifecycle |
| Machine Service | `8103` | `smpps_machine_db` | Runtime/downtime logs and OEE |
| Quality Service | `8104` | `smpps_quality_db` | Quality inspections |
| Maintenance Service | `8105` | `smpps_maintenance_db` | Maintenance work orders |

---

## 6. Prerequisites

Before running the application, ensure these are available:

1. **JDK 21**
2. **MySQL Server** running on port `3306`
3. MySQL username/password configured as:

```text
username: root
password: root
```

4. IntelliJ IDEA or terminal access.

The business service databases are created automatically if MySQL allows it because the JDBC URLs include:

```text
createDatabaseIfNotExist=true
```

---

## 7. How to Compile

### Option A: Use helper script

From PowerShell:

```powershell
cd "C:\Users\2503807\Downloads\final_smpps_project"
.\compile-smpps.cmd
```

### Option B: Compile from Maven root

```powershell
cd "C:\Users\2503807\Downloads\final_smpps_project\smpps-microservices"
.\mvnw.cmd compile -DskipTests
```

A successful compile should end with:

```text
BUILD SUCCESS
```

---

## 8. How to Run

### Recommended startup order

Start services in this order:

1. `EurekaServerApplication`
2. `ConfigServerApplication`
3. `ProductServiceApplication`
4. `ProductionOrderServiceApplication`
5. `MachineServiceApplication`
6. `QualityServiceApplication`
7. `MaintenanceServiceApplication`
8. `ApiGatewayApplication`

### Run using script

```powershell
cd "C:\Users\2503807\Downloads\final_smpps_project"
.\run-smpps.cmd
```

This opens each service in a separate command window.

### Run manually in IntelliJ

Open this folder as the project root:

```text
C:\Users\2503807\Downloads\final_smpps_project\smpps-microservices
```

Then run each main class one by one:

| Module | Main Class |
|---|---|
| `eureka-server` | `com.genc.smpps.eureka.EurekaServerApplication` |
| `config-server` | `com.genc.smpps.configserver.ConfigServerApplication` |
| `product-service` | `com.genc.smpps.ProductServiceApplication` |
| `production-order-service` | `com.genc.smpps.ProductionOrderServiceApplication` |
| `machine-service` | `com.genc.smpps.MachineServiceApplication` |
| `quality-service` | `com.genc.smpps.QualityServiceApplication` |
| `maintenance-service` | `com.genc.smpps.MaintenanceServiceApplication` |
| `api-gateway` | `com.genc.smpps.gateway.ApiGatewayApplication` |

Each service must remain running.

---

## 9. Application URLs

### Main UI

```text
http://localhost:8889/login
```

### Clean frontend routes

```text
http://localhost:8889/dashboard
http://localhost:8889/products
http://localhost:8889/orders
http://localhost:8889/machines
http://localhost:8889/quality
http://localhost:8889/maintenance
```

### Supporting service UIs/checks

```text
http://localhost:8761
http://localhost:8888/product-service/default
```

### Hostname normalization

If the browser opens a machine-hostname URL like:

```text
http://ltin691483.cts.com:8889/dashboard
```

The gateway redirects it to:

```text
http://localhost:8889/dashboard
```

This is handled by `CanonicalHostRedirectFilter` in the API Gateway.

---

## 10. Login Credentials and Roles

Users are configured in:

```text
api-gateway/src/main/java/com/genc/smpps/gateway/config/SecurityConfig.java
```

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | `ADMIN` |
| `planner` | `planner123` | `PRODUCTION_PLANNER` |
| `supervisor` | `supervisor123` | `SHOP_FLOOR_SUPERVISOR` |
| `quality` | `quality123` | `QUALITY_INSPECTOR` |
| `maintenance` | `maintenance123` | `MAINTENANCE_ENGINEER` |

---

## 11. Role-Based Access Control

Role security is primarily implemented at the API Gateway. The frontend also hides/shows menus and buttons based on the logged-in user's role.

### Module visibility

| Module | Allowed Roles |
|---|---|
| Dashboard | All authenticated users |
| Products | Admin, Planner, Supervisor, Quality Inspector |
| Orders | Admin, Planner, Supervisor, Quality Inspector |
| Machines | Admin, Planner, Supervisor, Maintenance Engineer |
| Quality | Admin, Quality Inspector |
| Maintenance | Admin, Maintenance Engineer |

### API action access summary

| Area | Action | Allowed Roles |
|---|---|---|
| Products | View | Admin, Planner, Supervisor, Quality |
| Products | Create/Edit/Delete | Admin, Planner |
| Orders | View | Admin, Planner, Supervisor, Quality |
| Orders | Create/Edit | Admin, Planner |
| Orders | Release | Admin, Planner |
| Orders | Start/Complete | Admin, Supervisor |
| Orders | Cancel | Admin, Planner, Supervisor |
| Machines | View | Admin, Planner, Supervisor, Maintenance |
| Machines | Add runtime/downtime | Admin, Supervisor, Maintenance |
| Quality | Inspect/Approve/Reject | Admin, Quality Inspector |
| Maintenance | Create/Assign/Spare/Close | Admin, Maintenance Engineer |

---

## 12. API Gateway Responsibilities

The API Gateway does the following:

1. Serves the static frontend.
2. Handles login/logout.
3. Enforces role-based authorization.
4. Routes API calls to backend services.
5. Aggregates dashboard counts.
6. Redirects hostname-based URLs to `localhost:8889`.
7. Provides clean UI routes such as `/dashboard` and `/quality`.

### Gateway routes

| Gateway Path | Target |
|---|---|
| `/api/products/**` | `http://localhost:8101` |
| `/api/orders/**` | `http://localhost:8102` |
| `/api/machines/**` | `http://localhost:8103` |
| `/api/quality/**` | `http://localhost:8104` |
| `/api/maintenance/**` | `http://localhost:8105` |
| `/api/dashboard` | Handled inside gateway |
| `/api/auth/me` | Handled inside gateway |

---

## 13. Business Modules

### 13.1 Product Service

**Port:** `8101`  
**Database:** `smpps_product_db`  
**Entity:** `FinishedProduct`

Responsibilities:

- Create products.
- Edit products.
- Delete products.
- View product list.
- View product structure.
- Update BOM version.

Important API endpoints:

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/products` | List all products |
| GET | `/api/products/{id}` | Get product by ID |
| POST | `/api/products` | Create product |
| PUT | `/api/products/{id}` | Update product |
| PATCH | `/api/products/{id}/bom` | Update BOM version |
| GET | `/api/products/{id}/structure` | View product structure |
| POST | `/api/products/{id}/components` | Add component mock operation |
| DELETE | `/api/products/{id}` | Delete product |

---

### 13.2 Production Order Service

**Port:** `8102`  
**Database:** `smpps_order_db`  
**Entity:** `ProductionOrder`

Responsibilities:

- Create production orders.
- Edit orders.
- Validate product exists using Feign client.
- Release orders.
- Start orders.
- Complete orders.
- Cancel orders.
- Show order progress.

Internal dependency:

```java
@FeignClient(name = "product-service")
```

Important API endpoints:

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/orders` | List all orders |
| GET | `/api/orders/{id}` | Get order by ID |
| POST | `/api/orders` | Create order |
| PUT | `/api/orders/{id}` | Update order |
| GET | `/api/orders/{id}/progress` | Get progress |
| POST | `/api/orders/{id}/release` | Release order |
| POST | `/api/orders/{id}/start` | Start order |
| POST | `/api/orders/{id}/complete` | Complete order |
| POST | `/api/orders/{id}/cancel` | Cancel order |

Order status flow:

```text
PLANNED -> RELEASED -> IN_PROGRESS -> COMPLETED
       \-> CANCELLED
```

---

### 13.3 Machine Service

**Port:** `8103`  
**Database:** `smpps_machine_db`  
**Entity:** `MachineLog`

Responsibilities:

- Record machine runtime.
- Record machine downtime.
- View machine logs.
- Calculate OEE-like availability value.

Important API endpoints:

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/machines/logs` | List machine logs |
| POST | `/api/machines/runtime` | Record runtime |
| POST | `/api/machines/downtime` | Record downtime |
| GET | `/api/machines/oee` | Get OEE summary |
| GET | `/api/machines/status` | Get machine status data |

---

### 13.4 Quality Service

**Port:** `8104`  
**Database:** `smpps_quality_db`  
**Entity:** `QualityInspection`

Responsibilities:

- Record quality inspections.
- Validate production order exists using Feign client.
- Auto-calculate inspection result.
- Approve or reject inspected batches.

Internal dependency:

```java
@FeignClient(name = "production-order-service")
```

Inspection result logic:

| Defect Count | Result |
|---:|---|
| `0` | `PASS` |
| `1` to `3` | `REWORK` |
| Greater than `3` | `FAIL` |

Important API endpoints:

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/quality/inspections` | List inspections |
| POST | `/api/quality/inspections` | Create inspection |
| POST | `/api/quality/inspections/{id}/approve` | Approve inspection |
| POST | `/api/quality/inspections/{id}/reject` | Reject inspection |

---

### 13.5 Maintenance Service

**Port:** `8105`  
**Database:** `smpps_maintenance_db`  
**Entity:** `MaintenanceWorkOrder`

Responsibilities:

- Create maintenance work orders.
- Assign technician.
- Issue spare parts.
- Close work orders.
- View maintenance work order list.

Important API endpoints:

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/maintenance/work-orders` | List work orders |
| POST | `/api/maintenance/work-orders` | Create work order |
| POST | `/api/maintenance/work-orders/{id}/assign` | Assign technician |
| POST | `/api/maintenance/work-orders/{id}/spare` | Issue spare parts |
| POST | `/api/maintenance/work-orders/{id}/close` | Close work order |

Work order status flow:

```text
OPEN -> IN_PROGRESS -> COMPLETED
```

---

## 14. End-to-End User Flow

### 14.1 Login flow

```text
User opens /login
  -> submits username/password to POST /login
  -> API Gateway authenticates using in-memory users
  -> success redirects to /dashboard
  -> frontend calls /api/auth/me
  -> frontend displays allowed menus based on role
```

### 14.2 Product/order planning flow

```text
Planner/Admin logs in
  -> opens /products
  -> creates or edits product
  -> opens /orders
  -> creates production order for product
  -> releases order
```

### 14.3 Shop floor flow

```text
Supervisor logs in
  -> opens /orders
  -> starts released order
  -> records machine runtime/downtime in /machines
  -> completes order when production is done
```

### 14.4 Quality flow

```text
Quality inspector logs in
  -> opens /quality
  -> selects production order
  -> enters sample size and defect count
  -> quality service calculates PASS / REWORK / FAIL
  -> inspector may approve/reject inspection
```

### 14.5 Maintenance flow

```text
Maintenance engineer logs in
  -> opens /maintenance
  -> creates work order
  -> assigns technician
  -> issues spare parts
  -> closes work order
```

### 14.6 Dashboard flow

```text
User opens /dashboard
  -> frontend calls /api/dashboard
  -> API Gateway counts records from:
       product-service
       production-order-service
       machine-service
       quality-service
       maintenance-service
  -> UI displays counts
```

---

## 15. Data Flow Diagram

```text
Frontend Form Submit
  |
  v
API Gateway Security Check
  |
  v
Gateway Route Matching
  |
  +--> /api/products/**      -> Product Service -> MySQL product DB
  +--> /api/orders/**        -> Order Service   -> MySQL order DB
  +--> /api/machines/**      -> Machine Service -> MySQL machine DB
  +--> /api/quality/**       -> Quality Service -> MySQL quality DB
  +--> /api/maintenance/**   -> Maintenance     -> MySQL maintenance DB
  |
  v
JSON Response
  |
  v
Frontend updates page
```

---

## 16. Important Implementation Notes

### 16.1 Gateway-level security

Role security is implemented in the API Gateway. The individual business services are mostly unsecured internally. Users should access the system through:

```text
http://localhost:8889
```

not directly through ports `8101` to `8105`.

### 16.2 Clean URLs

The UI uses clean browser URLs:

```text
/dashboard
/products
/orders
/machines
/quality
/maintenance
```

These routes are served by `UiController` in the gateway.

### 16.3 Hostname redirect

If the app is opened using the computer hostname, the gateway redirects to localhost using `CanonicalHostRedirectFilter`.

### 16.4 Feign clients

Feign clients use service discovery names only:

```java
@FeignClient(name = "product-service")
@FeignClient(name = "production-order-service")
```

This means Eureka should be running and services should be registered.

---

## 17. How to Verify the Application

### Check Eureka

```text
http://localhost:8761
```

You should see registered apps:

```text
API-GATEWAY
PRODUCT-SERVICE
PRODUCTION-ORDER-SERVICE
MACHINE-SERVICE
QUALITY-SERVICE
MAINTENANCE-SERVICE
CONFIG-SERVER
```

### Check login

```text
http://localhost:8889/login
```

Use:

```text
admin / admin123
```

Expected redirect:

```text
http://localhost:8889/dashboard
```

### Check dashboard API

After login, dashboard should show non-zero counts if records exist.

PowerShell check:

```powershell
$cookieFile = "$env:TEMP\smpps-cookies.txt"
curl.exe -c $cookieFile -d "username=admin&password=admin123" -H "Content-Type: application/x-www-form-urlencoded" "http://localhost:8889/login"
curl.exe -b $cookieFile "http://localhost:8889/api/dashboard"
```

Expected example:

```json
{"productCount":3,"orderCount":2,"machineCount":1,"qualityCount":1,"maintenanceCount":1}
```

---

## 18. Common Problems and Fixes

### Problem: Login does not redirect

Check:

```text
POST /login -> should return 302 Location: /dashboard
```

If not, restart `ApiGatewayApplication`.

### Problem: Save/Edit shows `Request failed (500)`

Possible causes:

1. API Gateway old instance still running.
2. Target business service is not running.
3. MySQL is not running.
4. Eureka is not running when Feign calls are needed.

Restart:

```text
Eureka
Product Service
Production Order Service
Quality Service
API Gateway
```

### Problem: Dashboard count shows zero

Check direct service endpoints:

```text
http://localhost:8101/api/products
http://localhost:8102/api/orders
http://localhost:8103/api/machines/logs
http://localhost:8104/api/quality/inspections
http://localhost:8105/api/maintenance/work-orders
```

If data exists directly but dashboard is zero, restart `ApiGatewayApplication`.

### Problem: Browser shows `ltin691483.cts.com`

The gateway redirects hostname URLs back to:

```text
http://localhost:8889
```

Use localhost URLs directly.

### Problem: Port already in use

PowerShell:

```powershell
netstat -ano | findstr ":8889"
```

Then stop the process if needed.

---

## 19. Restart Checklist

When code changes are made, restart the changed service.

| Changed Area | Restart Needed |
|---|---|
| UI, gateway routes, login, dashboard | `ApiGatewayApplication` |
| Product logic/entity | `ProductServiceApplication` |
| Order logic/Feign client | `ProductionOrderServiceApplication` |
| Machine logic | `MachineServiceApplication` |
| Quality logic/Feign client | `QualityServiceApplication` |
| Maintenance logic | `MaintenanceServiceApplication` |
| Config repo files | Config Server and affected services |

---

## 20. Quick Demo Script

1. Open:

```text
http://localhost:8889/login
```

2. Login as admin:

```text
admin / admin123
```

3. Go to Products:

```text
http://localhost:8889/products
```

Create product.

4. Go to Orders:

```text
http://localhost:8889/orders
```

Create order for that product.

5. Go to Machines:

```text
http://localhost:8889/machines
```

Record runtime or downtime.

6. Go to Quality:

```text
http://localhost:8889/quality
```

Create inspection.

7. Go to Maintenance:

```text
http://localhost:8889/maintenance
```

Create work order.

8. Return to Dashboard:

```text
http://localhost:8889/dashboard
```

Confirm counts update.

---

## 21. Summary for New Developers

If someone new joins this project, they should understand the system like this:

1. Start with `smpps-microservices/pom.xml`; it is the parent Maven project.
2. The frontend and security are in `api-gateway`.
3. All browser access should go through `http://localhost:8889`.
4. Each business domain has its own service and MySQL database.
5. Eureka registers all services.
6. Config Server reads properties from `config-repo`.
7. Role-based access is enforced mainly in the API Gateway.
8. Dashboard counts are aggregated by the API Gateway from all business services.
9. Production Order Service calls Product Service using Feign.
10. Quality Service calls Production Order Service using Feign.

---

## 22. Useful Commands

Compile all:

```powershell
cd "C:\Users\2503807\Downloads\final_smpps_project\smpps-microservices"
.\mvnw.cmd compile -DskipTests
```

Run one service:

```powershell
cd "C:\Users\2503807\Downloads\final_smpps_project\smpps-microservices"
.\mvnw.cmd -pl api-gateway spring-boot:run
```

Run all using script:

```powershell
cd "C:\Users\2503807\Downloads\final_smpps_project"
.\run-smpps.cmd
```

Check port:

```powershell
netstat -ano | findstr ":8889"
```

Check dashboard API:

```powershell
curl.exe http://localhost:8889/api/dashboard
```

---

End of documentation.

