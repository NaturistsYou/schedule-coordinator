# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Schedule Coordinator (日程調整アプリ) - A Spring Boot web application for coordinating event schedules with participant responses.

## Build and Development Commands

### Maven Commands
```bash
# Build the project
mvn clean compile

# Run tests
mvn test

# Run the application
mvn spring-boot:run

# Package as JAR
mvn clean package

# Run with development profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Development Server
- Application runs on `http://localhost:8080`
- H2 Console (if enabled): `http://localhost:8080/h2-console`

## Project Architecture

### Technology Stack
- **Framework**: Spring Boot 3.1.5
- **Java Version**: 17
- **Database**: H2 (development), JPA/Hibernate
- **Template Engine**: Thymeleaf
- **Build Tool**: Maven

### Package Structure
```
NaturistsYou.coordinator/
├── ScheduleCoordinatorApplication.java  # Main application entry point
├── HelloController.java                 # Web controller with API endpoints
├── Event.java                          # Event entity
├── EventDate.java                      # Event date candidates entity
├── Participant.java                    # Participant entity
├── Response.java                       # Participant response entity
├── ResponseType.java                   # Response type enum
└── *Repository.java                    # JPA repositories
```

### Entity Relationships
- **Event**: Root entity with title and creation timestamp
  - Has many `EventDate` (candidate dates)
  - Has many `Participant` (event participants)
- **Participant**: Represents event participants
  - Has many `Response` (responses to event dates)
- **Response**: Links participants to specific event dates with response type

### Key Endpoints
- `GET /` - Hello message
- `GET /events` - List all events (JSON)
- `GET /events/new` - Event creation form
- `POST /events` - Create new event with candidate dates
- `GET /test/tables` - Database table status (development)

## Development Guidelines

### Documentation Output
- New documentation should be output to: `work/lacal obsidian/dev obsidian`

### Database Configuration
- Uses H2 in-memory database for development
- JPA entities use `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- Relationships use `@JsonManagedReference` to prevent circular references

### Template Configuration
- Thymeleaf templates in `src/main/resources/templates/`
- Template caching disabled for development (`spring.thymeleaf.cache=false`)

### Testing
- Test files in `src/test/java/NaturistsYou/`
- Uses Spring Boot Test framework

## Development Notes

- DevTools enabled for hot reload during development
- Japanese language support (UTF-8 encoding)
- Entity relationships properly mapped with cascade operations
- Controller includes test endpoint for database verification