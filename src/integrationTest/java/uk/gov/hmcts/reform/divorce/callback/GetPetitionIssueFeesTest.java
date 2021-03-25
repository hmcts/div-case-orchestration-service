package uk.gov.hmcts.reform.divorce.callback;

import com.fasterxml.jackson.core.type.TypeReference;
import io.restassured.response.Response;
import org.apache.http.entity.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.model.ccd.CaseLink;
import uk.gov.hmcts.reform.divorce.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;
import uk.gov.hmcts.reform.divorce.support.CcdClientSupport;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.SOLICITOR_SUBMIT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PREVIOUS_CASE_ID_CCD_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

public class GetPetitionIssueFeesTest extends IntegrationTest {

    @Value("${case.orchestration.solicitor.petition-issue-fees.context-path}")
    private String petitionIssueFeesContextPath;

    @Autowired
    protected CcdClientSupport ccdClientSupport;

    protected static final String BASE_CASE_RESPONSE = "/fixtures/solicitor/case-data.json";

    protected UserDetails solicitorUser;

    protected uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails;
    protected Map<String, Object> baseCaseData;

    @Before
    public void setUp() throws IOException {
        solicitorUser = createSolicitorUser();

        baseCaseData = getJsonFromResourceFile(BASE_CASE_RESPONSE, new TypeReference<HashMap<String, Object>>() {
        });
        caseDetails = ccdClientSupport.submitSolicitorCase(baseCaseData, solicitorUser);
    }

    @Test
    public void givenCallbackRequest_whenGetPetitionIssueFees_thenReturnUpdatedData() {
        Response response = prepareAndCallCosEndpoint(caseDetails, serverUrl + petitionIssueFeesContextPath);

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
    }

    @Test
    public void givenAmendCaseCallbackRequest_whenGetPetitionIssueFees_thenReturnUpdatedData() {
        Map<String, Object> newCaseData = new HashMap<>(baseCaseData);
        newCaseData.put(PREVIOUS_CASE_ID_CCD_KEY, CaseLink.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .build());

        uk.gov.hmcts.reform.ccd.client.model.CaseDetails newCaseDetails = ccdClientSupport
            .submitSolicitorCase(newCaseData, solicitorUser);

        Response response = prepareAndCallCosEndpoint(newCaseDetails, serverUrl + petitionIssueFeesContextPath);

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
    }

    private Map<String, Object> getRequestHeaders() {
        Map<String, Object> requestHeaders = new HashMap<>();
        requestHeaders.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        requestHeaders.put(HttpHeaders.AUTHORIZATION, solicitorUser.getAuthToken());
        return requestHeaders;
    }

    protected Response prepareAndCallCosEndpoint(
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails,
        String endpoint) {
        final CcdCallbackRequest callbackData = CcdCallbackRequest.builder()
            .eventId(SOLICITOR_SUBMIT)
            .caseDetails(CaseDetails.builder()
                .caseId(String.valueOf(caseDetails.getId()))
                .state(caseDetails.getState())
                .caseData(caseDetails.getData())
                .build())
            .build();

        return RestUtil.postToRestService(
            endpoint,
            getRequestHeaders(),
            ObjectMapperTestUtil.convertObjectToJsonString(callbackData)
        );
    }
}
