package pt.bayonne.sensei.customer.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class ContainersConfiguration {

    private static final Network network = Network.newNetwork();


    private static MySQLContainer<?> mySQLContainer;


    private static ConfluentKafkaContainer kafkaContainer;


    @Bean
    @ServiceConnection
    ConfluentKafkaContainer kafkaContainer() {
        kafkaContainer = new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
                .withNetwork(network)
                .withNetworkAliases("kafka")
                .withListener("kafka:19092");

        return kafkaContainer;
    }

    @Bean
    @ServiceConnection
    MySQLContainer<?> mySQLContainer() {
        mySQLContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.24"))
                .withDatabaseName("Customer")
                .withNetwork(network);
        return mySQLContainer;
    }

}
