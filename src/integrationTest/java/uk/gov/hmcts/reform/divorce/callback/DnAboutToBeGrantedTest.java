package uk.gov.hmcts.reform.divorce.callback;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_CLARIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_REFUSAL_ORDER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.objectToJson;

public class DnAboutToBeGrantedTest extends IntegrationTest {

    private static final String CCD_CALLBACK_REQUEST = "fixtures/callback/dn-refusal-clarification.json";

    @Autowired
    private CosApiClient cosApiClient;

    @Test
    public void givenCcdCallbackRequest_whenDnAboutToBeGrantedWithRefusalClarification_thenGenerateRefusalOrderDocument() {
        CcdCallbackRequest ccdCallbackRequest = ResourceLoader.loadJsonToObject(CCD_CALLBACK_REQUEST, CcdCallbackRequest.class);

        CcdCallbackResponse response = cosApiClient.processDnAboutToBeGranted(createCaseWorkerUser().getAuthToken(), ccdCallbackRequest);

        String jsonResponse = objectToJson(response);

        assertThat(jsonResponse,
            hasJsonPath("$.data.state", is(AWAITING_CLARIFICATION)));

        assertThat(jsonResponse,
            hasJsonPath("$.data.D8DocumentsGenerated[0].value.DocumentType", is(DECREE_NISI_REFUSAL_ORDER_DOCUMENT_TYPE)));
    }
}
