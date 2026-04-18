# Parlop — Opposition Faction Secretariat DBS Application

Spring Boot web application implementing the three data-manipulation
forms required by Step 7 of Databases HW3 (Parlamental Opposition
domain, UCU Applied Sciences Faculty, Spring 2026).

## Stack

- Java 21
- Spring Boot 3.4.3
- Spring Data JDBC (not JPA)
- Thymeleaf + Bootstrap 5 (CDN)
- MySQL 8.4 via `mysql-connector-j`
- Gradle wrapper

## Prerequisites

1. Java 21 on `PATH`.
2. A running MySQL instance with the `parlop_db` schema populated by
   `../queries/step6_populate.sql`.

## Configuration

`src/main/resources/application.properties` reads the following
environment variables (defaults shown):

```
DB_URL      jdbc:mysql://localhost:3306/parlop_db
DB_USER     root
DB_PASSWORD secret
```

Override any of them if your local MySQL uses different values:

```sh
export DB_URL='jdbc:mysql://localhost:3306/parlop_db'
export DB_USER='root'
export DB_PASSWORD='your-real-password'
```

## Run

```sh
./gradlew bootRun
```

Then open <http://localhost:8080/>.

## Forms

| Route | Method | Step 7 Form | Step 1 Input document | DB operation |
|---|---|---|---|---|
| `/deputy/create` | POST | Form 1 ADD | Deputy Enrollment Form | INSERT deputy |
| `/bill/{number}/renumber` | POST | Form 2 UPDATE | Bill Renumbering Notice | UPDATE bill (PK cascade) |
| `/deputy/{id}/delete` | POST | Form 3 DELETE | Mandate Termination Notice | DELETE deputy (FK cascade) |

Supporting read-only routes: `/deputy`, `/bill`.

## Project layout

```
app/
  build.gradle
  settings.gradle
  gradlew, gradlew.bat
  gradle/wrapper/
  src/main/
    resources/
      application.properties
      templates/
        common/  layout.html, error.html
        index.html
        deputy/  list.html, create.html, delete.html
        bill/    list.html, renumber.html
    java/org/example/parlop/
      ParlopApplication.java
      IndexController.java
      ErrorControllerAdvice.java
      deputy/    Deputy.java, DeputyRepository.java, DeputyController.java
      bill/      Bill.java, BillRepository.java, BillController.java
      faction/   OppositionFaction.java, OppositionFactionRepository.java
      district/  ElectoralDistrict.java, ElectoralDistrictRepository.java
```
