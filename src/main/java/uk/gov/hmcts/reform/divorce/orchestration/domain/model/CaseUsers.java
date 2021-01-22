package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CaseUsers {
    private String case_id;
    private String user_id;
    private String case_role;
}
