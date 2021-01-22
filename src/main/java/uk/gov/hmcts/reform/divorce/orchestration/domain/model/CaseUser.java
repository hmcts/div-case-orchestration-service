package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@EqualsAndHashCode
public class CaseUser {
    @JsonProperty("case_id")
    private String caseId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("case_role")
    private String caseRole;
}
