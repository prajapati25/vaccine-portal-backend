# Vaccine Portal Backend

A Spring Boot backend application for managing student vaccination records in schools.

## Features

- Student Management
  - Create, read, update, and delete student records
  - Import/Export student data via CSV
  - Search and filter students by various criteria
  - Automatic roll number generation

- Vaccination Management
  - Record and track student vaccinations
  - Manage vaccination drives
  - Generate vaccination reports
  - Export reports in CSV and PDF formats

- Security
  - JWT-based authentication
  - Role-based access control
  - Secure password handling

## Tech Stack

- Java 17
- Spring Boot 3.x
- Spring Security
- Spring Data JPA
- PostgreSQL
- Maven
- JWT for authentication

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- PostgreSQL 12 or higher

## Setup

1. Clone the repository
```bash
git clone <repository-url>
cd vaccine-portal-backend
```

2. Configure the database
- Create a PostgreSQL database
- Update `application.properties` with your database credentials:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/vaccine_portal
spring.datasource.username=your_username
spring.datasource.password=your_password
```

3. Build the application
```bash
mvn clean install
```

4. Run the application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### Authentication
- POST `/api/auth/login` - User login
- POST `/api/auth/register` - Register new user

### Students
- GET `/api/students` - Get all students (paginated)
- POST `/api/students` - Create new student
- GET `/api/students/{id}` - Get student by ID
- PUT `/api/students/{id}` - Update student
- DELETE `/api/students/{id}` - Delete student
- POST `/api/students/import` - Import students from CSV
- GET `/api/students/export` - Export students to CSV

### Vaccination Records
- GET `/api/vaccination-records` - Get all vaccination records
- POST `/api/vaccination-records` - Create new vaccination record
- GET `/api/vaccination-records/{id}` - Get vaccination record by ID
- PUT `/api/vaccination-records/{id}` - Update vaccination record
- GET `/api/vaccination-records/report/export` - Export vaccination report

### Vaccination Drives
- GET `/api/drives` - Get all vaccination drives
- POST `/api/drives` - Create new vaccination drive
- GET `/api/drives/{id}` - Get drive by ID
- PUT `/api/drives/{id}` - Update drive
- DELETE `/api/drives/{id}` - Delete drive

## Database Schema

### Students Table
- student_id (PK)
- name
- grade
- section
- date_of_birth
- gender
- parent_name
- parent_email
- contact_number
- address
- is_active
- created_at
- updated_at

### Vaccination Records Table
- id (PK)
- student_id (FK)
- vaccine_id (FK)
- drive_id (FK)
- vaccination_date
- status
- notes
- created_at
- updated_at

### Vaccination Drives Table
- id (PK)
- vaccine_id (FK)
- drive_date
- available_doses
- applicable_grades
- status
- is_active
- created_at
- updated_at

## Error Handling

The application uses standard HTTP status codes and returns error messages in the following format:
```json
{
    "status": "error",
    "message": "Error message",
    "timestamp": "2024-01-01T00:00:00.000Z"
}
```

## Security

- All endpoints except `/api/auth/login` require authentication
- JWT token must be included in the Authorization header
- Role-based access control

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request
