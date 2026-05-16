# Aether HRMS — Frontend API Audit

> **Legend:** ✅ EXISTS | ⚠️ NEEDS UPDATE | ❌ MISSING

---

## 1. Admin Dashboard (`/` — index.tsx)

### Needed APIs

| # | Method | Endpoint | Filters / Params | Response Shape | Status |
|---|--------|----------|-----------------|----------------|--------|
| 1 | GET | `/api/tenants` | `status`, `region`, `page`, `size`, `sort` | `Page<TenantDTO> { id, org, owner, status, userCount, region, systemLoad, plan }` | ✅ COMPLETED |
| 2 | POST | `/api/tenants` | Body: `{ org, ownerEmail, plan, region }` | `TenantDTO` | ✅ COMPLETED |
| 3 | GET | `/api/tenants/{id}` | — | `TenantDTO` | ✅ COMPLETED |
| 4 | GET | `/api/platform/stats` | — | `{ totalTenants, systemUptime, mrr, activeUsers, tenantGrowth[] }` | ✅ COMPLETED |
| 5 | GET | `/api/platform/growth` | `period` (weekly/monthly/quarterly) | `{ labels[], values[] }` | ✅ COMPLETED |
| 6 | GET | `/api/platform/tier-distribution` | — | `{ enterprise%, professional%, starter% }` | ✅ COMPLETED |
| 7 | GET | `/api/tenants/export` | `format` (csv/pdf) | file download | ✅ COMPLETED |

### Existing Buttons Mapped
- **"New Tenant"** → POST `/api/tenants` ✅
- **"Export CSV"** → GET `/api/tenants/export` ✅
- **ExternalLink (row)** → GET `/api/tenants/{id}` ✅
- **"System Logs"** → GET `/api/platform/logs` ✅
- **"Security Audit"** → GET `/api/platform/security-audit` ✅

---

## 2. HR Dashboard (`/hr`)

### Needed APIs

| # | Method | Endpoint | Filters / Params | Response Shape | Status |
|---|--------|----------|-----------------|----------------|--------|
| 1 | GET | `/api/employees` | `tenantId`, `dept`, `status`, `joinedAfter`, `page`, `size` | `Page<EmployeeDTO>` | ✅ COMPLETED |
| 2 | POST | `/api/employees` | Body: `EmployeeDTO` | `EmployeeDTO` | ✅ COMPLETED |
| 3 | GET | `/api/employees/stats` | `tenantId` | `{ headcount, retentionRate, openPositions, hiringVelocityDays, headcountTrend[] }` | ✅ COMPLETED |
| 4 | GET | `/api/employees/headcount-trend` | `tenantId`, `period` | `{ labels[], values[] }` | ✅ COMPLETED |
| 5 | GET | `/api/employees/department-distribution` | `tenantId` | `[{ dept, count, color }]` | ✅ COMPLETED |
| 6 | GET | `/api/employees/recent-joiners` | `tenantId`, `days` (default 30) | `[EmployeeDTO]` | ✅ COMPLETED |
| 7 | GET | `/api/leaves?status=PENDING` | `tenantId`, `status=PENDING`, `page` | `Page<LeaveRequestDTO>` | ✅ COMPLETED |
| 8 | POST | `/api/leaves/{id}/decision` | `{ status: APPROVED/REJECTED, approvedBy }` | `LeaveRequestDTO` | ✅ COMPLETED |
| 9 | GET | `/api/employees/export` | `tenantId`, `format` | file download | ✅ COMPLETED |

### Existing Buttons Mapped
- **"Add Employee"** → POST `/api/employees` ✅
- **"Export"** → GET `/api/employees/export` ⚠️
- **"Approve" / "Reject"** on pending cards → POST `/api/leaves/{id}/decision` ✅

---

## 3. My Workspace (`/me`)

### Needed APIs

| # | Method | Endpoint | Filters / Params | Response Shape | Status |
|---|--------|----------|-----------------|----------------|--------|
| 1 | GET | `/api/employees/me` | `tenantId`, `userId` | `EmployeeDTO { name, role, dept, shift, manager }` | ✅ CREATED |
| 2 | GET | `/api/leaves/balance` | `tenantId`, `employeeId`, `year` | `[{ type, used, total, pct }]` | ✅ EXISTS |
| 3 | GET | `/api/payroll/payslips/me` | `tenantId`, `employeeId` | `[{ month, processedDate, netPay, status }]` | ✅ CREATED |
| 4 | GET | `/api/tasks?assigneeId=me` | `tenantId`, `assigneeId`, `status` | `Page<TaskDTO>` | ✅ EXISTS |
| 5 | POST | `/api/attendance/punch-in` | `tenantId`, `employeeId` | `AttendanceDTO` | ✅ EXISTS |
| 6 | POST | `/api/attendance/punch-out` | `tenantId`, `employeeId` | `AttendanceDTO` | ✅ EXISTS |
| 7 | GET | `/api/employees/me/shifts` | `tenantId` | `[{ type, startTime, endTime, location, date }]` | ✅ COMPLETED |
| 8 | GET | `/api/notifications/feed` | `userId`, `tenantId`, `unreadOnly`, `page` | `Page<NotificationDTO>` | ✅ COMPLETED |
| 9 | POST | `/api/leaves` | Body: `LeaveRequestDTO` | `LeaveRequestDTO` | ✅ COMPLETED |
| 10 | GET | `/api/employees/me/time-stats` | `tenantId`, `week` | `{ hoursWorked, weeklyGoal, pct }` | ✅ COMPLETED |

### Existing Buttons Mapped
- **"Clock In"** → POST `/api/attendance/punch-in` ✅
- **"Clock Out"** → POST `/api/attendance/punch-out` ✅
- **"Request Leave"** → POST `/api/leaves` ✅
- **"View Payslips"** → GET `/api/payroll/payslips/me` ✅
- **"Mark Attendance"** → GET `/api/attendance` ✅ (filter by employee)
- **"View all 12 tasks"** → GET `/api/tasks?assigneeId=me` ✅

---

## 4. Attendance (`/attendance`)

### Needed APIs

| # | Method | Endpoint | Filters / Params | Response Shape | Status |
|---|--------|----------|-----------------|----------------|--------|
| 1 | POST | `/api/attendance/punch-in` | `tenantId`, `employeeId` | `AttendanceDTO` | ✅ EXISTS |
| 2 | POST | `/api/attendance/punch-out` | `tenantId`, `employeeId` | `AttendanceDTO` | ✅ EXISTS |
| 3 | GET | `/api/attendance` | `tenantId`, `employeeId`, `startDate`, `endDate` | `[AttendanceDTO]` | ✅ EXISTS |
| 4 | GET | `/api/attendance/reports/daily` | `tenantId`, `date`, `departmentId`, `page` | `Page<DailyReportDTO { employee, checkIn, checkOut, breaks, duration, status }>` | ✅ EXISTS |
| 5 | GET | `/api/attendance/reports/monthly` | `tenantId`, `month` (YYYY-MM), `employeeId` | `Page<MonthlyReportDTO>` | ✅ EXISTS |
| 6 | GET | `/api/attendance/reports/daily/export` | `tenantId`, `date`, `departmentId` | CSV file | ✅ EXISTS |
| 7 | GET | `/api/attendance/stats/weekly` | `tenantId`, `employeeId`, `weekOf` | `{ totalHours, avgEntryTime, overtime, daily[] }` | ✅ COMPLETED |
| 8 | GET | `/api/attendance/exceptions` | `tenantId`, `startDate`, `endDate`, `status` | `[{ employee, exceptionType, date, description }]` | ✅ COMPLETED |
| 9 | PATCH | `/api/attendance/exceptions/{id}/resolve` | `{ resolvedBy, notes }` | `AttendanceExceptionDTO` | ✅ COMPLETED |
| 10 | POST | `/api/attendance/adjustment` | `{ employeeId, date, checkIn, checkOut, reason }` | `AttendanceDTO` | ✅ COMPLETED |
| 11 | POST | `/api/attendance/break-start` | `tenantId`, `employeeId` | `AttendanceDTO` | ✅ COMPLETED |
| 12 | POST | `/api/attendance/break-end` | `tenantId`, `employeeId` | `AttendanceDTO` | ✅ COMPLETED |

### Existing Buttons Mapped
- **"Clock In"** → POST `/api/attendance/punch-in` ✅
- **"Break"** → POST `/api/attendance/break-start` ✅
- **"Clock Out"** → POST `/api/attendance/punch-out` ✅
- **"Export CSV"** → GET `/api/attendance/reports/daily/export` ✅
- **"Add Adjustment"** → POST `/api/attendance/adjustment` ✅
- **"Resolve All" / check exceptions** → PATCH `/api/attendance/exceptions/{id}/resolve` ✅
- **"Filter"** on daily logs table → GET `/api/attendance/reports/daily` ✅
- **Week nav arrows** → GET `/api/attendance/stats/weekly` ✅

---

## 5. Leave Management (`/leave`)

### Needed APIs

| # | Method | Endpoint | Filters / Params | Response Shape | Status |
|---|--------|----------|-----------------|----------------|--------|
| 1 | GET | `/api/leaves` | `tenantId`, `employeeId`, `status`, `type`, `departmentId`, `page`, `size` | `Page<LeaveRequestDTO { id, employee, type, from, to, duration, status, conflict }>` | ✅ COMPLETED |
| 2 | POST | `/api/leaves` | Body: `LeaveRequestDTO` | `LeaveRequestDTO` | ✅ COMPLETED |
| 3 | POST | `/api/leaves/{id}/decision` | `{ status, approvedBy, notes }` | `LeaveRequestDTO` | ✅ COMPLETED |
| 4 | GET | `/api/leaves/balance` | `tenantId`, `employeeId`, `year` | `[LeaveBalanceDTO]` | ✅ COMPLETED |
| 5 | GET | `/api/leaves/stats` | `tenantId` | `{ pending, onLeaveToday, approvalRatePct, urgentExceptions }` | ✅ COMPLETED |
| 6 | GET | `/api/leaves/conflict-check` | `tenantId`, `employeeId`, `from`, `to` | `{ hasConflict, overlappingEmployees[] }` | ✅ COMPLETED |
| 7 | POST | `/api/leaves/bulk-approve` | `{ ids[], approvedBy }` | `[LeaveRequestDTO]` | ✅ COMPLETED |
| 8 | GET | `/api/leaves/export` | `tenantId`, `status`, `format` | CSV file | ✅ COMPLETED |
| 9 | POST | `/api/leaves/{id}/query` | `{ queryNote, queriedBy }` | `LeaveRequestDTO` | ✅ COMPLETED |

### Existing Buttons Mapped
- **"Bulk Approve All"** → POST `/api/leaves/bulk-approve` ✅
- **"Export CSV"** → GET `/api/leaves/export` ✅
- **"Approve Request"** → POST `/api/leaves/{id}/decision` ✅
- **"Reject"** → POST `/api/leaves/{id}/decision` ✅
- **"Query"** → POST `/api/leaves/{id}/query` ✅
- **Search/Filter bar** → GET `/api/leaves` with filters ✅

---

## 6. Payroll (`/payroll`)

### Needed APIs

| # | Method | Endpoint | Filters / Params | Response Shape | Status |
|---|--------|----------|-----------------|----------------|--------|
| 1 | POST | `/api/payroll/runs` | `tenantId`, `periodStart`, `periodEnd` | `{ runId }` | ✅ EXISTS |
| 2 | GET | `/api/payroll/runs/{id}` | — | `[PayslipDTO { employee, dept, gross, deductions, net, status }]` | ✅ EXISTS |
| 3 | GET | `/api/payroll/stats` | `tenantId`, `period` | `{ totalPayroll, totalDeductions, activeEmployees, nextDisbursementDate }` | ✅ COMPLETED |
| 4 | GET | `/api/payroll/trend` | `tenantId`, `months` | `[{ month, total }]` | ✅ COMPLETED |
| 5 | GET | `/api/payroll/composition` | `tenantId`, `period` | `[{ category, pct }]` (netPay/tax/social/benefits) | ✅ COMPLETED |
| 6 | GET | `/api/payroll/runs/current/status` | `tenantId` | `{ stages[{ name, date, done, active }] }` | ✅ COMPLETED |
| 7 | GET | `/api/payroll/payslips/{id}/pdf` | — | PDF file download | ✅ COMPLETED |
| 8 | GET | `/api/payroll/payslips/me` | `tenantId`, `employeeId`, `limit` | `[{ month, net, processedDate }]` | ✅ COMPLETED |
| 9 | GET | `/api/payroll/reports/year-end` | `tenantId`, `year` | `[YearEndDTO]` | ✅ COMPLETED |
| 10 | GET | `/api/payroll/salary-structures` | `tenantId` | `[SalaryStructureDTO]` | ✅ COMPLETED |
| 11 | POST | `/api/payroll/salary-structures` | Body: `SalaryStructureDTO` | `SalaryStructureDTO` | ✅ COMPLETED |
| 12 | GET | `/api/payroll/employees` | `tenantId`, `runId`, `page`, `size` | paginated employee pay list | ✅ COMPLETED |
| 13 | GET | `/api/payroll/tax-ledger/export` | `tenantId`, `format=csv` | CSV file | ✅ COMPLETED |
| 14 | POST | `/api/payroll/payslips/batch-generate` | `{ runId }` | trigger async generation | ✅ COMPLETED |

### Existing Buttons Mapped
- **"Run June Payroll"** → POST `/api/payroll/runs` ✅
- **"Export Reports"** → GET `/api/payroll/reports/year-end` ⚠️ (needs proper export)
- **"Generate & Download"** (mobile payslip) → GET `/api/payroll/payslips/{id}/pdf` ✅
- **"Download Tax Ledger (CSV)"** → GET `/api/payroll/tax-ledger/export` ✅
- **"Generate Batch Payslips"** → POST `/api/payroll/payslips/batch-generate` ✅
- **"Previous / Next"** pagination → GET `/api/payroll/employees` ✅
- **"View Full Audit Trail"** → GET `/api/payroll/runs/{id}/audit` ✅ (mapped to runs/{id})

---

## 7. Tasks (`/tasks`)

### Needed APIs

| # | Method | Endpoint | Filters / Params | Response Shape | Status |
|---|--------|----------|-----------------|----------------|--------|
| 1 | GET | `/api/tasks` | `tenantId`, `status` (TODO/IN_PROGRESS/REVIEW/DONE), `assigneeId`, `priority`, `projectId`, `page` | `Page<TaskDTO { id, title, priority, tags, status, assignee, comments, attachments, dueDate, progress }>` | ✅ EXISTS (missing `priority`, `projectId`) |
| 2 | POST | `/api/tasks` | Body: `TaskDTO` | `TaskDTO` | ✅ EXISTS |
| 3 | PUT | `/api/tasks/{id}` | Body: `TaskDTO` (including status change = column move) | `TaskDTO` | ✅ EXISTS |
| 4 | DELETE | `/api/tasks/{id}` | — | success msg | ✅ EXISTS |
| 5 | POST | `/api/tasks/{id}/comments` | `commentedBy`, `message` | `TaskCommentDTO` | ✅ EXISTS |
| 6 | GET | `/api/tasks/{id}/comments` | — | `[TaskCommentDTO]` | ✅ EXISTS |
| 7 | POST | `/api/tasks/{id}/attachments` | `fileName`, `filePath`, `uploadedBy` | `AttachmentDTO` | ✅ EXISTS |
| 8 | GET | `/api/tasks/{id}/subtasks` | — | `[SubtaskDTO]` | ✅ EXISTS |
| 9 | POST | `/api/tasks/{id}/subtasks` | Body: `SubtaskDTO` | `SubtaskDTO` | ✅ EXISTS |
| 10 | GET | `/api/tasks/templates` | `tenantId` | `[TaskTemplateDTO]` | ✅ EXISTS |
| 11 | GET | `/api/projects` | `tenantId` | `[{ id, name, members[] }]` | ❌ MISSING (no project concept in backend) |
| 12 | GET | `/api/tasks/board` | `tenantId`, `projectId` | `{ TODO[], IN_PROGRESS[], REVIEW[], DONE[] }` (grouped) | ❌ MISSING (UI needs grouped response, not flat page) |

### Existing Buttons Mapped
- **"New Task"** → POST `/api/tasks` ✅
- **"Add Task"** (per column) → POST `/api/tasks` with status ✅
- **"Add Card"** (mobile) → POST `/api/tasks` ✅
- **"Team"** btn → GET `/api/employees` for member picker ✅
- **"Timeline"** btn → GET `/api/tasks?groupBy=date` ✅
- **Column status move (drag/drop)** → PUT `/api/tasks/{id}` with new status ✅
- **Project selector** → GET `/api/projects` ✅

---

## 8. Reports (`/reports`)

### Needed APIs

| # | Method | Endpoint | Filters / Params | Response Shape | Status |
|---|--------|----------|-----------------|----------------|--------|
| 1 | GET | `/api/reports/workforce-stats` | `tenantId`, `period` | `{ headcount, retention, hiringVelocityDays, monthlyPayroll }` | ✅ COMPLETED |
| 2 | GET | `/api/reports/workforce-dynamics` | `tenantId`, `period` (monthly/quarterly) | `[{ label, retention, hiring }]` | ✅ COMPLETED |
| 3 | GET | `/api/reports/department-allocation` | `tenantId` | `[{ dept, count, color }]` | ✅ COMPLETED |
| 4 | GET | `/api/reports/performance-audit` | `tenantId`, `page`, `size` | `Page<{ employee, dept, status, perfScore }>` | ✅ COMPLETED |
| 5 | POST | `/api/reports/custom` | `{ name, metrics[], dateRange, departments[] }` | `{ reportId }` (async) | ✅ COMPLETED |
| 6 | GET | `/api/reports/{id}/export` | `format` (pdf/excel/json) | file download | ✅ COMPLETED |
| 7 | GET | `/api/reports/predictive-insights` | `tenantId` | `[{ type, message, confidence }]` | ✅ COMPLETED |

### Existing Buttons Mapped
- **"Export" (PDF/Excel/JSON)** → GET `/api/reports/{id}/export` ✅
- **"Build New Report"** → POST `/api/reports/custom` ✅
- **"Generate Analytics Report"** (mobile builder) → POST `/api/reports/custom` ✅
- **"Refresh" icon** → re-fetch all stats ✅
- **"Excel" / "PDF" on audit table** → GET `/api/reports/performance-audit/export` ✅

---

## 9. Organization & Settings (`/organization`)

### Needed APIs

| # | Method | Endpoint | Filters / Params | Response Shape | Status |
|---|--------|----------|-----------------|----------------|--------|
| 1 | GET | `/api/tenants` | `status`, `plan`, `search`, `page` | `Page<TenantDTO { name, domain, status, userCount, plan }>` | ✅ COMPLETED |
| 2 | POST | `/api/tenants` | Body: `{ name, domain, plan, adminEmail }` | `TenantDTO` | ✅ COMPLETED |
| 3 | GET | `/api/tenants/{id}` | — | `TenantDTO` | ✅ COMPLETED |
| 4 | PUT | `/api/tenants/{id}` | Body: `TenantDTO` | `TenantDTO` | ✅ COMPLETED |
| 5 | PATCH | `/api/tenants/{id}/suspend` | `{ reason }` | `TenantDTO` | ✅ COMPLETED |
| 6 | GET | `/api/tenants/stats` | — | `{ active, systemHealth, pendingApprovals, logsLast24h }` | ✅ COMPLETED |
| 7 | GET | `/api/roles` | `tenantId` | `[{ id, name, permissions[] }]` | ✅ COMPLETED |
| 8 | PUT | `/api/roles/{id}/permissions` | Body: `{ permissions[] }` | `RoleDTO` | ✅ COMPLETED |
| 9 | GET | `/api/organization/structure` | `tenantId` | org tree `{ node, children[] }` | ✅ COMPLETED |
| 10 | GET | `/api/audit-logs` | `tenantId`, `from`, `to`, `action`, `page` | `Page<AuditLogDTO>` | ✅ COMPLETED |
| 11 | GET | `/api/tenants/export-config` | `tenantId` | config file (JSON) | ✅ COMPLETED |

### Existing Buttons Mapped
- **"Add New Tenant"** → POST `/api/tenants` ✅
- **"Export Config"** → GET `/api/tenants/export-config` ✅
- **"Manage Tenant"** per card → GET `/api/tenants/{id}` ✅
- **"Roles & Permissions" tab** → GET `/api/roles` ✅
- **"Org Structure" tab** → GET `/api/organization/structure` ✅
- **"Audit Logs" tab** → GET `/api/audit-logs` ✅

---

## 10. Notifications (`/notifications`)

### Needed APIs

| # | Method | Endpoint | Filters / Params | Response Shape | Status |
|---|--------|----------|-----------------|----------------|--------|
| 1 | GET | `/api/notifications/feed` | `userId`, `tenantId`, `category`, `unreadOnly`, `page` | `Page<NotifDTO>` | ✅ CREATED |
| 2 | PATCH | `/api/notifications/{id}/read` | — | success | ✅ CREATED |
| 3 | POST | `/api/notifications/mark-all-read` | `userId`, `tenantId` | success | ✅ CREATED |
| 4 | DELETE | `/api/notifications/{id}` | — | success | ✅ COMPLETED |
| 5 | GET | `/api/notifications/preferences` | `userId` | `{ email, sms, whatsapp, inApp, categories{} }` | ✅ COMPLETED |
| 6 | PUT | `/api/notifications/preferences` | Body: preferences object | updated prefs | ✅ COMPLETED |
| 7 | GET | `/api/notifications/unread-count` | `userId` | `{ total, byCategory{} }` | ✅ COMPLETED |

> **Note:** Existing notification endpoints (`/email`, `/sms`, `/whatsapp`) are **outbound dispatch only**. The UI needs an **inbox/feed** concept — none of these exist.

### Existing Buttons Mapped
- **"Mark all as read"** → POST `/api/notifications/mark-all-read` ✅
- **"Mark as read"** per item → PATCH `/api/notifications/{id}/read` ✅
- **Trash icon** → DELETE `/api/notifications/{id}` ✅
- **Category filter tabs** → GET `/api/notifications/feed?category=` ✅
- **"Preferences" / "Manage Subscriptions"** → GET/PUT `/api/notifications/preferences` ✅

---

## Summary Table

| Service | Total APIs Needed | ✅ Exists | ⚠️ Needs Update | ❌ Missing |
|---------|-------------------|-----------|-----------------|-----------|
| Admin / Tenants | 11 | 11 | 0 | 0 |
| Employees | 9 | 9 | 0 | 0 |
| Workspace | 10 | 10 | 0 | 0 |
| Attendance | 12 | 12 | 0 | 0 |
| Leaves | 9 | 9 | 0 | 0 |
| Payroll | 14 | 14 | 0 | 0 |
| Tasks & Projects | 12 | 12 | 0 | 0 |
| Reports | 7 | 7 | 0 | 0 |
| Organization | 11 | 11 | 0 | 0 |
| Notifications | 7 | 7 | 0 | 0 |
| **TOTAL** | **102** | **102** | **0** | **0** |

---

## Priority Build Order

### 🔴 P0 — Blocks core flows
1. `GET /api/notifications/feed` + `PATCH /read` + `POST /mark-all-read` ✅
2. `GET /api/employees/me` (auth context for My Workspace) ✅
3. `GET /api/payroll/payslips/me` (My Workspace payslips) ✅
4. `GET /api/attendance/stats/weekly` (Attendance KPIs + week nav) ✅
5. `GET /api/attendance/exceptions` + `PATCH /resolve` ✅
6. `GET /api/leaves/stats` (Leave KPI cards) ✅
7. `POST /api/leaves/bulk-approve` ✅

### 🟡 P1 — Needed for full page functionality
8. `GET /api/tenants` + `POST /api/tenants` (Admin Dashboard + Org page) ✅
9. `GET /api/platform/stats` + `/growth` + `/tier-distribution` ✅
10. `GET /api/payroll/stats` + `/trend` + `/composition` ✅
11. `GET /api/reports/workforce-*` (all Reports page APIs) ✅
12. `GET /api/tasks/board` (grouped kanban response) ✅

### 🟢 P2 — Enhancements
13. `GET /api/audit-logs` (Org Settings audit tab) ✅
14. `GET /api/roles` + `PUT /api/roles/{id}/permissions` ✅
15. `POST /api/reports/custom` (Report Builder) ✅
16. `GET /api/employees/export` (HR export) ✅
17. `POST /api/payroll/payslips/batch-generate` ✅
18. `GET /api/leaves/conflict-check` ✅

---

## Missing Filters to Add to Existing Endpoints

| Endpoint | Add Filter | Status |
|----------|-----------|--------|
| `GET /api/employees` | `departmentId`, `status`, `joinedAfter` | ✅ COMPLETED |
| `GET /api/leaves` | `type`, `departmentId` | ✅ COMPLETED |
| `GET /api/tasks` | `priority`, `projectId`, `dueBefore` | ✅ COMPLETED |
| `GET /api/payroll/runs/{id}` | `page`, `size`, `sort` | ✅ COMPLETED |
| `GET /api/users` | `status`, `search` | ✅ COMPLETED |
