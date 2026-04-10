# 📝 DEVELOPMENT GUIDE

**How to code, development standards, and best practices for the HRMS project**

---

## 🎯 BEFORE YOU START CODING

### Prerequisites
- [ ] Read 01-PROJECT-OVERVIEW.md (understand what we're building)
- [ ] Read 02-ARCHITECTURE-DESIGN.md (understand how it works)
- [ ] Have Java 17+ installed
- [ ] Have Maven 3.8+ installed
- [ ] Have Git configured
- [ ] Have IDE (IntelliJ IDEA recommended)
- [ ] Access to project repository

### Development Environment Setup

```bash
# 1. Clone repository
git clone <repo-url>
cd Upcraft/Product

# 2. Start infrastructure
docker-compose up -d

# 3. Build project
mvn clean install

# 4. Verify setup
mvn test
```

---

## 📋 CODING STANDARDS

### 1. Java Code Style

#### Naming Conventions
```java
// Classes: PascalCase
public class UserService { }
public class EmployeeController { }

// Methods: camelCase
public User getUserById(String userId) { }
public void recordAttendance(String employeeId) { }

// Constants: UPPER_SNAKE_CASE
public static final String TENANT_ID_HEADER = "X-Tenant-ID";
public static final int MAX_USERS_PER_TENANT = 1000;

// Variables: camelCase
String userName = "john";
int employeeCount = 5;
```

#### Format & Indentation
```java
// Use 4 spaces (NOT tabs)
// Max line length: 120 characters
// Braces: Java style (opening brace on same line)

public class UserService {
    public User getUserById(String userId) {
        if (userId == null) {
            throw new ValidationException("User ID cannot be null");
        }
        return userRepository.findById(userId);
    }
}
```

#### Class Structure Order
```java
public class UserService {
    // 1. Static constants
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final String ACTIVE_STATUS = "ACTIVE";

    // 2. Dependency injections
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationServiceClient notificationClient;

    // 3. Constructors
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 4. Public methods
    public User createUser(UserDTO dto) { }
    public User getUserById(String id) { }

    // 5. Private helper methods
    private void validateUserDTO(UserDTO dto) { }
    private void sendWelcomeEmail(User user) { }
}
```

### 2. Spring Boot Best Practices

#### Controller Pattern
```java
@RestController
@RequestMapping("/api/v1/users")
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(
            @PathVariable String id,
            @RequestHeader(value = "X-Tenant-ID") String tenantId) {
        try {
            User user = userService.getUserById(id, tenantId);
            return ResponseEntity.ok(
                ApiResponse.success("User retrieved", user)
            );
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}
```

#### Service Pattern
```java
@Service
@Slf4j
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    public User createUser(UserDTO dto, String tenantId) {
        // 1. Validate input
        validateUserDTO(dto);

        // 2. Check tenant exists
        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        // 3. Check unique constraint
        if (userRepository.existsByUsernameAndTenantId(dto.getUsername(), tenantId)) {
            throw new ValidationException("Username already exists");
        }

        // 4. Create entity
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setTenantId(tenantId);
        user.setStatus("ACTIVE");

        // 5. Save to database
        User saved = userRepository.save(user);

        // 6. Publish event
        logger.info("User created: {}", saved.getId());

        return saved;
    }
}
```

#### Repository Pattern
```java
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    // Custom queries with tenant isolation
    List<User> findByTenantIdAndStatus(String tenantId, String status);

    Optional<User> findByUsernameAndTenantId(String username, String tenantId);

    boolean existsByUsernameAndTenantId(String username, String tenantId);

    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.email LIKE %:email%")
    List<User> searchByEmail(@Param("tenantId") String tenantId,
                             @Param("email") String email);
}
```

### 3. Exception Handling

#### Throwing Exceptions
```java
// In service layer
public User getUserById(String userId, String tenantId) {
    return userRepository.findByIdAndTenantId(userId, tenantId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "User not found with id: " + userId
        ));
}

// Validation exceptions
if (dto.getAge() < 18) {
    throw new ValidationException("Age must be 18 or above");
}

// Business rule violations
if (employee.getLeaveBalance() < leaveRequest.getDays()) {
    throw new BusinessException("Insufficient leave balance");
}

// Service communication failures
if (userServiceResponse.isEmpty()) {
    throw new ServiceCommunicationException(
        "user-service",
        "Failed to fetch user details"
    );
}
```

#### Exception Handling in Controller
```java
// Automatic via GlobalExceptionHandler!
// No try-catch needed in controller - just let exception propagate
@PostMapping
public ResponseEntity<ApiResponse<UserDTO>> createUser(@RequestBody UserDTO dto) {
    User user = userService.createUser(dto);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("User created", user));
    // If exception thrown, GlobalExceptionHandler catches it automatically
}
```

### 4. Database & JPA

#### Entity Best Practices
```java
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_tenant_status", columnList = "tenant_id,status"),
    @Index(name = "idx_username_tenant", columnList = "username,tenant_id", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // IMPORTANT: Tenant isolation in query!
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
```

#### Liquibase Migration
```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.1.xsd">

    <changeSet id="001" author="dev">
        <createTable tableName="users">
            <column name="id" type="VARCHAR(36)">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="tenant_id" type="VARCHAR(36)">
                <constraints nullable="false"/>
            </column>
            <column name="username" type="VARCHAR(100)">
                <constraints nullable="false" unique="true"/>
            </column>
            <column name="email" type="VARCHAR(255)">
                <constraints nullable="false"/>
            </column>
            <column name="created_at" type="TIMESTAMP" defaultValueComputed="CURRENT_TIMESTAMP">
                <constraints nullable="false"/>
            </column>
        </createTable>

        <createIndex tableName="users" indexName="idx_tenant_id">
            <column name="tenant_id"/>
        </createIndex>
    </changeSet>
</databaseChangeLog>
```

### 5. API Design

#### Request/Response Format
```java
// Request DTO
@Data
@NoArgsConstructor
public class UserDTO {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100)
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotNull
    private String tenantId;
}

// Response
@Data
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private String path;

    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.message = message;
        response.data = data;
        response.timestamp = LocalDateTime.now();
        return response;
    }
}

// HTTP Status Mapping
// 200 OK - GET successful
// 201 Created - POST successful
// 204 No Content - DELETE successful
// 400 Bad Request - Validation failed
// 401 Unauthorized - Not authenticated
// 403 Forbidden - No permission
// 404 Not Found - Resource not found
// 422 Unprocessable Entity - Business rule violation
// 503 Service Unavailable - External service down
```

#### API Versioning
```java
// v1 endpoints
@RequestMapping("/api/v1/users")

// v2 endpoints (when needed)
@RequestMapping("/api/v2/users")

// Deprecation warning in response header
.header("X-API-Deprecated", "false")
```

---

## 🔄 INTER-SERVICE COMMUNICATION

### Using Service Clients
```java
@Service
public class EmployeeService {

    @Autowired
    private UserServiceClient userServiceClient;

    public Employee createEmployee(EmployeeDTO dto, String tenantId) {
        // Call another service
        User user = userServiceClient.getUserById(dto.getUserId(), tenantId);

        // Create employee based on user
        Employee employee = new Employee();
        employee.setUserId(user.getId());
        employee.setTenantId(tenantId);

        return employeeRepository.save(employee);
    }
}
```

### Error Handling
```java
// Service client handles errors automatically
// If service is down, you get ServiceCommunicationException

try {
    User user = userServiceClient.getUserById(userId, tenantId);
} catch (ServiceCommunicationException e) {
    logger.error("Failed to call user-service: {}", e.getMessage());
    // Decide: retry, fallback, or propagate
    throw new BusinessException("Unable to process at this time");
}
```

---

## 🧪 TESTING STANDARDS

### Unit Tests
```java
@SpringBootTest
@EnableAutoConfiguration
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void testCreateUserSuccess() {
        // Arrange
        UserDTO dto = new UserDTO();
        dto.setUsername("john");
        dto.setEmail("john@upcraft.com");

        User expected = new User();
        expected.setId("user-123");
        when(userRepository.save(any())).thenReturn(expected);

        // Act
        User result = userService.createUser(dto, "tenant-1");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("user-123");
        verify(userRepository).save(any());
    }

    @Test
    void testCreateUserDuplicateUsername() {
        // Arrange
        UserDTO dto = new UserDTO();
        dto.setUsername("john");
        when(userRepository.existsByUsernameAndTenantId(anyString(), anyString()))
            .thenReturn(true);

        // Act & Assert
        assertThrows(ValidationException.class,
            () -> userService.createUser(dto, "tenant-1"));
    }
}
```

### Integration Tests
```java
@SpringBootTest
class UserServiceIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testCreateUserEndToEnd() {
        // Clean database
        userRepository.deleteAll();

        // Create request
        UserDTO dto = new UserDTO();
        dto.setUsername("john");
        dto.setEmail("john@upcraft.com");

        // Make API call
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
            "/api/v1/users",
            dto,
            ApiResponse.class
        );

        // Verify
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
}
```

---

## 📝 DOCUMENTATION IN CODE

### Method Documentation
```java
/**
 * Creates a new user in the system.
 *
 * @param dto User creation request containing username and email
 * @param tenantId The tenant ID for multi-tenant isolation
 * @return Created User entity with generated ID
 * @throws ValidationException if username already exists or email is invalid
 * @throws ResourceNotFoundException if tenant not found
 *
 * @example
 * UserDTO dto = new UserDTO("john", "john@upcraft.com");
 * User user = userService.createUser(dto, "tenant-123");
 */
public User createUser(UserDTO dto, String tenantId) {
    // implementation
}
```

### Class Documentation
```java
/**
 * Service layer for User management operations.
 *
 * Handles:
 * - User CRUD operations
 * - Tenant isolation (all queries filtered by tenant_id)
 * - User validation
 * - Integration with notification service
 *
 * Note: All operations are transactional and tenant-aware.
 *
 * @see UserRepository
 * @see UserDTO
 * @since 1.0
 */
@Service
public class UserService {
}
```

---

## 🚀 COMMON DEVELOPMENT TASKS

### Creating a New Feature

**Step 1: Create Entity**
```java
@Entity
@Table(name = "attendance")
public class Attendance {
    @Id
    private String id;
    @Column(nullable = false)
    private String tenantId;
    @Column(nullable = false)
    private String employeeId;
    private LocalDateTime punchIn;
    private LocalDateTime punchOut;
}
```

**Step 2: Create Repository**
```java
@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, String> {
    List<Attendance> findByEmployeeIdAndTenantId(String employeeId, String tenantId);
}
```

**Step 3: Create Service**
```java
@Service
public class AttendanceService {
    @Autowired
    private AttendanceRepository attendanceRepository;

    public Attendance punchIn(String employeeId, String tenantId) {
        // implementation
    }
}
```

**Step 4: Create Controller**
```java
@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {
    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/punch-in")
    public ResponseEntity<?> punchIn(@RequestHeader String tenantId) {
        // implementation
    }
}
```

**Step 5: Create Liquibase Migration**
```xml
<createTable tableName="attendance">...</createTable>
```

**Step 6: Write Tests**
```java
class AttendanceServiceTest {
    @Test
    void testPunchIn() { }
}
```

---

## ⚠️ COMMON MISTAKES TO AVOID

### ❌ DON'T: Forget Tenant Isolation
```java
// WRONG - Returns data from all tenants!
List<User> users = userRepository.findAll();

// RIGHT - Tenant-specific query
List<User> users = userRepository.findByTenantId(tenantId);
```

### ❌ DON'T: Hardcode Values
```java
// WRONG
if (status.equals("ACTIVE")) { }

// RIGHT - Use constants
if (status.equals(UserStatus.ACTIVE)) { }
```

### ❌ DON'T: Catch Generic Exception
```java
// WRONG
try {
    userService.createUser(dto);
} catch (Exception e) {
    logger.error("Error: {}", e);
}

// RIGHT - Catch specific exceptions
try {
    userService.createUser(dto);
} catch (ValidationException e) {
    // Handle validation error
} catch (ServiceCommunicationException e) {
    // Handle service error
}
```

### ❌ DON'T: Ignore Null Safety
```java
// WRONG - Can throw NullPointerException
String email = user.getEmail().toLowerCase();

// RIGHT - Check null
String email = user.getEmail() != null ?
    user.getEmail().toLowerCase() : null;

// Or use Optional
String email = Optional.ofNullable(user.getEmail())
    .map(String::toLowerCase)
    .orElse(null);
```

---

## ✅ BEST PRACTICES SUMMARY

1. **Always include tenant_id** in queries
2. **Use service clients** for inter-service calls
3. **Throw specific exceptions** (not generic Exception)
4. **Write unit tests** as you code (80%+ coverage)
5. **Document public methods** with @param, @return
6. **Use constants** for magic strings
7. **Validate input** at controller boundary
8. **Log important operations** at info/warn level
9. **Use transactions** for multi-step operations
10. **Handle service failures** gracefully

---

## 🔗 RELATED DOCUMENTATION

- 02-ARCHITECTURE-DESIGN.md - Architecture foundation
- 06-QUICK-REFERENCE.md - Code snippets & examples
- 07-API-DOCUMENTATION.md - API endpoints
- 08-DATABASE-DESIGN.md - Database schema
- 10-TROUBLESHOOTING.md - Common issues

---

**Owner**: Tech Lead
**Last Updated**: Apr 10, 2026
**Status**: ✅ Ready to Use
