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
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_COE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

public class CourtOrderDocumentsUpdateCallbackTest extends MockedFunctionalTest {

    @Autowired
    private MockMvc webClient;

    @Test
    public void shouldUpdateExistingCourtOrderDocuments() throws Exception {
        stubDocumentGeneratorService("FL-DIV-GNO-ENG-00020.docx",//TODO - think of better way to store this value
            DOCUMENT_TYPE_COE);

        Map<String, Object> incomingCaseData = getJsonFromResourceFile("/jsonExamples/payloads/updateCourtOrderDocuments.json", new TypeReference<>() {
        });
//        assertThat(ObjectMapperTestUtil.convertObjectToJsonString(incomingCaseData), hasJsonPath("$.data.D8DocumentsGenerated", hasItem(
//            allOf(
//                hasJsonPath("$.value.DocumentType", is("coe")),
//                hasJsonPath("$.value.url", is("c14bb265-4a24-4640-a737-e1b50a97e678"))
//            )
//        )));//"c14bb265-4a24-4640-a737-e1b50a97e678"

        String response = webClient.perform(post("/update-court-order-documents")
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(
                CcdCallbackRequest.builder()
                    .caseDetails(CaseDetails.builder().caseId(TEST_CASE_ID).caseData(incomingCaseData).build())
                    .build()
            )))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(response, isJson());//TODO - join assertions?
        assertThat(response, hasJsonPath("$.data.D8DocumentsGenerated", hasItem(
            hasJsonPath("$.value.DocumentType", is("coe"))
//            hasJsonPath("$.DocumentType", is("coe"))//TODO - check more things about document to make sure it has replaced the existing one
        )));
//        ObjectMapperTestUtil.getObjectMapperInstance().readT//TODO- transform into CcdCallbackResponse
        //TODO - assert return
    }

}