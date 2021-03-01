package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.servicejourney;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.ReferenceNumber;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.MockedFunctionalTest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.util.Collections.emptyMap;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember.buildCollectionMember;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PBA_REF;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseFieldConstants.FEE_ACCOUNT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseFieldConstants.HELP_WITH_FEE_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.FeesAndPaymentHelper.buildFurtherPaymentData;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.FeesAndPaymentHelper.getPaymentCollectionProperty;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class ConfirmServicePaymentTest extends MockedFunctionalTest {

    private static final String API_URL = "/confirm-service-payment";

    @Autowired
    private MockMvc webClient;

    @Test
    public void given_validData_thenReturnWithReferenceCollection_whenFeeAccount() throws Exception {

        CcdCallbackRequest ccdCallbackRequest = buildCallbackRequest(FEE_ACCOUNT_TYPE);

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(commonNewPbaCollectionExpectations());
    }

    private ResultMatcher commonNewPbaCollectionExpectations() {
        return content().string(allOf(
            isJson(),
            hasJsonPath("$.data.FeeAccountReferenceNumber", emptyOrNullString()),
            hasJsonPath("$.data.FurtherPBAReferenceNumbers", hasSize(1))
        ));
    }

    @Test
    public void given_validData_thenReturnWithUpdatedReferenceCollection_whenFeeAccount() throws Exception {

        CcdCallbackRequest ccdCallbackRequest = buildCallbackRequestWithExistingCollection(FEE_ACCOUNT_TYPE);

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(commonExistingPbaCollectionExpectations());
    }

    private ResultMatcher commonExistingPbaCollectionExpectations() {
        return content().string(allOf(
            isJson(),
            hasJsonPath("$.data.FeeAccountReferenceNumber", emptyOrNullString()),
            hasJsonPath("$.data.FurtherPBAReferenceNumbers", hasSize(2))
        ));
    }

    @Test
    public void given_validData_thenReturnWithReferenceCollection_whenHWFPayment() throws Exception {

        CcdCallbackRequest ccdCallbackRequest = buildCallbackRequest(HELP_WITH_FEE_TYPE);

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(commonNewHwfCollectionExpectations());
    }

    private ResultMatcher commonNewHwfCollectionExpectations() {
        return content().string(allOf(
            isJson(),
            hasJsonPath("$.data.HelpWithFeesReferenceNumber", emptyOrNullString()),
            hasJsonPath("$.data.FurtherHWFReferenceNumbers", hasSize(1))
        ));
    }

    @Test
    public void given_validData_thenReturnWithUpdatedReferenceCollection_whenHWFPayment() throws Exception {

        CcdCallbackRequest ccdCallbackRequest = buildCallbackRequestWithExistingCollection(HELP_WITH_FEE_TYPE);

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(commonExistingHwfCollectionExpectations());
    }

    private ResultMatcher commonExistingHwfCollectionExpectations() {
        return content().string(allOf(
            isJson(),
            hasJsonPath("$.data.HelpWithFeesReferenceNumber", emptyOrNullString()),
            hasJsonPath("$.data.FurtherHWFReferenceNumbers", hasSize(2))
        ));
    }

    @Test
    public void given_invalidData_thenReturn_withNoChange() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = buildCallbackRequestWithNoPaymentData();

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(commonNoCollectionChangeExpectations());
    }

    private ResultMatcher commonNoCollectionChangeExpectations() {
        return content().string(allOf(
            isJson(),
            hasNoJsonPath("$.data.FurtherPBAReferenceNumbers"),
            hasNoJsonPath("$.data.FurtherHWFReferenceNumbers"))
        );
    }

    private CcdCallbackRequest buildCallbackRequest(String paymentType) {
        Map<String, Object> caseData = buildFurtherPaymentData(paymentType, TEST_PBA_REF);
        caseData.put(SERVICE_APPLICATION_PAYMENT, paymentType);

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(caseData)
            .build();

        return CcdCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
    }

    private CcdCallbackRequest buildCallbackRequestWithNoPaymentData() {
        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(emptyMap())
            .build();

        return CcdCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
    }

    private CcdCallbackRequest buildCallbackRequestWithExistingCollection(String paymentType) {
        Map<String, Object> caseData = buildFurtherPaymentData(paymentType, TEST_PBA_REF);
        caseData.put(SERVICE_APPLICATION_PAYMENT, paymentType);

        List<CollectionMember<ReferenceNumber>> pbaReferenceNumbers = Arrays.asList(
            buildCollectionMember(ReferenceNumber.builder()
                .reference("PBA123456")
                .build()));

        caseData.put(getPaymentCollectionProperty(paymentType), pbaReferenceNumbers);

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(caseData)
            .build();

        return CcdCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
    }
}
