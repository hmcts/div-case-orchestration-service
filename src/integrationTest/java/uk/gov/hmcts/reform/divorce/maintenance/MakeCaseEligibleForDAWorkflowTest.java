package uk.gov.hmcts.reform.divorce.maintenance;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.cos.RetrieveCaseSupport;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_DA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;

@Slf4j
public class MakeCaseEligibleForDAWorkflowTest extends RetrieveCaseSupport {

    private static final String TEST_DN_PRONOUNCED = "testDNPronounced";
    private static final String STATE_KEY = "state";

    private static final String SUBMIT_COMPLETE_CASE_JSON_FILE_PATH = "submit-complete-case.json";
    private static final String NO_STATE_CHANGE_EVENT_ID = "paymentReferenceGenerated";

    private static final String MAKE_CASES_ELIGIBLE_FOR_DA_ENDPOINT = "/cases/da/make-eligible";
    private static final String DECREE_NISI_GRANTED_DATE_KEY = "DecreeNisiGrantedDate";
    private static final String DECREE_NISI_GRANTED_DATE = "2019-03-31";

    @Test
    public void givenCaseIsInDNPronounced_WhenMakeCaseEligibleForDAIsCalled_CaseStateIsAwaitingDecreeAbsolute() {
        UserDetails citizenUser = createCitizenUser();
        final CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_CASE_JSON_FILE_PATH, citizenUser,
            Pair.of(D_8_PETITIONER_EMAIL, citizenUser.getEmailAddress()));

        String caseId = String.valueOf(caseDetails.getId());
        log.debug("Case " + caseId + " created.");

        updateCase(String.valueOf(caseDetails.getId()),
                null,
                NO_STATE_CHANGE_EVENT_ID,
                ImmutablePair.of(DECREE_NISI_GRANTED_DATE_KEY,
                        MakeCaseEligibleForDAWorkflowTest.DECREE_NISI_GRANTED_DATE));

        log.debug(String.format("%s=%s set in the case [%s]",
                DECREE_NISI_GRANTED_DATE_KEY,  DECREE_NISI_GRANTED_DATE, caseId));

        updateCaseForCitizen(caseId, null, TEST_DN_PRONOUNCED, citizenUser);
        log.debug("Case " + caseId + " moved to DNPronounced.");

        UserDetails caseWorkerUser = createCaseWorkerUser();
        makeCasesEligibleForDa(caseWorkerUser.getAuthToken());

        final Response retrievedCase = retrieveCase(citizenUser.getAuthToken());
        assertThat(retrievedCase.path(STATE_KEY), is(AWAITING_DA));
    }

    private void makeCasesEligibleForDa(String userToken) {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        if (userToken != null) {
            headers.put(HttpHeaders.AUTHORIZATION, userToken);
        }

        Response response = RestUtil.postToRestService(
            serverUrl + MAKE_CASES_ELIGIBLE_FOR_DA_ENDPOINT,
            headers,
            null,
            new HashMap<>()
        );

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
    }
}
