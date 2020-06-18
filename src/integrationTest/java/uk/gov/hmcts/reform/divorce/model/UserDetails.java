package uk.gov.hmcts.reform.divorce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDetails {

    private String username;
    private String emailAddress;
    private String password;
    private String authToken;
    private String id;

}
