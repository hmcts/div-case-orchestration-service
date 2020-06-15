package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FetchPrintDocsFromDmStore;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendCoRespondentGenericUpdateNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerGenericUpdateNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentGenericUpdateNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CostOrderCoRespondentCoverLetterGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CostOrderCoRespondentSolicitorCoverLetterGenerationTask;

import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_TYPE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_ORDER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_ORDER_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COST_ORDER_CO_RESPONDENT_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_CCD_CODE_FOR_BOTH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_CCD_CODE_FOR_CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_CCD_CODE_FOR_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_COSTS_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.CaseDataTestHelper.createCollectionMemberDocumentAsMap;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTaskWasNeverCalled;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;

@RunWith(MockitoJUnitRunner.class)
public class SendDnPronouncedNotificationWorkflowTest {

    @Mock
    private SendPetitionerGenericUpdateNotificationEmailTask sendPetitionerGenericUpdateNotificationEmailTask;

    @Mock
    private SendRespondentGenericUpdateNotificationEmailTask sendRespondentGenericUpdateNotificationEmailTask;

    @Mock
    private SendCoRespondentGenericUpdateNotificationEmailTask sendCoRespondentGenericUpdateNotificationEmailTask;

    @Mock
    private CostOrderCoRespondentCoverLetterGenerationTask costOrderCoRespondentCoverLetterGenerationTask;

    @Mock
    private CostOrderCoRespondentSolicitorCoverLetterGenerationTask costOrderCoRespondentSolicitorCoverLetterGenerationTask;

    @Mock
    private FetchPrintDocsFromDmStore fetchPrintDocsFromDmStore;

    @Mock
    private BulkPrinterTask bulkPrinterTask;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private SendDnPronouncedNotificationWorkflow sendDnPronouncedNotificationWorkflow;

    @Before
    public void setup() {
        when(costOrderCoRespondentCoverLetterGenerationTask.getDocumentType())
            .thenReturn(COST_ORDER_CO_RESPONDENT_LETTER_DOCUMENT_TYPE);
    }

    @Test
    public void genericEmailTaskShouldExecuteAndReturnPayload() throws Exception {
        Map<String, Object> caseData = ImmutableMap.of("testKey", "testValue");

        mockTasksExecution(
            caseData,
            sendPetitionerGenericUpdateNotificationEmailTask,
            sendRespondentGenericUpdateNotificationEmailTask
        );

        executeWorkflowRun(caseData);

        verifyTasksCalledInOrder(
            caseData,
            sendPetitionerGenericUpdateNotificationEmailTask,
            sendRespondentGenericUpdateNotificationEmailTask
        );
        verifyTaskWasNeverCalled(sendCoRespondentGenericUpdateNotificationEmailTask);
        verifyNoBulkPrintTasksCalled();
    }

    @Test
    public void givenDigitalCaseAndCoRespondentAreLiableForCosts_thenEmailsAreSent() throws Exception {
        Map<String, Object> caseData = ImmutableMap.of(
            WHO_PAYS_COSTS_CCD_FIELD,
            WHO_PAYS_CCD_CODE_FOR_CO_RESPONDENT
        );

        mockTasksExecution(
            caseData,
            sendPetitionerGenericUpdateNotificationEmailTask,
            sendRespondentGenericUpdateNotificationEmailTask,
            sendCoRespondentGenericUpdateNotificationEmailTask
        );

        executeWorkflowRun(caseData);

        verifyTasksCalledInOrder(
            caseData,
            sendPetitionerGenericUpdateNotificationEmailTask,
            sendRespondentGenericUpdateNotificationEmailTask,
            sendCoRespondentGenericUpdateNotificationEmailTask
        );
        verifyNoBulkPrintTasksCalled();
    }

    @Test
    public void givenDigitalCaseAndCoRespondentNotLiableForCosts_thenEmailsAreSent() throws Exception {
        Map<String, Object> caseData = ImmutableMap.of(
            WHO_PAYS_COSTS_CCD_FIELD,
            WHO_PAYS_CCD_CODE_FOR_RESPONDENT
        );

        mockTasksExecution(
            caseData,
            sendPetitionerGenericUpdateNotificationEmailTask,
            sendRespondentGenericUpdateNotificationEmailTask
        );

        executeWorkflowRun(caseData);

        verifyTasksCalledInOrder(
            caseData,
            sendPetitionerGenericUpdateNotificationEmailTask,
            sendRespondentGenericUpdateNotificationEmailTask
        );
        verifyTaskWasNeverCalled(sendCoRespondentGenericUpdateNotificationEmailTask);
        verifyNoBulkPrintTasksCalled();
    }

    @Test
    public void givenDigitalCaseAndRespondentAndCoRespondentAreLiableForCosts_thenEmailsAreSent() throws Exception {
        Map<String, Object> caseData = ImmutableMap.of(
            WHO_PAYS_COSTS_CCD_FIELD,
            WHO_PAYS_CCD_CODE_FOR_BOTH);

        mockTasksExecution(
            caseData,
            sendPetitionerGenericUpdateNotificationEmailTask,
            sendRespondentGenericUpdateNotificationEmailTask,
            sendCoRespondentGenericUpdateNotificationEmailTask
        );

        executeWorkflowRun(caseData);

        verifyTasksCalledInOrder(
            caseData,
            sendPetitionerGenericUpdateNotificationEmailTask,
            sendRespondentGenericUpdateNotificationEmailTask,
            sendCoRespondentGenericUpdateNotificationEmailTask
        );
        verifyNoBulkPrintTasksCalled();
    }

    @Test
    public void givenPaperBased_EmailTasksAreNotCalled() throws Exception {
        Map<String, Object> caseData = ImmutableMap.of(CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL, NO_VALUE);

        executeWorkflowRun(caseData);

        verifyNoBulkPrintTasksCalled();
        verifyNoEmailsSent();
    }


    @Test
    public void givenPaperUpdateToggleIsOff_thenNoBulkPrintTasksAreCalled() throws Exception {
        Map<String, Object> caseData = buildCaseDataWithCoRespondentAsAddressee();

        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(false);

        executeWorkflowRun(caseData);

        verifyNoBulkPrintTasksCalled();
        verifyNoEmailsSent();
    }

    @Test
    public void givenPaperBasedAndPaperUpdateToggledOnAndCostClaimGrantedAndIsNotRepresented_SendCoRespondentCoverLetter() throws Exception {
        Map<String, Object> caseData = buildCaseDataWithCoRespondentAsAddressee();

        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(true);

        mockTasksExecution(
            caseData,
            costOrderCoRespondentCoverLetterGenerationTask,
            fetchPrintDocsFromDmStore,
            bulkPrinterTask
        );

        executeWorkflowRun(caseData);

        verifyTasksCalledInOrder(
            caseData,
            costOrderCoRespondentCoverLetterGenerationTask,
            fetchPrintDocsFromDmStore,
            bulkPrinterTask
        );
        verifyTaskWasNeverCalled(costOrderCoRespondentSolicitorCoverLetterGenerationTask);
        verifyNoEmailsSent();
    }

    @Test
    public void givenPaperBasedAndPaperUpdateToggledOnAndCostClaimGrantedAndIsRepresented_SendCoRespondentSolicitorCoverLetter() throws Exception {
        Map<String, Object> caseData = ImmutableMap.of(
            CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL, NO_VALUE,
            DIVORCE_COSTS_CLAIM_CCD_FIELD, YES_VALUE,
            CO_RESPONDENT_REPRESENTED, YES_VALUE
        );

        mockTasksExecution(
            caseData,
            costOrderCoRespondentSolicitorCoverLetterGenerationTask,
            fetchPrintDocsFromDmStore,
            bulkPrinterTask
        );

        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(true);

        executeWorkflowRun(caseData);

        verifyTasksCalledInOrder(
            caseData,
            costOrderCoRespondentSolicitorCoverLetterGenerationTask,
            fetchPrintDocsFromDmStore,
            bulkPrinterTask
        );
        verifyTaskWasNeverCalled(costOrderCoRespondentCoverLetterGenerationTask);
        verifyNoEmailsSent();
    }

    @Test
    public void givenPaperBasedAndPaperUpdateToggledOnAndCostClaimNotGranted_thenNoBulkPrintTasksAreCalled() throws Exception {
        Map<String, Object> caseData = ImmutableMap.of(
            CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL, NO_VALUE,
            DIVORCE_COSTS_CLAIM_CCD_FIELD, NO_VALUE
        );

        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(true);

        executeWorkflowRun(caseData);

        verifyNoBulkPrintTasksCalled();
        verifyNoEmailsSent();
    }

    @Test
    public void givenPaperBasedAndPaperUpdateToggledOff_thenNoBulkPrintTasksAreCalled() throws Exception {
        Map<String, Object> caseData = ImmutableMap.of(CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL, NO_VALUE);

        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(false);

        executeWorkflowRun(caseData);

        verifyNoBulkPrintTasksCalled();
        verifyNoEmailsSent();
    }

    private void executeWorkflowRun(Map<String, Object> caseData) throws WorkflowException {
        Map<String, Object> returnedPayload = sendDnPronouncedNotificationWorkflow.run(buildCaseDetails(caseData), AUTH_TOKEN);
        assertThat(returnedPayload, is(caseData));
    }

    private Map<String, Object> buildCaseDataWithCoRespondentAsAddressee() {
        Map<String, Object> caseData = AddresseeDataExtractorTest.buildCaseDataWithCoRespondentAsAddressee();
        caseData.put(D8DOCUMENTS_GENERATED, asList(
            createCollectionMemberDocumentAsMap("http://dn-pronounced.com", COSTS_ORDER_DOCUMENT_TYPE, COSTS_ORDER_TEMPLATE_ID)
        ));
        return caseData;
    }

    private CaseDetails buildCaseDetails(Map<String, Object> casePayload) {
        return CaseDetails.builder()
            .caseId(CASE_TYPE_ID)
            .caseData(casePayload)
            .build();
    }

    private void verifyNoEmailsSent() throws TaskException {
        verifyTaskWasNeverCalled(sendPetitionerGenericUpdateNotificationEmailTask);
        verifyTaskWasNeverCalled(sendRespondentGenericUpdateNotificationEmailTask);
        verifyTaskWasNeverCalled(sendCoRespondentGenericUpdateNotificationEmailTask);
    }

    private void verifyNoBulkPrintTasksCalled() throws TaskException {
        verifyTaskWasNeverCalled(costOrderCoRespondentSolicitorCoverLetterGenerationTask);
        verifyTaskWasNeverCalled(costOrderCoRespondentCoverLetterGenerationTask);
        verifyTaskWasNeverCalled(fetchPrintDocsFromDmStore);
        verifyTaskWasNeverCalled(bulkPrinterTask);
    }
}
