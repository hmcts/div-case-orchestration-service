package uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.transformation.in;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExceptionRecord {

    public final String id;

    public ExceptionRecord(
        @JsonProperty("id") String id
    ) {
        this.id = id;
    }
}
