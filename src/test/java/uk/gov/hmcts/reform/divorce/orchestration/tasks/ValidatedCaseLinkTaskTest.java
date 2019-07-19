package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.TestConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.DUMMY_CASE_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_BULK_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_LINK_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_LISTING_CASE_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

public class ValidatedCaseLinkTaskTest {

    private ValidatedCaseLinkTask classToTest = new ValidatedCaseLinkTask();

    private DefaultTaskContext taskContext;

    @Before
    public void setup() {
        taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        taskContext.setTransientObject(BULK_LINK_CASE_ID, TEST_BULK_CASE_ID);
    }

    @Test
    public void givenValidCase_whenValidateCase_thenTaskNotFail() throws TaskException {
        CaseDetails caseDetails = CaseDetails
            .builder()
            .caseData(CaseDataUtils.createCaseLinkField(BULK_LISTING_CASE_ID_FIELD, TEST_BULK_CASE_ID))
            .build();
        taskContext.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        classToTest.execute(taskContext, TestConstants.DUMMY_CASE_DATA);

        assertThat(taskContext.isTaskFailed(), is(false));
    }

    @Test
    public void givenDifferentCaselink_whenValidateBulkLink_thenReturnTaskFailed() throws TaskException {

        CaseDetails caseDetails = CaseDetails
            .builder()
            .caseData(CaseDataUtils.createCaseLinkField(BULK_LISTING_CASE_ID_FIELD, TEST_CASE_ID))
            .build();
        taskContext.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        classToTest.execute(taskContext, TestConstants.DUMMY_CASE_DATA);

        assertThat(taskContext.isTaskFailed(), is(true));
    }

    @Test
    public void givenUnlinkedCase_whenValidateBulkLink_thenReturnTaskFailed() throws TaskException {
        CaseDetails caseDetails = CaseDetails
            .builder()
            .caseData(DUMMY_CASE_DATA)
            .build();
        taskContext.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        classToTest.execute(taskContext, TestConstants.DUMMY_CASE_DATA);

        assertThat(taskContext.isTaskFailed(), is(true));
    }
}
