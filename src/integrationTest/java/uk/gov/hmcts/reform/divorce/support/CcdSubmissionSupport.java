package uk.gov.hmcts.reform.divorce.support;

import feign.FeignException;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_DN_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.support.EvidenceManagementUtil.readDataFromEvidenceManagement;
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

    @Autowired
    @Qualifier("documentGeneratorTokenGenerator")
    private AuthTokenGenerator divDocAuthTokenGenerator;

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

    protected final CaseDetails submitSolicitorCase(String fileName, UserDetails userDetails) {

        final Map caseData = loadJsonToObject(PAYLOAD_CONTEXT_PATH + fileName, Map.class);
        return ccdClientSupport.submitSolicitorCase(caseData, userDetails);
    }

    protected CaseDetails submitBulkCase(String fileName, Pair<String, Object>... additionalCaseData) {
        final Map caseData = loadJsonToObject(BULK_PAYLOAD_CONTEXT_PATH + fileName, Map.class);

        Arrays.stream(additionalCaseData).forEach(
            caseField -> caseData.put(caseField.getKey(), caseField.getValue())
        );

        return ccdClientSupport.submitBulkCase(caseData, createCaseWorkerUser());
    }

    protected CaseDetails updateCaseWithSuperuser(String caseId, String fileName, String eventId,
                                                  Pair<String, Object>... additionalCaseData) {
        return updateCase(caseId, fileName, eventId, createCaseWorkerSuperUser(), false, additionalCaseData);
    }

    protected CaseDetails updateCase(String caseId, String fileName, String eventId,
                                     Pair<String, Object>... additionalCaseData) {
        return updateCase(caseId, fileName, eventId, createCaseWorkerUser(), false, additionalCaseData);
    }

    protected CaseDetails updateCase(String caseId, String fileName, String eventId, boolean isBulkType,
                                     Pair<String, Object>... additionalCaseData) {
        return updateCase(caseId, fileName, eventId, createCaseWorkerUser(), isBulkType, additionalCaseData);
    }

    protected CaseDetails updateCase(String caseId, String eventId, boolean isBulkType,
                                     Pair<String, Object>... additionalCaseData) {
        return updateCase(caseId, null, eventId, createCaseWorkerUser(), isBulkType, additionalCaseData);
    }

    protected CaseDetails updateCase(String caseId, String fileName, String eventId,
                                     UserDetails userDetails, Pair<String, Object>... additionalCaseData) {
        return updateCase(caseId, fileName, eventId, userDetails, false, additionalCaseData);
    }

    protected CaseDetails updateCase(String caseId, String fileName, String eventId, UserDetails userDetails,
                                     boolean isBulkType, Pair<String, Object>... additionalCaseData) {
        String payloadPath = isBulkType ? BULK_PAYLOAD_CONTEXT_PATH : PAYLOAD_CONTEXT_PATH;
        final Map caseData =
            fileName == null ? new HashMap() : loadJsonToObject(payloadPath + fileName, Map.class);

        Arrays.stream(additionalCaseData).forEach(
            caseField -> caseData.put(caseField.getKey(), caseField.getValue())
        );

        try {
            return ccdClientSupport.update(caseId, caseData, eventId, userDetails, isBulkType);
        } catch (FeignException e) {
            String errorMessage = e.content() == null ? e.getMessage() : e.contentUTF8();
            log.error("Failed calling to CCD {}", errorMessage);
            throw  e;
        }
    }

    protected CaseDetails updateCaseForCitizen(String caseId, String fileName, String eventId,
                                               UserDetails userDetails, Pair<String, String>... additionalCaseData) {
        final Map caseData = fileName == null
                ? new HashMap() : loadJsonToObject(PAYLOAD_CONTEXT_PATH + fileName, Map.class);

        Arrays.stream(additionalCaseData).forEach(
            caseField -> caseData.put(caseField.getKey(), caseField.getValue())
        );
        return ccdClientSupport.updateForCitizen(caseId, caseData, eventId, userDetails);
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

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getDocumentsGenerated(CaseDetails caseDetails) {
        List<Map<String, Object>> d8DocumentsGenerated = (List<Map<String, Object>>) caseDetails.getData().get("D8DocumentsGenerated");
        return d8DocumentsGenerated.stream().map(m -> (Map<String, Object>) m.get("value")).collect(toList());
    }

    public void assertGeneratedDocumentsExists(final CaseDetails caseDetails, String documentType, String fileNameFormat) {
        final List<Map<String, Object>> documentsCollection = getDocumentsGenerated(caseDetails);

        Map<String, Object> miniPetition = documentsCollection.stream()
                .filter(m -> m.get("DocumentType").equals(documentType))
                .findAny()
                .orElseThrow(() -> new AssertionError(
                        String.format("Document with type %s not found in %s", documentType, documentsCollection)
                ));

        assertDocumentWasGenerated(miniPetition, documentType, String.format(fileNameFormat, caseDetails.getId()));
    }

    public void assertDocumentWasGenerated(final Map<String, Object> documentData, final String expectedDocumentType,
                                            final String expectedFilename) {
        assertThat(documentData.get("DocumentType"), is(expectedDocumentType));

        final Map<String, String> documentLinkObject = getDocumentLinkObject(documentData);

        assertThat(documentLinkObject, allOf(
                hasEntry(equalTo("document_binary_url"), is(notNullValue())),
                hasEntry(equalTo("document_url"), is(notNullValue())),
                hasEntry(equalTo("document_filename"), is(expectedFilename))
        ));

        checkEvidenceManagement(documentLinkObject);
    }

    private void checkEvidenceManagement(final Map<String, String> documentLinkObject) {
        final String divDocAuthToken = divDocAuthTokenGenerator.generate();
        final String caseworkerAuthToken = createCaseWorkerUser().getAuthToken();

        final String document_binary_url = documentLinkObject.get("document_binary_url");
        final Response response = readDataFromEvidenceManagement(document_binary_url, divDocAuthToken, caseworkerAuthToken);

        assertThat("Unable to find " + document_binary_url + " in evidence management" , response.statusCode(), is(OK.value()));
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getDocumentLinkObject(Map<String, Object> documentGenerated) {
        return (Map<String, String>)documentGenerated.get("DocumentLink");
    }
}
