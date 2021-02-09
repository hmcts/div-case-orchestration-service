package uk.gov.hmcts.reform.divorce.callback;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CaseListedForHearingCallbackTest extends IntegrationTest {

    private static final String BASE_CASE_RESPONSE = "fixtures/case-linked-for-hearing/case-linked-for-hearing.json";

    @Autowired
    private CosApiClient cosApiClient;

    private UserDetails citizenUser;
    private Map<String, Object> incomingPayload;

    @Before
    public void setUp() {
        citizenUser = createCitizenUser();
        incomingPayload = ResourceLoader.loadJsonToObject(BASE_CASE_RESPONSE, Map.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenCaseLinkedForHearingIsCalledBack_RespondentMethodIsDigital_thenReturnAOSData() {
        addCustomValuesToPayload(
            incomingPayload,
            Collections.singletonMap("RespContactMethodIsDigital", "Yes")
        );

        Map<String, Object> response = cosApiClient.caseLinkedForHearing(citizenUser.getAuthToken(), incomingPayload);
        assertNotNull(response.get(DATA));
        assertEquals(((Map<String, Object>) incomingPayload.get(CASE_DETAILS)).get(CASE_DATA), response.get(DATA));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenCaseLinkedForHearingIsCalledBack_RespondentMethodIsNotDigital_thenReturnAOSData() {
        addCustomValuesToPayload(
            incomingPayload,
            ImmutableMap.of(
                "RespContactMethodIsDigital", "No",
                "D8DerivedRespondentCorrespondenceAddr", "10 Baker Street\nAnnex B1\nBakersville\nBakershire\nBK13 B34")
        );

        Map<String, Object> response = cosApiClient.caseLinkedForHearing(citizenUser.getAuthToken(), incomingPayload);
        assertNotNull(response.get(DATA));
        assertEquals(((Map<String, Object>) incomingPayload.get(CASE_DETAILS)).get(CASE_DATA), response.get(DATA));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenCaseLinkedForHearingIsCalledBack_CoRespondentMethodIsDigital_thenReturnAOSData() {
        addCustomValuesToPayload(
            incomingPayload,
            Collections.singletonMap("CoRespContactMethodIsDigital", "Yes")
        );

        Map<String, Object> response = cosApiClient.caseLinkedForHearing(citizenUser.getAuthToken(), incomingPayload);
        assertNotNull(response.get(DATA));
        assertEquals(((Map<String, Object>) incomingPayload.get(CASE_DETAILS)).get(CASE_DATA), response.get(DATA));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenCaseLinkedForHearingIsCalledBack_CoRespondentMethodIsNotDigital_thenReturnAOSData() {
        addCustomValuesToPayload(
            incomingPayload,
            ImmutableMap.of(
                "CoRespContactMethodIsDigital", "No",
                "D8DerivedReasonForDivorceAdultery3rdAddr", "10 CoRespondent's Close"
            )
        );

        Map<String, Object> response = cosApiClient.caseLinkedForHearing(citizenUser.getAuthToken(), incomingPayload);

        assertNotNull(response.get(DATA));
        assertEquals(((Map<String, Object>) incomingPayload.get(CASE_DETAILS)).get(CASE_DATA), response.get(DATA));
    }


    @SuppressWarnings("unchecked")
    @Test
    public void whenCaseLinkedForHearingIsCalledBack_thenReturnAOSData() {
        Map<String, Object> aosCase = ResourceLoader.loadJsonToObject(BASE_CASE_RESPONSE, Map.class);
        Map<String, Object> response = cosApiClient.caseLinkedForHearing(
            createCaseWorkerUser().getAuthToken(),
            aosCase);
        assertNotNull(response.get(DATA));
        assertEquals(((Map<String, Object>) incomingPayload.get(CASE_DETAILS)).get(CASE_DATA), response.get(DATA));
    }

    private void addCustomValuesToPayload(Map<String, Object> incomingPayload, Map<String, Object> testCustomValues) {
        Optional.of(incomingPayload.get(CASE_DETAILS))
            .map(i -> (Map<String, Object>) i)
            .map(map -> map.get(CASE_DATA))
            .map(i -> (Map<String, Object>) i)
            .ifPresent(map -> map.putAll(testCustomValues));
    }

}