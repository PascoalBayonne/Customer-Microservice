# Changelog

## [Unreleased] - 2025-01-07

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
