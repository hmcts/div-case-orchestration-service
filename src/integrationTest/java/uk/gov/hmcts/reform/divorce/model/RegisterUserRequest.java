package uk.gov.hmcts.reform.divorce.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RegisterUserRequest {
    private String email;
    private String forename;
    @Builder.Default
    private String surname = "User";
    private String password;
    @Builder.Default
    private int levelOfAccess = 1;
    private String[] roles;
    private UserGroup userGroup;
}

