package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.google.common.collect.ImmutableMap;
import org.apache.logging.log4j.util.Strings;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_FAMILY_MAN_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_MORE_INFO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class DnDecisionMadeCallbackITest extends MockedFunctionalTest {

    private static final String API_URL = "/dn-decision-made";
    private static final String CMS_UPDATE_CASE = "/casemaintenance/version/1/updateCase/%s/cleanCaseState";

    @MockBean
    private EmailClient emailClient;

    @Autowired
    private MockMvc webClient;

    @Autowired
    ThreadPoolTaskExecutor asyncTaskExecutor;

    @Test
    public void givenCase_whenDnDecisionMade_thenCleanState() throws Exception {
        String caseId = "1500234567891209";
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseId(caseId)
                .caseData(Collections.emptyMap())
                .build())
            .build();

        stubCmsServerEndpoint(String.format(CMS_UPDATE_CASE, caseId), HttpStatus.OK, Strings.EMPTY, POST);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(emailClient, never()).sendEmail(anyString(), anyString(), any(), anyString());

        waitAsyncCompleted();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(CASE_STATE_JSON_KEY,null);
        verifyCmsServerEndpoint(1, String.format(CMS_UPDATE_CASE, caseId), RequestMethod.POST,
            convertObjectToJsonString(requestBody));
    }

    @Test
    public void givenCase_whenDnDecisionMadeWithMoreInfo_thenSendNotificationAndCleanState() throws Exception {
        String caseId = "1509876543215678";
        when(emailClient.sendEmail(anyString(), anyString(), any(), anyString())).thenReturn(null);

        Map<String, Object> caseData = new HashMap<>();

        // Notification Fields
        caseData.putAll(ImmutableMap.of(
            D_8_CASE_REFERENCE, TEST_CASE_FAMILY_MAN_ID,
            D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME,
            D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME,
            D_8_PETITIONER_EMAIL, TEST_PETITIONER_EMAIL
        ));

        // DN Refusal Clarification Fields
        caseData.putAll(ImmutableMap.of(
            DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE,
            REFUSAL_DECISION_CCD_FIELD, REFUSAL_DECISION_MORE_INFO_VALUE
        ));

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseId(caseId)
                .caseData(caseData)
                .build())
            .build();

        stubCmsServerEndpoint(String.format(CMS_UPDATE_CASE, caseId), HttpStatus.OK, Strings.EMPTY, POST);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(emailClient).sendEmail(anyString(), anyString(), any(), anyString());

        waitAsyncCompleted();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(CASE_STATE_JSON_KEY,null);
        verifyCmsServerEndpoint(1, String.format(CMS_UPDATE_CASE, caseId), RequestMethod.POST,
            convertObjectToJsonString(requestBody));
    }

    private void stubCmsServerEndpoint(String path, HttpStatus status, String body, HttpMethod method) {

        maintenanceServiceServer.stubFor(WireMock.request(method.name(),urlEqualTo(path))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(body)));
    }

    private void verifyCmsServerEndpoint(int times, String path, RequestMethod method, String body) {
        maintenanceServiceServer.verify(times, new RequestPatternBuilder(method, urlEqualTo(path))
            .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
            .withRequestBody(equalTo(body)));
    }

    private void waitAsyncCompleted() {
        await().until(() -> asyncTaskExecutor.getThreadPoolExecutor().getActiveCount() == 0);
    }

}
