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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;


public class MakeCaseEligibleForDATest extends IntegrationTest {

    private static final String CCD_CALLBACK_REQUEST_DECISION_MADE = "fixtures/callback/dn-decision-made.json";
    private static final String CCD_CALLBACK_REQUEST_CASE_ELIGIBLE_FOR_DA_SUBMITTED  = "fixtures/callback/case-eligible-for-da-submitted.json";

    @Autowired
    private CosApiClient cosApiClient;

    @Test
    public void givenCcdCallbackRequest_whenMakeCaseEligibleForDASubmitted_thenErrorsShouldBeRaised() {
        final CcdCallbackRequest ccdCallbackRequest = ResourceLoader.loadJsonToObject(CCD_CALLBACK_REQUEST_DECISION_MADE, CcdCallbackRequest.class);
        CcdCallbackResponse response = null ;
        try {
            response = cosApiClient.handleMakeCaseEligibleForDASubmitted(ccdCallbackRequest);
        } catch (FeignException e) {
            assertThat(response.getErrors(), is(notNullValue()));
            assertThat(response.getData(),is(nullValue()));
        }
    }

    @Test
    public void givenCcdCallbackRequest_whenMakeCaseEligibleForDASubmitted_isTriggered_shouldReturnSuccess() {
        final CcdCallbackRequest ccdCallbackRequest = ResourceLoader
            .loadJsonToObject(CCD_CALLBACK_REQUEST_CASE_ELIGIBLE_FOR_DA_SUBMITTED, CcdCallbackRequest.class);
        CcdCallbackResponse response = null ;
        try {
            response = cosApiClient.handleMakeCaseEligibleForDASubmitted(ccdCallbackRequest);
            assertThat(response.getErrors(), is(nullValue()));
            assertThat(response.getData(), is(notNullValue()));
        } catch (FeignException e) {
            fail(e.contentUTF8());
        }
    }
}