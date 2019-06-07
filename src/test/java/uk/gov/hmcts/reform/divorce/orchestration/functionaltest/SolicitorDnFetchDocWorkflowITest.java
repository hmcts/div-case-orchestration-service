package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class SolicitorDnFetchDocWorkflowITest {

    private static final String API_URL = "/sol-dn-review-petition";
    private static final String MINI_PETITION_LINK = "minipetitionlink";

    @Autowired
    private MockMvc webClient;

    @Test
    public void givenNoGeneratedDocuments_whenSolicitorDnJourneyBegins_thenResponseContainsErrors() throws Exception {

        final Map<String, Object> caseData = Collections.emptyMap();

        final CaseDetails caseDetails =
                CaseDetails.builder()
                        .caseData(caseData)
                        .build();

        CcdCallbackRequest request = CcdCallbackRequest.builder()
                .caseDetails(caseDetails)
                .build();

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

    @SuppressWarnings("unchecked")
    @Test
    public void givenCaseData_whenSolicitorDnJourneyBegins_thenSetPetitionUrlField() throws Exception {

        final Map<String, Object> caseData = ObjectMapperTestUtil.getJsonFromResourceFile(
                "/jsonExamples/payloads/sol-dn-review-petition.json",
                Map.class
        );

        final CaseDetails caseDetails = CaseDetails.builder()
                        .caseData(caseData)
                        .build();

        CcdCallbackRequest request = CcdCallbackRequest.builder()
                .caseDetails(caseDetails)
                .build();

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(request))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(
                    convertObjectToJsonString(CcdCallbackResponse.builder()
                        .data(buildExpectedDataMap())
                        .build()
                    )
                )
            );
    }

    private Map<String, Object> buildExpectedDataMap() {
        HashMap<String, Object> expectedMiniPetitionLink = new HashMap<>();
        expectedMiniPetitionLink.put("document_url", "https://localhost:8080/documents/1234");
        expectedMiniPetitionLink.put("document_filename", "d8petition1513951627081724.pdf");
        expectedMiniPetitionLink.put("document_binary_url", "https://localhost:8080/documents/1234/binary");
        return Collections.singletonMap(MINI_PETITION_LINK, expectedMiniPetitionLink);
    }
}
