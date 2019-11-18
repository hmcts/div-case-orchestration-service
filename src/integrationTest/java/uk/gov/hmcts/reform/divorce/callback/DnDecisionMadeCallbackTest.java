package uk.gov.hmcts.reform.divorce.callback;

import feign.FeignException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class DnDecisionMadeCallbackTest extends IntegrationTest {

    private static final String CCD_CALLBACK_REQUEST = "fixtures/callback/dn-decision-made.json";

    @Autowired
    private CosApiClient cosApiClient;

    @Test
    public void givenCcdCallbackRequest_whenDnDecisionMade_thenExpectNoErrors() {
        CcdCallbackRequest ccdCallbackRequest = ResourceLoader.loadJsonToObject(CCD_CALLBACK_REQUEST, CcdCallbackRequest.class);
        try {
            CcdCallbackResponse response = cosApiClient.dnDecisionMade(createCaseWorkerUser().getAuthToken(), ccdCallbackRequest);
            assertThat(response.getErrors(), is(nullValue()));
            assertThat(response.getData(), is(notNullValue()));
        } catch (FeignException e) {
            fail(e.contentUTF8());
        }
    }
}
