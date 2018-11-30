package uk.gov.hmcts.reform.divorce.callback;

import io.restassured.response.ResponseBody;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.support.CcdClientSupport;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails.builder;
import static uk.gov.hmcts.reform.divorce.util.RestUtil.postToRestService;

@Slf4j
public class BulkPrintCallbackTest extends IntegrationTest {

    private static final String FIXTURES_ISSUE_PETITION_CCD_CALLBACK_AOS_INVITATION_JSON =
        "fixtures/issue-petition/ccd-callback-aos-invitation.json";

    @Value("${case.orchestration.petition-issued.context-path}")
    private String issueContextPath;

    @Value("${case.orchestration.bulk-print.context-path}")
    private String bulkPrintContextPath;

    @Value("${case.orchestration.maintenance.update.context-path}")
    private String updateContextPath;

    @Value("${case.orchestration.maintenance.retrieve-aos-case.context-path}")
    private String retrieveContextPath;

    @Value("core_case_data.api.url")
    private String ccdUrl;

    @Value("${ccd.jurisdictionid}")
    private String jurisdictionId;

    @Value("${ccd.casetype}")
    private String caseType;

    @Autowired
    @Qualifier("ccdSubmissionTokenGenerator")
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    @Qualifier("documentGeneratorTokenGenerator")
    private AuthTokenGenerator divDocAuthTokenGenerator;

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private CcdClientSupport ccdClientSupport;

    private Map<String, Object> citizenHeaders;

    private Map<String, Object> caseworkerHeaders;

    private UserDetails citizenUser;


    @Before
    public void setup() {
        citizenHeaders = new HashMap<>();
        caseworkerHeaders = new HashMap<>();
        citizenUser = createCitizenUser();
        createCaseWorkerUser();
        caseworkerHeaders.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        caseworkerHeaders.put(HttpHeaders.AUTHORIZATION, createCaseWorkerUser().getAuthToken());
        citizenHeaders.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        citizenHeaders.put(HttpHeaders.AUTHORIZATION, citizenUser.getAuthToken());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void givenValidCaseData_whenReceivedBulkPrint_thenReturnExpectedCaseData() throws Exception {

        Map response = postToRestService(
            serverUrl + issueContextPath,
            citizenHeaders,
            ResourceLoader.loadJson(FIXTURES_ISSUE_PETITION_CCD_CALLBACK_AOS_INVITATION_JSON)
        ).getBody().as(Map.class);
        CreateEvent createEvent = new CreateEvent();
        createEvent.setCaseDetails(builder().caseData(
            (Map) response.get("data")).caseId("323").state("submitted").build()
        );
        ResponseBody body = postToRestService(serverUrl + "/bulk-print", caseworkerHeaders,
            ResourceLoader.objectToJson(createEvent)).getBody();
        String result = ((Map) body.jsonPath().get("data")).get("dueDate").toString();
        assertEquals("Due date is not as expected ",
            LocalDate.now().plus(9, ChronoUnit.DAYS).format(DateTimeFormatter.ISO_LOCAL_DATE), result);
        log.info(result);

    }


}
