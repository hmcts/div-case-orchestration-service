package uk.gov.hmcts.reform.divorce.callback;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.objectToJson;

public class ProcessAosOfflineAnswersTest extends IntegrationTest {

    private static final String CCD_CALLBACK_REQUEST = "fixtures/co-resp-case/co-resp-aos-answers.json";

    @Autowired
    private CosApiClient cosApiClient;

    @Test
    public void givenCase_whenProcessAosOfflineAnswersEvent_shouldReturnCallbackResponse_With_ReceivedAosFromCoResp_Yes_Value() {

        CcdCallbackRequest ccdCallbackRequest = ResourceLoader.loadJsonToObject(CCD_CALLBACK_REQUEST, CcdCallbackRequest.class);

        CcdCallbackResponse response =  cosApiClient.processAosPackOfflineAnswers(ccdCallbackRequest, CO_RESPONDENT.getDescription());

        String jsonResponse = objectToJson(response);

        assertThat(jsonResponse, hasJsonPath("$.data.ReceivedAosFromCoResp", is(YES_VALUE)));
    }
}
