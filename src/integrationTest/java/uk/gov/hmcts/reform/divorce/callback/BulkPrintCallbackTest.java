package uk.gov.hmcts.reform.divorce.callback;

import io.restassured.response.ResponseBody;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.loadJson;
import static uk.gov.hmcts.reform.divorce.util.RestUtil.postToRestService;

@Slf4j
public class BulkPrintCallbackTest extends IntegrationTest {

    private static final String FIXTURES_ISSUE_PETITION_CCD_CALLBACK_AOS_INVITATION_JSON =
        "fixtures/issue-petition/ccd-callback-aos-invitation.json";

    @Value("${case.orchestration.petition-issued.context-path}")
    private String issueContextPath;

    @Value("${case.orchestration.bulk-print.context-path}")
    private String bulkPrintContextPath;

    private Map<String, Object> citizenHeaders;

    private Map<String, Object> caseworkerHeaders;


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
    }

    @Test
    @SuppressWarnings("unchecked")
    public void givenValidCaseData_whenReceivedBulkPrint_thenDueDatePopulated() throws Exception {

        Map response = postToRestService(serverUrl + issueContextPath + "?generateAosInvitation=true", citizenHeaders,
            loadJson(FIXTURES_ISSUE_PETITION_CCD_CALLBACK_AOS_INVITATION_JSON))
            .getBody()
            .as(Map.class);

        CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(CaseDetails.builder().caseData(
            (Map) response.get("data")).caseId("323").state("submitted").build()
        );
        ResponseBody body = postToRestService(serverUrl + bulkPrintContextPath, caseworkerHeaders,
            ResourceLoader.objectToJson(ccdCallbackRequest)).getBody();
        assertThat("Response body is not a JSON: " + body.print(),
                body.print(),
                isJson()
        );
        String result = ((Map) body.jsonPath().get("data")).get("dueDate").toString();
        assertEquals("Due date is not as expected ",
            LocalDate.now().plus(30, ChronoUnit.DAYS).format(DateTimeFormatter.ISO_LOCAL_DATE), result);
        log.info(result);

    }
}
