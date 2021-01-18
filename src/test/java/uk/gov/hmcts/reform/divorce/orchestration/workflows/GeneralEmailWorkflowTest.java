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
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
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
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper.Party.CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper.Party.CO_RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper.Party.OTHER;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper.Party.PETITIONER;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper.Party.PETITIONER_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper.Party.RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper.Party.RESPONDENT_SOLICITOR;
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

    private Map<String, Object> caseData;

    @Test
    public void shouldTriggerGeneralEmail_ToPetitionerSolicitor() throws WorkflowException {
        caseData = buildCaseData(PETITIONER_SOLICITOR);

        CaseDetails caseDetails = setupCaseDetails(caseData);
        CcdCallbackRequest ccdCallbackRequest = setupCallbackRequest(caseDetails);

        executeAndVerityTask(caseData, ccdCallbackRequest, generalEmailPetitionerSolicitorTask);
    }

    @Test
    public void shouldTriggerGeneralEmail_ToRespondentSolicitor() throws WorkflowException {
        caseData = buildCaseData(RESPONDENT_SOLICITOR);

        CaseDetails caseDetails = setupCaseDetails(caseData);
        CcdCallbackRequest ccdCallbackRequest = setupCallbackRequest(caseDetails);

        executeAndVerityTask(caseData, ccdCallbackRequest, generalEmailRespondentSolicitorTask);
    }

    @Test
    public void shouldTriggerGeneralEmail_ToCoRespondentSolicitor() throws WorkflowException {
        caseData = buildCaseData(CO_RESPONDENT_SOLICITOR);

        CaseDetails caseDetails = setupCaseDetails(caseData);
        CcdCallbackRequest ccdCallbackRequest = setupCallbackRequest(caseDetails);

        executeAndVerityTask(caseData, ccdCallbackRequest, generalEmailCoRespondentSolicitorTask);
    }

    @Test
    public void shouldTriggerGeneralEmail_ToRespondent() throws WorkflowException {
        caseData = buildCaseData(RESPONDENT);

        caseData.put(RESP_IS_USING_DIGITAL_CHANNEL, YES_VALUE);

        CaseDetails caseDetails = setupCaseDetails(caseData);
        CcdCallbackRequest ccdCallbackRequest = setupCallbackRequest(caseDetails);

        executeAndVerityTask(caseData, ccdCallbackRequest, generalEmailRespondentTask);
    }

    @Test
    public void shouldTriggerGeneralEmail_ToCoRespondent() throws WorkflowException {
        caseData = buildCaseData(CO_RESPONDENT);

        caseData.put(CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL, YES_VALUE);

        CaseDetails caseDetails = setupCaseDetails(caseData);
        CcdCallbackRequest ccdCallbackRequest = setupCallbackRequest(caseDetails);

        executeAndVerityTask(caseData, ccdCallbackRequest, generalEmailCoRespondentTask);
    }

    @Test
    public void shouldTriggerGeneralEmail_ToPetitioner() throws WorkflowException {
        caseData = buildCaseData(PETITIONER);

        caseData.put(D_8_PETITIONER_EMAIL, TEST_PETITIONER_EMAIL);

        CaseDetails caseDetails = setupCaseDetails(caseData);
        CcdCallbackRequest ccdCallbackRequest = setupCallbackRequest(caseDetails);

        executeAndVerityTask(caseData, ccdCallbackRequest, generalEmailPetitionerTask);
    }

    @Test
    public void shouldTriggerGeneralEmail_ToOtherParty() throws WorkflowException {
        caseData = buildCaseData(OTHER);

        CaseDetails caseDetails = setupCaseDetails(caseData);
        CcdCallbackRequest ccdCallbackRequest = setupCallbackRequest(caseDetails);

        executeAndVerityTask(caseData, ccdCallbackRequest, generalEmailOtherPartyTask);
    }

    private void executeAndVerityTask(Map<String, Object> caseData, CcdCallbackRequest ccdCallbackRequest, Task task) throws WorkflowException {
        mockTasksExecution(caseData, task);

        Map<String, Object> returnedCaseData = executeWorkflow(ccdCallbackRequest);

        verifyTaskWasCalled(returnedCaseData, task);
    }

    private CcdCallbackRequest setupCallbackRequest(CaseDetails caseDetails) {
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
        caseData = getPartyData(party);

        caseData.put(PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME);

        caseData.put(CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL, NO_VALUE);
        caseData.put(RESP_IS_USING_DIGITAL_CHANNEL, NO_VALUE);

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
                return AddresseeDataExtractorTest.buildCaseDataWithPetitionerCorrespondenceAddressButNoHomeAddress();
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
                throw new TaskException("CaseData could not be build with invalid Party declaration.");
        }
    }
}