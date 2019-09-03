package uk.gov.hmcts.reform.divorce.orchestration.tasks.datamigration;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;

import static org.junit.rules.ExpectedException.none;

public class DecreeAbsoluteDataExtractorTest {

    @Rule
    public ExpectedException expectedException = none();
    private DecreeAbsoluteDataExtractor classUnderTest;

    @Before
    public void setUp() throws Exception {
        classUnderTest = new DecreeAbsoluteDataExtractor();
    }

    @Test
    public void shouldThrowTaskExceptionWhenMandatoryField_Id_IsNotFound() throws TaskException {
        expectedException.expect(TaskException.class);

        HashMap<String, Object> caseData = new HashMap<>();
        caseData.put("DecreeAbsoluteApplicationDate", "2018-06-24");
        caseData.put("DecreeNisiGrantedDate", "2017-08-26");
        caseData.put("WhoAppliedForDA", "respondent");

        classUnderTest.mapCaseData(CaseDetails.builder().caseData(caseData).build());
    }

    @Test
    public void shouldThrowTaskExceptionWhenMandatoryField_DecreeNisiGrantedDate_IsNotFound() throws TaskException {
        expectedException.expect(TaskException.class);

        HashMap<String, Object> caseData = new HashMap<>();
        caseData.put("D8caseReference", "TEST2");
        caseData.put("DecreeAbsoluteApplicationDate", "2018-06-24");
        caseData.put("WhoAppliedForDA", "respondent");

        classUnderTest.mapCaseData(CaseDetails.builder().caseData(caseData).build());
    }

}