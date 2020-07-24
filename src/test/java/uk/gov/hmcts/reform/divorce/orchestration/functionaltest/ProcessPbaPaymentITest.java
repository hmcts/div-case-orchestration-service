package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtEnum;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeValue;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.PaymentItem;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static java.util.Collections.EMPTY_MAP;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_AMOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_DESCRIPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_VERSION;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_ACCOUNT_NUMBER;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_FIRM_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CURRENCY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_CENTRE_SITEID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PREVIOUS_CASE_ID_CCD_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SERVICE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SERVICE_AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_FEE_ACCOUNT_NUMBER_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_FIRM_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_HOW_TO_PAY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_STATEMENT_OF_TRUTH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATEMENT_OF_TRUTH;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerSubmissionNotificationEmailTask.AMEND_DESC;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerSubmissionNotificationEmailTask.AMEND_SOL_DESC;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerSubmissionNotificationEmailTask.SUBMITTED_DESC;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class ProcessPbaPaymentITest extends MockedFunctionalTest {

    private static final String API_URL = "/process-pba-payment";
    private static final String PAYMENTS_CREDIT_ACCOUNT_CONTEXT_PATH = "/credit-account-payments";
    private static final String FORMAT_REMOVE_PETITION_DOCUMENTS_CONTEXT_PATH = "/caseformatter/version/1/remove-all-petition-documents";
    private static final String YES_VALUE = "YES";
    private static final String NO_VALUE = "NO";

    @MockBean
    private EmailService emailService;

    @Autowired
    private MockMvc webClient;

    private Map<String, Object> caseData;
    private CaseDetails caseDetails;
    private CcdCallbackRequest ccdCallbackRequest;
    private CreditAccountPaymentRequest request;

    @Before
    public void setup() {
        FeeResponse feeResponse = FeeResponse.builder()
            .amount(TEST_FEE_AMOUNT)
            .feeCode(TEST_FEE_CODE)
            .version(TEST_FEE_VERSION)
            .description(TEST_FEE_DESCRIPTION)
            .build();
        OrderSummary orderSummary = new OrderSummary();
        orderSummary.add(feeResponse);

        FeeValue feeValue = new FeeValue();
        feeValue.setFeeAmount(TEST_FEE_AMOUNT.toString());
        feeValue.setFeeCode(TEST_FEE_CODE);
        feeValue.setFeeVersion(TEST_FEE_VERSION.toString());
        feeValue.setFeeDescription(TEST_FEE_DESCRIPTION);

        caseData = new HashMap<>();
        caseData.put(SOLICITOR_HOW_TO_PAY_JSON_KEY, FEE_PAY_BY_ACCOUNT);
        caseData.put(PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY, orderSummary);
        caseData.put(CASE_ID_JSON_KEY, TEST_CASE_ID);
        caseData.put(D_8_PETITIONER_EMAIL, TEST_PETITIONER_EMAIL);
        caseData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME);
        caseData.put(DIVORCE_CENTRE_SITEID_JSON_KEY, CourtEnum.EASTMIDLANDS.getSiteId());
        caseData.put(DIVORCE_UNIT_JSON_KEY, CourtEnum.EASTMIDLANDS.getId());
        caseData.put(SOLICITOR_FEE_ACCOUNT_NUMBER_JSON_KEY, TEST_SOLICITOR_ACCOUNT_NUMBER);
        caseData.put(SOLICITOR_FIRM_JSON_KEY, TEST_SOLICITOR_FIRM_NAME);
        caseData.put(SOLICITOR_REFERENCE_JSON_KEY, TEST_SOLICITOR_REFERENCE);

        request = new CreditAccountPaymentRequest();
        request.setService(SERVICE);
        request.setCurrency(CURRENCY);
        request.setAmount(orderSummary.getPaymentTotal());
        request.setCcdCaseNumber(TEST_CASE_ID);
        request.setSiteId(CourtEnum.EASTMIDLANDS.getSiteId());
        request.setAccountNumber(TEST_SOLICITOR_ACCOUNT_NUMBER);
        request.setOrganisationName(TEST_SOLICITOR_FIRM_NAME);
        request.setCustomerReference(TEST_SOLICITOR_REFERENCE);
        request.setDescription(TEST_FEE_DESCRIPTION);

        PaymentItem paymentItem = new PaymentItem();
        paymentItem.setCcdCaseNumber(TEST_CASE_ID);
        paymentItem.setCalculatedAmount(orderSummary.getPaymentTotal());
        paymentItem.setCode(TEST_FEE_CODE);
        paymentItem.setReference(TEST_SOLICITOR_REFERENCE);
        paymentItem.setVersion(TEST_FEE_VERSION.toString());
        request.setFees(Collections.singletonList(paymentItem));
    }

    @Test
    public void givenCaseData_whenProcessPbaPayment_thenMakePaymentAndReturn_PetitionerNewCase() throws Exception {
        caseData.put(STATEMENT_OF_TRUTH, YES_VALUE);
        caseData.put(SOLICITOR_STATEMENT_OF_TRUTH, YES_VALUE);

        caseData.remove(PETITIONER_SOLICITOR_EMAIL);

        makePaymentAndReturn();

        verify(emailService).sendEmail(
            eq(TEST_PETITIONER_EMAIL),
            eq(EmailTemplateNames.APPLIC_SUBMISSION.name()),
            anyMap(),
            eq(SUBMITTED_DESC),
            eq(LanguagePreference.ENGLISH)
        );
    }

    @Test
    public void givenCaseData_whenProcessPbaPayment_thenMakePaymentAndReturn_PetitionerAmendedCase() throws Exception {
        caseData.put(STATEMENT_OF_TRUTH, YES_VALUE);
        caseData.put(SOLICITOR_STATEMENT_OF_TRUTH, YES_VALUE);
        caseData.put(PREVIOUS_CASE_ID_CCD_KEY, EMPTY_MAP);

        caseData.remove(PETITIONER_SOLICITOR_EMAIL);

        makePaymentAndReturn();

        verify(emailService).sendEmail(
            eq(TEST_PETITIONER_EMAIL),
            eq(EmailTemplateNames.APPLIC_SUBMISSION_AMEND.name()),
            anyMap(),
            eq(AMEND_DESC),
            eq(LanguagePreference.ENGLISH)
        );
    }

    @Test
    public void givenCaseData_whenProcessPbaPayment_thenMakePaymentAndReturn_SolicitorPetitionerAmendedCase() throws Exception {
        caseData.put(STATEMENT_OF_TRUTH, YES_VALUE);
        caseData.put(SOLICITOR_STATEMENT_OF_TRUTH, YES_VALUE);
        caseData.put(PREVIOUS_CASE_ID_CCD_KEY, EMPTY_MAP);

        caseData.put(PETITIONER_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);

        makePaymentAndReturn();

        verify(emailService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(EmailTemplateNames.APPLIC_SUBMISSION_AMEND_SOLICITOR.name()),
            anyMap(),
            eq(AMEND_SOL_DESC),
            eq(LanguagePreference.ENGLISH)
        );
    }

    private void makePaymentAndReturn() throws Exception {
        caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .build();

        ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        final CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .data(caseData)
            .build();

        stubCreditAccountPayment(HttpStatus.OK, new CreditAccountPaymentResponse());
        stubServiceAuthProvider(HttpStatus.OK, TEST_SERVICE_AUTH_TOKEN);
        stubFormatterServerEndpoint(caseData);

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)));
    }

    @Test
    public void givenInvalidCaseData_whenProcessPbaPayment_thenReturnErrors() throws Exception {
        caseData.put(STATEMENT_OF_TRUTH, NO_VALUE);
        caseData.put(SOLICITOR_STATEMENT_OF_TRUTH, NO_VALUE);

        caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .build();

        ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .errors(Collections.singletonList(
                "Statement of truth for solicitor and petitioner needs to be accepted"
            ))
            .build();

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)));
    }

    private void stubCreditAccountPayment(HttpStatus status, CreditAccountPaymentResponse response) {
        paymentServiceServer.stubFor(WireMock.post(PAYMENTS_CREDIT_ACCOUNT_CONTEXT_PATH)
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .withHeader(SERVICE_AUTHORIZATION_HEADER, new EqualToPattern("Bearer " + TEST_SERVICE_AUTH_TOKEN))
            .withRequestBody(equalToJson(convertObjectToJsonString(request)))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(convertObjectToJsonString(response))));
    }

    private void stubFormatterServerEndpoint(Map<String, Object> data) {
        formatterServiceServer.stubFor(WireMock.post(FORMAT_REMOVE_PETITION_DOCUMENTS_CONTEXT_PATH)
            .withRequestBody(equalToJson(convertObjectToJsonString(data)))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(convertObjectToJsonString(data))));
    }
}
