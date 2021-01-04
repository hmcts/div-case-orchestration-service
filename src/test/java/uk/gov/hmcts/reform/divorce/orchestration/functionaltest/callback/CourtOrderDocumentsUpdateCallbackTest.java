package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.callback;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.MockedFunctionalTest;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;

import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocmosisTemplates.COE_ENGLISH_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_COE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

public class CourtOrderDocumentsUpdateCallbackTest extends MockedFunctionalTest {

    private static final String TEST_COE_INCOMING_DOCUMENT = "1234";

    @Autowired
    private MockMvc webClient;

    @Test
    public void shouldUpdateExistingCourtOrderDocuments() throws Exception {
        stubDocumentGeneratorService(COE_ENGLISH_TEMPLATE_ID, DOCUMENT_TYPE_COE);

        Map<String, Object> caseData = loadAndValidateTestCaseData();

        String response = webClient.perform(post("/update-court-order-documents")
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(
                CcdCallbackRequest.builder()
                    .caseDetails(CaseDetails.builder().caseId(TEST_CASE_ID).caseData(caseData).build())
                    .build()
            )))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        //Make sure document ids were replaced
        assertThat(response, hasJsonPath("$.data.D8DocumentsGenerated", hasItem(allOf(
            hasJsonPath("$.value.DocumentType", is("coe")),
            hasJsonPath("$.value.DocumentLink.document_url", not(endsWith("1234")))
        ))));
    }

    private Map<String, Object> loadAndValidateTestCaseData() throws java.io.IOException {
        Map<String, Object> caseData = getJsonFromResourceFile("/jsonExamples/payloads/updateCourtOrderDocuments.json", new TypeReference<>() {
        });

        //Check example contains the expected ids
        assertThat(ObjectMapperTestUtil.convertObjectToJsonString(caseData), hasJsonPath("$.D8DocumentsGenerated", hasItem(allOf(
            hasJsonPath("$.value.DocumentType", is("coe")),
            hasJsonPath("$.value.DocumentLink.document_url", endsWith(TEST_COE_INCOMING_DOCUMENT))
        ))));

        return caseData;
    }

}