package pt.bayonne.sensei.customer.contracts;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.web.context.WebApplicationContext;
import pt.bayonne.sensei.customer.config.IntegrationTestBaseConfig;


@AutoConfigureMessageVerifier
public class ShouldCreateCustomerBase extends IntegrationTestBaseConfig {


    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setUp() {
        RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
    }
}
