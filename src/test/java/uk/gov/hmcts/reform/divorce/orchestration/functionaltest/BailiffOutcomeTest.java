package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.IdamTestSupport;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.BAILIFF_SERVICE_SUCCESSFUL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.CERTIFICATE_OF_SERVICE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class BailiffOutcomeTest extends IdamTestSupport {

    private static final String API_URL = "/add-bailiff-return";
    private static final Integer BAILIFF_SUCCESS_DUE_DATE_OFFSET = 7;
    private static final Integer BAILIFF_UNSUCCESS_DUE_DATE_OFFSET = 30;
    private static final String COS_DATE = "2020-11-10";

    @Autowired
    private MockMvc webClient;

    @Test
    public void givenBailiffSuccessful_whenCalledEndpoint_thenCoSDueDateFieldIsPopulated() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(BAILIFF_SERVICE_SUCCESSFUL, YES_VALUE);
        caseData.put(CERTIFICATE_OF_SERVICE_DATE, COS_DATE);
        CcdCallbackRequest input = new CcdCallbackRequest(
                AUTH_TOKEN,
                "addBailiffReturn",
                CaseDetails.builder().caseData(caseData).caseId(TEST_CASE_ID).build()
        );

        String date = LocalDate.parse((String) caseData.get(CERTIFICATE_OF_SERVICE_DATE))
                .plusDays(BAILIFF_SUCCESS_DUE_DATE_OFFSET).toString();

        webClient.perform(post(API_URL)
                .header(AUTHORIZATION, AUTH_TOKEN)
                .content(convertObjectToJsonString(input))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        isJson(),
                        hasJsonPath("$.data", allOf(
                                hasJsonPath("dueDate", is(date))
                        )),
                        hasNoJsonPath("$.errors"),
                        hasNoJsonPath("$.warnings")
                )));
    }

    @Test
    public void givenBailiffUnsuccessful_whenCalledEndpoint_thenCoSDueDateFieldIsPopulated() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(BAILIFF_SERVICE_SUCCESSFUL, NO_VALUE);
        caseData.put(CERTIFICATE_OF_SERVICE_DATE, COS_DATE);
        CcdCallbackRequest input = new CcdCallbackRequest(
                AUTH_TOKEN,
                "addBailiffReturn",
                CaseDetails.builder().caseData(caseData).caseId(TEST_CASE_ID).build()
        );

        String date = LocalDate.parse((String) caseData.get(CERTIFICATE_OF_SERVICE_DATE))
                .plusDays(BAILIFF_UNSUCCESS_DUE_DATE_OFFSET).toString();

        webClient.perform(post(API_URL)
                .header(AUTHORIZATION, AUTH_TOKEN)
                .content(convertObjectToJsonString(input))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        isJson(),
                        hasJsonPath("$.data", allOf(
                                hasJsonPath("dueDate", is(date))
                        )),
                        hasNoJsonPath("$.errors"),
                        hasNoJsonPath("$.warnings")
                )));
    }
}
