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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_DATE_CCD_FIELD;

public class SubmitDaCaseTest extends CcdSubmissionSupport {

    private static final String STATE_JSON_KEY = "state";
    private static final String DA_REQUESTED_STATE = "DARequested";
    private static final String UPDATE_TO_DN_PRONOUNCED_EVENT_ID = "testDNPronounced";
    private static final String UPDATE_TO_AWAITING_DA_EVENT_ID = "MakeEligibleForDA_Petitioner";

    @Autowired
    private CosApiClient cosApiClient;

    @Test
    public void whenSubmitDa_thenProceedAsExpected() {
        final UserDetails userDetails = createCitizenUser();

        final CaseDetails caseDetails = submitCase("submit-complete-case.json", userDetails);

        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, UPDATE_TO_DN_PRONOUNCED_EVENT_ID, userDetails);
        updateCase(String.valueOf(caseDetails.getId()), null, UPDATE_TO_AWAITING_DA_EVENT_ID,
                ImmutablePair.of(DECREE_NISI_GRANTED_DATE_CCD_FIELD, "2000-01-01"));

        Map<String, Object> cosResponse = cosApiClient
                .submitDaCase(userDetails.getAuthToken(), ImmutableMap.of(
                        "applyForDecreeAbsolute", "yes"
                ), String.valueOf(caseDetails.getId()));

        assertEquals(DA_REQUESTED_STATE, cosResponse.get(STATE_JSON_KEY));
    }
}
