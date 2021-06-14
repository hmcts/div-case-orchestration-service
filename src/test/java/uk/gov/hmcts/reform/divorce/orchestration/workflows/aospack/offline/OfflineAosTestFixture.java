package uk.gov.hmcts.reform.divorce.orchestration.workflows.aospack.offline;

import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CO_RESPONDENT_NAMED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_RESPONDENT_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_TO_FACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

public class OfflineAosTestFixture {
    public static final String AUTH_TOKEN = "abcde12345";

    public static final String CASE_ID = "1234-5678-9012-3456";

    public static final String PET_SOL_EMAIL = "divorce.petsol@mailinator.com";

    public static final String PET_EMAIL = "divorce.petcitizen@mailinator.com";

    public static final String PET_FIRST_NAME = "Fred";

    public static final String PET_LAST_NAME = "Bloggs";

    public static final String RESP_GENDER = "female";

    public Map<String, Object> getCaseDataForPetitionerSolicitorEmail() {
        return ImmutableMap.<String, Object>builder()
            .put(PETITIONER_SOLICITOR_EMAIL, PET_SOL_EMAIL)
            .put(D_8_CASE_REFERENCE, CASE_ID)
            .build();
    }

    public Map<String, Object> getCaseDataForPetitionerEmail(boolean respondentRepresented, boolean defended,
                                                             DivorceFact reasonForDivorce, String respAdmit,
         String coRespNamed, String receivedAosFromCoResp) {

        return ImmutableMap.<String, Object>builder()
            .put(D_8_PETITIONER_EMAIL, PET_EMAIL)
            .put(D_8_PETITIONER_FIRST_NAME, PET_FIRST_NAME)
            .put(D_8_PETITIONER_LAST_NAME, PET_LAST_NAME)
            .put(D_8_INFERRED_RESPONDENT_GENDER, RESP_GENDER)
            .put(D_8_CASE_REFERENCE, CASE_ID)
            .put(RESP_SOL_REPRESENTED, respondentRepresented ? YES_VALUE : NO_VALUE)
            .put(RESP_WILL_DEFEND_DIVORCE, defended ? YES_VALUE : NO_VALUE)
            .put(D_8_REASON_FOR_DIVORCE, reasonForDivorce != null ? reasonForDivorce.getValue() : EMPTY)
            .put(RESP_ADMIT_OR_CONSENT_TO_FACT, respAdmit)
            .put(D_8_CO_RESPONDENT_NAMED, coRespNamed)
            .put(RECEIVED_AOS_FROM_CO_RESP, receivedAosFromCoResp)
            .build();
    }
}
