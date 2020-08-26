package uk.gov.hmcts.reform.divorce.orchestration.util;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;

import java.util.stream.Stream;

@Slf4j
public class FlywayNoOpStrategy implements FlywayMigrationStrategy {

    @Override
    public void migrate(Flyway flyway) {
        Stream.of(flyway.info().all())
            .filter(info -> !info.getState().isApplied())
            .findFirst()
            .ifPresent(info -> {
                log.warn("info.getDescription: {}", info.getDescription());
                log.warn("info.getVersion: {}", info.getVersion());
                log.warn("info.getType: {}", info.getType());
                log.warn("info.getState: {}", info.getState());
                log.warn("info.getInstalledOn: {}", info.getInstalledOn());
                log.warn("info.getInstalledRank: {}", info.getInstalledRank());
                throw new IllegalStateException("Newer version of script not applied " + info.getScript());
            });
    }
}

