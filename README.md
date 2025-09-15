# Supermarket Checkout App

## Description
This application emulates a supermarket checkout that calculates the total price of a basket of items, taking into account special promotional offers (e.g., "3 for the price of 2"). Provided that prices and promotions can change relatively frequently.

## Architecture

The application is stateless, cloud-native, ready-to-scale service, built as a monolith but with a clear logical separation internally which could be separate as independent microservices 

- **Product Module**: Responsible for CRUD operations on the product catalog.
- **Checkout Module**:  Responsible for all business logic related to the shopping process.

## Data Flow:

- **Products** are stored in PostgreSQL as the single source of truth. They are cached in Redis for faster access.
- **Baskets** are temporary and stored exclusively in Redis for maximum performance and keep the app stateless.
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
- **Node.js 22.19+** and **npm** (for frontend development)

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

## Future Improvements

This application serves as a demonstration implementation for a technical assessment and is **not production-ready**. While it showcases core functionality and architectural approaches. This version doesn't cover a lot of edge cases, and several areas require significant enhancement for real-world deployment:

#### Business Logic Enhancements:
- Add support for complex promotional rules and combinations
- Implement inventory management and stock validation
- Clarify business requirements at what point the price and spetial offers should be fixed

#### Security & Authentication:
- Implement proper authentication and authorization mechanisms
- Add API rate limiting and request validation
- Add input validation

#### Data Management & Persistence:
- Implement proper database migrations and schema versioning
- Add comprehensive data validation and integrity constraints
- Add data backup and recovery strategies

#### Error Handling & Resilience:
- Implement circuit breakers for external service calls
- Add comprehensive error handling with proper HTTP status codes
- Add proper logging and monitoring for production debugging

#### Testing & Quality Assurance:
- Expand test coverage to include edge cases and error scenarios
- Add performance and load testing

#### Monitoring & Observability:
- Implement comprehensive application metrics and alerts
- Add distributed tracing for request flow analysis
- Implement proper health checks for all dependencies
- Add business metrics and analytics capabilities

