package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.generalreferrals;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.MockedFunctionalTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.fail;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class GeneralReferralTest extends MockedFunctionalTest {

    private static final String API_URL = "/general-referral";

    @Autowired
    private MockMvc webClient;

    @Test
    public void testGeneralReferralEndpointCanBeReached() throws Exception { // TODO refactor out of existence
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    // 1.)
    // if 'GeneralReferralFee' as 'Yes' -> State: AwaitingGeneralReferralPayment
    @Test
    @Ignore
    public void givenCaseData_whenGeneralReferralFee_Yes_ThenStateAwaitingGeneralReferralPayment(){
        fail("Not yet implemented");
    }

    // 2.)
    // if 'GeneralReferralFee' as 'No' -> State: AwaitingGeneralConsideration
    @Test
    @Ignore
    public void givenCaseData_whenGeneralReferralFee_No_ThenStateAwaitingGeneralConsideration(){
        fail("Not yet implemented");
    }


    // TODO 3.)
    // if State: AwaitingGeneralReferralPayment and 'GeneralReferralFee' as 'No' -> State: AwaitingGeneralConsideration
    @Test
    @Ignore
    public void givenCaseAsAwaitingGeneralReferralPayment_WhenGeneralReferralFeeNo_ThenStateAwaitingGeneralConsideration(){
        fail("Not yet implemented");
    }

    // TODO 4.)
    // if State: AwaitingGeneralConsideration and 'GeneralReferralFee' as 'Yes' -> State: AwaitingGeneralReferralPayment
    @Test
    @Ignore
    public void giveCaseAsAwaitingGeneralConsideration_WhenGeneralReferralFeeYes_ThenStateAwaitingGeneralReferralPayment(){
        fail("Not yet implemented");
    }
}
