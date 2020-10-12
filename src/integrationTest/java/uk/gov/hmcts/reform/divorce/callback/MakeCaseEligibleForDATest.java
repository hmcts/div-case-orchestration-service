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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class MakeCaseEligibleForDATest extends IntegrationTest {

    // TODO Change this file.
    private static final String CCD_CALLBACK_REQUEST = "fixtures/callback/dn-decision-made.json";

    @Autowired
    private CosApiClient cosApiClient;

    @Test
    public void givenCcdCallbackRequest_whenMakeCaseEligibleForDASubmitted_thenErrorShouldBeRaised() {
        final CcdCallbackRequest ccdCallbackRequest = ResourceLoader.loadJsonToObject(CCD_CALLBACK_REQUEST, CcdCallbackRequest.class);
        CcdCallbackResponse response = null ;
        try {
            response = cosApiClient.handleMakeCaseEligibleForDASubmitted(ccdCallbackRequest);
            fail("Should not reach here as this is an exception Scenario");
        } catch (FeignException e) {
            assertThat(response.getErrors(), is(notNullValue()));
        }
    }


}