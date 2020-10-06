package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GeneralEmailParties {

    public static final String RESPONDENT_GENERAL_EMAIL_SELECTION = "respondent";
    public static final String PETITIONER_GENERAL_EMAIL_SELECTION = "petitioner";
    public static final String CO_RESPONDENT_GENERAL_EMAIL_SELECTION = "co-respondent";
    public static final String OTHER_GENERAL_EMAIL_SELECTION = "other";
}
