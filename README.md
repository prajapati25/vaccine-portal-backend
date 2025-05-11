# School Vaccination Portal - Backend

A robust Spring Boot-based backend application for managing school vaccination drives and student records.

## Features

- 🔐 Secure authentication and authorization
- 📊 RESTful API endpoints
- 📝 Data validation and error handling
- 🔄 Transaction management
- 📈 Comprehensive reporting
- 📱 Cross-origin resource sharing (CORS) support
- 📦 Pagination and sorting
- 🔍 Advanced search capabilities

## Tech Stack

- Java 17
- Spring Boot 3.x
- Spring Security
- Spring Data JPA
- MySQL Database
- Maven
- JWT Authentication
- Lombok
- Validation API

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher

## Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd vaccine-portal-backend
```

2. Configure the database:
   - Create a MySQL database named `vaccine_portal`
   - Update `application.properties` with your database credentials

3. Build the project:
```bash
mvn clean install
```

4. Run the application:
```bash
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`

## API Documentation

The API documentation is available at `http://localhost:8080/swagger-ui.html` when the application is running.

### Key Endpoints

#### Authentication
- POST `/api/auth/login` - User login
- POST `/api/auth/refresh` - Refresh JWT token

#### Students
- GET `/api/students` - Get all students
- POST `/api/students` - Create new student
- GET `/api/students/{id}` - Get student by ID
- PUT `/api/students/{id}` - Update student
- DELETE `/api/students/{id}` - Delete student

#### Vaccination Drives
- GET `/api/vaccination-drives` - Get all drives
- POST `/api/vaccination-drives` - Create new drive
- GET `/api/vaccination-drives/{id}` - Get drive by ID
- PUT `/api/vaccination-drives/{id}` - Update drive
- DELETE `/api/vaccination-drives/{id}` - Delete drive

#### Vaccination Records
- GET `/api/vaccination-records` - Get all records
- POST `/api/vaccination-records` - Create new record
- GET `/api/vaccination-records/{id}` - Get record by ID
- PUT `/api/vaccination-records/{id}` - Update record

## Project Structure

```
vaccine-portal-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/school/vaccineportalbackend/
│   │   │       ├── config/         # Configuration classes
│   │   │       ├── controller/     # REST controllers
│   │   │       ├── dto/           # Data Transfer Objects
│   │   │       ├── model/         # Entity classes
│   │   │       ├── repository/    # Data repositories
│   │   │       ├── service/       # Business logic
│   │   │       └── util/          # Utility classes
│   │   └── resources/
│   │       └── application.properties
│   └── test/                      # Test classes
└── pom.xml                        # Maven configuration
```

## Database Schema

The application uses the following main entities:
- Users
- Students
- Vaccines
- Vaccination Drives
- Vaccination Records
- Grades

## Security

- JWT-based authentication
- Role-based authorization
- Password encryption
- CORS configuration
- Input validation
- SQL injection prevention

## Error Handling

The application implements a global exception handler that provides:
- Consistent error responses
- Detailed error messages
- HTTP status codes
- Validation error details

## Testing

Run tests using:
```bash
mvn test
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 