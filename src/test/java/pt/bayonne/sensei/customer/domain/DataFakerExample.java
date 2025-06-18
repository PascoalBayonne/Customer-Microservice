package pt.bayonne.sensei.customer.domain;

import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import net.datafaker.providers.base.Text;
import net.datafaker.transformations.Field;
import net.datafaker.transformations.Schema;
import net.datafaker.transformations.sql.SqlDialect;
import net.datafaker.transformations.sql.SqlTransformer;
import org.springframework.test.context.jdbc.Sql;

import java.util.Locale;

@Slf4j
public class DataFakerExample {
    // This is a simple example of how to use the Faker library to generate random data
    private static final Faker faker = new Faker(Locale.of("de_DE"));

    public static void main(String[] args) {

        // schema and transformers
        Schema<String, String> schema = Schema.of(
                Field.field("firstName", () -> faker.name().firstName()),
                Field.field("lastName", () -> faker.name().lastName()),
                Field.field("email", () -> faker.internet().emailAddress()),
                Field.field("address", () -> faker.address().state()),
                Field.field("phoneNumber", () -> faker.phoneNumber().phoneNumber())
        );

        SqlTransformer<String> sqlTransformer = new SqlTransformer.SqlTransformerBuilder<String>()
                .batch(5)
                .tableName("customer")
                .dialect(SqlDialect.MYSQL)
                .build();

        String sqlStatement = sqlTransformer.generate(schema, 10);
        log.info("SQL Statement: {}", sqlStatement);
    }
}
