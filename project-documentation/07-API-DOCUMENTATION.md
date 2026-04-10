# 📡 API DOCUMENTATION

**Complete REST API endpoints with request/response examples for all microservices**

---

## 🎯 API OVERVIEW

**Base URL**: `http://localhost:8080/api/v1`
**Authentication**: JWT Bearer Token in Authorization header
**Content Type**: `application/json`
**Response Format**: All responses wrapped in ApiResponse envelope

### Standard Headers
```
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
Content-Type: application/json
```

### Response Envelope
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { /* actual data */ },
  "timestamp": "2026-04-10T10:30:00Z",
  "path": "/api/v1/users"
}
```

---

## 🔐 AUTH SERVICE

### 1. Login
```
POST /auth/login
```

**Request**:
```json
{
  "username": "john@upcraft.com",
  "password": "SecurePassword123!"
}
```

**Response (201 Created)**:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ...",
    "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ...",
    "expiresIn": 3600,
    "user": {
      "id": "user-123",
      "username": "john@upcraft.com",
      "email": "john@upcraft.com",
      "roles": ["EMPLOYEE", "MANAGER"]
    }
  }
}
```

**Error (401 Unauthorized)**:
```json
{
  "success": false,
  "message": "Invalid credentials",
  "timestamp": "2026-04-10T10:30:00Z"
}
```

---

### 2. Refresh Token
```
POST /auth/refresh
```

**Request**:
```json
{
  "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ..."
}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ...",
    "expiresIn": 3600
  }
}
```

---

### 3. Logout
```
POST /auth/logout
Authorization: Bearer <token>
```

**Response (200 OK)**:
```json
{
  "success": true,
  "message": "Logout successful"
}
```

---

## 👤 USER SERVICE

### 1. Get All Users
```
GET /users
Authorization: Bearer <token>
X-Tenant-ID: tenant-123
```

**Query Parameters**:
- `page`: 0 (default)
- `size`: 20 (default)
- `status`: ACTIVE|INACTIVE (optional)
- `search`: Search by username or email (optional)

**Response (200 OK)**:
```json
{
  "success": true,
  "message": "Users retrieved",
  "data": [
    {
      "id": "user-123",
      "tenantId": "tenant-1",
      "username": "john",
      "email": "john@upcraft.com",
      "status": "ACTIVE",
      "createdAt": "2026-04-10T08:00:00Z"
    },
    {
      "id": "user-124",
      "tenantId": "tenant-1",
      "username": "jane",
      "email": "jane@upcraft.com",
      "status": "ACTIVE",
      "createdAt": "2026-04-10T08:15:00Z"
    }
  ]
}
```

---

### 2. Get User by ID
```
GET /users/{id}
Authorization: Bearer <token>
X-Tenant-ID: tenant-123
```

**Path Parameters**:
- `id`: User ID (required)

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "id": "user-123",
    "tenantId": "tenant-1",
    "username": "john",
    "email": "john@upcraft.com",
    "status": "ACTIVE",
    "createdAt": "2026-04-10T08:00:00Z",
    "updatedAt": "2026-04-10T09:30:00Z"
  }
}
```

**Error (404 Not Found)**:
```json
{
  "success": false,
  "message": "User not found with id: user-999"
}
```

---

### 3. Create User
```
POST /users
Authorization: Bearer <token>
X-Tenant-ID: tenant-123
Content-Type: application/json
```

**Request Body**:
```json
{
  "username": "newuser",
  "email": "newuser@upcraft.com",
  "tenantId": "tenant-123"
}
```

**Response (201 Created)**:
```json
{
  "success": true,
  "message": "User created successfully",
  "data": {
    "id": "user-125",
    "tenantId": "tenant-123",
    "username": "newuser",
    "email": "newuser@upcraft.com",
    "status": "ACTIVE",
    "createdAt": "2026-04-10T10:30:00Z"
  }
}
```

**Error (400 Bad Request - Validation)**:
```json
{
  "success": false,
  "message": "Email is required",
  "timestamp": "2026-04-10T10:30:00Z"
}
```

**Error (400 Bad Request - Duplicate)**:
```json
{
  "success": false,
  "message": "Username already exists"
}
```

---

### 4. Update User
```
PUT /users/{id}
Authorization: Bearer <token>
X-Tenant-ID: tenant-123
Content-Type: application/json
```

**Request Body**:
```json
{
  "email": "newemail@upcraft.com",
  "status": "INACTIVE"
}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "message": "User updated successfully",
  "data": {
    "id": "user-123",
    "email": "newemail@upcraft.com",
    "status": "INACTIVE",
    "updatedAt": "2026-04-10T10:30:00Z"
  }
}
```

---

### 5. Delete User
```
DELETE /users/{id}
Authorization: Bearer <token>
X-Tenant-ID: tenant-123
```

**Response (204 No Content)**: No body

**Error (404 Not Found)**:
```json
{
  "success": false,
  "message": "User not found"
}
```

---

## 👨‍💼 EMPLOYEE SERVICE

### 1. Get All Employees
```
GET /employees
Authorization: Bearer <token>
X-Tenant-ID: tenant-123
```

**Query Parameters**:
- `page`: 0 (default)
- `size`: 20 (default)
- `department`: Filter by department (optional)
- `status`: ACTIVE|INACTIVE (optional)

**Response (200 OK)**:
```json
{
  "success": true,
  "data": [
    {
      "id": "emp-001",
      "tenantId": "tenant-123",
      "userId": "user-123",
      "name": "John Doe",
      "email": "john@upcraft.com",
      "department": "Engineering",
      "designation": "Senior Developer",
      "salary": 50000,
      "status": "ACTIVE",
      "joinDate": "2024-01-15"
    }
  ]
}
```

---

### 2. Get Employee by ID
```
GET /employees/{id}
Authorization: Bearer <token>
X-Tenant-ID: tenant-123
```

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "id": "emp-001",
    "tenantId": "tenant-123",
    "userId": "user-123",
    "name": "John Doe",
    "email": "john@upcraft.com",
    "department": "Engineering",
    "designation": "Senior Developer",
    "salary": 50000,
    "status": "ACTIVE",
    "joinDate": "2024-01-15",
    "leaveBalance": {
      "sick": 10,
      "casual": 12,
      "earned": 15
    }
  }
}
```

---

### 3. Create Employee
```
POST /employees
Authorization: Bearer <token>
X-Tenant-ID: tenant-123
Content-Type: application/json
```

**Request Body**:
```json
{
  "userId": "user-125",
  "name": "Jane Smith",
  "department": "Engineering",
  "designation": "Developer",
  "salary": 45000,
  "joinDate": "2026-04-15"
}
```

**Response (201 Created)**:
```json
{
  "success": true,
  "message": "Employee created",
  "data": {
    "id": "emp-002",
    "userId": "user-125",
    "name": "Jane Smith",
    "department": "Engineering"
  }
}
```

---

## ⏰ ATTENDANCE SERVICE

### 1. Punch In
```
POST /attendance/punch-in
Authorization: Bearer <token>
X-Tenant-ID: tenant-123
X-Employee-ID: emp-001
```

**Request Body**:
```json
{
  "location": "Office Building A",
  "latitude": 28.6139,
  "longitude": 77.2090
}
```

**Response (201 Created)**:
```json
{
  "success": true,
  "message": "Punched in successfully",
  "data": {
    "id": "att-001",
    "employeeId": "emp-001",
    "date": "2026-04-10",
    "punchInTime": "2026-04-10T09:00:00Z",
    "punchInLocation": "Office Building A"
  }
}
```

---

### 2. Punch Out
```
POST /attendance/punch-out
Authorization: Bearer <token>
X-Tenant-ID: tenant-123
X-Employee-ID: emp-001
```

**Response (200 OK)**:
```json
{
  "success": true,
  "message": "Punched out successfully",
  "data": {
    "id": "att-001",
    "employeeId": "emp-001",
    "date": "2026-04-10",
    "punchInTime": "2026-04-10T09:00:00Z",
    "punchOutTime": "2026-04-10T17:30:00Z",
    "workedHours": 8.5
  }
}
```

---

### 3. Get Attendance Records
```
GET /attendance
Authorization: Bearer <token>
X-Tenant-ID: tenant-123
X-Employee-ID: emp-001
```

**Query Parameters**:
- `fromDate`: YYYY-MM-DD (optional)
- `toDate`: YYYY-MM-DD (optional)

**Response (200 OK)**:
```json
{
  "success": true,
  "data": [
    {
      "id": "att-001",
      "date": "2026-04-10",
      "punchInTime": "2026-04-10T09:00:00Z",
      "punchOutTime": "2026-04-10T17:30:00Z",
      "workedHours": 8.5,
      "status": "PRESENT"
    }
  ]
}
```

---

## 🗓️ LEAVE SERVICE

### 1. Get Leave Balance
```
GET /leaves/balance
Authorization: Bearer <token>
X-Tenant-ID: tenant-123
X-Employee-ID: emp-001
```

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "employeeId": "emp-001",
    "year": 2026,
    "sickLeave": {
      "allocated": 12,
      "used": 2,
      "available": 10
    },
    "casualLeave": {
      "allocated": 12,
      "used": 3,
      "available": 9
    },
    "earnedLeave": {
      "allocated": 15,
      "used": 5,
      "available": 10
    }
  }
}
```

---

### 2. Apply for Leave
```
POST /leaves/apply
Authorization: Bearer <token>
X-Tenant-ID: tenant-123
X-Employee-ID: emp-001
Content-Type: application/json
```

**Request Body**:
```json
{
  "leaveType": "CASUAL",
  "fromDate": "2026-04-15",
  "toDate": "2026-04-17",
  "numberOfDays": 3,
  "reason": "Personal reasons"
}
```

**Response (201 Created)**:
```json
{
  "success": true,
  "message": "Leave request submitted",
  "data": {
    "id": "leave-001",
    "employeeId": "emp-001",
    "leaveType": "CASUAL",
    "fromDate": "2026-04-15",
    "toDate": "2026-04-17",
    "numberOfDays": 3,
    "status": "PENDING",
    "appliedOn": "2026-04-10T10:30:00Z"
  }
}
```

---

### 3. Get Leave Requests
```
GET /leaves/requests
Authorization: Bearer <token>
X-Tenant-ID: tenant-123
```

**Query Parameters**:
- `status`: PENDING|APPROVED|REJECTED (optional)
- `page`: 0 (default)

**Response (200 OK)**:
```json
{
  "success": true,
  "data": [
    {
      "id": "leave-001",
      "employeeId": "emp-001",
      "leaveType": "CASUAL",
      "fromDate": "2026-04-15",
      "toDate": "2026-04-17",
      "status": "PENDING",
      "appliedOn": "2026-04-10T10:30:00Z"
    }
  ]
}
```

---

### 4. Approve/Reject Leave
```
PUT /leaves/requests/{id}/approve
Authorization: Bearer <token>
X-Tenant-ID: tenant-123
Content-Type: application/json
```

**Request Body**:
```json
{
  "approved": true,
  "comments": "Approved"
}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "message": "Leave request approved",
  "data": {
    "id": "leave-001",
    "status": "APPROVED",
    "approvedBy": "manager-123",
    "approvedOn": "2026-04-11T09:00:00Z"
  }
}
```

---

## 📋 TASK SERVICE

### 1. Get All Tasks
```
GET /tasks
Authorization: Bearer <token>
X-Tenant-ID: tenant-123
```

**Query Parameters**:
- `assignee`: emp-id (optional)
- `status`: OPEN|IN_PROGRESS|COMPLETED (optional)
- `priority`: HIGH|MEDIUM|LOW (optional)

**Response (200 OK)**:
```json
{
  "success": true,
  "data": [
    {
      "id": "task-001",
      "title": "Implement API endpoints",
      "description": "Create REST API for user service",
      "assignee": "emp-001",
      "status": "IN_PROGRESS",
      "priority": "HIGH",
      "dueDate": "2026-04-15",
      "createdOn": "2026-04-10T08:00:00Z",
      "estimatedHours": 16
    }
  ]
}
```

---

### 2. Create Task
```
POST /tasks
Authorization: Bearer <token>
X-Tenant-ID: tenant-123
Content-Type: application/json
```

**Request Body**:
```json
{
  "title": "Implement API endpoints",
  "description": "Create REST API for user service",
  "assignee": "emp-001",
  "priority": "HIGH",
  "dueDate": "2026-04-15",
  "estimatedHours": 16
}
```

**Response (201 Created)**:
```json
{
  "success": true,
  "data": {
    "id": "task-001",
    "title": "Implement API endpoints",
    "status": "OPEN",
    "createdOn": "2026-04-10T10:30:00Z"
  }
}
```

---

### 3. Update Task
```
PUT /tasks/{id}
Authorization: Bearer <token>
X-Tenant-ID: tenant-123
Content-Type: application/json
```

**Request Body**:
```json
{
  "status": "COMPLETED",
  "actualHours": 15.5
}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "id": "task-001",
    "status": "COMPLETED",
    "completedOn": "2026-04-12T17:00:00Z",
    "actualHours": 15.5
  }
}
```

---

### 4. Approve Task (Calculate Bonus)
```
POST /tasks/{id}/approve
Authorization: Bearer <token>
X-Tenant-ID: tenant-123
X-Employee-ID: manager-123
Content-Type: application/json
```

**Request Body**:
```json
{
  "performanceRating": "EXCELLENT",
  "bonusPercentage": 10
}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "message": "Task approved and bonus calculated",
  "data": {
    "id": "task-001",
    "status": "APPROVED",
    "bonusAmount": 5000,
    "approvedOn": "2026-04-12T18:00:00Z"
  }
}
```

---

## 💰 PAYROLL SERVICE

### 1. Get Salary Structure
```
GET /payroll/salary-structure/{employeeId}
Authorization: Bearer <token>
X-Tenant-ID: tenant-123
```

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "id": "sal-001",
    "employeeId": "emp-001",
    "baseSalary": 40000,
    "hra": 8000,
    "dearness": 2000,
    "conveyance": 1600,
    "totalGross": 51600,
    "effectiveFrom": "2025-04-01"
  }
}
```

---

### 2. Run Monthly Payroll
```
POST /payroll/run
Authorization: Bearer <token>
X-Tenant-ID: tenant-123
Content-Type: application/json
```

**Request Body**:
```json
{
  "month": 4,
  "year": 2026,
  "processDate": "2026-04-30"
}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "message": "Payroll processed for April 2026",
  "data": {
    "id": "payrun-001",
    "month": 4,
    "year": 2026,
    "employeesProcessed": 45,
    "totalAmount": 2250000,
    "status": "COMPLETED",
    "processedOn": "2026-04-30T18:00:00Z"
  }
}
```

---

### 3. Get Payslip
```
GET /payroll/payslips/{payslipId}
Authorization: Bearer <token>
X-Tenant-ID: tenant-123
```

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "id": "payslip-001",
    "employeeId": "emp-001",
    "month": 4,
    "year": 2026,
    "earnings": {
      "basicSalary": 40000,
      "hra": 8000,
      "dearness": 2000,
      "conveyance": 1600
    },
    "deductions": {
      "incomeTax": 4800,
      "professionalTax": 200,
      "pf": 2000,
      "esi": 483
    },
    "grossPay": 51600,
    "netPay": 41317,
    "generatedOn": "2026-04-30T18:00:00Z"
  }
}
```

---

## 📧 NOTIFICATION SERVICE

### 1. Send Email
```
POST /notifications/email
Authorization: Bearer <token>
X-Tenant-ID: tenant-123
Content-Type: application/json
```

**Request Body**:
```json
{
  "to": "employee@upcraft.com",
  "subject": "Payslip for April 2026",
  "templateName": "PAYSLIP_EMAIL",
  "variables": {
    "employeeName": "John Doe",
    "month": "April",
    "year": 2026,
    "netPay": 41317
  }
}
```

**Response (201 Created)**:
```json
{
  "success": true,
  "message": "Email sent successfully",
  "data": {
    "id": "notif-001",
    "to": "employee@upcraft.com",
    "status": "SENT",
    "sentOn": "2026-04-30T18:00:00Z"
  }
}
```

---

### 2. Send SMS
```
POST /notifications/sms
Authorization: Bearer <token>
X-Tenant-ID: tenant-123
Content-Type: application/json
```

**Request Body**:
```json
{
  "phone": "+91-9999999999",
  "message": "Your leave request has been approved",
  "type": "LEAVE_APPROVAL"
}
```

**Response (201 Created)**:
```json
{
  "success": true,
  "message": "SMS sent successfully",
  "data": {
    "id": "notif-002",
    "phone": "+91-9999999999",
    "status": "SENT"
  }
}
```

---

## 🔍 ERROR CODES & STATUS

| Code | HTTP Status | Meaning |
|------|-------------|---------|
| 200 | 200 OK | Success |
| 201 | 201 Created | Resource created |
| 204 | 204 No Content | Success, no content |
| 400 | 400 Bad Request | Validation failed |
| 401 | 401 Unauthorized | Missing/invalid token |
| 403 | 403 Forbidden | Insufficient permission |
| 404 | 404 Not Found | Resource not found |
| 422 | 422 Unprocessable | Business rule violation |
| 503 | 503 Service Unavailable | External service down |

---

## 🧪 TESTING WITH CURL

```bash
# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "password": "password"
  }'

# Get Users
curl -X GET http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer <token>" \
  -H "X-Tenant-ID: tenant-123"

# Create User
curl -X POST http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer <token>" \
  -H "X-Tenant-ID: tenant-123" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "email": "newuser@upcraft.com"
  }'
```

---

**API Version**: v1
**Last Updated**: Apr 10, 2026
**Status**: ✅ Complete
