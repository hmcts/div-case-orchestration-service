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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_ORDER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_FILENAME;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.objectToJson;

public class DnPronouncedDocumentsGeneratedTest extends IntegrationTest {

    private static final String BULK_CASE_LINK_CCD_CALLBACK_REQUEST = "fixtures/callback/basic-case-with-bulk-case-link.json";
    private static final String COSTS_CLAIM_CCD_CALLBACK_REQUEST = "fixtures/callback/costs-claim-granted.json";
    private static final String TEST_CASE_ID = "0123456789012345";

    @Autowired
    private CosApiClient cosApiClient;

    @Test
    public void givenCase_whenGenerateDnPronouncedDocumentsWithNoClaimCosts_thenReturnCallbackResponseWithDecreeNisiDocument() {
        CcdCallbackRequest ccdCallbackRequest = ResourceLoader.loadJsonToObject(BULK_CASE_LINK_CCD_CALLBACK_REQUEST, CcdCallbackRequest.class);

        Map<String, Object> response = cosApiClient.generateDnPronouncedDocuments(createCaseWorkerUser().getAuthToken(), ccdCallbackRequest);

        String jsonResponse = objectToJson(response);

        assertThat(
                jsonResponse,
                hasJsonPath("$.data.D8DocumentsGenerated[0].value.DocumentFileName", is(DECREE_NISI_FILENAME + TEST_CASE_ID)));
        assertThat(
                jsonResponse,
                hasJsonPath("$.data.D8DocumentsGenerated", hasSize(1)));
    }

    @Test
    public void givenCase_whenGenerateDnPronouncedDocumentsWithClaimCosts_thenReturnCallbackResponseWithDecreeNisiDocumentAndCostsOrder() {
        CcdCallbackRequest ccdCallbackRequest = ResourceLoader.loadJsonToObject(COSTS_CLAIM_CCD_CALLBACK_REQUEST, CcdCallbackRequest.class);

        Map<String, Object> response = cosApiClient.generateDnPronouncedDocuments(createCaseWorkerUser().getAuthToken(), ccdCallbackRequest);

        String jsonResponse = objectToJson(response);

        assertThat(
                jsonResponse,
                hasJsonPath("$.data.D8DocumentsGenerated[0].value.DocumentFileName", is(DECREE_NISI_FILENAME + TEST_CASE_ID)));
        assertThat(
                jsonResponse,
                hasJsonPath("$.data.D8DocumentsGenerated[1].value.DocumentFileName", is(COSTS_ORDER_DOCUMENT_TYPE + TEST_CASE_ID)));
    }
}
