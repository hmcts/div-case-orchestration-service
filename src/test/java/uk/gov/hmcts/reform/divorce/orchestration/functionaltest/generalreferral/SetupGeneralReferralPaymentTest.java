package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.generalreferral;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.IdamTestSupport;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.service.GeneralReferralService;

import java.util.HashMap;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_REFERRAL_WITHOUT_NOTICE_FEE_SUMMARY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.FeesAndPaymentHelper.buildExpectedResponse;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.FeesAndPaymentHelper.getApplicationWithoutNoticeFee;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.FeesAndPaymentHelper.stubGetFeeFromFeesAndPayments;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class SetupGeneralReferralPaymentTest extends IdamTestSupport {

    private static final String API_URL = "/set-up-order-summary/without-notice-fee";

    @Autowired
    private MockMvc webClient;

    @SpyBean
    private GeneralReferralService generalReferralService;

    @Test
    public void shouldPopulateGeneralApplicationWithoutNoticeFeeSummaryInResponse() throws Exception {
        CcdCallbackRequest input = buildRequest();
        FeeResponse applicationWithoutNoticeFee = getApplicationWithoutNoticeFee();
        CcdCallbackResponse expectedResponse = buildExpectedResponse(
            applicationWithoutNoticeFee,
            GENERAL_REFERRAL_WITHOUT_NOTICE_FEE_SUMMARY
        );

        stubGetFeeFromFeesAndPayments(feesAndPaymentsServer, applicationWithoutNoticeFee);

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(input))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expectedResponse)));
    }

    @Test
    public void givenServiceThrowsCaseOrchestrationServiceException_ThenErrorMessagesShouldBeReturned()
        throws Exception {
        doThrow(new CaseOrchestrationServiceException("My error message."))
            .when(generalReferralService).setupGeneralReferralPaymentEvent(any());

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(CcdCallbackRequest.builder().build()))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(hasJsonPath("$.errors", hasItem("My error message."))));
    }

    private CcdCallbackRequest buildRequest() {
        return new CcdCallbackRequest(
            AUTH_TOKEN,
            "",
            CaseDetails.builder().caseData(new HashMap<>()).caseId(TEST_CASE_ID).build()
        );
    }
}
