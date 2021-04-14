package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.hamcrest.Matcher;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ADDED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CERTIFICATE_OF_SERVICE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_DECISION_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_MY_REASON;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RECEIVED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_APPLICATION_PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.BAILIFF_SERVICE_SUCCESSFUL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.CERTIFICATE_OF_SERVICE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.BailiffServiceApplicationDataTaskTest.TEST_COURT_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.BailiffServiceApplicationDataTaskTest.TEST_COURT_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.BailiffServiceApplicationDataTaskTest.TEST_REASON_FAILURE_TO_SERVE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class BailiffOutcomeTest extends IdamTestSupport {

    private static final String API_URL = "/add-bailiff-return";

    private static final Integer BAILIFF_SUCCESS_DUE_DATE_OFFSET = 7;
    private static final Integer BAILIFF_UNSUCCESS_DUE_DATE_OFFSET = 30;

    @Autowired
    private MockMvc webClient;

    private CcdCallbackRequest buildRequest(String bailiffServiceSuccess) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(BAILIFF_SERVICE_SUCCESSFUL, bailiffServiceSuccess);
        caseData.put(CERTIFICATE_OF_SERVICE_DATE, TEST_CERTIFICATE_OF_SERVICE_DATE);

        caseData.put(CcdFields.RECEIVED_SERVICE_APPLICATION_DATE, TEST_RECEIVED_DATE);
        caseData.put(CcdFields.SERVICE_APPLICATION_DECISION_DATE, TEST_DECISION_DATE);
        caseData.put(CcdFields.RECEIVED_SERVICE_ADDED_DATE, TEST_ADDED_DATE);
        caseData.put(CcdFields.SERVICE_APPLICATION_TYPE, ApplicationServiceTypes.BAILIFF);
        caseData.put(CcdFields.SERVICE_APPLICATION_PAYMENT, TEST_SERVICE_APPLICATION_PAYMENT);
        caseData.put(CcdFields.SERVICE_APPLICATION_REFUSAL_REASON, TEST_MY_REASON);
        caseData.put(CcdFields.SERVICE_APPLICATION_GRANTED, YES_VALUE);
        caseData.put(CcdFields.BAILIFF_APPLICATION_GRANTED, YES_VALUE);

        caseData.put(CcdFields.LOCAL_COURT_ADDRESS, TEST_COURT_ADDRESS);
        caseData.put(CcdFields.LOCAL_COURT_EMAIL, TEST_COURT_EMAIL);
        caseData.put(CcdFields.REASON_FAILURE_TO_SERVE, TEST_REASON_FAILURE_TO_SERVE);

        return new CcdCallbackRequest(
            AUTH_TOKEN,
            "addBailiffReturn",
            CaseDetails.builder().caseData(caseData).caseId(TEST_CASE_ID).build()
        );
    }

    @Test
    public void givenBailiffSuccessful_whenCalledEndpoint_thenCoSDueDateFieldIsPopulated() throws Exception {
        CcdCallbackRequest input = buildRequest(YES_VALUE);

        String date = LocalDate.parse((String) input.getCaseDetails().getCaseData()
            .get(CERTIFICATE_OF_SERVICE_DATE))
            .plusDays(BAILIFF_SUCCESS_DUE_DATE_OFFSET).toString();

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(input))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(
                allOf(
                    isJson(),
                    hasJsonPath("$.data.dueDate", is(date)),
                    hasJsonPath("$.data.ServiceApplications", hasSize(1)),
                    hasLastBailiffServiceApplication(YES_VALUE),
                    hasNoCcdFieldsThatShouldBeRemoved(),
                    hasNoJsonPath("$.errors"),
                    hasNoJsonPath("$.warnings")
                )
            ));
    }

    @Test
    public void givenBailiffUnsuccessful_whenCalledEndpoint_thenCoSDueDateFieldIsPopulated() throws Exception {
        CcdCallbackRequest input = buildRequest(NO_VALUE);

        String date = LocalDate.parse((String) input.getCaseDetails().getCaseData()
            .get(CERTIFICATE_OF_SERVICE_DATE))
            .plusDays(BAILIFF_UNSUCCESS_DUE_DATE_OFFSET).toString();

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(input))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(
                allOf(
                    isJson(),
                    hasJsonPath("$.data.dueDate", is(date)),
                    hasJsonPath("$.data.ServiceApplications", hasSize(1)),
                    hasLastBailiffServiceApplication(NO_VALUE),
                    hasNoCcdFieldsThatShouldBeRemoved(),
                    hasNoJsonPath("$.errors"),
                    hasNoJsonPath("$.warnings")
                )
            ));
    }

    private Matcher<String> hasLastBailiffServiceApplication(String success) {
        String pattern = "$.data.LastServiceApplication.%s";

        return allOf(
            hasJsonPath(String.format(pattern, "ReceivedDate", is(TEST_RECEIVED_DATE))),
            hasJsonPath(String.format(pattern, "AddedDate"), is(TEST_ADDED_DATE)),
            hasJsonPath(String.format(pattern, "Type"), is(ApplicationServiceTypes.BAILIFF)),
            hasJsonPath(String.format(pattern, "Payment"), is(TEST_SERVICE_APPLICATION_PAYMENT)),
            hasJsonPath(String.format(pattern, "DecisionDate"), is(TEST_DECISION_DATE)),
            hasJsonPath(String.format(pattern, "RefusalReason"), is(TEST_MY_REASON)),
            hasJsonPath(String.format(pattern, "ApplicationGranted"), is(YES_VALUE)),
            hasJsonPath(String.format(pattern, "BailiffApplicationGranted"), is(YES_VALUE)),
            hasJsonPath(String.format(pattern, "LocalCourtAddress"), is(TEST_COURT_ADDRESS)),
            hasJsonPath(String.format(pattern, "LocalCourtEmail"), is(TEST_COURT_EMAIL)),
            hasJsonPath(String.format(pattern, "CertificateOfServiceDate"), is(TEST_CERTIFICATE_OF_SERVICE_DATE)),
            hasJsonPath(String.format(pattern, "SuccessfulServedByBailiff"), is(success)),
            hasJsonPath(String.format(pattern, "ReasonFailureToServe"), is(TEST_REASON_FAILURE_TO_SERVE))
        );
    }

    private Matcher<? super Object> hasNoCcdFieldsThatShouldBeRemoved() {
        String pathPattern = "$.data.%s";

        return allOf(
            hasNoJsonPath(String.format(pathPattern, CcdFields.SERVICE_REFUSAL_DRAFT)),
            hasNoJsonPath(String.format(pathPattern, CcdFields.RECEIVED_SERVICE_APPLICATION_DATE)),
            hasNoJsonPath(String.format(pathPattern, CcdFields.RECEIVED_SERVICE_ADDED_DATE)),
            hasNoJsonPath(String.format(pathPattern, CcdFields.SERVICE_APPLICATION_TYPE)),
            hasNoJsonPath(String.format(pathPattern, CcdFields.SERVICE_APPLICATION_PAYMENT)),
            hasNoJsonPath(String.format(pathPattern, CcdFields.SERVICE_APPLICATION_GRANTED)),
            hasNoJsonPath(String.format(pathPattern, CcdFields.BAILIFF_APPLICATION_GRANTED)),
            hasNoJsonPath(String.format(pathPattern, CcdFields.SERVICE_APPLICATION_DECISION_DATE)),
            hasNoJsonPath(String.format(pathPattern, CcdFields.SERVICE_APPLICATION_REFUSAL_REASON)),
            hasNoJsonPath(String.format(pathPattern, CcdFields.LOCAL_COURT_ADDRESS)),
            hasNoJsonPath(String.format(pathPattern, CcdFields.LOCAL_COURT_EMAIL)),
            hasNoJsonPath(String.format(pathPattern, CcdFields.CERTIFICATE_OF_SERVICE_DATE)),
            hasNoJsonPath(String.format(pathPattern, CcdFields.BAILIFF_SERVICE_SUCCESSFUL)),
            hasNoJsonPath(String.format(pathPattern, CcdFields.REASON_FAILURE_TO_SERVE))
        );
    }
}
