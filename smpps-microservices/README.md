# Smart Manufacturing & Production Planning System - Microservices

This folder is the microservices conversion workspace.

## Generated foundation

- `eureka-server` - service registry, port `8761`
- `config-server` - centralized config server, port `8888`
- `api-gateway` - single API/frontend entry point, port `8080`
- `config-repo` - centralized `.properties` configuration files
- Business service projects with module code:
  - `product-service` - port `8101`, DB `smpps_product_db`
  - `production-order-service` - port `8102`, DB `smpps_order_db`
  - `machine-service` - port `8103`, DB `smpps_machine_db`
  - `quality-service` - port `8104`, DB `smpps_quality_db`
  - `maintenance-service` - port `8105`, DB `smpps_maintenance_db`

## Gateway routes

- `/api/products/**` -> `product-service`
- `/api/orders/**` -> `production-order-service`
- `/api/machines/**` -> `machine-service`
- `/api/quality/**` -> `quality-service`
- `/api/maintenance/**` -> `maintenance-service`
- `/api/dashboard` -> gateway dashboard aggregation endpoint

## Role-based access

The API Gateway contains the same roles from the monolith:

- `ADMIN`
- `PRODUCTION_PLANNER`
- `SHOP_FLOOR_SUPERVISOR`
- `QUALITY_INSPECTOR`
- `MAINTENANCE_ENGINEER`

The static frontend and login page are served from `api-gateway`.

Self-registration is disabled because SMPPS is an internal factory system. Users are pre-provisioned with fixed roles, and passwords are stored as BCrypt hashes in `api-gateway/data/registered-users.json`. The gateway also seeds these accounts in code so a fresh run always has internal users available.

Authentication is JWT-based. `POST /api/auth/login` returns a bearer token, and protected API requests must include `Authorization: Bearer <token>`. The browser UI stores the token locally and sends it with API calls; logout removes the token from the browser.

Default local/demo accounts:

| Username | Password | Role |
| --- | --- | --- |
| `admin` | `Admin@123` | `ADMIN` |
| `planner` | `Planner@123` | `PRODUCTION_PLANNER` |
| `supervisor` | `Supervisor@123` | `SHOP_FLOOR_SUPERVISOR` |
| `quality` | `Quality@123` | `QUALITY_INSPECTOR` |
| `maintenance` | `Maintenance@123` | `MAINTENANCE_ENGINEER` |

Optional admin-only user management API:

- `GET /api/admin/users` - list users without password hashes
- `GET /api/admin/users/roles` - list assignable roles
- `POST /api/admin/users` - create a user; requires an authenticated `ADMIN`

Main UI URLs:

- `http://localhost:8889/login`
- `http://localhost:8889/dashboard`
- `http://localhost:8889/products`
- `http://localhost:8889/orders`
- `http://localhost:8889/machines`
- `http://localhost:8889/quality`
- `http://localhost:8889/maintenance`

## Current validation status

The microservices structure and business module code have been generated. All service configuration is in `.properties` files.

Project-local Maven settings are stored in `.mvn/settings.xml` and `.mvn/maven.config` so Maven uses the configured mirror instead of directly contacting Maven Central. The full multi-module compile has been verified successfully with:

```powershell
Set-Location "C:\Users\2503807\Downloads\final_smpps_project\smpps-microservices"
.\mvnw.cmd -pl eureka-server compile
.\mvnw.cmd -pl config-server compile
.\mvnw.cmd -pl api-gateway compile
.\mvnw.cmd compile -DskipTests
```

Start the services in the order below.

## Startup order

1. `eureka-server`
2. `config-server`
3. `product-service`
4. `production-order-service`
5. `machine-service`
6. `quality-service`
7. `maintenance-service`
8. `api-gateway`
