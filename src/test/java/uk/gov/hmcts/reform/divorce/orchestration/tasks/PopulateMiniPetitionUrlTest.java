package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class PopulateMiniPetitionUrlTest {

    @InjectMocks
    private PopulateMiniPetitionUrl populateMiniPetitionUrl;

    @Test
    public void executeSetsMiniPetitionUrl() throws IOException {
        Map<String, Object> payload = ObjectMapperTestUtil.getJsonFromResourceFile("/jsonExamples/payloads/caseListedForHearing.json", Map.class);

        Map<String, Object> result = populateMiniPetitionUrl.execute(null, payload);

        assertEquals(payload, result);
        assertEquals("https://localhost:8080/documents/1234/binary", result.get("minipetitionlink"));
    }
}