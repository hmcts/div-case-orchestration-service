package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;

import java.util.Collections;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_AMOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_DESCRIPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_VERSION;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
public class GetPetitionIssueFeesITest extends MockedFunctionalTest {
    private static final String API_URL = "/petition-issue-fees";
    private static final String PETITION_ISSUE_FEE_CONTEXT_PATH = "/fees-and-payments/version/1/petition-issue-fee";

    private static final Map<String, Object> CASE_DATA = Collections.emptyMap();
    private static final CaseDetails CASE_DETAILS =
            CaseDetails.builder()
                    .caseData(CASE_DATA)
                    .caseId(TEST_CASE_ID)
                    .state(TEST_STATE)
                    .build();

    private static final CcdCallbackRequest CREATE_EVENT = CcdCallbackRequest.builder()
            .caseDetails(CASE_DETAILS)
            .build();

    @Autowired
    private MockMvc webClient;

    @Test
    public void givenCaseData_whenGetPetitionIssueFee_thenReturnUpdatedResponseWithFees() throws Exception {
        FeeResponse feeResponse = FeeResponse.builder()
                .amount(TEST_FEE_AMOUNT)
                .feeCode(TEST_FEE_CODE)
                .version(TEST_FEE_VERSION)
                .description(TEST_FEE_DESCRIPTION)
                .build();

        stubGetFeeFromFeesAndPayments(HttpStatus.OK, feeResponse);

        OrderSummary orderSummary = new OrderSummary();
        orderSummary.add(feeResponse);

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
                .data(Collections.singletonMap(PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY, orderSummary))
                .build();

        webClient.perform(post(API_URL)
                .content(convertObjectToJsonString(CREATE_EVENT))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(convertObjectToJsonString(expected)));
    }

    private void stubGetFeeFromFeesAndPayments(HttpStatus status, FeeResponse feeResponse) {
        feesAndPaymentsServer.stubFor(WireMock.get(PETITION_ISSUE_FEE_CONTEXT_PATH)
                .willReturn(aResponse()
                        .withStatus(status.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBody(convertObjectToJsonString(feeResponse))));
    }
}
