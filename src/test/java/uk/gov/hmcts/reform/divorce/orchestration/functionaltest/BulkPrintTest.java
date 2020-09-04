package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.util.Collections.emptyMap;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.PERSONAL_SERVICE_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.SOL_SERVICE_METHOD_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_CO_RESPONDENT_INVITATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_PETITION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_RESPONDENT_INVITATION;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.CaseDataTestHelper.createCollectionMemberDocument;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class BulkPrintTest extends IdamTestSupport {

    private static final String API_URL = "/bulk-print";

    @Autowired
    private MockMvc webClient;

    @MockBean
    private EmailClient emailClient;

    private String testDocumentId;

    @Before
    public void setup() {
        sendLetterService.resetAll();
        testDocumentId = UUID.randomUUID().toString();
        stubDMStore(testDocumentId, "testContent".getBytes());
        stubServiceAuthProvider(HttpStatus.OK, TEST_SERVICE_AUTH_TOKEN);
    }

    @Test
    public void givenCaseDataWithNoSolicitor_whenCalledBulkPrint_thenExpectDueDateInCCDResponse() throws Exception {
        stubSendLetterService(HttpStatus.OK);

        Map<String, Object> expectedCaseData = caseDataWithDocuments();
        expectedCaseData.put("dueDate", LocalDate.now().plus(9, ChronoUnit.DAYS).format(DateTimeFormatter.ISO_LOCAL_DATE));
        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .data(expectedCaseData)
            .errors(Collections.emptyList())
            .warnings(Collections.emptyList())
            .build();

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(callbackWithDocuments()))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)));

        verifyNoInteractions(emailClient);
    }

    @Test
    public void givenValidCaseDataWithSendLetterApiDown_whenCalledBulkPrint_thenExpectErrorInCCDResponse() throws Exception {
        stubSendLetterService(HttpStatus.INTERNAL_SERVER_ERROR);

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .data(emptyMap())
            .errors(Collections.singletonList("Failed to bulk print documents"))
            .warnings(Collections.emptyList())
            .build();

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(callbackWithDocuments()))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)));
    }

    private CcdCallbackRequest callbackWithDocuments() {
        final Map<String, Object> caseData = caseDataWithDocuments();
        return new CcdCallbackRequest("abacccd", "BulkPrint", CaseDetails.builder()
            .caseData(caseData)
            .caseId("12345")
            .state("AOSPackGenerated").build());
    }

    private Map<String, Object> caseDataWithDocuments() {
        final Map<String, Object> caseData = new HashMap<>();
        caseData.put("D8DocumentsGenerated", Arrays.asList(
            createCollectionMemberDocument("http://localhost:4020/documents/" + testDocumentId, DOCUMENT_TYPE_PETITION, "issue"),
            createCollectionMemberDocument("http://localhost:4020/documents/" + testDocumentId, DOCUMENT_TYPE_RESPONDENT_INVITATION, "aosletter"),
            createCollectionMemberDocument("http://localhost:4020/documents/" + testDocumentId, DOCUMENT_TYPE_CO_RESPONDENT_INVITATION, "coRespondentletter")
        ));
        return caseData;
    }

    @Test
    public void givenServiceMethodIsPersonalServiceAndStateIsNotAwaitingService_thenResponseContainsErrors() throws Exception {
        final Map<String, Object> caseData = Collections.singletonMap(
            SOL_SERVICE_METHOD_CCD_FIELD, PERSONAL_SERVICE_VALUE
        );

        final CaseDetails caseDetails = CaseDetails.builder()
            .state("Issued")
            .caseData(caseData)
            .build();

        CcdCallbackRequest request = CcdCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(request))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.data", is(Collections.emptyMap())),
                hasJsonPath("$.errors",
                    hasItem("Failed to bulk print documents - This event cannot be used when "
                        + "service method is Personal Service and the case is not in Awaiting Service.")
                )
            )));
    }

}