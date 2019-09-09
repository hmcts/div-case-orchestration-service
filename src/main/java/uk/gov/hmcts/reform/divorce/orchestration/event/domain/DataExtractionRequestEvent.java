package uk.gov.hmcts.reform.divorce.orchestration.event.domain;

import org.springframework.context.ApplicationEvent;

import java.time.LocalDate;

public class DataExtractionRequestEvent extends ApplicationEvent {

    private Status status;
    private LocalDate date;

    public DataExtractionRequestEvent(Object source, Status status, LocalDate date) {
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
        AOS,
        DN,
        DA
    }

}
