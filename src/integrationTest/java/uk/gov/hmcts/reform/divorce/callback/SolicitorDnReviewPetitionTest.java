package uk.gov.hmcts.reform.divorce.callback;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SolicitorDnReviewPetitionTest extends IntegrationTest {

    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/callback/";

    @Autowired
    private CosApiClient cosApiClient;

    @SuppressWarnings("unchecked")
    @Test
    public void givenCase_solicitorDnReviewPetitionIsCalled_thenSetMiniPetitionUrlField() {
        Map<String, Object> payload = ResourceLoader.loadJsonToObject(PAYLOAD_CONTEXT_PATH + "sol-dn-review-petition.json", Map.class);

        Map<String, Object> response = cosApiClient.solDnReviewPetition(payload);
        Map<String, Object> data = (Map<String, Object>) response.get(DATA);

        assertNotNull(data);
        assertEquals("https://localhost:8080/documents/1234/binary", data.get("minipetitionlink"));
    }
}
