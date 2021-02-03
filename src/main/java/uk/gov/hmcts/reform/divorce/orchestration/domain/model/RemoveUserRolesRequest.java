package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@EqualsAndHashCode
public class RemoveUserRolesRequest {
    @JsonProperty("case_users")
    private List<CaseUser> caseUsers;
}