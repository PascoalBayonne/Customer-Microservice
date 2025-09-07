package pt.bayonne.sensei.customer.config;

import org.springframework.boot.SpringApplication;
import pt.bayonne.sensei.customer.CustomerApplication;

public class TestCustomerApplication {

    public static void main(String[] args) {
        SpringApplication.from(CustomerApplication::main)
                .with(ContainersConfiguration.class)
                .run(args);
    }
}
