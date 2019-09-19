package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.Pin;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.PinRequest;

import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.BEARER_AUTH_TOKEN_1;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_LETTER_HOLDER_ID_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

public class RespondentSolicitorNominatedITest extends IdamTestSupport {

    private static final String API_URL = "/aos-solicitor-nominated";
    private static final String AOS_SOL_NOMINATED_JSON = "/jsonExamples/payloads/aosSolicitorNominated.json";

    @Autowired
    private MockMvc webClient;

    @Test
    public void givenRespondentSolicitorNominated_whenCallbackCalled_linkingFieldsAreReset() throws Exception {
        final PinRequest pinRequest = PinRequest.builder()
                        .firstName("")
                        .lastName("")
                        .build();

        final Pin pin = Pin.builder().pin(TEST_PIN_CODE).userId(TEST_LETTER_HOLDER_ID_CODE).build();

        stubSignIn();
        stubPinDetailsEndpoint(BEARER_AUTH_TOKEN_1, pinRequest, pin);

        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
                AOS_SOL_NOMINATED_JSON, CcdCallbackRequest.class);

        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();
        CcdCallbackResponse expected = CcdCallbackResponse.builder()
                .data(caseData)
                .build();

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
                .content(convertObjectToJsonString(ccdCallbackRequest))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(convertObjectToJsonString(expected)))
                .andExpect(content().string(allOf(
                        isJson(),
                        hasJsonPath("$.errors", nullValue())
                )));
    }
}
