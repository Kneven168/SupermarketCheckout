# Supermarket Checkout App

## Description
This application emulates a supermarket checkout that calculates the total price of a basket of items, taking into account special promotional offers (e.g., "3 for the price of 2"). Provided that prices and promotions can change relatively frequently.

## Architecture

The application is built as a monolith but with a clear logical separation of modules internally which could be separate as independent microservices 

- **Product Module**: Responsible for CRUD operations on the product catalog.
- **Checkout Module**:  Responsible for all business logic related to the shopping process.

## Data Flow:

- **Products** are stored in PostgreSQL as the single source of truth. They are cached in Redis for faster access.
- **Baskets** are temporary and stored exclusively in Redis for maximum performance.
- **Orders** are permanent records of completed purchases and are saved to PostgreSQL.

## Technology Stack

### Backend
- **Java 17** with Spring Boot 3.2.5 & WebFlux
- **PostgreSQL** as the primary database for storing the product catalog and final orders
- **Spring Data R2DBC** for reactive database access 
- **Redis wish Spring Data Redis Reactive** for caching products and storing temporary customer baskets
- **Testing** JUnit 5, Mockito, Testcontainers for integration testing
- **Lombok** for reducing boilerplate code
- **SpringDoc OpenAPI** for API documentation

### Frontend (generated with AI)
- **Angular 20** with standalone components
- **TypeScript 5.9** for type safety
- **Angular Signals** for reactive state management

### DevOps & Infrastructure
- **Docker** and **Docker Compose** for containerization
- **Gradle** for build automation

## Features

### Core Functionality
- **Product Management**: CRUD operations for products with pricing and promotional offers
- **Basket Management**: Create, modify, delete, and checkout shopping baskets
- **Promotional Pricing**: Automatic calculation of special offers (e.g., "3 for 130" instead of 3×50)
- **Display the price** displaying the current price of the basket after each addition and removal of a product
- **Checkout Process**: Complete order processing with order history in DB

## Getting Started

### Prerequisites
- **Docker** and **Docker Compose**
- **Java 17** (for local development)
- **Node.js 18+** and **npm** (for frontend development)

### Quick Start with Docker

**Start all services**
   ```bash
   docker-compose up -d
   ```

**Access the application**
   - **Frontend**: http://localhost:4200
   - **Backend API**: http://localhost:8081
   - **Swagger UI**: http://localhost:8081/swagger-ui.html
   - **API Docs**: http://localhost:8081/v3/api-docs

## Testing

### Backend Testing
```bash
# Unit tests
./gradlew test

# Integration tests with Testcontainers
./gradlew integrationTest

# All tests
./gradlew check
```
## Sample Data

The application comes with pre-loaded sample products:

| SKU | Name   | Unit Price | Offer          |
|-----|--------|------------|----------------|
| A   | Apple  | $0.50      | 3 for $1.30    |
| B   | Banana | $0.30      | 2 for $0.45    |
| C   | Cherry | $0.20      | No offer       |
| D   | Date   | $0.15      | 5 for $0.60    |

## API Endpoints

### Basket Management
- `POST /api/v1/baskets` - Create a new basket
- `GET /api/v1/baskets/{basketId}` - Get basket by ID
- `POST /api/v1/baskets/{basketId}/items/{sku}` - Add item to basket
- `DELETE /api/v1/baskets/{basketId}/items/{sku}` - Remove item from basket
- `DELETE /api/v1/baskets/{basketId}` - Cancel basket
- `POST /api/v1/baskets/{basketId}/checkout` - Checkout basket

### Product Management
- `POST /api/v1/products` - Create a new product
- `GET /api/v1/products/{sku}` - Get product by SKU
- `PUT /api/v1/products/{sku}` - Update product
- `DELETE /api/v1/products/{sku}` - Delete product

## API Documentation

The application provides comprehensive API documentation via Swagger/OpenAPI:

- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8081/v3/api-docs

## Monitoring and Health Checks

### Health Endpoints
- **Application Health**: http://localhost:8081/actuator/health
- **Application Info**: http://localhost:8081/actuator/info




