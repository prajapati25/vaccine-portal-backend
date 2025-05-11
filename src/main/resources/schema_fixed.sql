-- Drop existing tables if they exist
DROP TABLE IF EXISTS vaccination_records;
DROP TABLE IF EXISTS vaccination_drives;
DROP TABLE IF EXISTS vaccines;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create vaccines table
CREATE TABLE vaccines (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    manufacturer VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    doses_required INTEGER NOT NULL,
    days_between_doses INTEGER NOT NULL,
    expiry_date DATE NOT NULL,
    available_doses INTEGER NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create students table
CREATE TABLE students (
    student_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    grade VARCHAR(50) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(10),
    parent_name VARCHAR(255),
    parent_email VARCHAR(255),
    contact_number VARCHAR(20),
    address TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create vaccination_drives table
CREATE TABLE vaccination_drives (
    id BIGSERIAL PRIMARY KEY,
    vaccine_id BIGINT NOT NULL REFERENCES vaccines(id),
    vaccine_batch VARCHAR(100),
    drive_date DATE NOT NULL,
    available_doses INTEGER NOT NULL,
    applicable_grades VARCHAR(255) NOT NULL,
    minimum_age INTEGER,
    maximum_age INTEGER,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create vaccination_records table
CREATE TABLE vaccination_records (
    id BIGSERIAL PRIMARY KEY,
    student_id VARCHAR(50) NOT NULL REFERENCES students(student_id),
    vaccination_drive_id BIGINT NOT NULL REFERENCES vaccination_drives(id),
    dose_number INTEGER NOT NULL,
    vaccination_date TIMESTAMP NOT NULL,
    batch_number VARCHAR(100) NOT NULL,
    administered_by VARCHAR(255) NOT NULL,
    vaccination_site VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    side_effects TEXT,
    next_dose_date DATE,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_students_grade ON students(grade);
CREATE INDEX idx_students_name ON students(name);
CREATE INDEX idx_students_email ON students(parent_email);
CREATE INDEX idx_vaccines_name ON vaccines(name);
CREATE INDEX idx_vaccines_manufacturer ON vaccines(manufacturer);
CREATE INDEX idx_vaccination_drives_date ON vaccination_drives(drive_date);
CREATE INDEX idx_vaccination_drives_status ON vaccination_drives(status);
CREATE INDEX idx_vaccination_drives_grades ON vaccination_drives(applicable_grades);
CREATE INDEX idx_vaccination_records_student ON vaccination_records(student_id);
CREATE INDEX idx_vaccination_records_drive ON vaccination_records(vaccination_drive_id);
CREATE INDEX idx_vaccination_records_status ON vaccination_records(status);
CREATE INDEX idx_vaccination_records_date ON vaccination_records(vaccination_date);

-- Create triggers for updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_students_updated_at
    BEFORE UPDATE ON students
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_vaccines_updated_at
    BEFORE UPDATE ON vaccines
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_vaccination_drives_updated_at
    BEFORE UPDATE ON vaccination_drives
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_vaccination_records_updated_at
    BEFORE UPDATE ON vaccination_records
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column(); 