package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.solicitor.dn.rejectandamend;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.divorce.model.ccd.CaseLink;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;

import java.util.Collections;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PETITIONER_SOLICITOR_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PREVIOUS_CASE_ID_CCD_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_APPLICATION_FEE_IN_POUNDS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.CaseDataTestHelper.buildOrganisationPolicy;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@SpringBootTest(properties = {"feature-toggle.toggle.share_a_case=true"})
public class GetPetitionIssueFeesTestShareCaseFeatureSwitchedOnTest extends GetPetitionIssueFeesAbstractTest {

    @Before
    public void modifySetUp() {
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getCaseData();
        caseData.put(PETITIONER_SOLICITOR_ORGANISATION_POLICY, buildOrganisationPolicy());
        callbackRequest.getCaseDetails().setCaseData(caseData);
    }

    @Test
    public void givenCaseData_whenGetPetitionIssueFee_thenReturnUpdatedResponseWithFees() throws Exception {
        callbackRequest.getCaseDetails().setCaseId(TEST_CASE_ID);

        stubUserDetailsEndpoint(HttpStatus.OK, AUTH_TOKEN, USER_DETAILS_PIN_USER_JSON);
        stubServiceAuthProvider(HttpStatus.OK, TEST_SERVICE_AUTH_TOKEN);
        stubRemoveCaseRoleServerEndpoint(AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, HttpStatus.OK);
        stubAssignCaseAccessServerEndpoint(AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, HttpStatus.OK);

        OrderSummary orderSummary = new OrderSummary();
        orderSummary.add(issueFeeResponse);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder()
            .data(ImmutableMap.of(PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY, orderSummary))
            .build();

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(callbackRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expectedResponse)))
            .andExpect(content().string(hasNoJsonPath("data." + SOL_APPLICATION_FEE_IN_POUNDS_JSON_KEY)));
    }

    @Test
    public void givenCaseAmendment_whenGetPetitionIssueFee_thenReturnUpdatedResponseWithFees() throws Exception {
        callbackRequest.getCaseDetails().getCaseData().put(PREVIOUS_CASE_ID_CCD_KEY, CaseLink.builder().caseReference("1234567890123456").build());

        stubUserDetailsEndpoint(HttpStatus.OK, AUTH_TOKEN, USER_DETAILS_PIN_USER_JSON);
        stubServiceAuthProvider(HttpStatus.OK, TEST_SERVICE_AUTH_TOKEN);
        stubRemoveCaseRoleServerEndpoint(AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, HttpStatus.OK);
        stubAssignCaseAccessServerEndpoint(AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, HttpStatus.OK);

        OrderSummary orderSummary = new OrderSummary();
        orderSummary.add(issueFeeResponse);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder()
            .data(ImmutableMap.of(PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY, orderSummary))
            .build();

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(callbackRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expectedResponse)))
            .andExpect(content().string(hasNoJsonPath("data." + SOL_APPLICATION_FEE_IN_POUNDS_JSON_KEY)));
    }

    @Test
    public void givenCaseData_whenRemovingCaseRoleFails_thenReturnErrorResponse() throws Exception {
        stubUserDetailsEndpoint(HttpStatus.OK, AUTH_TOKEN, USER_DETAILS_PIN_USER_JSON);
        stubServiceAuthProvider(HttpStatus.OK, TEST_SERVICE_AUTH_TOKEN);
        stubRemoveCaseRoleServerEndpoint(AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, HttpStatus.BAD_GATEWAY);
        stubAssignCaseAccessServerEndpoint(AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, HttpStatus.OK);

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .errors(Collections.singletonList("Problem calling assign case access API to set the [PETSOLICITOR] role to the case"))
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
    public void givenCaseData_whenAddingCaseRoleFails_thenReturnErrorResponse() throws Exception {
        stubUserDetailsEndpoint(HttpStatus.OK, AUTH_TOKEN, USER_DETAILS_PIN_USER_JSON);
        stubServiceAuthProvider(HttpStatus.OK, TEST_SERVICE_AUTH_TOKEN);
        stubRemoveCaseRoleServerEndpoint(AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, HttpStatus.OK);
        stubAssignCaseAccessServerEndpoint(AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, HttpStatus.BAD_GATEWAY);

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .errors(Collections.singletonList("Problem calling assign case access API to set the [PETSOLICITOR] role to the case"))
            .build();

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(callbackRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)));
    }
}
