package uk.gov.hmcts.reform.divorce.callback;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.loadJson;
import static uk.gov.hmcts.reform.divorce.util.RestUtil.postToRestService;

@Slf4j
public class BulkPrintCallbackTest extends IntegrationTest {

    private static final String FILES_PATH = "fixtures/issue-petition/";
    private static final String NON_ADULTERY_CASE = FILES_PATH + "ccd-callback-aos-invitation.json";
    private static final String ADULTERY_CASE_WITH_CORESPONDENT = FILES_PATH + "ccd-callback-aos-invitation-service-centre-with-coRespondent.json";
    private static final String RESPONDENT_SOLICITOR_AOS_INVITATION = FILES_PATH + "ccd-callback-solicitor-aos-invitation.json";

    @Value("${case.orchestration.petition-issued.context-path}")
    private String issueContextPath;

    @Value("${case.orchestration.bulk-print.context-path}")
    private String bulkPrintContextPath;

    private Map<String, Object> citizenHeaders;

    private Map<String, Object> caseworkerHeaders;
    private String expectedDueDate;

    @Before
    public void setup() {
        final UserDetails citizenUser = createCitizenUser();
        final UserDetails caseWorkerUser = createCaseWorkerUser();

        caseworkerHeaders = new HashMap<>();
        caseworkerHeaders.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        caseworkerHeaders.put(HttpHeaders.AUTHORIZATION, caseWorkerUser.getAuthToken());

        citizenHeaders = new HashMap<>();
        citizenHeaders.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        citizenHeaders.put(HttpHeaders.AUTHORIZATION, citizenUser.getAuthToken());

        expectedDueDate = LocalDate.now().plus(30, ChronoUnit.DAYS).format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void givenNonAdulteryCase_whenReceivedBulkPrint_thenDueDatePopulated() throws Exception {
        Map<String, Object> response = callApiToGenerateAos(NON_ADULTERY_CASE);

        CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(CaseDetails.builder().caseData((Map) response.get("data")).caseId("323").state("submitted").build());

        String jsonResponse = callBulkPrintAsCaseworker(ccdCallbackRequest);
        assertThat(jsonResponse, allOf(
            isJson(),
            hasJsonPath("data.dueDate", equalTo(expectedDueDate))
        ));
    }

    @Test
    public void givenAdulteryCaseWithCoRespondent_whenReceivedBulkPrint_thenDueDatePopulated() throws Exception {
        Map<String, Object> response = callApiToGenerateAos(ADULTERY_CASE_WITH_CORESPONDENT);

        CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(
            CaseDetails.builder()
                .caseData((Map<String, Object>) response.get("data"))
                .caseId("323")
                .state("submitted")
                .build()
        );

        String jsonResponse = callBulkPrintAsCaseworker(ccdCallbackRequest);

        assertResponseIsValid(jsonResponse);
    }

    @Test
    public void givenRespondentSolicitorAos_whenReceivedBulkPrint_thenDueDatePopulated() throws Exception {
        Map<String, Object> response = callApiToGenerateAos(RESPONDENT_SOLICITOR_AOS_INVITATION);

        CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(
            CaseDetails.builder()
                .caseData((Map<String, Object>) response.get(DATA))
                .caseId("1517833758870511")
                .state("Issued")
                .build()
        );

        String jsonResponse = callBulkPrintAsCaseworker(ccdCallbackRequest);
        assertResponseIsValid(jsonResponse);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> callApiToGenerateAos(String respondentSolicitorAosInvitation) throws Exception {
        return postToRestService(
            serverUrl + issueContextPath + "?generateAosInvitation=true",
            citizenHeaders,
            loadJson(respondentSolicitorAosInvitation)
        )
            .getBody()
            .as(Map.class);
    }

    private String callBulkPrintAsCaseworker(CcdCallbackRequest ccdCallbackRequest) {
        log.info("ccdCallbackRequest {}", ccdCallbackRequest);

        Response response = postToRestService(
            serverUrl + bulkPrintContextPath,
            caseworkerHeaders,
            ccdCallbackRequest
        );

        log.info("Bulk print response {}", response);

        return response.getBody().asString();
    }

    private void assertResponseIsValid(String jsonResponse) {
        log.info("JSON {}", jsonResponse);

        assertThat("Response from bulk print is not JSON", jsonResponse, isJson());
        assertThat(
            "data.dueDate is wrong",
            jsonResponse,
            hasJsonPath("data.dueDate", equalTo(expectedDueDate))
        );
    }
}
