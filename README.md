# Gym CRM Application

A Java-based Gym CRM application for managing trainees, trainers, and training sessions.

The project uses **Java 21, Spring Core, JPA/Hibernate, PostgreSQL, and Maven**.

## Technologies

- Java 21
- Spring Core, Spring Context, Spring Transactions
- JPA / Hibernate
- PostgreSQL
- Flyway for database migrations
- HikariCP for database connection pooling
- Jakarta Bean Validation
- MapStruct and Lombok
- BCrypt password hashing
- JUnit 5 and Mockito
- SLF4J for logging
- Maven

## Features

The application supports:

- **Profile management:** Create, retrieve, and update trainee and trainer profiles. Delete trainee profiles by username.
- **Authentication:** Verify usernames and passwords, change passwords, and generate credentials during registration.
- **Profile activation:** Activate and deactivate trainee and trainer accounts.
- **Training management:** Create training sessions associated with existing trainees, trainers, and training types.
- **Training search:** Filter trainee training history by date range, trainer name, and training type. Filter trainer training history by date range and trainee name.
- **Trainer assignments:** Retrieve trainers not assigned to a trainee and update a trainee's trainer list.

## Business Rules

- Usernames and initial passwords are generated automatically during registration.
- All operations except profile registration require authentication.
- Required fields are validated before creating or updating profiles.
- Users have one-to-one relationships with trainee or trainer profiles.
- Trainees and trainers have a many-to-many relationship.
- Activation and deactivation are non-idempotent operations.
- Deleting a trainee permanently removes the profile and its associated training records.
- Training dates and dates of birth use date types; training duration is stored numerically.
- Training types are maintained as reference data and cannot be modified through application operations.
- Transactions protect operations that modify persistent data.

## Getting Started

### Prerequisites

- JDK 21
- Maven
- PostgreSQL

### 1. Configure the database

Create a PostgreSQL database for the application.

Update `src/main/resources/application-dev.properties` with your local database configuration:

```properties
db.url=jdbc:postgresql://localhost:5432/gym_crm
db.username=gym_user
#// your_password (In my case I have added it to environment variables)
db.password=${DB_PASSWORD}

db.pool.maximum-size=10
db.pool.minimum-idle=2

flyway.locations=classpath:db/migration/postgresql

```

### 2. Build the project

```bash
mvn clean package
```

### 3. Run the application


The application initializes the Spring context, configures PostgreSQL persistence, and executes Flyway database migrations.

### 4. Use the application

The application is built with Spring Core rather than Spring Boot and does not expose REST endpoints.

Business operations are accessible through the `GymCrmFacade` and application services.

Typical workflow:

1. Register a trainee or trainer and receive generated credentials.
2. Authenticate using the generated username and password.
3. Retrieve or update profiles.
4. Assign trainers to trainees.
5. Create training sessions.
6. Search training history using optional filtering criteria.
7. Activate, deactivate, or delete profiles as permitted.

## Testing

The project includes unit tests for business logic and repository integration tests using H2.

Run tests with:

```bash
mvn clean test
```

Repository integration tests verify JPA operations, entity relationships, database queries, and persistence behavior.

## Project Architecture

The application follows a layered architecture:

- **Facade:** Provides a unified entry point for CRM operations.
- **Service:** Implements business logic, authentication, and transaction management.
- **DAO/Repository:** Performs persistence operations using JPA `EntityManager`.
- **Model:** Defines JPA entities and relationships.
- **DTO/Mapper:** Transfers and maps application data.
- **Validation:** Validates incoming data and domain constraints.
- **Configuration:** Configures Spring, Hibernate, data sources, Flyway, and application profiles.



