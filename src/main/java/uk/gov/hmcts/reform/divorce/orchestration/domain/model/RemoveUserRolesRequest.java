package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class RemoveUserRolesRequest {
    @JsonProperty("case_users")
    private List<CaseUser> caseUsers;
}