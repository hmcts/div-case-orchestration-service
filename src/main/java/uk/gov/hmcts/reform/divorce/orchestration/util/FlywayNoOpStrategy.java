package uk.gov.hmcts.reform.divorce.orchestration.util;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.Configuration;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;

import java.util.Arrays;
import java.util.stream.Stream;

@Slf4j
public class FlywayNoOpStrategy implements FlywayMigrationStrategy {

    @Override
    public void migrate(Flyway flyway) {
        log.warn("these are my schemas...");
        Configuration configuration = flyway.getConfiguration();
        if (configuration != null) {
            String[] schemas = configuration.getSchemas();
            if (schemas != null) {
                Arrays.stream(schemas).forEach(log::warn);
            }
        }
        log.warn("done with schemas...");

        Stream.of(flyway.info().all())
            .filter(info -> !info.getState().isApplied())
            .findFirst()
            .ifPresent(info -> {
                throw new IllegalStateException("Newer version of script not applied " + info.getScript());
            });
    }
}
