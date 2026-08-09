# E-Notes REST API

A secure and production-oriented **Notes Management REST API** built with **Spring Boot**.  
The application provides authentication, JWT-based authorization, note management, categories, favorites, recycle-bin functionality, file handling, password management, API documentation, monitoring, automated testing, and code-quality analysis.

---

## 🚀 Features

### Authentication & Security
- User registration and login
- JWT-based authentication and authorization
- Role-based access control
- Password encryption
- Change password
- Forgot-password / password-reset workflow
- Account verification support
- Protected REST endpoints using Spring Security

### Notes Management
- Create notes
- Get user notes
- Get all notes with pagination
- Update notes
- Search notes
- Soft delete notes
- Restore deleted notes
- Permanently delete notes
- Recycle-bin management
- Empty recycle bin
- Favorite / unfavorite notes
- Get favorite notes
- Copy notes

### Category Management
- Create category
- Get all categories
- Get active categories
- Get category by ID
- Delete category
- Category validation and duplicate detection

### File Management
- Upload files with notes
- Store file metadata
- Download attached files

### Development & Quality
- REST API architecture
- Spring Data JPA / Hibernate
- Global exception handling
- DTO-based request/response design
- Logging and AOP-based request tracing
- Spring Boot Actuator
- Swagger / OpenAPI documentation
- Unit and controller testing with JUnit and Mockito
- Integration testing with Spring Boot and MockMvc
- SonarQube code-quality analysis

---

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| Java 17+ | Programming language |
| Spring Boot | Application framework |
| Spring Security | Authentication & authorization |
| JWT | Stateless authentication |
| Spring Data JPA | Data access |
| Hibernate | ORM |
| MySQL | Relational database |
| Maven | Build & dependency management |
| JUnit | Testing |
| Mockito | Mocking |
| MockMvc | Controller/API testing |
| Swagger / OpenAPI | API documentation |
| Spring Boot Actuator | Application monitoring |
| SonarQube | Code quality analysis |

---

## 🏗️ Architecture

The application follows a layered Spring Boot architecture:

```text
Client
  |
  v
REST Controller
  |
  v
Service Layer
  |
  v
Repository Layer
  |
  v
MySQL Database
```

Security flow:

```text
Client
  |
  | Login
  v
Authentication API
  |
  v
JWT Token
  |
  | Authorization: Bearer <token>
  v
JWT Filter
  |
  v
Protected Controller
```

The project also separates DTOs, entities, services, repositories, exception handling, security configuration, and cross-cutting logging concerns.

---

## 📚 API Documentation

The project includes Swagger/OpenAPI documentation for exploring and testing the REST endpoints.

The API documentation contains endpoint groups such as:

- Authentication
- Category
- Home / account verification
- User / Notes operations

Example documented endpoints include:

```text
POST   /api/v1/auth/register
POST   /api/v1/auth/login

POST   /api/v1/category/save
GET    /api/v1/category/
GET    /api/v1/category/active
GET    /api/v1/category/{id}
DELETE /api/v1/category/{id}

GET    /api/v1/notes/user-notes
GET    /api/v1/notes/search
POST   /api/v1/notes/save
DELETE /api/v1/notes/delete/{id}
DELETE /api/v1/notes/delete/{id}
```

### Swagger Screenshot

<img width="936" height="2025" alt="swagger" src="https://github.com/user-attachments/assets/f87f77a2-bac2-4e32-b4ab-eebce612ca3d" />


---

## 🧪 Testing

The project contains unit/controller tests and integration tests.

The test suite covers areas including:

- Category controller
- Notes controller
- User controller
- Service layer
- Spring Boot application context
- Category integration testing
- Notes integration testing

### Latest Test Result

```text
Tests run: 82, Failures: 0, Errors: 0, Skipped: 0

BUILD SUCCESS
```

<img width="984" height="413" alt="Screenshot 2026-08-09 153638" src="https://github.com/user-attachments/assets/8a0c222f-9733-4cc0-9c52-9772d2c08d6f" />

---

## 🔍 Code Quality

SonarQube is used to analyze the project for code quality and maintainability.

The project currently shows:

- Quality Gate: **Passed**
- Security: **A**
- Reliability: **A**
- Maintainability: **A**
- Security issues: **0**
- Reliability issues: **0**
- Code coverage: **0.0%**
- Duplications: **0.0%**

> Coverage is currently not configured/populated in the displayed SonarQube analysis, so the 0.0% value should not be interpreted as "no tests". The Maven test suite itself reports 82 passing tests.

### SonarQube Screenshot

<img width="1917" height="675" alt="SonarQube" src="https://github.com/user-attachments/assets/4e443f11-5d82-46f1-a165-78d4e809b5dc" />

---

## 📊 Monitoring with Spring Boot Actuator

Spring Boot Actuator is included for application monitoring and operational endpoints.

The application exposes Actuator endpoints under:

```text
/actuator
```

Examples include:

```text
/actuator/health
/actuator/info
/actuator/metrics
/actuator/loggers
/actuator/beans
```

### Actuator Screenshot

<img width="1341" height="882" alt="Actuator" src="https://github.com/user-attachments/assets/1f351d0f-e648-4374-a425-27c45d8a56d4" />

---

## ⚙️ Getting Started

### 1. Clone the repository

```bash
git clone <[your-repository-url](https://github.com/Manjit-Kumar-Mahato/Enotes-api-Service)>
cd Enotes-api-Service
```

### 2. Configure MySQL

Create a MySQL database and configure the application's database properties.

Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/<database-name>
spring.datasource.username=<username>
spring.datasource.password=<password>
```

### 3. Configure application properties

Set the required values for:

- Database connection
- JWT secret/configuration
- Mail configuration
- File storage configuration
- Active Spring profile

Use environment variables or an ignored local configuration file for secrets.

### 4. Build the project

```bash
mvn clean install
```

### 5. Run the application

```bash
mvn spring-boot:run
```

The API can then be accessed through the configured server port.

For local development, Swagger and Actuator can be accessed from the application's configured host and port.

---

## 🧪 Run Tests

Run the complete test suite with:

```bash
mvn test
```

Expected result:

```text
Tests run: 82, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 🔐 Security Notes

This project uses JWT-based authentication for protected API resources.

For authenticated requests, send the token using:

```http
Authorization: Bearer <JWT_TOKEN>
```

## 📁 Project Structure

A simplified project structure:

```text
src/
├── main/
│   ├── java/
│   │   └── com/prog/
│   │       ├── config/
│   │       ├── controller/
│   │       ├── dto/
│   │       ├── entity/
│   │       ├── exception/
│   │       ├── repository/
│   │       ├── service/
│   │       └── ...
│   └── resources/
│
└── test/
    └── java/
        └── com/prog/
            ├── controller/
            ├── integration/
            ├── service/
            └── ...
```

---

## 🎯 What This Project Demonstrates

This project was developed to demonstrate practical backend development using Spring Boot, including:

- Designing RESTful APIs
- Implementing JWT authentication
- Securing endpoints with Spring Security
- Building layered backend architecture
- Working with JPA and Hibernate
- Designing DTOs and API responses
- Handling exceptions globally
- Implementing file upload/download
- Implementing pagination and search
- Writing unit and integration tests
- Monitoring applications with Actuator
- Documenting APIs with Swagger/OpenAPI
- Performing static code-quality analysis with SonarQube

---

## 📸 Project Screenshots

### Swagger / OpenAPI

<img width="936" height="2025" alt="swagger" src="https://github.com/user-attachments/assets/b1b80388-0d51-4846-bb67-3868f4452c3a" />

### SonarQube

<img width="1917" height="675" alt="SonarQube" src="https://github.com/user-attachments/assets/abe92a35-a1e8-43d2-8c58-3acab3898dbd" />

### Spring Boot Actuator

<img width="1341" height="882" alt="Actuator" src="https://github.com/user-attachments/assets/f1bcc0c6-915c-4ed0-8fb4-bf7b4c0c0527" />

### Automated Test Results

<img width="984" height="413" alt="Screenshot 2026-08-09 153638" src="https://github.com/user-attachments/assets/c5d78e10-8283-4d32-af50-ddbff0c81f5f" />

---

## 👨‍💻 Author

**Manjit Kumar Mahato**

Backend-focused Java / Spring Boot developer with a focus on REST APIs, Spring Security, databases, and backend system development.

---

## 📄 License

This project is licensed under the MIT License.
