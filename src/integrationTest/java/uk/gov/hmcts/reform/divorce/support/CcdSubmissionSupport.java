package uk.gov.hmcts.reform.divorce.support;

import io.restassured.response.Response;
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

import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.loadJsonToObject;

public abstract class CcdSubmissionSupport extends IntegrationTest {
    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/issue-petition/";
    protected static final String USER_DEFAULT_EMAIL = "simulate-delivered@notifications.service.gov.uk";
    protected static final String CO_RESPONDENT_DEFAULT_EMAIL = "co-respondent@divorce.co.uk";
    protected static final String RESPONDENT_DEFAULT_EMAIL = "respondent@divorce.co.uk";

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
        return submitCase(fileName, createCitizenUser());
    }

    protected CaseDetails updateCase(String caseId, String fileName, String eventId,
                                     Pair<String, String>... additionalCaseData) {
        final Map caseData =
                fileName == null ? new HashMap() : loadJsonToObject(PAYLOAD_CONTEXT_PATH + fileName, Map.class);

        Arrays.stream(additionalCaseData).forEach(
            caseField -> caseData.put(caseField.getKey(), caseField.getValue())
        );

        return ccdClientSupport.update(caseId, caseData, eventId, createCaseWorkerUser());
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

    private Map<String, Object> populateHeaders(String userToken) {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        if (userToken != null) {
            headers.put(HttpHeaders.AUTHORIZATION, userToken);
        }
        return headers;
    }

}
