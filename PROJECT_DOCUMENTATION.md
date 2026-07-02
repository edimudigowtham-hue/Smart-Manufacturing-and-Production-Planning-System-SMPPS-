# Smart Manufacturing & Production Planning System (SMPPS)

## 1. Project Overview

SMPPS is a Java Spring Boot microservices project for managing manufacturing operations such as product master data, production orders, machine monitoring, quality inspections, and maintenance work orders.

The application uses:

- Java 21
- Spring Boot
- Spring Cloud Gateway
- Spring Cloud Config Server
- Netflix Eureka Service Discovery
- Spring Security with JWT authentication
- MySQL databases
- Maven multi-module project structure
- HTML, CSS, and JavaScript frontend hosted by the API Gateway

The frontend is served from the API Gateway and split module-wise for easier team ownership and review.

---

## 2. Workspace Structure

```text
final_smpps_project/
  compile-smpps.cmd
  open-correct-project-folder.cmd
  run-smpps.cmd
  PROJECT_DOCUMENTATION.md
  SMPPS_PROJECT_DOCUMENTATION.md
  data/
    registered-users.json
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

Important root scripts:

| File | Purpose |
|---|---|
| `run-smpps.cmd` | Starts all SMPPS services in separate command windows. |
| `compile-smpps.cmd` | Compiles all Maven modules with tests skipped. |
| `open-correct-project-folder.cmd` | Helper script for opening the correct project folder. |

---

## 3. Microservices and Ports

| Service | Port | Purpose |
|---|---:|---|
| Eureka Server | `8761` | Service discovery dashboard and registry. |
| Config Server | `8888` | Centralized external configuration from `config-repo`. |
| API Gateway | `8889` | Frontend UI, login, JWT security, routing, dashboard aggregation. |
| Product Service | `8101` | Product master, BOM versions, components, product deletion safety. |
| Production Order Service | `8102` | Production order lifecycle and machine scheduling at start time. |
| Machine Service | `8103` | Factory machines, availability, runtime logs, downtime logs, OEE. |
| Quality Service | `8104` | Quality inspection, defect logging, AQL-like result calculation. |
| Maintenance Service | `8105` | Maintenance work orders, technician assignment, spare issue, close/cancel. |

Main application URL:

```text
http://localhost:8889
```

Login page:

```text
http://localhost:8889/login
```

Eureka dashboard:

```text
http://localhost:8761
```

---

## 4. Configuration

Centralized service configuration is stored in:

```text
smpps-microservices/config-repo/
```

Key files:

| File | Purpose |
|---|---|
| `application.properties` | Shared config for services. |
| `api-gateway.properties` | API Gateway port, routes, JWT, Eureka link settings. |
| `product-service.properties` | Product Service database and Eureka settings. |
| `production-order-service.properties` | Production Order Service database and Eureka settings. |
| `machine-service.properties` | Machine Service database and Eureka settings. |
| `quality-service.properties` | Quality Service database and Eureka settings. |
| `maintenance-service.properties` | Maintenance Service database and Eureka settings. |

The API Gateway also has fallback local configuration at:

```text
smpps-microservices/api-gateway/src/main/resources/application.properties
```

### API Gateway routes

The gateway routes frontend API calls to backend services:

| Gateway Path | Routed Service | Target |
|---|---|---|
| `/api/products/**` | Product Service | `http://localhost:8101` |
| `/api/orders/**` | Production Order Service | `http://localhost:8102` |
| `/api/machines/**` | Machine Service | `http://localhost:8103` |
| `/api/quality/**` | Quality Service | `http://localhost:8104` |
| `/api/maintenance/**` | Maintenance Service | `http://localhost:8105` |

### Eureka API Gateway link fix

The API Gateway is configured so the Eureka dashboard opens the local login page instead of the machine hostname:

```properties
eureka.instance.hostname=localhost
eureka.instance.prefer-ip-address=false
eureka.instance.instance-id=${spring.application.name}:${server.port}
eureka.instance.home-page-url=http://localhost:${server.port}/login
eureka.instance.status-page-url=http://localhost:${server.port}/login
```

This prevents Eureka from opening links like:

```text
LTIN691483.cts.com:api-gateway:8889
```

and makes it open:

```text
http://localhost:8889/login
```

---

## 5. Database Configuration

The project uses MySQL. Each business microservice has its own database.

| Service | Database |
|---|---|
| Product Service | `smpps_product_db` |
| Production Order Service | `smpps_order_db` |
| Machine Service | `smpps_machine_db` |
| Quality Service | `smpps_quality_db` |
| Maintenance Service | `smpps_maintenance_db` |

Default database credentials in `config-repo`:

```properties
spring.datasource.username=root
spring.datasource.password=root
```

Hibernate is configured with:

```properties
spring.jpa.hibernate.ddl-auto=update
```

So tables are created/updated automatically when services start.

---

## 6. Build and Run

### Prerequisites

Install/configure:

1. Java 21
2. MySQL Server
3. Maven wrapper is already included as `mvnw.cmd`
4. MySQL user/password should match the project config: `root/root`

### Compile all services

From the root folder:

```powershell
C:\Users\2503807\Downloads\final_smpps_project\compile-smpps.cmd
```

Or from the Maven project folder:

```powershell
Set-Location "C:\Users\2503807\Downloads\final_smpps_project\smpps-microservices"
.\mvnw.cmd compile -DskipTests
```

### Run all services

From the root folder:

```powershell
C:\Users\2503807\Downloads\final_smpps_project\run-smpps.cmd
```

This starts services in this order:

1. Eureka Server
2. Config Server
3. Product Service
4. Production Order Service
5. Machine Service
6. Quality Service
7. Maintenance Service
8. API Gateway

### Stop services manually

Close the service command windows, or stop Java processes listening on the SMPPS ports.

Expected ports:

```text
8761, 8888, 8889, 8101, 8102, 8103, 8104, 8105
```

---

## 7. Authentication and Roles

Authentication is handled by the API Gateway using JWT tokens.

User data is stored as BCrypt hashes in:

```text
data/registered-users.json
smpps-microservices/api-gateway/data/registered-users.json
```

Available roles:

| Role | Meaning |
|---|---|
| `ADMIN` | Full system access. |
| `PRODUCTION_PLANNER` | Product and production planning operations. |
| `SHOP_FLOOR_SUPERVISOR` | Shop floor execution, machine logs, order execution. |
| `QUALITY_INSPECTOR` | Quality inspection and defect disposition. |
| `MAINTENANCE_ENGINEER` | Machine and maintenance work order operations. |

Verified current admin login:

```text
Username: admin
Password: Admin@123
```

JWT configuration:

```properties
smpps.jwt.issuer=smpps-api-gateway
smpps.jwt.expiration-minutes=120
```

---

## 8. Frontend Architecture

The complete frontend is hosted by the API Gateway:

```text
smpps-microservices/api-gateway/src/main/resources/static/
```

This keeps one URL, one login flow, one JWT token, one sidebar, and one consistent UI.

### Main frontend files

```text
static/
  index.html
  login.html
  css/
    style.css
  js/
    app.js
    modules/
      dashboard.js
      products.js
      orders.js
      machines.js
      quality.js
      maintenance.js
  partials/
    dashboard.html
    products.html
    orders.html
    machines.html
    quality.html
    maintenance.html
```

### Frontend split

The frontend was split for team readability:

| Module | HTML Partial | JavaScript File |
|---|---|---|
| Dashboard | `partials/dashboard.html` | `js/modules/dashboard.js` |
| Products | `partials/products.html` | `js/modules/products.js` |
| Orders | `partials/orders.html` | `js/modules/orders.js` |
| Machines | `partials/machines.html` | `js/modules/machines.js` |
| Quality | `partials/quality.html` | `js/modules/quality.js` |
| Maintenance | `partials/maintenance.html` | `js/modules/maintenance.js` |

Common state, routing, API helper, role visibility, alerts, and startup logic are in:

```text
js/app.js
```

---

## 9. Role-Based UI Access

The sidebar and buttons use role metadata to hide unauthorized actions.

Example sections:

| UI Section | Allowed Roles |
|---|---|
| Dashboard | Authenticated users |
| Products | Admin, Planner, Supervisor, Quality Inspector |
| Orders | Admin, Planner, Supervisor, Quality Inspector |
| Machines | Admin, Planner, Supervisor, Maintenance Engineer |
| Quality | Admin, Quality Inspector |
| Maintenance | Admin, Maintenance Engineer |

The UI visibility is a convenience feature. Backend/API security is enforced by the API Gateway security configuration.

---

## 10. Product Service

Product Service manages finished products, BOM versions, and BOM components.

Base path through Gateway:

```text
/api/products
```

Common operations:

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/products` | List products. |
| `POST` | `/api/products` | Create product. |
| `PUT` | `/api/products/{id}` | Update product. |
| `DELETE` | `/api/products/{id}` | Delete product if not used in production orders. |
| `GET` | `/api/products/{id}/structure` | View active product structure/BOM. |
| `POST` | `/api/products/{id}/boms` | Create BOM version. |
| `PATCH` | `/api/products/{id}/boms/{bomId}/activate` | Activate BOM version. |
| `POST` | `/api/products/{id}/boms/{bomId}/components` | Add component to a BOM. |

### Product deletion rule

A product cannot be deleted if it is used in production orders.

The Product Service checks Production Order Service before deletion. If order usage cannot be verified, deletion fails safely.

---

## 11. Production Order Service

Production Order Service manages order planning and shop-floor execution.

Base path through Gateway:

```text
/api/orders
```

Common operations:

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/orders` | List orders. |
| `POST` | `/api/orders` | Create order. |
| `GET` | `/api/orders/{id}` | Get order by ID. |
| `GET` | `/api/orders/product/{productId}/exists` | Check if a product is used in orders. |
| `POST` | `/api/orders/{id}/schedule` | Schedule/update work center. |
| `POST` | `/api/orders/{id}/release` | Move order from `PLANNED` to `RELEASED`. |
| `POST` | `/api/orders/{id}/start` | Start released order and assign machine/work center. |
| `POST` | `/api/orders/{id}/complete` | Complete an in-progress order. |
| `POST` | `/api/orders/{id}/cancel` | Cancel an order. |
| `GET` | `/api/orders/{id}/progress` | View order progress. |

### Order lifecycle

```text
PLANNED -> RELEASED -> IN_PROGRESS -> COMPLETED
```

Cancellation is allowed unless the order is already completed.

```text
PLANNED/RELEASED/IN_PROGRESS -> CANCELLED
```

### Realistic machine assignment

Machine selection is not required during order creation.

Instead, the user selects the machine/work center when clicking **Start** on a released order. The selected machine must be `AVAILABLE`.

---

## 12. Machine Service

Machine Service manages factory machines, runtime logs, downtime logs, availability, and OEE metrics.

Base path through Gateway:

```text
/api/machines
```

Common operations:

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/machines` | List all machines. |
| `GET` | `/api/machines/available` | List only available machines. |
| `GET` | `/api/machines/{machineId}` | Get a machine. |
| `POST` | `/api/machines` | Register a factory machine. |
| `POST` | `/api/machines/{machineId}/availability` | Update machine availability. |
| `POST` | `/api/machines/runtime` | Record runtime hours. |
| `POST` | `/api/machines/downtime` | Record downtime hours and reason. |
| `GET` | `/api/machines/logs` | List machine logs. |
| `GET` | `/api/machines/oee` | Get OEE text. |
| `GET` | `/api/machines/oee/summary` | Get OEE summary/card metrics. |

### Machine availability values

```text
AVAILABLE
UNAVAILABLE
UNDER_MAINTENANCE
```

### Latest UI behavior

The Machine page now allows editing availability after creation:

- `AVAILABLE` machines show **Set Unavailable**
- `UNAVAILABLE` machines show **Set Available**
- `UNDER_MAINTENANCE` machines are controlled by maintenance work orders

Downtime rows are prioritized over runtime rows when both are logged for the same machine/date.

---

## 13. Quality Service

Quality Service manages inspections, defects, and inspection disposition.

Base path through Gateway:

```text
/api/quality
```

Common operations:

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/quality/inspections` | List inspections. |
| `POST` | `/api/quality/inspections` | Record inspection. |
| `POST` | `/api/quality/inspections/{id}/defect` | Log defect details. |
| `POST` | `/api/quality/inspections/{id}/approve` | Approve batch/inspection. |
| `POST` | `/api/quality/inspections/{id}/reject` | Reject batch/inspection. |

### Inspection creation

The inspection form records only:

- Order
- Inspection date
- Sample size
- Defect count

Defect type, severity, and description are not entered during inspection creation. They are entered using **Log Defect**.

### AQL-like result rule

The service applies this result logic:

| Condition | Result |
|---|---|
| `defectCount = 0` | `PASS` |
| Severity is `CRITICAL` | `FAIL` |
| Defect rate `<= 5%` | `REWORK` |
| Defect rate `> 5%` | `FAIL` |

The Quality Service validates that sample size does not exceed produced quantity for the selected production order.

---

## 14. Maintenance Service

Maintenance Service manages work orders for machine maintenance.

Base path through Gateway:

```text
/api/maintenance
```

Common operations:

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/maintenance/work-orders` | List work orders. |
| `POST` | `/api/maintenance/work-orders` | Create work order. |
| `POST` | `/api/maintenance/work-orders/{id}/assign` | Assign technician. |
| `POST` | `/api/maintenance/work-orders/{id}/spare` | Issue spare parts. |
| `POST` | `/api/maintenance/work-orders/{id}/close` | Close/complete work order. |
| `POST` | `/api/maintenance/work-orders/{id}/cancel` | Cancel work order. |

### Work order status values

```text
OPEN
IN_PROGRESS
COMPLETED
CANCELLED
```

### Maintenance workflow

1. Create work order for an `AVAILABLE` machine.
2. Machine becomes `UNDER_MAINTENANCE`.
3. Assign technician if required.
4. Issue spare parts if required.
5. Close the work order to mark it `COMPLETED` and release the machine to `AVAILABLE`.
6. Or cancel the work order to mark it `CANCELLED` and release the machine to `AVAILABLE`.

The Maintenance Service validates the machine through Machine Service.

---

## 15. Dashboard

The dashboard is served by the API Gateway and aggregates counts from the business services.

Endpoint:

```text
GET /api/dashboard
```

Dashboard cards include counts for:

- Products
- Orders
- Machine logs/machines
- Quality inspections
- Maintenance work orders

The dashboard UI includes module icons for easier navigation.

---

## 16. Inter-Service Communication

Services communicate using HTTP clients/Feign clients.

Important interactions:

| Source Service | Target Service | Purpose |
|---|---|---|
| Product Service | Production Order Service | Check product usage before deletion. |
| Production Order Service | Product Service | Validate product when creating orders. |
| Production Order Service | Machine Service | Validate selected machine availability when starting orders. |
| Quality Service | Production Order Service | Validate order and produced quantity before inspection. |
| Maintenance Service | Machine Service | Validate machine and update availability. |
| API Gateway | All services | Route API calls and aggregate dashboard data. |

For local reliability, some clients use direct localhost URLs in configuration rather than depending only on Eureka discovery.

---

## 17. Important Business Rules

### Product rules

- Product code and name are required.
- Products can have BOM versions.
- A product used in production orders cannot be deleted.
- Product deletion fails safely if order usage cannot be verified.

### Production order rules

- Orders start as `PLANNED`.
- Only `PLANNED` orders can be released.
- Only `RELEASED` orders can be started.
- Machine/work center is selected when starting production.
- Only `IN_PROGRESS` orders can be completed.
- Produced quantity must match planned quantity before completion.
- Completed orders cannot be cancelled.

### Machine rules

- Machine ID and name are required.
- New machines can be created as `AVAILABLE` or `UNAVAILABLE`.
- Runtime hours and downtime hours must be positive and cannot exceed 24 hours per log.
- Total runtime plus downtime for the same machine/date cannot exceed 24 hours.
- Machines under maintenance are controlled by maintenance work orders.

### Quality rules

- Inspection requires a valid production order.
- Sample size must be greater than zero.
- Defect count cannot be negative.
- Defect count cannot exceed sample size.
- Inspection cannot be recorded until produced quantity exists.
- Sample size cannot exceed produced quantity.
- Critical defect severity forces `FAIL`.

### Maintenance rules

- Maintenance can be scheduled only for `AVAILABLE` machines.
- Creating maintenance makes the machine `UNDER_MAINTENANCE`.
- Closing or cancelling maintenance releases the machine back to `AVAILABLE`.
- Completed and cancelled work orders cannot be modified.

---

## 18. Suggested Review Ownership for Team

Since this is a team project with five business services, review can be split as follows:

| Team Member | Backend Service | Frontend Files |
|---|---|---|
| Product owner | `product-service` | `partials/products.html`, `js/modules/products.js` |
| Orders owner | `production-order-service` | `partials/orders.html`, `js/modules/orders.js` |
| Machine owner | `machine-service` | `partials/machines.html`, `js/modules/machines.js` |
| Quality owner | `quality-service` | `partials/quality.html`, `js/modules/quality.js` |
| Maintenance owner | `maintenance-service` | `partials/maintenance.html`, `js/modules/maintenance.js` |

Shared files:

| File | Ownership |
|---|---|
| `api-gateway/src/main/resources/static/index.html` | Shared UI shell/sidebar. |
| `api-gateway/src/main/resources/static/js/app.js` | Shared frontend state, auth, routing, API helper. |
| `api-gateway/src/main/resources/static/css/style.css` | Shared styling. |
| `api-gateway/src/main/java/.../SecurityConfig.java` | Shared security/role rules. |

---

## 19. Useful URLs

| Page | URL |
|---|---|
| Application | `http://localhost:8889` |
| Login | `http://localhost:8889/login` |
| Dashboard | `http://localhost:8889/dashboard` |
| Products | `http://localhost:8889/products` |
| Orders | `http://localhost:8889/orders` |
| Machines | `http://localhost:8889/machines` |
| Quality | `http://localhost:8889/quality` |
| Maintenance | `http://localhost:8889/maintenance` |
| Eureka | `http://localhost:8761` |
| Config Server | `http://localhost:8888` |

---

## 20. Troubleshooting

### Gateway is not opening

Check if port `8889` is listening:

```powershell
netstat -ano | findstr :8889
```

If it is not running, restart the project:

```powershell
C:\Users\2503807\Downloads\final_smpps_project\run-smpps.cmd
```

### Eureka opens machine hostname instead of localhost

API Gateway has been configured with:

```properties
eureka.instance.home-page-url=http://localhost:${server.port}/login
```

Restart Eureka, Config Server, and API Gateway after config changes.

### Maintenance says machine not found

Ensure Machine Service is running on:

```text
http://localhost:8103
```

Maintenance Service validates machines through Machine Service.

### Product cannot be deleted

If the product is used in production orders, deletion is intentionally blocked.

### Maven target folders

`target/` folders are generated build output. They can be deleted and regenerated by compiling again.

---

## 21. Final Notes

- The API Gateway hosts the full frontend intentionally to keep one login flow, one JWT token, one sidebar, and one consistent UI.
- Frontend files are split into module-specific partials and JavaScript files so each team member can review their own module easily.
- Business services remain separated by domain and database.
- All services should be started together for full application functionality.

