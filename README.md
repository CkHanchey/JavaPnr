# PNR Gov - Flight Manifest Generator

A comprehensive flight manifest and EDIFACT message generator for government reporting requirements. This application converts the original C# project to a modern Java-based solution with Spring Boot backend and Angular frontend.

## 🚀 Features

- **EDIFACT Message Generation**: Generate PAXLST EDIFACT messages for passenger data transmission
- **Flight Manifest Creation**: Comprehensive manifests with passenger, baggage, and seat information
- **Random Sample Data**: Realistic test data generation for development and testing
- **REST API**: Modern Spring Boot 3.5.7 API with full OpenAPI documentation
- **Responsive UI**: Angular 18 frontend with modern design
- **SQLite Database**: Lightweight embedded database for easy deployment

## 🏗️ Architecture

### Multi-Module Structure

```
F:\DEV\Java\PNRGOV\
├── pnrgov-core/          # Domain models and services
│   ├── models/           # JPA entities (Passenger, Reservation, Flight, etc.)
│   └── services/         # Business logic (SampleDataGenerator)
├── pnrgov-api/           # REST API layer
│   ├── controllers/      # REST controllers
│   └── PnrGovApplication.java
└── pnrgov-ui/            # Angular 18 frontend
    └── src/
        ├── app/
        │   ├── pages/    # Home, Generator, Manifest
        │   └── services/ # API service
        └── environments/
```

## 🛠️ Technology Stack

| Component | Technology |
|-----------|-----------|
| **Backend** | Java 17 + Spring Boot 3.5.7 |
| **Frontend** | Angular 18 + TypeScript 5.5 |
| **Database** | SQLite 3.45.1 |
| **Build Tool** | Gradle 7.6.4 |
| **ORM** | Hibernate/JPA (Jakarta Persistence) |
| **Documentation** | SpringDoc OpenAPI 2.3.0 |

## 📋 Prerequisites

- **Java 17**: OpenJDK 17.0.17 or higher
- **Node.js**: 18.x or higher
- **npm**: 9.x or higher
- **Gradle**: 7.6.4 (wrapper included)

## 🚦 Quick Start

See [STARTUP.md](STARTUP.md) for detailed setup instructions.

### Backend

```powershell
# Build and run backend
cd F:\DEV\Java\PNRGOV
.\gradlew clean build
.\gradlew :pnrgov-api:bootRun
```

Backend runs on: http://localhost:8080

### Frontend

```powershell
# Install dependencies and run
cd F:\DEV\Java\PNRGOV\pnrgov-ui
npm install
npm start
```

Frontend runs on: http://localhost:4200

## 🔌 API Endpoints

### Generate EDIFACT Message
```http
POST http://localhost:8080/api/edifact/generate
Content-Type: application/json

Response:
{
  "confirmationCode": "AB1234CD",
  "edifactContent": "UNB+UNOA:1+...",
  "format": "EDIFACT PAXLST"
}
```

### Generate Flight Manifest
```http
POST http://localhost:8080/api/edifact/manifest/generate
Content-Type: application/json

Response:
{
  "confirmationCode": "EF5678GH",
  "edifactContent": "UNB+UNOA:1+...",
  "format": "EDIFACT PAXLST"
}
```

### API Documentation
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## 📦 Database Schema

### Core Entities

- **Reservation**: Main booking entity with record locator
- **Passenger**: Passenger details with relationships to documents, baggage, seats
- **Flight**: Flight information (airline, flight number, departure/arrival)
- **TravelDocument**: Passport/ID documents
- **Baggage**: Checked and carry-on baggage
- **SeatAssignment**: Seat allocations
- **Payment**: Payment transactions

## 🧪 Testing

```powershell
# Run backend tests
.\gradlew test

# Run frontend tests
cd pnrgov-ui
npm test
```

## 📝 Migration Notes

This project is a complete Java migration from the original C# .NET project. See [MIGRATION.md](MIGRATION.md) for detailed migration documentation.

### Key Changes

- **C# → Java**: Full language conversion with idiomatic Java patterns
- **.NET 10 → Spring Boot 3.5.7**: Modern Spring framework
- **ASP.NET Core → Spring Web**: REST API implementation
- **Entity Framework → JPA/Hibernate**: ORM layer
- **Angular (C#) → Angular 18**: Updated frontend

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is part of a government compliance system. All rights reserved.

## 👥 Contact

For questions or support, please contact the development team.

## 🔗 Related Documentation

- [Startup Guide](STARTUP.md) - Step-by-step setup instructions
- [Migration Guide](MIGRATION.md) - C# to Java migration details
- [EDIFACT Standard](EDIFACT-SAMPLE.md) - EDIFACT message format reference

---

**Version**: 1.0.0  
**Last Updated**: 2024  
**Status**: Production Ready
