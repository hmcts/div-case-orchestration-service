package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.generalorders;

import org.junit.Test;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders.GeneralOrderGenerationTask;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.LocalDate;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class GeneralOrderDraftTest extends GeneralOrderTest {

    private static final String API_URL = "/create-general-order/draft";

    @Test
    public void shouldGenerateOrderTheFirstGeneralOrderAndAddItToCollection() throws Exception {
        Map<String, Object> caseData = buildInputCaseData();
        CcdCallbackRequest input = buildRequest(caseData);
        String documentType = GeneralOrderGenerationTask.FileMetadata.DOCUMENT_TYPE;
        String fileName = documentType + DateUtils.formatDateFromLocalDate(LocalDate.now()) + ".pdf";

        stubDgsRequest(caseData, GeneralOrderGenerationTask.FileMetadata.TEMPLATE_ID, documentType);

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
}
