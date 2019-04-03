package uk.gov.hmcts.reform.divorce.callback;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CaseListedForHearingCallbackTest extends IntegrationTest {

    private static final String BASE_CASE_RESPONSE = "fixtures/case-linked-for-hearing/case-linked-for-hearing.json";
    private static final String ERROR_CASE_RESPONSE = "fixtures/case-linked-for-hearing/case-linked-for-hearing-error.json";

    @Autowired
    private CosApiClient cosApiClient;

    @SuppressWarnings("unchecked")
    @Test
    public void whenCaseLinkedForHearingIsCalledBack_thenReturnAOSData() {
        Map<String, Object> aosCase = ResourceLoader.loadJsonToObject(BASE_CASE_RESPONSE, Map.class);
        Map<String, Object> response = cosApiClient.caseLinkedForHearing(null, aosCase);
        assertNotNull(response.get(DATA));
        assertEquals(((Map<String, Object>)aosCase.get(CASE_DETAILS)).get(CASE_DATA), response.get(DATA));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenCaseWithoutEmail_whenCaseLinkedForHearingIsCalledBack_thenReturnNotificationError() {
        Map<String, Object> aosCaseWithoutEmailAddress = ResourceLoader
                .loadJsonToObject(ERROR_CASE_RESPONSE, Map.class);
        Map<String, Object> response = cosApiClient.caseLinkedForHearing(null, aosCaseWithoutEmailAddress);

        assertNull(response.get(DATA));
        List<String> error = (List<String>) response.get(ERRORS);
        assertEquals(1, error.size());
        assertTrue(error.get(0).contains("Could not evaluate value of mandatory property \"D8PetitionerEmail\""));
    }

}