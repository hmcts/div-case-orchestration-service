package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AssignCaseAccessRequest {
    private String case_id;
    private String assignee_id;
    private String case_type_id;
}
