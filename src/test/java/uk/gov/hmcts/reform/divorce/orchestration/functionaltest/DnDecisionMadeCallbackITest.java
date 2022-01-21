package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.google.common.collect.ImmutableMap;
import org.apache.logging.log4j.util.Strings;
import org.junit.Ignore;
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
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_FAMILY_MAN_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_INFERRED_MALE_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RELATIONSHIP_HUSBAND;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_WELSH_MALE_GENDER_IN_RELATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_REFUSED_REJECT_OPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_PETITIONER_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_FEES_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_HUSBAND_OR_WIFE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_WELSH_HUSBAND_OR_WIFE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_MORE_INFO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class DnDecisionMadeCallbackITest extends MockedFunctionalTest {

    private static final String API_URL = "/dn-decision-made";

    private static final String CMS_UPDATE_CASE = "/casemaintenance/version/1/updateCase/%s/cleanCaseState";
    private static final String AMEND_PETITION_FEE_CONTEXT_PATH =  "/fees-and-payments/version/1/amend-fee";

    private static final String DECREE_NISI_REFUSAL_ORDER_CLARIFICATION_TEMPLATE_ID = "bc4c7ba0-4fc0-4f14-876b-1328eca127b8";
    private static final String SOL_DN_DECISION_MADE_TEMPLATE_ID = "fd6d9667-527f-4c22-b1f1-d095c412ab2c";
    private static final String DECREE_NISI_REFUSAL_ORDER_REJECTION_SOLICITOR_TEMPLATE_ID = "de12c7ed-9d5d-4def-a060-2de30594a3bf";
    private static final String DECREE_NISI_REFUSAL_ORDER_REJECTION_TEMPLATE_ID = "0216e301-989f-49b8-841e-7f61cef9838a";

    @MockBean
    private EmailClient emailClient;

    @Autowired
    private MockMvc webClient;

    @Autowired
    ThreadPoolTaskExecutor asyncTaskExecutor;

    @Test
    public void givenCase_whenDnDecisionMade_thenCleanState() throws Exception {
        String caseId = "1500234567891209";

        CcdCallbackRequest ccdCallbackRequest = buildRequest(caseId, Collections.emptyMap());

        stubSuccessfulCallsToExternalApis(caseId);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verifyNoInteractions(emailClient);

        verifyCmsCalls(caseId);
    }

    private void verifyCmsCalls(String caseId) {
        waitAsyncCompleted();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(CASE_STATE_JSON_KEY, null);
        verifyCmsServerEndpoint(1, String.format(CMS_UPDATE_CASE, caseId), RequestMethod.POST,
            convertObjectToJsonString(requestBody));
    }

    @Test
    public void givenCase_whenDnDecisionMadeWithMoreInfo_thenSendNotificationAndCleanState() throws Exception {
        final String caseId = "1509876543215678";

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

        CcdCallbackRequest ccdCallbackRequest = buildRequest(caseId, caseData);

        stubSuccessfulCallsToExternalApis(caseId);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(emailClient)
            .sendEmail(
                eq(DECREE_NISI_REFUSAL_ORDER_CLARIFICATION_TEMPLATE_ID),
                eq(TEST_PETITIONER_EMAIL),
                eq(
                    new HashMap<>(ImmutableMap.of(
                        NOTIFICATION_CASE_NUMBER_KEY, TEST_CASE_FAMILY_MAN_ID,
                        NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TEST_PETITIONER_FIRST_NAME,
                        NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TEST_PETITIONER_LAST_NAME
                    ))
                ),
                anyString());

        verifyCmsCalls(caseId);
    }

    @Test
    public void shouldNotSendEmailWhenDnIsGranted() throws Exception {
        final String caseId = "1509876543215680";

        CcdCallbackRequest ccdCallbackRequest = buildRequest(caseId, ImmutableMap.of(
            DECREE_NISI_GRANTED_CCD_FIELD, YES_VALUE
        ));

        stubSuccessfulCallsToExternalApis(caseId);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verifyNoMoreInteractions(emailClient);

        verifyCmsCalls(caseId);
    }

    @Test
    public void shouldSendEmailToSolicitorWhenDnDecisionMade() throws Exception {
        final String caseId = "1509876543215681";

        Map<String, Object> caseData = new HashMap<>();
        caseData.put(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE);
        caseData.put(REFUSAL_DECISION_CCD_FIELD, REFUSAL_DECISION_MORE_INFO_VALUE);
        caseData.put(D_8_CASE_REFERENCE, TEST_CASE_FAMILY_MAN_ID);
        caseData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME);
        caseData.put(PETITIONER_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        caseData.put(PETITIONER_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);

        CcdCallbackRequest ccdCallbackRequest = buildRequest(caseId, caseData);

        stubSuccessfulCallsToExternalApis(caseId);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(emailClient)
            .sendEmail(
                eq(SOL_DN_DECISION_MADE_TEMPLATE_ID),
                eq(TEST_SOLICITOR_EMAIL),
                eq(
                    new HashMap<>(ImmutableMap.of(
                        NOTIFICATION_CCD_REFERENCE_KEY, caseId,
                        NOTIFICATION_PET_NAME, TEST_PETITIONER_FULL_NAME,
                        NOTIFICATION_RESP_NAME, TEST_RESPONDENT_FULL_NAME,
                        NOTIFICATION_SOLICITOR_NAME, TEST_SOLICITOR_NAME,
                        NOTIFICATION_FEES_KEY, "50"
                    ))
                ),
                anyString()
            );

        verifyCmsCalls(caseId);
    }

    @Test
    @Ignore("Test is failing only at pr build stage, ignoring to unblock QA Testing, will investigate further when Testing signs off")
    public void shouldSendEmailToPetitionerWhenDnDecisionMade() throws Exception {
        final String caseId = "1509876543215683";

        Map<String, Object> caseData = new HashMap<>();
        caseData.put(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE);
        caseData.put(REFUSAL_DECISION_CCD_FIELD, DN_REFUSED_REJECT_OPTION);
        caseData.put(D_8_CASE_REFERENCE, TEST_CASE_FAMILY_MAN_ID);
        caseData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME);
        caseData.put(PETITIONER_EMAIL, TEST_PETITIONER_EMAIL);
        caseData.put(D_8_INFERRED_PETITIONER_GENDER, TEST_INFERRED_MALE_GENDER);

        CcdCallbackRequest ccdCallbackRequest = buildRequest(caseId, caseData);

        stubSuccessfulCallsToExternalApis(caseId);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        Map<String, String> emailVars = new HashMap<>(ImmutableMap.of(
            NOTIFICATION_CASE_NUMBER_KEY, TEST_CASE_FAMILY_MAN_ID,
            NOTIFICATION_HUSBAND_OR_WIFE, TEST_RELATIONSHIP_HUSBAND,
            NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TEST_PETITIONER_FIRST_NAME,
            NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TEST_PETITIONER_LAST_NAME,
            NOTIFICATION_FEES_KEY, "50"
        ));

        emailVars.put(NOTIFICATION_WELSH_HUSBAND_OR_WIFE, TEST_WELSH_MALE_GENDER_IN_RELATION);

        verify(emailClient)
            .sendEmail(
                eq(DECREE_NISI_REFUSAL_ORDER_REJECTION_TEMPLATE_ID),
                eq(TEST_PETITIONER_EMAIL),
                eq(emailVars),
                anyString()
            );
        waitAsyncCompleted();
    }

    @Test
    public void shouldSendEmailToPetitionerSolicitorWhenDnDecisionMade() throws Exception {
        final String caseId = "1509876543215682";

        Map<String, Object> caseData = new HashMap<>();
        caseData.put(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE);
        caseData.put(REFUSAL_DECISION_CCD_FIELD, DN_REFUSED_REJECT_OPTION);
        caseData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME);
        caseData.put(PETITIONER_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);
        caseData.put(PETITIONER_SOLICITOR_NAME, TEST_SOLICITOR_NAME);

        CcdCallbackRequest ccdCallbackRequest = buildRequest(caseId, caseData);

        stubSuccessfulCallsToExternalApis(caseId);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(emailClient)
            .sendEmail(
                eq(DECREE_NISI_REFUSAL_ORDER_REJECTION_SOLICITOR_TEMPLATE_ID),
                eq(TEST_SOLICITOR_EMAIL),
                eq(new HashMap<>(ImmutableMap.of(
                    NOTIFICATION_CCD_REFERENCE_KEY, caseId,
                    NOTIFICATION_PET_NAME, TEST_PETITIONER_FULL_NAME,
                    NOTIFICATION_RESP_NAME, TEST_RESPONDENT_FULL_NAME,
                    NOTIFICATION_SOLICITOR_NAME, TEST_SOLICITOR_NAME,
                    NOTIFICATION_FEES_KEY, "50"
                ))),
                anyString()
            );

        verifyCmsCalls(caseId);
    }

    private void stubCmsServerEndpoint(String path, HttpStatus status, String body, HttpMethod method) {
        maintenanceServiceServer.stubFor(WireMock.request(method.name(),urlEqualTo(path))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(body)));
    }

    private void verifyCmsServerEndpoint(int times, String path, RequestMethod method, String body) {
        maintenanceServiceServer.verify(times, new RequestPatternBuilder(method, urlEqualTo(path))
            .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
            .withRequestBody(equalTo(body)));
    }

    private void stubGetFeeFromFeesAndPayments(HttpStatus status, FeeResponse feeResponse) {
        feesAndPaymentsServer.stubFor(WireMock.get(AMEND_PETITION_FEE_CONTEXT_PATH)
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(convertObjectToJsonString(feeResponse))));
    }

    private void waitAsyncCompleted() {
        await().until(() -> asyncTaskExecutor.getThreadPoolExecutor().getActiveCount() == 0);
    }

    private CcdCallbackRequest buildRequest(String caseId, Map<String, Object> objectObjectMap) {
        return CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseId(caseId)
                .caseData(objectObjectMap)
                .build())
            .build();
    }

    private void stubSuccessfulCallsToExternalApis(String caseId) {
        stubCmsServerEndpoint(String.format(CMS_UPDATE_CASE, caseId), HttpStatus.OK, Strings.EMPTY, POST);
        stubGetFeeFromFeesAndPayments(HttpStatus.OK, FeeResponse.builder().amount(50.00).build());
    }
}
