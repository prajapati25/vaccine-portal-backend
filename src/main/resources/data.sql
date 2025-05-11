INSERT INTO users (username, password, role, created_at) VALUES
('admin', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'ADMIN', CURRENT_TIMESTAMP);


-- Insert sample vaccines
INSERT INTO vaccines (name, manufacturer, description, doses_required, days_between_doses, expiry_date, available_doses, price, active, created_at, updated_at) VALUES
('COVID-19 Vaccine', 'Pfizer', 'mRNA vaccine for COVID-19', 2, 21, '2024-12-31', 1000, 0.00, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Hepatitis B', 'GSK', 'Recombinant vaccine for Hepatitis B', 3, 30, '2024-12-31', 500, 0.00, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MMR', 'Merck', 'Measles, Mumps, and Rubella vaccine', 2, 28, '2024-12-31', 300, 0.00, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample vaccination drives
INSERT INTO vaccination_drives (vaccine_id, vaccine_batch, drive_date, available_doses, applicable_grades, minimum_age, maximum_age, status, is_active, notes, created_at, updated_at)
SELECT 
    v.id,
    'BATCH-001',
    '2024-03-15',
    100,
    '9,10,11,12',
    12,
    18,
    'SCHEDULED',
    true,
    'First dose vaccination drive',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM vaccines v
WHERE v.name = 'COVID-19 Vaccine';

INSERT INTO vaccination_drives (vaccine_id, vaccine_batch, drive_date, available_doses, applicable_grades, minimum_age, maximum_age, status, is_active, notes, created_at, updated_at)
SELECT 
    v.id,
    'BATCH-002',
    '2024-03-20',
    50,
    '9,10,11,12',
    12,
    18,
    'SCHEDULED',
    true,
    'Second dose vaccination drive',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM vaccines v
WHERE v.name = 'Hepatitis B';

INSERT INTO vaccination_drives (vaccine_id, vaccine_batch, drive_date, available_doses, applicable_grades, minimum_age, maximum_age, status, is_active, notes, created_at, updated_at)
SELECT 
    v.id,
    'BATCH-003',
    '2024-03-25',
    30,
    '9,10,11,12',
    12,
    18,
    'SCHEDULED',
    true,
    'Third dose vaccination drive',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM vaccines v
WHERE v.name = 'MMR';

-- Insert sample students with new roll number format
INSERT INTO students (student_id, name, grade, date_of_birth, gender, parent_name, parent_email, contact_number, address, is_active, created_at, updated_at) VALUES
('ROLL-2024-0001', 'John Doe', '10', '2008-05-15', 'MALE', 'Jane Doe', 'jane.doe@email.com', '1234567890', '123 Main St', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('ROLL-2024-0002', 'Jane Smith', '9', '2009-03-20', 'FEMALE', 'John Smith', 'john.smith@email.com', '0987654321', '456 Oak Ave', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('ROLL-2024-0003', 'Mike Johnson', '11', '2007-11-10', 'MALE', 'Sarah Johnson', 'sarah.j@email.com', '1122334455', '789 Pine St', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample vaccination records
INSERT INTO vaccination_records (student_id, vaccination_drive_id, dose_number, vaccination_date, batch_number, administered_by, vaccination_site, status, side_effects, next_dose_date, notes, created_at, updated_at)
SELECT 
    'ROLL-2024-0001',
    vd.id,
    1,
    '2024-03-15 10:00:00',
    'BATCH-001',
    'Dr. Smith',
    'School Clinic',
    'COMPLETED',
    'Mild fever',
    '2024-04-05',
    'First dose administered',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM vaccination_drives vd
WHERE vd.vaccine_batch = 'BATCH-001';

INSERT INTO vaccination_records (student_id, vaccination_drive_id, dose_number, vaccination_date, batch_number, administered_by, vaccination_site, status, side_effects, next_dose_date, notes, created_at, updated_at)
SELECT 
    'ROLL-2024-0002',
    vd.id,
    1,
    '2024-03-15 11:00:00',
    'BATCH-001',
    'Dr. Smith',
    'School Clinic',
    'COMPLETED',
    NULL,
    '2024-04-05',
    'First dose administered',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM vaccination_drives vd
WHERE vd.vaccine_batch = 'BATCH-001';

INSERT INTO vaccination_records (student_id, vaccination_drive_id, dose_number, vaccination_date, batch_number, administered_by, vaccination_site, status, side_effects, next_dose_date, notes, created_at, updated_at)
SELECT 
    'ROLL-2024-0003',
    vd.id,
    1,
    '2024-03-20 10:00:00',
    'BATCH-002',
    'Dr. Johnson',
    'School Clinic',
    'COMPLETED',
    'Sore arm',
    '2024-04-09',
    'First dose administered',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM vaccination_drives vd
WHERE vd.vaccine_batch = 'BATCH-002';