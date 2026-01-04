package pt.bayonne.sensei.customer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.AbstractJacksonHttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@RequiredArgsConstructor
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S") //Maximum time a lock can be held
// @EnableScheduling is required to enable scheduling. We have to enable it on the main class.
public class ShedLockJdbcConfig {

    private final JdbcTemplate jdbcTemplate;

    @Bean
    public LockProvider lockProvider() {
        return new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(jdbcTemplate)
//                        .withTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault())) YOU CANNOT SET BOTH
                        .usingDbTime()
                        .build()
        );
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public AbstractJacksonHttpMessageConverter abstractJacksonHttpMessageConverter() {
        return new JacksonJsonHttpMessageConverter();
    }
}
