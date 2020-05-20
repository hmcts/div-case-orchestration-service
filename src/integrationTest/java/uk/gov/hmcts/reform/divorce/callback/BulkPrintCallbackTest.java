package uk.gov.hmcts.reform.divorce.callback;

import org.apache.http.entity.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.loadJson;
import static uk.gov.hmcts.reform.divorce.util.RestUtil.postToRestService;

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
        Map response = postToRestService(serverUrl + issueContextPath + "?generateAosInvitation=true", citizenHeaders,
            loadJson(NON_ADULTERY_CASE))
            .getBody()
            .as(Map.class);

        CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(CaseDetails.builder().caseData((Map) response.get("data")).caseId("323").state("submitted").build());

        String jsonResponse = postToRestService(serverUrl + bulkPrintContextPath, caseworkerHeaders, ccdCallbackRequest).getBody().asString();
        assertThat(jsonResponse, allOf(
            isJson(),
            hasJsonPath("data.dueDate", equalTo(expectedDueDate))
        ));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void givenAdulteryCaseWithCoRespondent_whenReceivedBulkPrint_thenDueDatePopulated() throws Exception {
        Map response = postToRestService(serverUrl + issueContextPath + "?generateAosInvitation=true", citizenHeaders,
            loadJson(ADULTERY_CASE_WITH_CORESPONDENT))
            .getBody()
            .as(Map.class);

        CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(CaseDetails.builder().caseData((Map) response.get("data")).caseId("323").state("submitted").build());

        String jsonResponse = postToRestService(serverUrl + bulkPrintContextPath, caseworkerHeaders, ccdCallbackRequest).getBody().asString();
        assertThat(jsonResponse, allOf(
            isJson(),
            hasJsonPath("data.dueDate", equalTo(expectedDueDate))
        ));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void givenRespondentSolicitorAos_whenReceivedBulkPrint_thenDueDatePopulated() throws Exception {
        Map response = postToRestService(serverUrl + issueContextPath + "?generateAosInvitation=true", citizenHeaders,
            loadJson(RESPONDENT_SOLICITOR_AOS_INVITATION))
            .getBody()
            .as(Map.class);

        CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(CaseDetails.builder().caseData(
            (Map) response.get(DATA)).caseId("1517833758870511").state("Issued").build()
        );

        String jsonResponse = postToRestService(serverUrl + bulkPrintContextPath, caseworkerHeaders, ccdCallbackRequest).getBody().asString();
        assertThat(jsonResponse, allOf(
            isJson(),
            hasJsonPath("data.dueDate", equalTo(expectedDueDate))
        ));
    }

}