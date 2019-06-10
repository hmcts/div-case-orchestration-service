package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_PETITION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.MINI_PETITION_LINK;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_DOCUMENT_LINK_FIELD;

@RunWith(MockitoJUnitRunner.class)
public class PopulateDocLinkTest {

    @InjectMocks
    private PopulateDocLink populateDocLink;

    private TaskContext taskContext;

    @Before
    public void setup() {
        taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(DOCUMENT_TYPE, DOCUMENT_TYPE_PETITION);
        taskContext.setTransientObject(SOL_DOCUMENT_LINK_FIELD, MINI_PETITION_LINK);
    }

    @Test(expected = TaskException.class)
    public void throwsTaskExceptionIfMiniPetitionIsNotPresent() throws TaskException {
        Map<String, Object> payload =  Collections.singletonMap("D8DocumentsGenerated", new ArrayList<>());

        populateDocLink.execute(taskContext, payload);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testExecuteSetsMiniPetitionUrl() throws Exception {
        Map<String, Object> payload = ObjectMapperTestUtil.getJsonFromResourceFile("/jsonExamples/payloads/sol-dn-review-petition.json", Map.class);

        Map<String, Object> result = populateDocLink.execute(taskContext, payload);

        assertThat(result, is(payload));

        Map<String, Object> miniPetitionLink = (Map<String, Object>) result.get(MINI_PETITION_LINK);
        assertThat(miniPetitionLink.get("document_url"), is("https://localhost:8080/documents/1234"));
        assertThat(miniPetitionLink.get("document_filename"), is("d8petition1513951627081724.pdf"));
        assertThat(miniPetitionLink.get("document_binary_url"), is("https://localhost:8080/documents/1234/binary"));
    }
}