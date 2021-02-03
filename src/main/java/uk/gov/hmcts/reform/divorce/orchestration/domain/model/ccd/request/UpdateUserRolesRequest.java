package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@EqualsAndHashCode
public class UpdateUserRolesRequest {
    @JsonProperty("case_id")
    private String caseId;

    @Builder.Default
    @JsonProperty("case_roles")
    private List<String> caseRoles = new ArrayList<>();
}