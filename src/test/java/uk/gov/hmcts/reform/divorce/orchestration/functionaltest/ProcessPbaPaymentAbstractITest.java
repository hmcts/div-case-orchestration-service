package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtEnum;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeValue;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.PaymentItem;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.PaymentStatus;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ProcessPbaPaymentTask;
import uk.gov.hmcts.reform.divorce.orchestration.util.payment.PbaErrorMessage;
import uk.gov.service.notify.NotificationClientException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.util.Collections.EMPTY_MAP;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
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
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_ACCOUNT_NUMBER;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_FIRM_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_NAME;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RDC_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PREVIOUS_CASE_ID_CCD_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SERVICE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SERVICE_AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_FIRM_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_HOW_TO_PAY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_STATEMENT_OF_TRUTH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATEMENT_OF_TRUTH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.PbaClientErrorTestUtil.buildPaymentClientResponse;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.PbaClientErrorTestUtil.formatMessage;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.PbaClientErrorTestUtil.getBasicFailedResponse;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.formatCaseIdToReferenceNumber;

public abstract class ProcessPbaPaymentAbstractITest extends MockedFunctionalTest {

    private static final String API_URL = "/process-pba-payment";
    private static final String PAYMENTS_CREDIT_ACCOUNT_CONTEXT_PATH = "/credit-account-payments";
    private static final String FORMAT_REMOVE_PETITION_DOCUMENTS_CONTEXT_PATH = "/caseformatter/version/1/remove-all-petition-documents";
    private static final String EAST_MIDLANDS_RDC = "East Midlands Regional Divorce Centre";

    private static final String SOL_APPLICANT_APPLICATION_SUBMITTED_TEMPLATE_ID = "93c79e53-e638-42a6-8584-7d19604e7697";
    private static final String APPLIC_SUBMISSION_TEMPLATE_ID = "c323844c-5fb9-4ba4-8290-b84139eb033c";
    private static final String APPLIC_SUBMISSION_AMEND_SOLICITOR_TEMPLATE_ID = "643525d3-9543-4e17-a07e-15f8aa9b1732";
    private static final String APPLIC_SUBMISSION_AMEND_TEMPLATE_ID = "dafe6549-3b6d-4dca-a7bc-1ab2b1b1b9d6";

    @MockBean
    private EmailClient mockEmailClient;

    @Autowired
    private MockMvc webClient;

    protected Map<String, Object> caseData;
    private CaseDetails caseDetails;
    private CcdCallbackRequest ccdCallbackRequest;
    private CreditAccountPaymentRequest request;
    private CreditAccountPaymentResponse basicFailedResponse;

    protected abstract void setPbaNumber();

    @Before
    public void setUp() {
        basicFailedResponse = getBasicFailedResponse();

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
        caseData.put(PETITIONER_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        caseData.put(DIVORCE_CENTRE_SITEID_JSON_KEY, CourtEnum.EASTMIDLANDS.getSiteId());
        caseData.put(DIVORCE_UNIT_JSON_KEY, CourtEnum.EASTMIDLANDS.getId());
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

        verifyApplicationSubmittedEmailWasSent();
    }

    @Test
    public void givenCaseData_whenProcessPbaPayment_thenMakePaymentAndReturn_PetitionerAmendedCase() throws Exception {
        caseData.put(STATEMENT_OF_TRUTH, YES_VALUE);
        caseData.put(SOLICITOR_STATEMENT_OF_TRUTH, YES_VALUE);
        caseData.put(PREVIOUS_CASE_ID_CCD_KEY, EMPTY_MAP);

        caseData.remove(PETITIONER_SOLICITOR_EMAIL);

        makePaymentAndReturn();

        verifyAmendedApplicationSubmittedEmailWasSent();
    }

    @Test
    public void makePaymentAndSendEmailToPetitionerSolicitor_whenPetitionerRepresentedAndCaseNotAmended() throws Exception {
        caseData.put(STATEMENT_OF_TRUTH, YES_VALUE);
        caseData.put(SOLICITOR_STATEMENT_OF_TRUTH, YES_VALUE);
        caseData.put(PREVIOUS_CASE_ID_CCD_KEY, null);
        caseData.put(PETITIONER_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);

        makePaymentAndReturn();

        verifyApplicationSubmittedEmailWasSent();
        verifySolicitorApplicationSubmittedEmailWasSent();
    }

    @Test
    public void givenCaseData_whenProcessPbaPayment_thenMakePaymentAndReturn_SolicitorPetitionerAmendedCase() throws Exception {
        caseData.put(STATEMENT_OF_TRUTH, YES_VALUE);
        caseData.put(SOLICITOR_STATEMENT_OF_TRUTH, YES_VALUE);
        caseData.put(PREVIOUS_CASE_ID_CCD_KEY, EMPTY_MAP);

        caseData.put(PETITIONER_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);

        makePaymentAndReturn();

        verify(mockEmailClient).sendEmail(
            eq(APPLIC_SUBMISSION_AMEND_SOLICITOR_TEMPLATE_ID),
            eq(TEST_SOLICITOR_EMAIL),
            eq(
                ImmutableMap.of(
                    NOTIFICATION_PET_NAME, TEST_PETITIONER_FIRST_NAME + " " + TEST_PETITIONER_LAST_NAME,
                    NOTIFICATION_RESP_NAME, TEST_RESPONDENT_FIRST_NAME + " " + TEST_RESPONDENT_LAST_NAME,
                    NOTIFICATION_SOLICITOR_NAME, TEST_SOLICITOR_NAME,
                    NOTIFICATION_RDC_NAME_KEY, EAST_MIDLANDS_RDC,
                    NOTIFICATION_CCD_REFERENCE_KEY, TEST_CASE_ID
                )
            ),
            anyString()
        );
    }

    @Test
    public void givenCaseData_whenOtherPaymentMethod_thenReturnDefaultStateForNonPbaPayments() throws Exception {
        caseData.put(STATEMENT_OF_TRUTH, YES_VALUE);
        caseData.put(SOLICITOR_STATEMENT_OF_TRUTH, YES_VALUE);
        caseData.put(SOLICITOR_HOW_TO_PAY_JSON_KEY, "NotByAccount");

        caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .build();

        ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        final CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .state(ProcessPbaPaymentTask.DEFAULT_END_STATE_FOR_NON_PBA_PAYMENTS)
            .data(caseData)
            .build();

        stubFormatterServerEndpoint(caseData);

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)));

        verifyApplicationSubmittedEmailWasSent();
    }

    @Test
    public void givenCaseData_whenPendingPayment_AndPaymentStatusPending_thenReturnStateSolicitorAwaitingPaymentConfirmation() throws Exception {
        caseData.put(STATEMENT_OF_TRUTH, YES_VALUE);
        caseData.put(SOLICITOR_STATEMENT_OF_TRUTH, YES_VALUE);

        caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .build();

        ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        Map<String, Object> workFlowCaseDataResponse = new HashMap<>(caseData);
        workFlowCaseDataResponse.put(ProcessPbaPaymentTask.PAYMENT_STATUS, PaymentStatus.PENDING.value());
        stubFormatterServerEndpoint(workFlowCaseDataResponse);

        stubCreditAccountPayment(HttpStatus.OK, CreditAccountPaymentResponse.builder()
            .status(PaymentStatus.PENDING.value())
            .build());

        stubServiceAuthProvider(HttpStatus.OK, TEST_SERVICE_AUTH_TOKEN);

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .state(ProcessPbaPaymentTask.DEFAULT_END_STATE_FOR_NON_PBA_PAYMENTS)
            .data(caseData)
            .build();

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)));

        verifyApplicationSubmittedEmailWasSent();
    }

    @Test
    public void givenCaseData_whenSuccessPayment_AndPaymentStatusSuccess_thenReturnStateSubmitted() throws Exception {
        caseData.put(STATEMENT_OF_TRUTH, YES_VALUE);
        caseData.put(SOLICITOR_STATEMENT_OF_TRUTH, YES_VALUE);

        caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .build();

        ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        Map<String, Object> workFlowCaseDataResponse = new HashMap<>(caseData);
        workFlowCaseDataResponse.put(ProcessPbaPaymentTask.PAYMENT_STATUS, PaymentStatus.SUCCESS.value());

        stubCreditAccountPayment(HttpStatus.OK, CreditAccountPaymentResponse.builder()
            .status(PaymentStatus.SUCCESS.value())
            .build());

        stubServiceAuthProvider(HttpStatus.OK, TEST_SERVICE_AUTH_TOKEN);
        stubFormatterServerEndpoint(workFlowCaseDataResponse);

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .state(CcdStates.SUBMITTED)
            .data(caseData)
            .build();

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)));

        verifyApplicationSubmittedEmailWasSent();
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

        verifyNoInteractions(mockEmailClient);
    }

    @Test
    public void givenCaseData_whenProcessPbaPayment_403_thenReturn_CAE0001_ErrorMessage() throws Exception {
        CreditAccountPaymentResponse failedResponse = buildPaymentClientResponse("Failed", "CA-E0001");

        callPaymentClientWithStatusAndVerifyErrorMessage(
            HttpStatus.FORBIDDEN,
            expectedErrorMessage(PbaErrorMessage.CAE0001),
            buildValidCaseData(),
            failedResponse
        );
    }

    @Test
    public void givenCaseData_whenProcessPbaPayment_403_thenReturn_CAE0004_ErrorMessage() throws Exception {
        CreditAccountPaymentResponse failedResponse = buildPaymentClientResponse("Failed", "CA-E0004");

        callPaymentClientWithStatusAndVerifyErrorMessage(
            HttpStatus.FORBIDDEN,
            expectedErrorMessage(PbaErrorMessage.CAE0004),
            buildValidCaseData(),
            failedResponse
        );
    }

    @Test
    public void givenCaseData_whenProcessPbaPayment_404_thenReturnErrorMessage() throws Exception {

        callPaymentClientWithStatusAndVerifyErrorMessage(
            HttpStatus.NOT_FOUND,
            expectedErrorMessage(PbaErrorMessage.NOTFOUND),
            buildValidCaseData(),
            basicFailedResponse
        );
    }

    @Test
    public void givenCaseData_whenProcessPbaPayment_422_thenReturnErrorMessage() throws Exception {

        callPaymentClientWithStatusAndVerifyErrorMessage(
            HttpStatus.UNPROCESSABLE_ENTITY,
            expectedErrorMessage(PbaErrorMessage.GENERAL),
            buildValidCaseData(),
            basicFailedResponse
        );
    }

    @Test
    public void givenCaseData_whenProcessPbaPayment_504_thenReturnErrorMessage() throws Exception {

        callPaymentClientWithStatusAndVerifyErrorMessage(
            HttpStatus.GATEWAY_TIMEOUT,
            expectedErrorMessage(PbaErrorMessage.GENERAL),
            buildValidCaseData(),
            basicFailedResponse
        );
    }

    @Test
    public void givenCaseData_whenProcessPbaPayment_AnyOtherStatus_thenReturnErrorMessage() throws Exception {

        callPaymentClientWithStatusAndVerifyErrorMessage(
            HttpStatus.BAD_REQUEST,
            expectedErrorMessage(PbaErrorMessage.GENERAL),
            buildValidCaseData(),
            basicFailedResponse
        );
    }

    private String expectedErrorMessage(PbaErrorMessage pbaErrorMessage) {
        return formatMessage(pbaErrorMessage);
    }

    private Map<String, Object> buildValidCaseData() {
        caseData.put(STATEMENT_OF_TRUTH, YES_VALUE);
        caseData.put(SOLICITOR_STATEMENT_OF_TRUTH, YES_VALUE);
        return caseData;
    }

    private CcdCallbackRequest buildCcdCallbackRequest() {
        return CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseData(caseData)
                .caseId(TEST_CASE_ID)
                .state(TEST_STATE)
                .build())
            .build();
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

        Map<String, Object> workFlowCaseDataResponse = new HashMap<>(caseData);
        workFlowCaseDataResponse.put(ProcessPbaPaymentTask.PAYMENT_STATUS, PaymentStatus.SUCCESS.value());
        stubFormatterServerEndpoint(workFlowCaseDataResponse);

        stubCreditAccountPayment(HttpStatus.OK, CreditAccountPaymentResponse.builder()
            .status(PaymentStatus.SUCCESS.value())
            .build());
        stubServiceAuthProvider(HttpStatus.OK, TEST_SERVICE_AUTH_TOKEN);

        final CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .state(CcdStates.SUBMITTED)
            .data(caseData)
            .build();

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)));
    }

    private void callPaymentClientWithStatusAndVerifyErrorMessage(HttpStatus httpStatus,
                                                                  String expectedErrorMessage,
                                                                  Map<String, Object> caseData,
                                                                  CreditAccountPaymentResponse errorPaymentResponse)
        throws Exception {
        ccdCallbackRequest = buildCcdCallbackRequest();
        setupStubs(httpStatus, caseData, errorPaymentResponse);
        performRequestAndVerifyExpectations(expectedErrorMessage);

        verifyNoInteractions(mockEmailClient);
    }

    private void performRequestAndVerifyExpectations(String expectedErrorMessage) throws Exception {
        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(
                allOf(
                    isJson(),
                    hasJsonPath("$.data", nullValue()),
                    hasJsonPath("$.errors", hasSize(1)),
                    hasJsonPath("$.errors[0]", is(expectedErrorMessage)),
                    hasJsonPath("$.state", nullValue())
                ))
            );
    }

    private void setupStubs(HttpStatus httpStatus, Map<String, Object> caseData,
                            CreditAccountPaymentResponse errorCreditAccountPaymentResponse) {
        stubCreditAccountPayment(httpStatus, errorCreditAccountPaymentResponse);
        stubServiceAuthProvider(HttpStatus.OK, TEST_SERVICE_AUTH_TOKEN);
        stubFormatterServerEndpoint(caseData);
    }

    private void stubCreditAccountPayment(HttpStatus status, CreditAccountPaymentResponse response) {
        paymentServiceServer.stubFor(WireMock.post(PAYMENTS_CREDIT_ACCOUNT_CONTEXT_PATH)
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .withHeader(SERVICE_AUTHORIZATION_HEADER, new EqualToPattern("Bearer " + TEST_SERVICE_AUTH_TOKEN))
            .withRequestBody(equalToJson(convertObjectToJsonString(request)))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(convertObjectToJsonString(response))));
    }

    private void stubFormatterServerEndpoint(Map<String, Object> data) {
        formatterServiceServer.stubFor(WireMock.post(FORMAT_REMOVE_PETITION_DOCUMENTS_CONTEXT_PATH)
            .withRequestBody(equalToJson(convertObjectToJsonString(data)))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(convertObjectToJsonString(data))));
    }

    private void verifySolicitorApplicationSubmittedEmailWasSent() throws NotificationClientException {
        verify(mockEmailClient).sendEmail(
            eq(SOL_APPLICANT_APPLICATION_SUBMITTED_TEMPLATE_ID),
            eq(TEST_SOLICITOR_EMAIL),
            eq(ImmutableMap.<String, Object>builder()
                .put(NOTIFICATION_PET_NAME, TEST_PETITIONER_FULL_NAME)
                .put(NOTIFICATION_RESP_NAME, TEST_RESPONDENT_FULL_NAME)
                .put(NOTIFICATION_CCD_REFERENCE_KEY, TEST_CASE_ID)
                .put(NOTIFICATION_SOLICITOR_NAME, TEST_SOLICITOR_NAME)
                .build()
            ),
            anyString()
        );
    }

    private void verifyApplicationSubmittedEmailWasSent() throws NotificationClientException {
        verify(mockEmailClient).sendEmail(
            eq(APPLIC_SUBMISSION_TEMPLATE_ID),
            eq(TEST_PETITIONER_EMAIL),
            eq(
                ImmutableMap.of(
                    NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TEST_PETITIONER_LAST_NAME,
                    NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TEST_PETITIONER_FIRST_NAME,
                    NOTIFICATION_RDC_NAME_KEY, EAST_MIDLANDS_RDC,
                    NOTIFICATION_CCD_REFERENCE_KEY, formatCaseIdToReferenceNumber(TEST_CASE_ID)
                )
            ),
            anyString()
        );
    }

    private void verifyAmendedApplicationSubmittedEmailWasSent() throws NotificationClientException {
        verify(mockEmailClient).sendEmail(
            eq(APPLIC_SUBMISSION_AMEND_TEMPLATE_ID),
            eq(TEST_PETITIONER_EMAIL),
            eq(
                ImmutableMap.of(
                    NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TEST_PETITIONER_LAST_NAME,
                    NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TEST_PETITIONER_FIRST_NAME,
                    NOTIFICATION_RDC_NAME_KEY, EAST_MIDLANDS_RDC,
                    NOTIFICATION_CCD_REFERENCE_KEY, formatCaseIdToReferenceNumber(TEST_CASE_ID)
                )
            ),
            anyString()
        );
    }
}
