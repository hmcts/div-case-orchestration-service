package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalemail.GeneralEmailCoRespondentSolicitorTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalemail.GeneralEmailCoRespondentTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalemail.GeneralEmailOtherPartyTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalemail.GeneralEmailPetitionerSolicitorTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalemail.GeneralEmailPetitionerTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalemail.GeneralEmailRespondentSolicitorTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalemail.GeneralEmailRespondentTask;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper.Party.PETITIONER_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTaskWasCalled;

@RunWith(MockitoJUnitRunner.class)
public class GeneralEmailWorkflowTest {

    @InjectMocks
    private GeneralEmailWorkflow classUnderTest;

    @Mock
    private GeneralEmailCoRespondentSolicitorTask generalEmailCoRespondentSolicitorTask;

    @Mock
    private GeneralEmailCoRespondentTask generalEmailCoRespondentTask;

    @Mock
    private GeneralEmailOtherPartyTask generalEmailOtherPartyTask;

    @Mock
    private GeneralEmailPetitionerSolicitorTask generalEmailPetitionerSolicitorTask;

    @Mock
    private GeneralEmailPetitionerTask generalEmailPetitionerTask;

    @Mock
    private GeneralEmailRespondentSolicitorTask generalEmailRespondentSolicitorTask;

    @Mock
    private GeneralEmailRespondentTask generalEmailRespondentTask;

//    @Test
//    public void shouldGeneralEmail_ToPetitioner() throws WorkflowException {
//        Map<String, Object> caseData = buildCaseData(PETITIONER);
//        CaseDetails caseDetails = setupCaseDetails(caseData);
//        CcdCallbackRequest ccdCallbackRequest = setupCallbakRequest(caseDetails);
//
//        executeAndVerityTask(caseData, ccdCallbackRequest, generalEmailPetitionerTask);
//    }

    @Test
    public void shouldGeneralEmail_ToPetitionerSolicitor() throws WorkflowException {
        Map<String, Object> caseData = buildCaseData(PETITIONER_SOLICITOR);
        CaseDetails caseDetails = setupCaseDetails(caseData);
        CcdCallbackRequest ccdCallbackRequest = setupCallbakRequest(caseDetails);

        executeAndVerityTask(caseData, ccdCallbackRequest, generalEmailPetitionerSolicitorTask);
    }

    private void executeAndVerityTask(Map<String, Object> caseData, CcdCallbackRequest ccdCallbackRequest, Task task) throws WorkflowException {
        mockTasksExecution(caseData, task);

        Map<String, Object> returnedCaseData = executeWorkflow(ccdCallbackRequest);

        verifyTaskWasCalled(returnedCaseData, task);
    }

    private CcdCallbackRequest setupCallbakRequest(CaseDetails caseDetails) {
        return CcdCallbackRequest.builder()
                  .caseDetails(caseDetails)
                  .build();
    }

    private CaseDetails setupCaseDetails(Map<String, Object> caseData) {
        return CaseDetails.builder()
            .caseData(caseData)
            .caseId(TEST_CASE_ID)
            .build();
    }

    private Map<String, Object> buildCaseData(GeneralEmailTaskHelper.Party party) {
        Map<String, Object> caseData = getPartyData(party);

        caseData.put(PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME);

        return caseData;
    }

    private Map<String, Object> executeWorkflow(CcdCallbackRequest ccdCallbackRequest)
        throws WorkflowException {
        Map<String, Object> returnedData = classUnderTest.run(ccdCallbackRequest.getCaseDetails());
        assertThat(returnedData, is(notNullValue()));

        return returnedData;
    }

    private Map<String, Object> getPartyData(GeneralEmailTaskHelper.Party party) {
        switch (party) {
            case PETITIONER:
                return AddresseeDataExtractorTest.buildCaseDataWithPetitioner();
            case PETITIONER_SOLICITOR:
                return AddresseeDataExtractorTest.buildCaseDataWithPetitionerSolicitor();
            case RESPONDENT:
                return AddresseeDataExtractorTest.buildCaseDataWithRespondent();
            case RESPONDENT_SOLICITOR:
                return AddresseeDataExtractorTest.buildCaseDataWithRespondentSolicitor();
            case CO_RESPONDENT:
                return AddresseeDataExtractorTest.buildCaseDataWithCoRespondent();
            case CO_RESPONDENT_SOLICITOR:
                return AddresseeDataExtractorTest.buildCaseDataWithCoRespondentSolicitor();
            case OTHER:
                return AddresseeDataExtractorTest.buildCaseDataWithOtherParty();
            default:
                throw new IllegalArgumentException("CaseData could not be build with invalid Party declaration.");
        }
    }
}