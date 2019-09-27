package uk.gov.hmcts.reform.divorce.callback;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_LETTER_HOLDER_ID;

public class AosSolicitorNominatedCallbackTest extends IntegrationTest {
    private static final String BASE_CASE_RESPONSE = "fixtures/respond-to-a-divorce/ccd-callback-aos-solicitor-nominated.json";

    @Autowired
    private CosApiClient cosApiClient;

    @SuppressWarnings("unchecked")
    @Test
    public void whenSubmitAOSSubmittedIsCalledBack_thenReturnAOSData() {
        //given
        UserDetails solicitorUser = createSolicitorUser();
        Map<String, Object> aosCase = ResourceLoader.loadJsonToObject(BASE_CASE_RESPONSE, Map.class);

        //when
        Map<String, Object> response = cosApiClient.aosSolicitorNominated(solicitorUser.getAuthToken(), aosCase);

        //then
        Map<String, String> responseData = (Map<String, String>)response.get(DATA);
        assertNotNull(responseData);
        assertNotNull(responseData.get(RESPONDENT_LETTER_HOLDER_ID));
        assertNull(responseData.get(RECEIVED_AOS_FROM_RESP));
        assertNull(responseData.get(RECEIVED_AOS_FROM_RESP_DATE));
        assertNull(responseData.get(RESPONDENT_EMAIL_ADDRESS));
    }
}
