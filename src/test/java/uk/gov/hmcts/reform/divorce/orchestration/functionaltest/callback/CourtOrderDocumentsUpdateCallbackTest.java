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
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_ORDER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_COE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.COE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.COSTS_ORDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.DECREE_ABSOLUTE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.DECREE_NISI;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

public class CourtOrderDocumentsUpdateCallbackTest extends MockedFunctionalTest {

    private static final String TEST_COE_INCOMING_DOCUMENT = "1234";
    private static final String TEST_COSTS_ORDER_INCOMING_DOCUMENT = "2345";
    private static final String TEST_DECREE_NISI_INCOMING_DOCUMENT = "3456";
    private static final String TEST_DECREE_ABSOLUTE_INCOMING_DOCUMENT = "4567";

    @Autowired
    private MockMvc webClient;

    @Test
    public void shouldUpdateExistingCourtOrderDocuments() throws Exception {
        stubDocumentGeneratorService(COE.getTemplateByLanguage(ENGLISH), DOCUMENT_TYPE_COE);
        stubDocumentGeneratorService(COSTS_ORDER.getTemplateByLanguage(ENGLISH), COSTS_ORDER_DOCUMENT_TYPE);
        stubDocumentGeneratorService(DECREE_NISI.getTemplateByLanguage(ENGLISH), DECREE_NISI_DOCUMENT_TYPE);
        stubDocumentGeneratorService(DECREE_ABSOLUTE.getTemplateByLanguage(ENGLISH), DECREE_ABSOLUTE_DOCUMENT_TYPE);

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
        assertThat(response, hasJsonPath("$.data.D8DocumentsGenerated", hasItems(
            allOf(
                hasJsonPath("$.value.DocumentType", is(DOCUMENT_TYPE_COE)),
                hasJsonPath("$.value.DocumentLink.document_url", not(endsWith(TEST_COE_INCOMING_DOCUMENT)))
            ),
            allOf(
                hasJsonPath("$.value.DocumentType", is(COSTS_ORDER_DOCUMENT_TYPE)),
                hasJsonPath("$.value.DocumentLink.document_url", not(endsWith(TEST_COSTS_ORDER_INCOMING_DOCUMENT)))
            ),
            allOf(
                hasJsonPath("$.value.DocumentType", is(DECREE_NISI_DOCUMENT_TYPE)),
                hasJsonPath("$.value.DocumentLink.document_url", not(endsWith(TEST_DECREE_NISI_INCOMING_DOCUMENT)))
            ),
            allOf(
                hasJsonPath("$.value.DocumentType", is(DECREE_ABSOLUTE_DOCUMENT_TYPE)),
                hasJsonPath("$.value.DocumentLink.document_url", not(endsWith(TEST_DECREE_ABSOLUTE_INCOMING_DOCUMENT)))
            )
        )));
    }

    @Test
    public void shouldNotUpdateCourtOrderDocumentsIfTheyAreNotInCaseData() throws Exception {
        Map<String, Object> caseData = emptyMap();

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
        assertThat(response, hasNoJsonPath("$.data.D8DocumentsGenerated"));
    }

    private Map<String, Object> loadAndValidateTestCaseData() throws java.io.IOException {
        Map<String, Object> caseData = getJsonFromResourceFile("/jsonExamples/payloads/updateCourtOrderDocuments.json", new TypeReference<>() {
        });

        //Check example contains the expected ids
        assertThat(ObjectMapperTestUtil.convertObjectToJsonString(caseData), hasJsonPath("$.D8DocumentsGenerated", hasItems(
            allOf(
                hasJsonPath("$.value.DocumentType", is(DOCUMENT_TYPE_COE)),
                hasJsonPath("$.value.DocumentLink.document_url", endsWith(TEST_COE_INCOMING_DOCUMENT))
            ),
            allOf(
                hasJsonPath("$.value.DocumentType", is(COSTS_ORDER_DOCUMENT_TYPE)),
                hasJsonPath("$.value.DocumentLink.document_url", endsWith(TEST_COSTS_ORDER_INCOMING_DOCUMENT))
            )
        )));

        return caseData;
    }

}