package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.generalreferral;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.MockedFunctionalTest;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.util.Collections;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.time.LocalDate.now;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class GeneralConsiderationTest extends MockedFunctionalTest {

    private static final String API_URL = "/general-consideration";

    @Autowired
    private MockMvc webClient;

    @Test
    public void givenCaseData_whenGeneralConsiderationCalled_thenFieldIsSetup() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(
                CaseDetails.builder()
                    .caseData(Collections.emptyMap())
                    .caseId(TEST_CASE_ID)
                    .build()
            )
            .build();

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(
                allOf(
                    isJson(),
                    hasJsonPath(
                        "$.data.GeneralReferralDecisionDate",
                        is(DateUtils.formatDateFromLocalDate(now()))
                    )
                ))
            );
    }
}
