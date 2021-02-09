package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.Pin;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.DateCalculator;
import uk.gov.hmcts.reform.idam.client.models.GeneratePinRequest;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.BEARER_AUTH_TOKEN_1;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_LETTER_HOLDER_ID_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ACCESS_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_RESPONDENT_INVITATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_LETTER_HOLDER_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

public class RespondentSolicitorNominatedITest extends IdamTestSupport {

    private static final String API_URL = "/aos-solicitor-nominated";

    private static final String AOS_SOL_NOMINATED_JSON = "/jsonExamples/payloads/aosSolicitorNominated.json";
    private static final String RESPONDENT_INVITATION_TEMPLATE_NAME = "aosinvitation";

    @Value("${bulk-print.dueDate}")
    private Integer dueDateOffset;

    @Autowired
    private MockMvc webClient;

    @Before
    public void setUp() {
        stubSignIn();

        final GeneratePinRequest pinRequest = GeneratePinRequest.builder()
            .firstName("")
            .lastName("")
            .build();
        final Pin pin = Pin.builder().pin(TEST_PIN_CODE).userId(TEST_LETTER_HOLDER_ID_CODE).build();
        stubPinDetailsEndpoint(BEARER_AUTH_TOKEN_1, pinRequest, pin);
    }

    @Test
    public void givenRespondentSolicitorNominated_whenCallbackCalled_linkingFieldsAreReset() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(AOS_SOL_NOMINATED_JSON, CcdCallbackRequest.class);
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        caseDetails.getCaseData().put(RESPONDENT_LETTER_HOLDER_ID, TEST_LETTER_HOLDER_ID_CODE);

        String documentId = stubDocumentGeneratorService(RESPONDENT_INVITATION_TEMPLATE_NAME,
            ImmutableMap.of(
                DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails,
                ACCESS_CODE, TEST_PIN_CODE
            ),
            DOCUMENT_TYPE_RESPONDENT_INVITATION
        );
        stubServiceAuthProvider(OK, TEST_SERVICE_AUTH_TOKEN);
        stubDMStore(documentId, new byte[] {1, 2, 3});

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath(
                    "$.data.dueDate",
                    is(DateCalculator.getDateWithOffset(dueDateOffset))
                ),
                hasJsonPath("$.errors", nullValue())
            )));
    }
}
