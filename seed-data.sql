-- Insert sample tenant
INSERT INTO tenant (id, name, created_at) VALUES
('550e8400-e29b-41d4-a716-446655440000', 'Acme Corporation', NOW());

-- Insert sample users
INSERT INTO user (id, tenant_id, username, email, role, created_at) VALUES
('550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440000', 'admin', 'admin@acme.com', 'ROLE_ADMIN', NOW()),
('550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440000', 'hr_manager', 'hr@acme.com', 'ROLE_HR_MANAGER', NOW()),
('550e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440000', 'manager', 'manager@acme.com', 'ROLE_MANAGER', NOW()),
('550e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440000', 'employee', 'employee@acme.com', 'ROLE_EMPLOYEE', NOW());

-- Employee sample data (for employee-service database)
-- INSERT INTO employee (id, tenant_id, user_id, name, department, designation, email, phone, join_date, salary, created_at) VALUES
-- ('550e8400-e29b-41d4-a716-446655440010', '550e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440004', 'John Doe', 'Engineering', 'Senior Developer', 'john@acme.com', '9876543210', '2020-01-15', 500000, NOW());

-- Task sample data (for task-service database)
-- INSERT INTO task (id, tenant_id, title, description, status, priority, assignee_id, created_by, due_date, created_at) VALUES
-- ('550e8400-e29b-41d4-a716-446655440020', '550e8400-e29b-41d4-a716-446655440000', 'File TDS Returns', 'Prepare and file TDS returns', 'Pending', 'High', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440001', '2026-04-25', NOW());

-- Salary structure sample data (for payroll-service database)
-- INSERT INTO salary_structure (id, tenant_id, employee_id, basic_pay, hra, other_allowances, created_at) VALUES
-- ('550e8400-e29b-41d4-a716-446655440030', '550e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440010', 300000, 75000, 25000, NOW());
