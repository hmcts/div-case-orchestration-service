package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.alternativeservice;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.IdamTestSupport;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.DateCalculator;

import java.util.HashMap;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class ConfirmProcessServerServiceTest extends IdamTestSupport {

    private static final String API_URL = "/confirm-process-server-service";
    private static final Integer DUE_DATE_OFFSET = 7;

    @Autowired
    private MockMvc webClient;

    @Test
    public void givenCaseData_whenCalledEndpoint_thenDueDateFieldIsPopulatedAndFlagsAreSet() throws Exception {
        CcdCallbackRequest input = new CcdCallbackRequest(
            AUTH_TOKEN,
            "confirmProcessServerService",
            CaseDetails.builder().caseData(new HashMap<>()).caseId(TEST_CASE_ID).build()
        );

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(input))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.data", allOf(
                    hasJsonPath("dueDate", is(DateCalculator.getDateWithOffset(DUE_DATE_OFFSET))),
                    hasJsonPath("ServedByProcessServer", is(YES_VALUE)),
                    hasJsonPath("ServedByAlternativeMethod", is(NO_VALUE))
                )),
                hasNoJsonPath("$.errors"),
                hasNoJsonPath("$.warnings")
            )));
    }
}
