package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.decreeabsolute;

import com.jayway.jsonpath.JsonPath;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.MockedFunctionalTest;

import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.skyscreamer.jsonassert.JSONCompareMode.LENIENT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATE_CASE_NO_LONGER_ELIGIBLE_FOR_DA_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATE_RESPONDENT_ELIGIBLE_FOR_DA_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

public class ProcessApplicantDAEligibilityTest extends MockedFunctionalTest {

    private static final String API_URL = "/process-applicant-da-eligibility";
    private static final String GENERIC_PETITIONER_DATA_JSON = "/jsonExamples/payloads/genericPetitionerData.json";

    @Autowired
    private MockMvc webClient;

    @Test
    public void shouldReturnCaseData_WhenCalling_GrantDecreeAbsoluteForPetitioner() throws Exception {

        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(GENERIC_PETITIONER_DATA_JSON, CcdCallbackRequest.class);

        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();
        caseData.put(DECREE_NISI_GRANTED_DATE_CCD_FIELD, "2019-03-15");
        String jsonResponse = webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThat(jsonResponse, allOf(
            isJson(),
            hasJsonPath("$.data", allOf(
                hasJsonPath(DATE_RESPONDENT_ELIGIBLE_FOR_DA_CCD_FIELD, equalTo("2019-07-28")),
                hasJsonPath(DATE_CASE_NO_LONGER_ELIGIBLE_FOR_DA_CCD_FIELD, equalTo("2020-03-15"))
            )),
            hasJsonPath("$.warnings", is(nullValue())),
            hasJsonPath("$.errors", is(nullValue()))
        ));
        String responseCaseData = convertObjectToJsonString(JsonPath.read(jsonResponse, "data"));
        JSONAssert.assertEquals(convertObjectToJsonString(caseData), responseCaseData, LENIENT);
    }
}