package uk.gov.hmcts.reform.divorce.support;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_DN_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.loadJson;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.loadJsonToObject;

@Slf4j
public abstract class CcdSubmissionSupport extends IntegrationTest {
    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/issue-petition/";
    private static final String BULK_PAYLOAD_CONTEXT_PATH = "fixtures/bulk-list/";
    protected static final String USER_DEFAULT_EMAIL = "simulate-delivered@notifications.service.gov.uk";
    protected static final String CO_RESPONDENT_DEFAULT_EMAIL = "co-respondent@notifications.service.gov.uk";
    protected static final String RESPONDENT_DEFAULT_EMAIL = "respondent@notifications.service.gov.uk";
    private static final String SUBMIT_DN_PAYLOAD_CONTEXT_PATH = "fixtures/maintenance/submit-dn/";
    private static final String TEST_AOS_STARTED_EVENT_ID = "testAosStarted";

    @Autowired
    protected CcdClientSupport ccdClientSupport;

    @Value("${case.orchestration.maintenance.submit-respondent-aos.context-path}")
    private String submitRespondentAosContextPath;

    @Value("${case.orchestration.maintenance.submit-co-respondent-aos.context-path}")
    private String submitCoRespondentAosContextPath;

    @Value("${case.orchestration.maintenance.submit.context-path}")
    private String caseCreationContextPath;

    @Value("${case.orchestration.maintenance.submit-dn.context-path}")
    private String submitDnContextPath;

    @SuppressWarnings("unchecked")
    @SafeVarargs
    protected final CaseDetails submitCase(String fileName, UserDetails userDetails,
                                           Pair<String, String>... additionalCaseData) {

        final Map caseData = loadJsonToObject(PAYLOAD_CONTEXT_PATH + fileName, Map.class);

        Arrays.stream(additionalCaseData).forEach(
            caseField -> caseData.put(caseField.getKey(), caseField.getValue())
        );

        return ccdClientSupport.submitCase(caseData, userDetails);
    }

    protected CaseDetails submitCase(String fileName) {
        return submitCase(fileName, createCitizenUser());
    }

    protected CaseDetails submitBulkCase(String fileName, Pair<String, Object>... additionalCaseData) {
        final Map caseData = loadJsonToObject(BULK_PAYLOAD_CONTEXT_PATH + fileName, Map.class);

        Arrays.stream(additionalCaseData).forEach(
            caseField -> caseData.put(caseField.getKey(), caseField.getValue())
        );

        return ccdClientSupport.submitBulkCase(caseData, createCaseWorkerUser());
    }

    protected CaseDetails updateCase(String caseId, String fileName, String eventId,
                                     Pair<String, String>... additionalCaseData) {
        return updateCase(caseId, fileName, eventId, false, additionalCaseData);
    }

    protected CaseDetails updateCase(String caseId, String fileName, String eventId, boolean isBulkType,
                                     Pair<String, String>... additionalCaseData) {
        String payloadPath = isBulkType ? BULK_PAYLOAD_CONTEXT_PATH : PAYLOAD_CONTEXT_PATH;
        final Map caseData =
                fileName == null ? new HashMap() : loadJsonToObject(payloadPath + fileName, Map.class);

        Arrays.stream(additionalCaseData).forEach(
            caseField -> caseData.put(caseField.getKey(), caseField.getValue())
        );

        return ccdClientSupport.update(caseId, caseData, eventId, createCaseWorkerUser(), isBulkType);
    }

    protected CaseDetails updateCaseForCitizen(String caseId, String fileName, String eventId,
                                               UserDetails userDetails) {
        return ccdClientSupport.updateForCitizen(caseId,
                fileName == null ? null : loadJsonToObject(PAYLOAD_CONTEXT_PATH + fileName, Map.class),
                eventId, userDetails);
    }

    public Response submitRespondentAosCase(String userToken, Long caseId, String requestBody) {
        final Map<String, Object> headers = populateHeaders(userToken);

        return RestUtil.postToRestService(
                serverUrl + submitRespondentAosContextPath + "/" + caseId,
                headers,
                requestBody
        );
    }

    public Response submitCoRespondentAosCase(final UserDetails userDetails, final String requestBody) {
        final Map<String, Object> headers = populateHeaders(userDetails.getAuthToken());
        String bodyWithUser = requestBody;
        if (StringUtils.isNotEmpty(bodyWithUser)) {
            bodyWithUser = bodyWithUser.replaceAll(CO_RESPONDENT_DEFAULT_EMAIL, userDetails.getEmailAddress());
        }
        return RestUtil.postToRestService(
                serverUrl + submitCoRespondentAosContextPath,
                headers,
                bodyWithUser
        );
    }

    public CaseDetails retrieveCase(final UserDetails user, String caseId) {
        return ccdClientSupport.retrieveCase(user, caseId);
    }

    public CaseDetails retrieveCaseForCaseworker(final UserDetails user, String caseId) {
        return ccdClientSupport.retrieveCaseForCaseworker(user, caseId);
    }

    private Map<String, Object> populateHeaders(String userToken) {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        if (userToken != null) {
            headers.put(HttpHeaders.AUTHORIZATION, userToken);
        }
        return headers;
    }

    protected CaseDetails createAwaitingPronouncementCase(UserDetails userDetails) throws Exception {

        final CaseDetails caseDetails = submitCase("submit-complete-case.json", userDetails);

        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, TEST_AOS_STARTED_EVENT_ID, userDetails);
        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, AWAITING_DN_AOS_EVENT_ID, userDetails);

        Response cosResponse = submitDnCase(userDetails.getAuthToken(), caseDetails.getId(),
                "dn-submit.json");
        assertEquals(OK.value(), cosResponse.getStatusCode());

        assertEquals(caseDetails.getId(), cosResponse.path("id"));
        updateCase(String.valueOf(caseDetails.getId()), null, "refertoLegalAdvisor");
        return updateCase(String.valueOf(caseDetails.getId()), null, "entitlementGranted");
    }


    protected Response submitDnCase(String userToken, Long caseId, String filePath) throws Exception {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        if (userToken != null) {
            headers.put(HttpHeaders.AUTHORIZATION, userToken);
        }

        return RestUtil.postToRestService(
                serverUrl + submitDnContextPath + "/" + caseId,
                headers,
                filePath == null ? null : loadJson(SUBMIT_DN_PAYLOAD_CONTEXT_PATH + filePath)
        );
    }
}
