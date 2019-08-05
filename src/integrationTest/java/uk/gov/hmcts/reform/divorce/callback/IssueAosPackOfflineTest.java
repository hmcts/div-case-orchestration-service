package uk.gov.hmcts.reform.divorce.callback;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_INVITATION_LETTER_FILENAME;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.objectToJson;

public class IssueAosPackOfflineTest extends IntegrationTest {

    private static final String CCD_CALLBACK_REQUEST = "fixtures/callback/basic-case.json";

    @Autowired
    private CosApiClient cosApiClient;

    @Test
    public void givenCase_whenIssuingAosPackOfflineForRespondent_thenReturnCallbackResponseWithRightDocuments() {
        CcdCallbackRequest ccdCallbackRequest = ResourceLoader.loadJsonToObject(CCD_CALLBACK_REQUEST, CcdCallbackRequest.class);
        String testCaseId = ccdCallbackRequest.getCaseDetails().getCaseId();

        Map<String, Object> response = cosApiClient.issueAosPackOfflineForRespondent(createCaseWorkerUser().getAuthToken(), ccdCallbackRequest);
        String jsonResponse = objectToJson(response);

        assertThat(
            jsonResponse,
            hasJsonPath("$.data.D8DocumentsGenerated", allOf(
                hasSize(1),
                hasJsonPath("[0].value.DocumentFileName", is(AOS_INVITATION_LETTER_FILENAME + testCaseId))
            )));
    }

}