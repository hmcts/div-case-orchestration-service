package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class PopulateMiniPetitionUrlTest {

    @InjectMocks
    private PopulateMiniPetitionUrl populateMiniPetitionUrl;

    @Test(expected = TaskException.class)
    public void throwsTaskExceptionIfMiniPetitionIsNotPresent() throws TaskException {
        Map<String, Object> payload =  Collections.singletonMap("D8DocumentsGenerated", new ArrayList<>());

        populateMiniPetitionUrl.execute(null, payload);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testExecuteSetsMiniPetitionUrl() throws Exception {
        Map<String, Object> payload = ObjectMapperTestUtil.getJsonFromResourceFile("/jsonExamples/payloads/sol-dn-review-petition.json", Map.class);

        Map<String, Object> result = populateMiniPetitionUrl.execute(null, payload);

        assertThat(result, is(payload));
        assertThat(result.get("minipetitionlink"), is("https://localhost:8080/documents/1234/binary"));
    }
}