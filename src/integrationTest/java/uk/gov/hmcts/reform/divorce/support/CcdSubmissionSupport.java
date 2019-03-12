package uk.gov.hmcts.reform.divorce.support;

import io.restassured.response.Response;
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

import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.loadJsonToObject;

public abstract class CcdSubmissionSupport extends IntegrationTest {
    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/issue-petition/";

    @Autowired
    protected CcdClientSupport ccdClientSupport;

    @Value("${case.orchestration.maintenance.submit-respondent-aos.context-path}")
    private String submitRespondentAosContextPath;

    @Value("${case.orchestration.maintenance.submit-co-respondent-aos.context-path}")
    private String submitCoRespondentAosContextPath;

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
        return submitCase(fileName, createCaseWorkerUser());
    }

    protected CaseDetails updateCase(String caseId, String fileName, String eventId) {
        return ccdClientSupport.update(caseId,
            fileName == null ? null : loadJsonToObject(PAYLOAD_CONTEXT_PATH + fileName, Map.class),
            eventId, createCaseWorkerUser());
    }

    protected CaseDetails updateCaseForCaseWorkerOnly(String caseId, String fileName, String eventId) {
        return ccdClientSupport.update(caseId,
            fileName == null ? null : loadJsonToObject(PAYLOAD_CONTEXT_PATH + fileName, Map.class),
            eventId, createOnlyCaseWorkerUser());
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

    public Response submitCoRespondentAosCase(final String userToken, final String requestBody) {
        final Map<String, Object> headers = populateHeaders(userToken);

        return RestUtil.postToRestService(
            serverUrl + submitCoRespondentAosContextPath,
            headers,
            requestBody
        );
    }

    public CaseDetails retrieveCase(final UserDetails user, String caseId) {
        return ccdClientSupport.retrieveCase(user, caseId);
    }

    private Map<String, Object> populateHeaders(String userToken) {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        if (userToken != null) {
            headers.put(HttpHeaders.AUTHORIZATION, userToken);
        }
        return headers;
    }

}
