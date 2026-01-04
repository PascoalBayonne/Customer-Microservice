package pt.bayonne.sensei.customer.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractJacksonHttpMessageConverter;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.web.context.WebApplicationContext;
import pt.bayonne.sensei.customer.config.ContainersConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(ContainersConfiguration.class)
class CustomerControllerTest {

    private final MockMvcTester mvcTester;


    CustomerControllerTest(@Autowired WebApplicationContext webAppContext) {
        this.mvcTester = MockMvcTester.from(webAppContext)
                .withHttpMessageConverters(List.of(webAppContext.getBean(AbstractJacksonHttpMessageConverter.class)));
    }


    @Test
    void givenACustomerIdWhenGetCustomerThenReturnCustomer() {
        MvcTestResult mvcTestResult = mvcTester.get()
                .uri("/api/v1/customer/{customerId}", 1)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange();

        assertThat(mvcTestResult)
                .hasStatus(HttpStatus.OK)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .bodyJson()
                .isLenientlyEqualTo("expectations/get-customer-success.json");
    }
}