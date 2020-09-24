package uk.gov.hmcts.reform.divorce.scheduler.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(value = "scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class SchedulerDBConfiguration {

    @Bean
    public DataSource schedulerDataSource() {
        DataSourceProperties dataSourceProperties = schedulerDataSourceProperties();
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.scheduler")
    public DataSourceProperties schedulerDataSourceProperties() {
        return new DataSourceProperties();
    }

}