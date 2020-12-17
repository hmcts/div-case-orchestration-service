package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.generalreferral;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember.buildCollectionMember;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_HWF_REF;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseFieldConstants.FEE_ACCOUNT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseFieldConstants.HELP_WITH_FEE_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.FURTHER_HWF_REFERENCE_NUMBERS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.FURTHER_PBA_REFERENCE_NUMBERS;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.FeesAndPaymentHelper.buildFurtherPaymentData;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class GeneralReferralPaymentTest extends MockedFunctionalTest {

    private static final String API_URL = "/general-referral-payment";

    @Autowired
    private MockMvc webClient;

    @Test
    public void given_validData_thenReturnWithReferenceCollection_whenFeeAccount() throws Exception {

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(buildCallbackRequest(FEE_ACCOUNT_TYPE)))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.data.FeeAccountReferenceNumber", emptyOrNullString()),
                hasJsonPath("$.data.FurtherPBAReferenceNumbers", hasSize(1))
            )));
    }

    @Test
    public void given_validData_thenReturnWithUpdatedReferenceCollection_whenFeeAccount() throws Exception {

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(buildCallbackRequestWithExistingCollection(FEE_ACCOUNT_TYPE)))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.data.FeeAccountReferenceNumber", emptyOrNullString()),
                hasJsonPath("$.data.FurtherPBAReferenceNumbers", hasSize(2))
            )));
    }

    @Test
    public void given_validData_thenReturnWithReferenceCollection_whenHWFPayment() throws Exception {

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(buildCallbackRequest(HELP_WITH_FEE_TYPE)))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.data.HelpWithFeesReferenceNumber", emptyOrNullString()),
                hasJsonPath("$.data.FurtherHWFReferenceNumbers", hasSize(1))
            )));
    }

    @Test
    public void given_validData_thenReturnWithUpdatedReferenceCollection_whenHWFPayment() throws Exception {

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(buildCallbackRequestWithExistingCollection(HELP_WITH_FEE_TYPE)))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.data.HelpWithFeesReferenceNumber", emptyOrNullString()),
                hasJsonPath("$.data.FurtherHWFReferenceNumbers", hasSize(2))
            )));
    }

    @Test
    public void given_invalidData_thenReturn_withNoChange() throws Exception {
        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(buildCallbackRequestWithNoPaymentData()))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasNoJsonPath("$.data.FurtherPBAReferenceNumbers"),
                hasNoJsonPath("$.data.FurtherHWFReferenceNumbers"))
            ));
    }

    private CcdCallbackRequest buildCallbackRequest(String paymentType) {
        Map<String, Object> caseData = buildFurtherPaymentData(paymentType, TEST_HWF_REF);
        caseData.put(CcdFields.GENERAL_REFERRAL_PAYMENT_TYPE, paymentType);

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
        Map<String, Object> caseData = buildFurtherPaymentData(paymentType, TEST_HWF_REF);
        caseData.put(CcdFields.GENERAL_REFERRAL_PAYMENT_TYPE, paymentType);

        List<CollectionMember<ReferenceNumber>> hwfReferenceNumbers = Arrays.asList(
            buildCollectionMember(ReferenceNumber.builder()
                .reference("HWF-098-765")
                .build()));

        caseData.put(getPaymentCollectionProperty(paymentType), hwfReferenceNumbers);

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(caseData)
            .build();

        return CcdCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
    }

    private String getPaymentCollectionProperty(String paymentType) {
        return HELP_WITH_FEE_TYPE.equals(paymentType) ? FURTHER_HWF_REFERENCE_NUMBERS : FURTHER_PBA_REFERENCE_NUMBERS;
    }
}
