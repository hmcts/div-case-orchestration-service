package uk.gov.hmcts.reform.divorce.callback;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_PETITION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.MINI_PETITION_TEMPLATE_NAME;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.objectToJson;

public class DocumentGeneratedTest extends IntegrationTest {

    private static final String CCD_CALLBACK_REQUEST = "fixtures/callback/basic-case.json";
    private static final String TEST_CASE_ID = "0123456789012345";

    @Autowired
    private CosApiClient cosApiClient;

    @SuppressWarnings("unchecked")
    @Test
    public void givenCase_whenGenerateDocumentForMiniPetition_thenReturnCallbackResponseWithMiniPetitionDocument() {
        CcdCallbackRequest ccdCallbackRequest = ResourceLoader.loadJsonToObject(CCD_CALLBACK_REQUEST, CcdCallbackRequest.class);

        Map<String, Object> response = cosApiClient
                .generateDocument(createCaseWorkerUser().getAuthToken(), ccdCallbackRequest,
                        MINI_PETITION_TEMPLATE_NAME, DOCUMENT_TYPE_PETITION, MINI_PETITION_TEMPLATE_NAME);

        String jsonResponse = objectToJson(response);

        assertThat(
                jsonResponse,
                hasJsonPath("$.data.D8DocumentsGenerated[0].value.DocumentFileName", is(MINI_PETITION_TEMPLATE_NAME + TEST_CASE_ID))
        );
    }
}
