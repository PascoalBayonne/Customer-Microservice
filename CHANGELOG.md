# Changelog

## [Unreleased] - 2025-10-11

### 🚀 Major Updates

#### MockMvcTester Integration and Modern Testing Framework
- **Implemented MockMvcTester** - Modern replacement for MockMvc with fluent API
- **Enhanced Test Infrastructure** - Improved test configuration and data management
- **API Exception Handling** - Added structured exception handling with proper HTTP status codes

### ✨ New Features

#### Modern MockMvcTester Testing Framework
- **Added `CustomerControllerTest.java`** - Comprehensive controller testing using MockMvcTester
- **Implemented AssertJ-style assertions** - Fluent and readable test assertions
- **JSON-based test expectations** - External JSON files for expected responses
- **Automated test data setup** - SQL scripts for consistent test data

#### Enhanced Exception Handling
- **Added `ResourceNotFoundException.java`** - Custom exception for resource not found scenarios
- **Implemented @ResponseStatus** - Automatic HTTP status code mapping
- **Structured error handling** - Consistent API error responses

#### Improved Test Configuration
- **Enhanced ContainersConfiguration** - Better testcontainer management
- **Added test-specific properties** - Isolated test configuration
- **Test data management** - Automated schema and data setup

### 🔧 Technical Improvements

#### Modern Testing Approach
- **MockMvcTester Integration**: Replaced traditional MockMvc with modern fluent API
- **JSON Expectation Files**: External JSON files for maintainable test expectations
- **Test Data Isolation**: SQL scripts for consistent test data setup
- **Spring Boot Test Integration**: Full integration testing with testcontainers

#### API Enhancement
- **Customer Lookup API**: Enhanced customer retrieval functionality
- **Error Response Standardization**: Consistent HTTP status codes and error handling
- **JSON Response Validation**: Structured response format validation

#### Development Guidelines
- **Added Junie Guidelines**: Comprehensive development standards for Java/Spring Boot
- **TDD Practices**: Test-first development methodology
- **Hexagonal Architecture**: Clean architecture principles and DDD practices
- **Testing Strategy**: Multi-layer testing approach (Unit, Integration, Contract)

### 📁 New Files

#### Configuration and Guidelines
- `.junie/guidelines.md` - **NEW** - Project development guidelines and standards
- `src/main/resources/application-test.properties` - **NEW** - Test-specific application configuration
- `src/test/resources/application-test.yaml` - **NEW** - YAML-based test configuration
- `src/test/resources/testcontainers.properties` - **NEW** - Testcontainer-specific settings

#### Exception Handling
- `src/main/java/pt/bayonne/sensei/customer/controller/dto/ResourceNotFoundException.java` - **NEW** - Custom not found exception

#### Testing Infrastructure
- `src/test/java/pt/bayonne/sensei/customer/controller/CustomerControllerTest.java` - **NEW** - MockMvcTester-based controller tests
- `src/test/resources/db/data.sql` - **NEW** - Test database schema and data setup
- `src/test/resources/expectations/get-customer-success.json` - **NEW** - Expected JSON response for customer retrieval

### 🔄 Enhanced Files

#### Controller Layer
- `CustomerController.java` - Enhanced with better exception handling and response mapping
- `CustomerMapper.java` - Improved mapping functionality

#### Service Layer
- `CustomerService.java` - Added new service methods
- `CustomerServiceImpl.java` - Enhanced service implementation with better error handling

#### Domain Layer
- `BirthDate.java` - Refined value object implementation

#### Configuration
- `ContainersConfiguration.java` - Enhanced testcontainer setup
- `ShouldPublishCustomerCreatedBase.java` - Updated to use new testing patterns

### 🧠 Technical Concepts Explained

#### MockMvcTester Benefits
The new MockMvcTester provides significant advantages over traditional MockMvc:

**Before (MockMvc)**:
```java
mockMvc.perform(get("/api/v1/customer/1"))
    .andExpect(status().isOk())
    .andExpected(content().contentType(MediaType.APPLICATION_JSON))
    .andExpected(jsonPath("$.firstName").value("John"));
```

**After (MockMvcTester)**:
```java
mvcTester.get().uri("/api/v1/customer/{customerId}", 1)
    .accept(MediaType.APPLICATION_JSON)
    .exchange()
    .hasStatus(HttpStatus.OK)
    .hasContentTypeCompatibleWith(MediaType.APPLICATION_JSON)
    .bodyJson().isLenientlyEqualTo("expectations/get-customer-success.json");
```

**Benefits:**
1. **Fluent API**: More readable and chainable assertions
2. **External JSON Expectations**: Maintainable test data in separate files
3. **AssertJ Integration**: Consistent assertion style across the project
4. **Better IDE Support**: Enhanced autocomplete and type safety
5. **Simplified Setup**: Less boilerplate for test configuration

#### Test Data Management Strategy
The new approach introduces structured test data management:

```sql
-- data.sql
create table if not exists customer (
    birth_date    date         null,
    ssn           int          null,
    id            bigint       not null primary key,
    email_address varchar(255) null,
    first_name    varchar(255) null,
    last_name     varchar(255) null
);
```

**Advantages:**
- **Consistent Test Data**: Same data setup across all tests
- **Version Control**: Test schemas tracked in git
- **Isolated Testing**: Each test run starts with clean data
- **Realistic Scenarios**: Production-like data structures

#### Exception Handling Modernization
Implemented structured exception handling following Spring Boot best practices:

```java
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    // Automatic HTTP status mapping
}
```

**Benefits:**
- **Automatic Status Codes**: No manual response entity creation needed
- **Consistent Error Format**: Standardized error responses
- **Better API Documentation**: Clear HTTP status code semantics
- **Client-Friendly**: Predictable error handling for API consumers

### 🔄 Migration Impact

#### For Developers
1. **Modern Testing Experience**: More intuitive and readable test assertions
2. **Faster Test Development**: External JSON expectations reduce boilerplate
3. **Better Debugging**: Clear test failure messages with MockMvcTester
4. **Consistent Guidelines**: Comprehensive development standards in `.junie/guidelines.md`

#### For API Consumers
1. **Predictable Error Responses**: Standardized HTTP status codes
2. **Better API Documentation**: Clear error scenarios
3. **Consistent Response Format**: Uniform JSON response structure

### 🎯 Future Considerations

1. **API Documentation**: Consider adding OpenAPI/Swagger documentation
2. **Error Response Bodies**: Implement structured error response DTOs
3. **Test Coverage**: Expand MockMvcTester tests to cover all endpoints
4. **Performance Testing**: Add load testing with the new test infrastructure
5. **Contract Testing**: Implement Spring Cloud Contract for API contracts

---

**Note**: This release introduces modern Spring Boot testing practices with MockMvcTester and establishes comprehensive development guidelines for the project.

## [Previous Release] - 2025-01-07

### 🚀 Major Updates

#### Spring Boot and Testcontainers Modernization
- **Upgraded Spring Boot** from `3.4.1` to `3.5.5`
- **Upgraded Spring Cloud** from `2024.0.0` to `2025.0.0`
- **Upgraded Testcontainers** from `1.18.3` to `1.21.3`
- **Added Spring Boot Testcontainers Integration** - Native support for testcontainers in Spring Boot

### ✨ New Features

#### Modern Testcontainers Configuration with @ServiceConnection
- **Added `ContainersConfiguration.java`** - A new modern approach to testcontainer management
- **Implemented @ServiceConnection annotation** - Eliminates the need for manual property configuration
- **Created shared network infrastructure** - Containers can now communicate with each other
- **Added `TestCustomerApplication.java`** - Dedicated test application entry point

#### Enhanced Container Setup
- **MySQL Container**: Now uses network-aware configuration
- **Kafka Container**: Upgraded to Confluent Kafka 7.4.0 with network aliases
- **Shared Network**: All containers now share a common Docker network for inter-service communication

### 🔧 Technical Improvements

#### Dependency Management
- **Centralized Testcontainers Version**: All testcontainer dependencies now use `${test-containers.version}` property
- **Added spring-boot-testcontainers**: Native Spring Boot integration for testcontainers
- **Added testcontainers junit-jupiter**: Enhanced JUnit 5 integration

#### Code Quality and Structure
- **Removed Legacy Configuration**: Eliminated `IntegrationTestBaseConfig.java` 
- **Modernized Test Base Class**: Updated `ShouldPublishCustomerCreatedBase.java` to use new configuration pattern
- **Simplified Property Management**: No more manual `@DynamicPropertySource` configurations

### 🗑️ Removed

#### Deprecated Test Infrastructure
- **Removed `IntegrationTestBaseConfig.java`**: Legacy testcontainer configuration approach
- **Eliminated Manual Property Configuration**: No more `@DynamicPropertySource` boilerplate
- **Removed JUnit Extension Dependencies**: Simplified test execution model

### 📋 Changed Files

#### Configuration Files
- `pom.xml` - Dependency upgrades and new testcontainer dependencies
- `src/test/java/pt/bayonne/sensei/customer/config/ContainersConfiguration.java` - **NEW FILE**
- `src/test/java/pt/bayonne/sensei/customer/config/TestCustomerApplication.java` - **NEW FILE**
- `src/test/java/pt/bayonne/sensei/customer/config/IntegrationTestBaseConfig.java` - **DELETED**

#### Test Files
- `src/test/java/pt/bayonne/sensei/customer/contractsss/ShouldPublishCustomerCreatedBase.java` - Updated to use modern configuration

### 🧠 Technical Concepts Explained

#### @ServiceConnection Annotation
The `@ServiceConnection` annotation is a powerful new feature in Spring Boot 3.x that provides automatic configuration for testcontainers. Here's why it's revolutionary:

**Before (@ServiceConnection)**:
```java
@DynamicPropertySource
public static void setup(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
    registry.add("spring.datasource.username", mySQLContainer::getUsername);
    registry.add("spring.datasource.password", mySQLContainer::getPassword);
    // Manual configuration for each property...
}
```

**After (@ServiceConnection)**:
```java
@Bean
@ServiceConnection
MySQLContainer<?> mySQLContainer() {
    return new MySQLContainer<>(DockerImageName.parse("mysql:8.0.24"))
            .withDatabaseName("Customer")
            .withNetwork(network);
}
```

**Benefits:**
1. **Automatic Property Mapping**: Spring Boot automatically configures connection properties
2. **Type Safety**: Compile-time validation of container types
3. **Reduced Boilerplate**: Eliminates manual property source configuration
4. **Better Maintainability**: Changes to container setup don't require property updates
5. **Standardization**: Consistent approach across different container types

#### Network-Aware Container Architecture
The new configuration introduces a shared Docker network:

```java
private static final Network network = Network.newNetwork();
```

**Advantages:**
- **Inter-Service Communication**: Containers can communicate using service names
- **Realistic Test Environment**: Mimics production networking patterns
- **Service Discovery**: Kafka can be accessed via `kafka:19092` alias
- **Isolation**: Test networks are isolated from other containers

#### TestCustomerApplication Pattern
The new `TestCustomerApplication.java` follows Spring Boot's recommended testing patterns:

```java
SpringApplication.from(CustomerApplication::main)
    .with(ContainersConfiguration.class)
    .run(args);
```

**Benefits:**
- **Development Testing**: Can run the application with test containers locally
- **Integration Testing**: Provides a consistent test environment
- **Configuration Composition**: Combines main application with test-specific configurations
- **Debugging**: Easier to debug integration issues during development

### 🔄 Migration Impact

#### For Developers
1. **Simplified Test Writing**: New tests require less boilerplate
2. **Faster Feedback**: Improved container startup and configuration
3. **Better IDE Support**: Enhanced autocomplete and type checking
4. **Local Development**: Easy to run application with test containers

#### For CI/CD
1. **Improved Reliability**: More stable container lifecycle management
2. **Better Performance**: Optimized container networking and startup
3. **Consistent Environments**: Same container setup across all test scenarios

### 🚨 Breaking Changes

#### Test Configuration
- Tests extending `IntegrationTestBaseConfig` need to be updated to use `@Import(ContainersConfiguration.class)`
- Manual property configuration is no longer needed
- Container lifecycle is now managed automatically by Spring Boot

### 📊 Version Compatibility

| Component | Old Version | New Version | Notes |
|-----------|-------------|-------------|-------|
| Spring Boot | 3.4.1 | 3.5.5 | Major upgrade with new features |
| Spring Cloud | 2024.0.0 | 2025.0.0 | Latest release train |
| Testcontainers | 1.18.3/1.21.0 | 1.21.3 | Unified version across all modules |
| Kafka Image | cp-kafka:6.2.1 | cp-kafka:7.4.0 | Major Kafka version upgrade |

### 🎯 Future Considerations

1. **Container Optimization**: Consider using specific container images for faster startup
2. **Test Slicing**: Implement `@TestcontainersTest` for focused container testing
3. **Resource Management**: Add container resource limits for CI environments
4. **Monitoring Integration**: Consider adding test container observability

---

**Note**: This upgrade modernizes the testing infrastructure to use Spring Boot 3.x best practices and provides a foundation for more maintainable and efficient integration testing.
