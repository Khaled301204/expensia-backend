# Expensia Backend

The backend service for **Expensia**, an AI-powered personal finance management application. It provides secure REST APIs for user authentication, expense and income management, budgeting, savings goals, reports, notifications, and communication with the AI service.

## Tech Stack

- Java 17
- Spring Boot
- Spring Security (JWT Authentication)
- Spring Data JPA
- PostgreSQL
- Maven
- Docker
- Swagger / OpenAPI

---

## Features

### Authentication
- User registration
- User login
- JWT-based authentication
- Password encryption using BCrypt

### User Management
- View profile
- Update profile
- Risk preference management

### Income Management
- Create income
- Update income
- Delete income
- View all incomes
- Support for recurring income

### Expense Management
- Create expense
- Update expense
- Delete expense
- View expenses
- AI expense categorization
- Voice expense support
- Support for recurring expenses

### Wallet
- Automatic balance calculation
- Automatic synchronization with income and expenses

### Budget Management
- Create budgets
- Update budgets
- Delete budgets
- Budget usage tracking
- Budget alerts

### Saving Goals
- Create saving goals
- Contribute savings to goals
- Withdraw savings from goals
- Automatic goal completion
- Wallet synchronization

### Reports
- Financial summary
- Spending insights
- AI recommendations
- Spending forecasts
- Spending benchmarks
- Export reports as PDF
- Export reports as CSV
- Optional report filtering using start and end dates

### Notifications
- Budget alerts
- Goal completion notifications
- Notification management

### AI Integration
Communicates with the Expensia AI service for:

- Expense categorization
- Voice expense processing
- NLP expense parsing
- Spending insights
- Forecasting
- Recommendations

---

## Project Structure

```
src
 ├── config
 ├── controller
 ├── dto
 │    ├── request
 │    └── response
 ├── exception
 ├── model
 │    ├── entity
 │    └── enums
 ├── repository
 ├── security
 ├── service
 │    ├── ai
 │    ├── auth
 │    ├── budget
 │    ├── expense
 │    ├── goal
 │    ├── income
 │    ├── notification
 │    ├── report
 │    ├── scheduler
 │    ├── user
 │    └── wallet
 └── util
```

---

## Environment Variables

Create a `.env` file inside the project root.

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://YOUR_DATABASE_HOST:5432/expensia
SPRING_DATASOURCE_USERNAME=YOUR_USERNAME
SPRING_DATASOURCE_PASSWORD=YOUR_PASSWORD

JWT_SECRET=YOUR_SECRET_KEY
JWT_EXPIRATION=86400000

AI_BASE_URL=http://localhost:8000/api
```

---

## Running Locally

### Clone

```bash
git clone <repository-url>
cd expensia-backend
```

### Build

```bash
mvn clean install
```

### Run

```bash
mvn spring-boot:run
```

The backend will be available at:

```
http://localhost:8080
```

---

## Running with Docker

Build the image:

```bash
docker build -t expensia-backend .
```

Run the container:

```bash
docker run \
-p 8080:8080 \
--env-file .env \
expensia-backend
```

---

## API Documentation

Swagger UI:

```
http://localhost:8080/swagger-ui/index.html
```

OpenAPI:

```
http://localhost:8080/v3/api-docs
```

---

## Main API Modules

| Module | Description |
|---------|-------------|
| Authentication | User registration and login |
| Users | User profile management |
| Income | Income CRUD operations |
| Expenses | Expense CRUD operations |
| Voice Expenses | AI-powered voice expense recording |
| Wallet | Current balance and savings |
| Budgets | Budget management and alerts |
| Saving Goals | Savings goal management |
| Reports | Reports, insights, exports |
| Notifications | Budget and goal notifications |

---

## Scheduler

The backend includes scheduled jobs for recurring transactions.

Recurring incomes and expenses are automatically generated according to their configured frequency:

- Daily
- Weekly
- Monthly
- Yearly

---

## Security

- JWT Authentication
- Stateless sessions
- BCrypt password hashing
- Spring Security authorization
- CORS support

---

## AI Service

The backend communicates with the Expensia AI service through REST APIs.

Current AI capabilities include:

- Voice-to-expense processing
- Expense categorization
- NLP expense parsing
- Spending insights
- Spending forecasts
- Personalized recommendations

---

## License

This project was developed as part of the **Expensia Graduation Project**.