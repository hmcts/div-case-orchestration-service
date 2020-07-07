package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseLink;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
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
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PREVIOUS_CASE_ID_CCD_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_APPLICATION_FEE_IN_POUNDS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class GetPetitionIssueFeesITest extends MockedFunctionalTest {

    private static final String API_URL = "/petition-issue-fees";
    private static final String PETITION_ISSUE_FEE_CONTEXT_PATH = "/fees-and-payments/version/1/petition-issue-fee";
    private static final String PETITION_AMENDMENT_ISSUE_FEE_CONTEXT_PATH = "/fees-and-payments/version/1/amend-fee";
    private static final String ADD_PETITIONER_SOLICITOR_ROLE = String
        .format("/casemaintenance/version/1/add-petitioner-solicitor-role/%s", TEST_CASE_ID);

    private CcdCallbackRequest callbackRequest;

    @Autowired
    private MockMvc webClient;

    @Before
    public void setUp() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .build();
        callbackRequest = CcdCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
    }

    @Test
    public void givenCaseData_whenGetPetitionIssueFee_thenReturnUpdatedResponseWithFees() throws Exception {
        FeeResponse feeResponse = FeeResponse.builder()
            .amount(550d)
            .feeCode(TEST_FEE_CODE)
            .version(TEST_FEE_VERSION)
            .description(TEST_FEE_DESCRIPTION)
            .build();

        stubGetFeeFromFeesAndPayments(feeResponse, false);
        stubMaintenanceServerEndpointForAddPetitionerSolicitorRole(HttpStatus.OK);

        OrderSummary orderSummary = new OrderSummary();
        orderSummary.add(feeResponse);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder()
            .data(ImmutableMap.of(
                PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY, orderSummary,
                SOL_APPLICATION_FEE_IN_POUNDS_JSON_KEY, "550"
            ))
            .build();

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(callbackRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expectedResponse)));
    }

    @Test
    public void givenCaseAmendment_whenGetPetitionIssueFee_thenReturnUpdatedResponseWithFees() throws Exception {
        callbackRequest.getCaseDetails().getCaseData().put(PREVIOUS_CASE_ID_CCD_KEY, new CaseLink("1234567890123456"));

        FeeResponse feeResponse = FeeResponse.builder()
            .amount(95d)
            .feeCode(TEST_FEE_CODE)
            .version(TEST_FEE_VERSION)
            .description(TEST_FEE_DESCRIPTION)
            .build();

        stubGetFeeFromFeesAndPayments(feeResponse, true);
        stubMaintenanceServerEndpointForAddPetitionerSolicitorRole(HttpStatus.OK);

        OrderSummary orderSummary = new OrderSummary();
        orderSummary.add(feeResponse);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder()
            .data(ImmutableMap.of(
                PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY, orderSummary,
                SOL_APPLICATION_FEE_IN_POUNDS_JSON_KEY, "95"
            ))
            .build();

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(callbackRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expectedResponse)));
    }

    @Test
    public void givenCaseData_whenAddingCaseRoleFails_thenReturnErrorResponse() throws Exception {
        FeeResponse feeResponse = FeeResponse.builder()
            .amount(TEST_FEE_AMOUNT)
            .feeCode(TEST_FEE_CODE)
            .version(TEST_FEE_VERSION)
            .description(TEST_FEE_DESCRIPTION)
            .build();

        stubGetFeeFromFeesAndPayments(feeResponse, false);
        stubMaintenanceServerEndpointForAddPetitionerSolicitorRole(HttpStatus.BAD_GATEWAY);

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .errors(Collections.singletonList("Problem setting the [PETSOLICITOR] role to the case"))
            .build();

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(callbackRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)));
    }

    @Test
    public void givenUnauthorizedRequest_whenGetPetitionIssueFees_thenReturnErrorData() throws Exception {
        FeeResponse feeResponse = FeeResponse.builder()
            .amount(TEST_FEE_AMOUNT)
            .feeCode(TEST_FEE_CODE)
            .version(TEST_FEE_VERSION)
            .description(TEST_FEE_DESCRIPTION)
            .build();

        stubMaintenanceServerEndpointForAddPetitionerSolicitorRole(HttpStatus.FORBIDDEN);
        stubGetFeeFromFeesAndPayments(feeResponse, false);

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .errors(Collections.singletonList("Problem setting the [PETSOLICITOR] role to the case"))
            .build();

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(callbackRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)));
    }

    private void stubGetFeeFromFeesAndPayments(FeeResponse feeResponse, boolean petitionAmendment) {
        String url = petitionAmendment ? PETITION_AMENDMENT_ISSUE_FEE_CONTEXT_PATH : PETITION_ISSUE_FEE_CONTEXT_PATH;
        feesAndPaymentsServer.stubFor(WireMock.get(url)
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(convertObjectToJsonString(feeResponse))));
    }

    private void stubMaintenanceServerEndpointForAddPetitionerSolicitorRole(HttpStatus status) {
        maintenanceServiceServer.stubFor(put(ADD_PETITIONER_SOLICITOR_ROLE)
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)));
    }

}