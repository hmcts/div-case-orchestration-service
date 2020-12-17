package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.lang.String.format;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes.DEEMED;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.SolicitorDnFetchDocWorkflowTest.buildCaseData;

public class SolicitorDnFetchDocTest extends MockedFunctionalTest {

    private static final String API_URL = "/sol-dn-review-petition";
    private static final String API_URL_RESP_ANSWERS = "/sol-dn-resp-answers-doc";
    private static final String MINI_PETITION_LINK = "minipetitionlink";

    @Autowired
    private MockMvc webClient;

    @Test
    public void givenNoGeneratedDocuments_whenSolicitorDnJourneyBegins_thenResponseContainsErrors() throws Exception {

        final Map<String, Object> caseData = Collections.emptyMap();

        CcdCallbackRequest request = buildRequest(caseData);

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(request))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.data", is(nullValue())),
                hasJsonPath("$.errors",
                    hasItem("petition document not found")
                )
            )));
    }

    @Test
    public void givenValidServiceApplicationGranted_whenRequestingRespondentAnswers_thenResponseDoesNotSetRespondentAnswersDocumentLinkAndContainsNoErrors() throws Exception {

        final Map<String, Object> caseData = buildCaseData(DEEMED, YES_VALUE);

        CcdCallbackRequest request = buildRequest(caseData);

        webClient.perform(post(API_URL_RESP_ANSWERS)
            .content(convertObjectToJsonString(request))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.data.ServiceApplications", hasSize(1)),
                assertServiceApplicationElement(DEEMED, YES_VALUE),
                hasNoJsonPath("$.errors")
            )));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenCaseData_whenSolicitorDnJourneyBegins_thenSetPetitionUrlField() throws Exception {

        final Map<String, Object> caseData = ObjectMapperTestUtil.getJsonFromResourceFile(
            "/jsonExamples/payloads/sol-dn-review-petition.json",
            Map.class
        );

        CcdCallbackRequest request = buildRequest(caseData);

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(request))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(
                convertObjectToJsonString(CcdCallbackResponse.builder()
                    .data(buildExpectedDataMap())
                    .build()
                ))
            );
    }

    private Map<String, Object> buildExpectedDataMap() {
        HashMap<String, Object> expectedMiniPetitionLink = new HashMap<>();
        expectedMiniPetitionLink.put("document_url", "https://localhost:8080/documents/1234");
        expectedMiniPetitionLink.put("document_filename", "d8petition1513951627081724.pdf");
        expectedMiniPetitionLink.put("document_binary_url", "https://localhost:8080/documents/1234/binary");

        return Collections.singletonMap(MINI_PETITION_LINK, expectedMiniPetitionLink);
    }

    private static Matcher<String> assertServiceApplicationElement(String applicationType, String granted) {
        String pattern = "$.data.ServiceApplications[0].value.%s";

        return allOf(
            hasJsonPath(format(pattern, "Type"), is(applicationType)),
            hasJsonPath(format(pattern, "ApplicationGranted"), is(granted))
        );
    }

    private CcdCallbackRequest buildRequest(Map<String, Object> caseData) {
        CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).build();

        return CcdCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
    }
}
