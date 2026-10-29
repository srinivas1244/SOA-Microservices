# Spring Cloud Config: E-Commerce SOA Experiment

This Java 17 / Spring Boot 3.3.5 / Spring Cloud 2023.0.3 Maven project demonstrates centralized, Git-backed configuration for three Spring Boot services. The same naming convention can be used for the other 12 services.

## Architecture

```text
                                  local Git repository
                                  company-config (main)
                                / application.yml
                               /  product-service.yml
                              /   order-service.yml
                             /    payment-service.yml
                            v
Clients --config import--> Config Server :8888
  product-service :8081  <--- Git-backed Environment API
  order-service    :8082  <--- Git-backed Environment API
  payment-service  :8083  <--- Git-backed Environment API
```

`application.yml` contributes shared `app.environment` and `app.owner` properties. Each service-specific file supplies its own `server.port`, `app.message`, and sample `app.database-url`. Database URLs are configuration examples only; no database driver, connection, or schema is required.

## Project layout

```text
pom.xml
README.md
config-server/
  pom.xml
  src/main/java/com/example/ecommerce/configserver/ConfigServerApplication.java
  src/main/resources/application.yml
  src/test/java/com/example/ecommerce/configserver/ConfigServerIntegrationTest.java
product-service/
  pom.xml
  src/main/java/com/example/ecommerce/product/{ProductServiceApplication,AppProperties,ProductController}.java
  src/main/resources/application.yml
  src/test/resources/application.yml
  src/test/java/com/example/ecommerce/product/{ProductControllerTest,ProductConfigClientIntegrationTest,AppPropertiesValidationTest}.java
order-service/
  pom.xml
  src/main/java/com/example/ecommerce/order/{OrderServiceApplication,AppProperties,OrderController}.java
  src/main/resources/application.yml
  src/test/resources/application.yml
  src/test/java/com/example/ecommerce/order/{OrderControllerTest,OrderConfigClientIntegrationTest}.java
payment-service/
  pom.xml
  src/main/java/com/example/ecommerce/payment/{PaymentServiceApplication,AppProperties,PaymentController}.java
  src/main/resources/application.yml
  src/test/resources/application.yml
  src/test/java/com/example/ecommerce/payment/{PaymentControllerTest,PaymentConfigClientIntegrationTest}.java
company-config/                 # Its own Git repository, branch main
  application.yml
  product-service.yml
  order-service.yml
  payment-service.yml
scripts/test-endpoints.ps1
```

Add another service by naming its Spring application `inventory-service`, creating `company-config/inventory-service.yml` (and committing it), and reusing the existing client pattern. Shared properties stay in `application.yml`; service-specific values override shared values.

## Prerequisites

- JDK 17 or later (`java -version`)
- Maven 3.8.6 or later (`mvn -version`)
- Git

There is no Maven Wrapper in this workspace. For the verified setup here, Temurin JDK 17.0.20.1 and Maven 3.9.11 were downloaded to `%LOCALAPPDATA%\Programs\soa-toolchain` and do not change the system `PATH`. In each PowerShell terminal, make them available with:

```powershell
$toolRoot = Join-Path $env:LOCALAPPDATA 'Programs\soa-toolchain'
$env:JAVA_HOME = Join-Path $toolRoot 'jdk-17\jdk-17.0.20.1+1'
$env:PATH = "$env:JAVA_HOME\bin;$(Join-Path $toolRoot 'maven\apache-maven-3.9.11\bin');$env:PATH"
```

If using another installation, set `JAVA_HOME` and `PATH` to your JDK 17+ and Maven 3.8.6+ installations instead.

## Build and automated tests

From the project root (`E:\soa` in this workspace):

```powershell
mvn --batch-mode --no-transfer-progress clean verify
```

This runs the Config Server integration tests against the checked-in local Git repository, exercises each Config Client against a test HTTP Config Server and verifies its loaded values and REST response, exercises all three controllers with MockMvc, and verifies required-property validation. For an unknown application, Config Server returns shared defaults but no service-specific message; client startup fails clearly because required properties are validated.

## Run the components

Build first, then open four terminals. In each terminal set `JAVA_HOME` as shown under Prerequisites, `Set-Location` to the module, and launch its packaged JAR. The heap limits used below let all four services run on a machine with a constrained Windows paging file:

```powershell
Set-Location E:\soa\config-server
& "$env:JAVA_HOME\bin\java.exe" -Xms32m -Xmx192m -XX:MaxMetaspaceSize=160m -XX:+UseSerialGC -jar target\config-server-1.0.0-SNAPSHOT.jar
```

```powershell
Set-Location E:\soa\product-service
& "$env:JAVA_HOME\bin\java.exe" -Xms32m -Xmx160m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC -jar target\product-service-1.0.0-SNAPSHOT.jar
```

```powershell
Set-Location E:\soa\order-service
& "$env:JAVA_HOME\bin\java.exe" -Xms32m -Xmx160m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC -jar target\order-service-1.0.0-SNAPSHOT.jar
```

```powershell
Set-Location E:\soa\payment-service
& "$env:JAVA_HOME\bin\java.exe" -Xms32m -Xmx160m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC -jar target\payment-service-1.0.0-SNAPSHOT.jar
```

The Config Server's default repository URI is `file:../company-config`, relative to the `config-server` module directory. Start it from that directory as shown below so it resolves the local Git repository consistently. To override it, set `CONFIG_REPO_URI` before starting the Config Server. Clients use mandatory `spring.config.import` and fail startup when the Config Server cannot supply their required configuration; override its address with `CONFIG_SERVER_URL` if needed.

## Verify HTTP responses

After all four applications report that they have started, run:

```powershell
.\scripts\test-endpoints.ps1
```

The script makes six HTTP checks: each Config Server response and each client REST response. Expected Config Server response shape (values shown for Product):

```json
{
  "name": "product-service",
  "profiles": ["default"],
  "propertySources": [
    { "name": "...product-service.yml", "source": {
      "server.port": 8081,
      "app.message": "Product catalog configuration updated centrally in Git",
      "app.database-url": "jdbc:postgresql://localhost:5432/products"
    }},
    { "name": "...application.yml", "source": {
      "app.environment": "local",
      "app.owner": "ecommerce-platform"
    }}
  ]
}
```

Expected product endpoint response:

```json
{
  "serviceName": "product-service",
  "message": "Product catalog configuration updated centrally in Git",
  "serverPort": "8081",
  "databaseUrl": "jdbc:postgresql://localhost:5432/products",
  "environment": "local"
}
```

Order and payment return the same response shape with their own service name, message, configured port (`8082` / `8083`) and database URL (`.../orders` / `.../payments`).

Expected order response:

```json
{
  "serviceName": "order-service",
  "message": "Order processing configuration loaded from Git",
  "serverPort": "8082",
  "databaseUrl": "jdbc:postgresql://localhost:5432/orders",
  "environment": "local"
}
```

Expected payment response:

```json
{
  "serviceName": "payment-service",
  "message": "Payment processing configuration loaded from Git",
  "serverPort": "8083",
  "databaseUrl": "jdbc:postgresql://localhost:5432/payments",
  "environment": "local"
}
```

Manual URLs:

- `http://localhost:8888/product-service/default`
- `http://localhost:8888/order-service/default`
- `http://localhost:8888/payment-service/default`
- `http://localhost:8081/products`
- `http://localhost:8082/orders`
- `http://localhost:8083/payments`

The Config Server endpoints prove service and shared YAML values are resolved from Git. The client endpoints prove each running app has bound its configured port and is returning the message, database URL, and shared environment it loaded.

## Demonstrate a central configuration change

1. The checked-in demo already contains the centrally updated Product message, committed in Git. To repeat the demonstration, change only `message` in `company-config/product-service.yml` to a new value, for example:

   ```yaml
   app:
     message: Product catalog message changed again in Git
   ```

2. Commit the change in the configuration repository:

   ```powershell
   git -C E:\soa\company-config add product-service.yml
   git -C E:\soa\company-config commit -m "Change product service message again"
   ```

3. Restart the Config Server if necessary, then restart Product Service. A restarted client fetches the latest committed value during startup.
4. Call `http://localhost:8081/products`. The `message` field should now be `Product catalog message changed again in Git`; no Java source change is involved.
5. To rerun the automated tests or six-check endpoint script after using a different message, update their expected Product message to match the new committed value.

A Git commit makes a new value available to clients on their next configuration fetch; it does **not** automatically update an already-running client. This implementation does not enable or test Spring Cloud Bus or actuator refresh. Restart the client to apply changes reliably.

## Use GitHub instead of the local repository

Create a private GitHub repository containing the four YAML files, push the `main` branch, and set the Config Server's `CONFIG_REPO_URI` to `https://github.com/<owner>/<repo>.git`. For a private repository, supply credentials using a supported Git credential helper or secret-backed environment configuration; never put a token in YAML or commit it. Keep `default-label: main` aligned with the branch. The local `company-config` repository remains the offline default.

## Troubleshooting

| Symptom | Cause / diagnostic | Resolution |
|---|---|---|
| Client cannot start; connection refused to `localhost:8888` | Config Server is stopped or its startup failed. Check its console and request its health/startup logs. | Start Config Server first; check port 8888 and `CONFIG_SERVER_URL`. The client import is mandatory so failure is visible rather than silently ignored. |
| Config Server fails to clone/read the repository | Bad `CONFIG_REPO_URI`, missing directory, invalid Git repository, or branch mismatch. Inspect Config Server startup logs and run `git -C E:\soa\company-config status`. | Restore the path/remote, initialize/commit the YAML repository, and ensure the configured branch exists. Run the server from `config-server` when using its relative default URI. |
| Service fails with a missing/blank `app.*` configuration error | A required key is absent or blank; `AppProperties` validates `environment`, `message`, and `databaseUrl` at startup. | Add the property to the service YAML or the shared YAML, commit it, and restart the client. |
| Config endpoint lacks service-specific settings | `spring.application.name` does not match a config filename, or that file/branch is absent. Config Server still returns shared `application.yml` properties with HTTP 200. | Ensure app name exactly matches `<application-name>.yml`; inspect `http://localhost:8888/<application-name>/default`. A client with missing required settings fails startup validation. |
| Bind error / “port already in use” | Another process owns 8888 or one of 8081–8083. | Stop the conflicting process or change the corresponding `server.port` in the config repo, commit, and restart the service. |
| Spring Cloud compatibility/BOM errors | Boot and Cloud release trains are incompatible or dependency versions were overridden. | Keep the BOMs in the root POM aligned. This project uses Spring Boot 3.3.5 with Spring Cloud 2023.0.3; avoid individually pinning Spring Cloud artifacts. |
| Maven reports an unsupported Java version | Maven is using a JDK older than 17. | Install/select JDK 17+, verify `java -version` and `mvn -version` report the intended runtime, then rerun `mvn clean verify`. |

## Validation status for this workspace

Verified on 2026-10-10 with Temurin 17.0.20.1 and Maven 3.9.11: `mvn --batch-mode --no-transfer-progress clean verify` completed with **BUILD SUCCESS** and all **9 tests passed**. The services were started from their packaged JARs, and `.\scripts\test-endpoints.ps1` passed **6 of 6 live HTTP checks**. The central Product message change is committed in `company-config` as `9e56fe4`; after restarting Product Service, its endpoint returned the updated message without a Java source change. The services currently listen on localhost ports 8888 and 8081–8083; they remain available while their host processes stay running.
