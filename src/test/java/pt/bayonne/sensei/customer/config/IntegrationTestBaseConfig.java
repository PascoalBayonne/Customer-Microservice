package pt.bayonne.sensei.customer.config;

import lombok.SneakyThrows;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;
import pt.bayonne.sensei.customer.CustomerApplication;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = {CustomerApplication.class}, properties = {
        "spring.kafka.consumer.auto-offset-reset=earliest"
}
)
@Testcontainers
public class IntegrationTestBaseConfig {

    @Container
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.24"))
            .withDatabaseName("Customer");

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.2.1"));


    @SneakyThrows
    @DynamicPropertySource
    public static void setup(DynamicPropertyRegistry dynamicPropertyRegistry) {
        Startables.deepStart(mySQLContainer, kafkaContainer).join();



        dynamicPropertyRegistry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
        dynamicPropertyRegistry.add("spring.datasource.username", mySQLContainer::getUsername);
        dynamicPropertyRegistry.add("spring.datasource.password", mySQLContainer::getPassword);

        dynamicPropertyRegistry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        dynamicPropertyRegistry.add("bootstrap.servers", kafkaContainer::getBootstrapServers);
        dynamicPropertyRegistry.add("spring.cloud.stream.kafka.binder.brokers", kafkaContainer::getBootstrapServers);
    }

}
