package uk.gov.hmcts.reform.divorce.orchestration.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.EqualsAndHashCode;

@Builder
@EqualsAndHashCode
public class UserRole {

    private final String userId;

    private final String role;

    @JsonCreator
    public UserRole(String userId, String role) {
        this.userId = userId;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

}
