package uk.gov.hmcts.reform.divorce.orchestration.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Builder
@Data
public class Jwt {
    private final String defaultUrl;
    private final String subject;
    private final String data;
    private final String type;
    private final String forename;
    private final String surname;
    private final long id;
    private final LocalDate expiration;
    private final LocalDate issuedAt;
    private final String jwtId;
    private final String defaultService;
    private final long levelOfAssurance;
    private final String group;
}
