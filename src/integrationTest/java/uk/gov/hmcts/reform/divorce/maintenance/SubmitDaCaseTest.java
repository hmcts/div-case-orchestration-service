package uk.gov.hmcts.reform.divorce.maintenance;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.CcdSubmissionSupport;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DA_REQUESTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.MAKE_CASE_ELIGIBLE_FOR_DA_PETITIONER_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATE_CCD_FIELD;

public class SubmitDaCaseTest extends CcdSubmissionSupport {

    private static final String UPDATE_TO_DN_PRONOUNCED_EVENT_ID = "testDNPronounced";

    @Autowired
    private CosApiClient cosApiClient;

    @Test
    public void whenSubmitDa_thenProceedAsExpected() {
        final UserDetails userDetails = createCitizenUser();
        final CaseDetails caseDetails = submitCase("submit-complete-case.json", userDetails);

        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, UPDATE_TO_DN_PRONOUNCED_EVENT_ID, userDetails);
        updateCase(String.valueOf(caseDetails.getId()), null, MAKE_CASE_ELIGIBLE_FOR_DA_PETITIONER_EVENT_ID,
                ImmutablePair.of(DECREE_NISI_GRANTED_DATE_CCD_FIELD, "2000-01-01"));

        Map<String, Object> cosResponse = cosApiClient
                .submitDaCase(userDetails.getAuthToken(), ImmutableMap.of(
                        "applyForDecreeAbsolute", "yes"
                ), String.valueOf(caseDetails.getId()));

        assertEquals(DA_REQUESTED, cosResponse.get(STATE_CCD_FIELD));
    }
}
