package uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.transformation.out;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CaseCreationDetails {

    @JsonProperty("case_type_id")
    public final String caseTypeId;

    @JsonProperty("event_id")
    public final String eventId;

    @JsonProperty("case_data")
    public final ExampleD8Case caseData;

    public CaseCreationDetails(
        String caseTypeId,
        String eventId,
        ExampleD8Case caseData
    ) {
        this.caseTypeId = caseTypeId;
        this.eventId = eventId;
        this.caseData = caseData;
    }
}
