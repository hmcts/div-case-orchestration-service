package uk.gov.hmcts.reform.divorce.scheduler.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@ConfigurationProperties
@ConditionalOnProperty(value = "scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class SchedulerDBConfiguration {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.scheduler")
    public DataSourceProperties schedulerDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("schedulerDataSource")
    @Primary
    public DataSource schedulerDataSource() {
        return schedulerDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean("schedulerTransactionAwareDataSourceProxy")
    public TransactionAwareDataSourceProxy transactionAwareDataSourceProxy(
        @Qualifier("schedulerDataSource") DataSource dataSource
    ) {
        TransactionAwareDataSourceProxy dataSourceProxy = new TransactionAwareDataSourceProxy(dataSource);

        migrateFlyway(dataSourceProxy);
        return dataSourceProxy;
    }

    private void migrateFlyway(DataSource dataSource) {
        Flyway.configure()
            .dataSource(dataSource)
            .locations("db/migration")
            .load()
            .migrate();
    }

    @Bean("schedulerTransactionManager")
    public PlatformTransactionManager transactionManager(
        @Qualifier("schedulerTransactionAwareDataSourceProxy")
            TransactionAwareDataSourceProxy schedulerTransactionAwareDataSourceProxy
    ) {
        return new DataSourceTransactionManager(schedulerTransactionAwareDataSourceProxy);
    }
}
