package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.decreeabsolute;

import com.jayway.jsonpath.JsonPath;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.hamcrest.HamcrestArgumentMatcher;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;

import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.skyscreamer.jsonassert.JSONCompareMode.LENIENT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATE_CASE_NO_LONGER_ELIGIBLE_FOR_DA_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATE_RESPONDENT_ELIGIBLE_FOR_DA_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ProcessApplicantDAEligibilityTest {

    private static final String API_URL = "/process-applicant-da-eligibility";
    private static final String NOTIFICATION_TEMPLATE_ID = "71fd2e7e-42dc-4dcf-a9bb-007ae9d4b27f";

    @Autowired
    private MockMvc webClient;

    @MockBean
    private EmailClient mockClient;

    @Test
    public void shouldReturnCaseData_WhenCalling_GrantDecreeAbsolute() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
            "/jsonExamples/payloads/genericPetitionerData.json", CcdCallbackRequest.class);
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
        verify(mockClient).sendEmail(eq(NOTIFICATION_TEMPLATE_ID),
            eq("petitioner@divorce.co.uk"),
            argThat(new HamcrestArgumentMatcher<Map<String, Object>>(allOf(
                hasEntry(NOTIFICATION_CASE_NUMBER_KEY, "LV80D85000"),
                hasEntry(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, "Ted"),
                hasEntry(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, "Jones")
            ))),
            any());
    }

}