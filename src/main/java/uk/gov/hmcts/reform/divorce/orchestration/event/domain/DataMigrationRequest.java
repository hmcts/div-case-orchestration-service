package uk.gov.hmcts.reform.divorce.orchestration.event.domain;

import org.springframework.context.ApplicationEvent;

import java.time.LocalDate;

/**
 * This event symbolises a request for a data migration process to start.
 */
public class DataMigrationRequest extends ApplicationEvent {

    private final Status status;
    private final LocalDate date;

    public DataMigrationRequest(Object source, Status status, LocalDate date) {
        super(source);
        this.status = status;
        this.date = date;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDate getDate() {
        return date;
    }

    public enum Status {
        DA
    }

}