package uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.transformation.in;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ExceptionRecord {

    private final String caseTypeId;
    private final String id;
    private final String poBox;

    public ExceptionRecord(
        @JsonProperty("case_type_id") String caseTypeId,
        @JsonProperty("id") String id,
        @JsonProperty("po_box") String poBox
    ) {
        this.caseTypeId = caseTypeId;
        this.id = id;
        this.poBox = poBox;
    }
}
