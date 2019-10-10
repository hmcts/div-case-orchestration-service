package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_LIST_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.REMOVED_CASE_LIST;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

public class SyncBulkCaseListTaskUTest {

    private static final String CMS_RESPONSE_BODY_FILE = "/jsonExamples/payloads/removeCaseBulkCaseCallbackRequest.json";

    private SyncBulkCaseListTask classToTest;
    private TaskContext context;

    @Before
    public void setup() {
        classToTest =  new SyncBulkCaseListTask();
        context = new DefaultTaskContext();
    }

    @Test
    public void whenExecuteSyncAcceptedBulkCase_thenPopulateListToRemove() throws Exception {
        CcdCallbackRequest callbackRequest = getCmsBulkCaseResponse();
        Map<String, Object> bulkCase = classToTest.execute(context, callbackRequest.getCaseDetails().getCaseData());
        List<String> removedList = context.getTransientObject(REMOVED_CASE_LIST);
        assertThat(removedList, is(Arrays.asList("1558711407435839", "1558711407435840")));
        assertThat(bulkCase.get(CASE_LIST_KEY), is(callbackRequest.getCaseDetails().getCaseData().get(CASE_LIST_KEY)));
    }

    private CcdCallbackRequest getCmsBulkCaseResponse() throws Exception {
        return getJsonFromResourceFile(CMS_RESPONSE_BODY_FILE, CcdCallbackRequest.class);
    }
}
