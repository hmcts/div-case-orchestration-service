package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class PopulateMiniPetitionUrlTest {

    @InjectMocks
    private PopulateMiniPetitionUrl populateMiniPetitionUrl;

    @Test
    public void testExecuteSetsMiniPetitionUrl() throws IOException {
        Map<String, Object> payload = ObjectMapperTestUtil.getJsonFromResourceFile("/jsonExamples/payloads/sol-dn-review-petition.json", Map.class);

        Map<String, Object> result = populateMiniPetitionUrl.execute(null, payload);

        assertThat(payload).isEqualTo(result);
        assertThat(result.get("minipetitionlink")).isEqualTo("https://localhost:8080/documents/1234/binary");
    }
}