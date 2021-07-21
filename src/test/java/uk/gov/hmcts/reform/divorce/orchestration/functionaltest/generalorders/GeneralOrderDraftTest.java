package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.generalorders;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders.GeneralOrderGenerationTask;

import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.JUDGE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@WebMvcTest
public class GeneralOrderDraftTest extends GeneralOrderTest {

    private static final String API_URL = "/create-general-order/draft";

    @Test
    public void shouldGenerateOrderTheFirstGeneralOrderAndAddItToCollection() throws Exception {
        Map<String, Object> caseData = buildInputCaseData();
        CcdCallbackRequest input = buildRequest(caseData);
        String documentType = GeneralOrderGenerationTask.FileMetadata.DOCUMENT_TYPE;
        String fileName = formatDocumentFileName(documentType);

        stubDocumentGeneratorServiceRequest(caseData, GeneralOrderGenerationTask.FileMetadata.TEMPLATE_ID, documentType);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(input))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.data.GeneralOrderDraft"),
                hasJsonPath("$.data.GeneralOrderDraft.document_filename", is(fileName)),
                hasNoJsonPath("$.errors"),
                hasNoJsonPath("$.warnings")
            )));
    }

    @Test
    public void shouldThrowErrorWhenNoJudgeName() throws Exception {
        Map<String, Object> caseData = buildInputCaseData();
        caseData.remove(JUDGE_NAME);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(buildRequest(caseData)))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.errors", hasSize(1)),
                hasJsonPath(
                    "$.errors[0]",
                    containsString("Could not evaluate value of mandatory property \"JudgeName\"")
                )
            )));
    }
}
