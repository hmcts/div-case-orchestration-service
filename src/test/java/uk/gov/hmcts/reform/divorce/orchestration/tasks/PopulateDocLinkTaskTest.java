package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.model.ccd.DocumentLink;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_DRAFT_LINK_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_PETITION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.MINI_PETITION_LINK;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getObjectMapperInstance;

public class PopulateDocLinkTaskTest {

    private PopulateDocLinkTask populateDocLinkTask;

    private TaskContext taskContext;

    @Before
    public void setup() {
        populateDocLinkTask = new PopulateDocLinkTask(getObjectMapperInstance());

        taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(DOCUMENT_TYPE, DOCUMENT_TYPE_PETITION);
        taskContext.setTransientObject(DOCUMENT_DRAFT_LINK_FIELD, MINI_PETITION_LINK);
    }

    @Test(expected = TaskException.class)
    public void throwsTaskExceptionIfMiniPetitionIsNotPresent() throws TaskException {
        Map<String, Object> payload =  Collections.singletonMap("D8DocumentsGenerated", new ArrayList<>());

        populateDocLinkTask.execute(taskContext, payload);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testExecuteSetsMiniPetitionUrl() throws Exception {
        Map<String, Object> payload = ObjectMapperTestUtil.getJsonFromResourceFile("/jsonExamples/payloads/sol-dn-review-petition.json", Map.class);

        Map<String, Object> result = populateDocLinkTask.execute(taskContext, payload);

        assertThat(result, is(payload));

        DocumentLink miniPetitionLink = (DocumentLink) result.get(MINI_PETITION_LINK);
        assertThat(miniPetitionLink.getDocumentUrl(), is("https://localhost:8080/documents/1234"));
        assertThat(miniPetitionLink.getDocumentFilename(), is("d8petition1513951627081724.pdf"));
        assertThat(miniPetitionLink.getDocumentBinaryUrl(), is("https://localhost:8080/documents/1234/binary"));
    }

}