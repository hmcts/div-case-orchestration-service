package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.exceptions.verification.WantedButNotInvoked;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FetchPrintDocsFromDmStoreTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendCoRespondentGenericUpdateNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendDecreeNisiGrantedPetitionerSolicitorNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendDecreeNisiGrantedRespondentSolicitorNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerGenericUpdateNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentGenericUpdateNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CostOrderCoRespondentCoverLetterGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CostOrderCoRespondentSolicitorCoverLetterGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.DnGrantedRespondentCoverLetterGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.DnGrantedRespondentSolicitorCoverLetterGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.MultiBulkPrinterTask;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;

import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RESPONDENT_SOLICITOR_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_TYPE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_ORDER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_SOLICITOR_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_CCD_CODE_FOR_CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_CCD_CODE_FOR_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_COSTS_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest.buildCaseDataWithPetitionerSolicitor;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest.buildCaseDataWithRespondent;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest.buildCaseDataWithRespondentSolicitor;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractor.CaseDataKeys.COSTS_CLAIM_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.CaseDataTestHelper.buildOrganisationPolicy;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.CaseDataTestHelper.createCollectionMemberDocumentAsMap;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTaskWasCalled;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTaskWasNeverCalled;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;

@RunWith(MockitoJUnitRunner.class)
public class SendDnPronouncedNotificationWorkflowTest {

    @InjectMocks
    private SendDnPronouncedNotificationWorkflow sendDnPronouncedNotificationWorkflow;

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
    private DnGrantedRespondentCoverLetterGenerationTask dnGrantedRespondentCoverLetterGenerationTask;

    @Mock
    private DnGrantedRespondentSolicitorCoverLetterGenerationTask dnGrantedRespondentSolicitorCoverLetterGenerationTask;

    @Mock
    private SendDecreeNisiGrantedPetitionerSolicitorNotificationEmailTask sendDecreeNisiGrantedPetitionerSolicitorNotificationEmailTask;

    @Mock
    private SendDecreeNisiGrantedRespondentSolicitorNotificationEmailTask sendDecreeNisiGrantedRespondentSolicitorNotificationEmailTask;

    @Mock
    private FetchPrintDocsFromDmStoreTask fetchPrintDocsFromDmStoreTask;

    @Mock
    private MultiBulkPrinterTask multiBulkPrinterTask;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private CaseDataUtils caseDataUtils;

    private static final String COSTS_ORDER_TEMPLATE_ID = "FL-DIV-DEC-ENG-00060.docx";

    @Before
    public void setup() {
        when(costOrderCoRespondentCoverLetterGenerationTask.getDocumentType())
            .thenReturn(CostOrderCoRespondentCoverLetterGenerationTask.FileMetadata.DOCUMENT_TYPE);
        when(costOrderCoRespondentSolicitorCoverLetterGenerationTask.getDocumentType())
            .thenReturn(CostOrderCoRespondentSolicitorCoverLetterGenerationTask.FileMetadata.DOCUMENT_TYPE);
        when(dnGrantedRespondentCoverLetterGenerationTask.getDocumentType())
            .thenReturn(DnGrantedRespondentCoverLetterGenerationTask.FileMetadata.DOCUMENT_TYPE);
        when(dnGrantedRespondentSolicitorCoverLetterGenerationTask.getDocumentType())
            .thenReturn(DnGrantedRespondentSolicitorCoverLetterGenerationTask.FileMetadata.DOCUMENT_TYPE);
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
    public void givenPaperBased_OnlyEmailToPetitionerSentAndBulkPrintCalled() throws Exception {
        Map<String, Object> caseData = buildCaseDataWithRespondentSolicitor();
        caseData.putAll(
            ImmutableMap.of(
                CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL, NO_VALUE,
                RESP_IS_USING_DIGITAL_CHANNEL, NO_VALUE
            )
        );

        mockTasksExecution(
            caseData,
            sendPetitionerGenericUpdateNotificationEmailTask,
            dnGrantedRespondentSolicitorCoverLetterGenerationTask,
            fetchPrintDocsFromDmStoreTask,
            multiBulkPrinterTask
        );

        executeWorkflowRun(caseData);

        verifyTasksCalledInOrder(
            caseData,
            sendPetitionerGenericUpdateNotificationEmailTask,
            dnGrantedRespondentSolicitorCoverLetterGenerationTask,
            fetchPrintDocsFromDmStoreTask,
            multiBulkPrinterTask
        );
        verifyTaskWasNeverCalled(sendRespondentGenericUpdateNotificationEmailTask);
        verifyTaskWasNeverCalled(sendCoRespondentGenericUpdateNotificationEmailTask);
        verifyTaskWasNeverCalled(dnGrantedRespondentCoverLetterGenerationTask);
    }

    @Test
    public void givenToggleIsOffAndNoCoResp_thenSendEmailsToDivorcingParties() throws Exception {
        Map<String, Object> caseData = buildCaseDataWithCoRespondentAsAddressee();

        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(false);
        mockTasksExecution(
            caseData,
            sendPetitionerGenericUpdateNotificationEmailTask,
            sendRespondentGenericUpdateNotificationEmailTask
        );

        executeWorkflowRun(caseData);

        verifyNoBulkPrintTasksCalled();
        verifyTasksCalledInOrder(
            caseData,
            sendPetitionerGenericUpdateNotificationEmailTask,
            sendRespondentGenericUpdateNotificationEmailTask
        );
    }

    @Test
    public void givenToggleIsOffAndCoRespPaper_thenSendEmailsAndCallBulkPrinting() throws Exception {
        Map<String, Object> caseData = buildCaseDataWithCoRespondentAsAddressee();

        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(false);
        when(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(eq(caseData))).thenReturn(true);
        mockTasksExecution(
            caseData,
            sendPetitionerGenericUpdateNotificationEmailTask,
            sendRespondentGenericUpdateNotificationEmailTask,
            costOrderCoRespondentCoverLetterGenerationTask,
            fetchPrintDocsFromDmStoreTask,
            multiBulkPrinterTask
        );

        executeWorkflowRun(caseData);

        verifyTasksCalledInOrder(
            caseData,
            sendPetitionerGenericUpdateNotificationEmailTask,
            sendRespondentGenericUpdateNotificationEmailTask,
            costOrderCoRespondentCoverLetterGenerationTask,
            fetchPrintDocsFromDmStoreTask,
            multiBulkPrinterTask
        );
    }

    @Test
    public void givenToggleIsOffAndCoRespDigitalAndLiableForCosts_thenSendEmailsToAll() throws Exception {
        Map<String, Object> caseData = buildCaseDataWithCoRespondentAsAddressee();
        caseData.put(CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL, YES_VALUE);
        caseData.put(WHO_PAYS_COSTS_CCD_FIELD, WHO_PAYS_CCD_CODE_FOR_CO_RESPONDENT);

        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(false);
        when(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(eq(caseData))).thenReturn(true);
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
    public void givenToggleIsOffAndCostsClaimNotGranted_thenSendEmailsToAll() throws Exception {
        Map<String, Object> caseData = buildCaseDataWithCoRespondentAsAddressee();
        caseData.put(COSTS_CLAIM_GRANTED, NO_VALUE);
        caseData.put(CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL, YES_VALUE);
        caseData.put(WHO_PAYS_COSTS_CCD_FIELD, WHO_PAYS_CCD_CODE_FOR_CO_RESPONDENT);

        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(false);
        when(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(eq(caseData))).thenReturn(true);

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
    public void givenCostsClaimGrantedAndOffline_thenSendEmailToPetitionerAndSendDocsToBulkPrint()
        throws Exception {
        Map<String, Object> caseData = buildCaseDataWithCoRespondentAsAddressee();
        caseData.putAll(buildCaseDataWithRespondent());
        caseData.put(RESP_IS_USING_DIGITAL_CHANNEL, NO_VALUE);
        caseData.put(CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL, NO_VALUE);
        caseData.put(WHO_PAYS_COSTS_CCD_FIELD, WHO_PAYS_CCD_CODE_FOR_CO_RESPONDENT);
        caseData.put(CO_RESPONDENT_REPRESENTED, NO_VALUE);
        caseData.put(RESP_SOL_REPRESENTED, NO_VALUE);

        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(true);
        when(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(eq(caseData))).thenReturn(true);

        mockTasksExecution(
            caseData,
            sendPetitionerGenericUpdateNotificationEmailTask,
            dnGrantedRespondentCoverLetterGenerationTask,
            costOrderCoRespondentCoverLetterGenerationTask,
            fetchPrintDocsFromDmStoreTask,
            multiBulkPrinterTask
        );

        executeWorkflowRun(caseData);

        verifyTasksCalledInOrder(
            caseData,
            sendPetitionerGenericUpdateNotificationEmailTask,
            dnGrantedRespondentCoverLetterGenerationTask,
            costOrderCoRespondentCoverLetterGenerationTask,
            fetchPrintDocsFromDmStoreTask,
            multiBulkPrinterTask
        );
        verifyTaskWasNeverCalled(sendRespondentGenericUpdateNotificationEmailTask);
        verifyTaskWasNeverCalled(sendCoRespondentGenericUpdateNotificationEmailTask);
        verifyTaskWasNeverCalled(dnGrantedRespondentSolicitorCoverLetterGenerationTask);
        verifyTaskWasNeverCalled(costOrderCoRespondentSolicitorCoverLetterGenerationTask);
    }

    @Test
    public void givenCostsClaimGrantedAndOfflineRespAndCoRespRepresented_thenSendEmailToPetitionerAndSendDocsToBulkPrint()
        throws Exception {
        Map<String, Object> caseData = buildCaseDataWithCoRespondentAsAddressee();
        caseData.putAll(buildCaseDataWithRespondentSolicitor());
        caseData.put(RESP_IS_USING_DIGITAL_CHANNEL, NO_VALUE);
        caseData.put(CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL, NO_VALUE);
        caseData.put(WHO_PAYS_COSTS_CCD_FIELD, WHO_PAYS_CCD_CODE_FOR_CO_RESPONDENT);
        caseData.put(CO_RESPONDENT_REPRESENTED, YES_VALUE);

        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(true);
        when(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(eq(caseData))).thenReturn(true);

        mockTasksExecution(
            caseData,
            sendPetitionerGenericUpdateNotificationEmailTask,
            dnGrantedRespondentSolicitorCoverLetterGenerationTask,
            costOrderCoRespondentSolicitorCoverLetterGenerationTask,
            fetchPrintDocsFromDmStoreTask,
            multiBulkPrinterTask
        );

        executeWorkflowRun(caseData);

        verifyTasksCalledInOrder(
            caseData,
            sendPetitionerGenericUpdateNotificationEmailTask,
            dnGrantedRespondentSolicitorCoverLetterGenerationTask,
            costOrderCoRespondentSolicitorCoverLetterGenerationTask,
            fetchPrintDocsFromDmStoreTask,
            multiBulkPrinterTask
        );
        verifyTaskWasNeverCalled(sendRespondentGenericUpdateNotificationEmailTask);
        verifyTaskWasNeverCalled(sendCoRespondentGenericUpdateNotificationEmailTask);
        verifyTaskWasNeverCalled(dnGrantedRespondentCoverLetterGenerationTask);
        verifyTaskWasNeverCalled(costOrderCoRespondentCoverLetterGenerationTask);
    }

    @Test
    public void givenToggleIsOffAndCoRespDigitalAndNotLiableForCosts_thenSendEmailsToDivorcingParties() throws Exception {
        Map<String, Object> caseData = buildCaseDataWithCoRespondentAsAddressee();
        caseData.put(CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL, YES_VALUE);
        caseData.put(WHO_PAYS_COSTS_CCD_FIELD, "Not me!");

        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(false);
        when(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(eq(caseData))).thenReturn(true);
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
    public void givenPaperBasedAndPaperUpdateToggledOnAndCostClaimGrantedAndIsNotRepresented_SendCoRespondentCoverLetter() throws Exception {
        Map<String, Object> caseData = buildCaseDataWithCoRespondentAsAddressee();

        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(true);
        when(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(eq(caseData))).thenReturn(true);

        mockTasksExecution(
            caseData,
            sendPetitionerGenericUpdateNotificationEmailTask,
            sendRespondentGenericUpdateNotificationEmailTask,
            costOrderCoRespondentCoverLetterGenerationTask,
            fetchPrintDocsFromDmStoreTask,
            multiBulkPrinterTask
        );

        executeWorkflowRun(caseData);

        verifyTasksCalledInOrder(
            caseData,
            sendPetitionerGenericUpdateNotificationEmailTask,
            sendRespondentGenericUpdateNotificationEmailTask,
            costOrderCoRespondentCoverLetterGenerationTask,
            fetchPrintDocsFromDmStoreTask,
            multiBulkPrinterTask
        );
        verifyTaskWasNeverCalled(costOrderCoRespondentSolicitorCoverLetterGenerationTask);
        verifyTaskWasNeverCalled(sendCoRespondentGenericUpdateNotificationEmailTask);
    }

    @Test
    public void givenPaperBasedAndPaperUpdateToggledOnAndCostClaimGrantedAndIsRepresented_SendCoRespondentSolicitorCoverLetter() throws Exception {
        Map<String, Object> caseData = ImmutableMap.of(
            CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL, NO_VALUE,
            COSTS_CLAIM_GRANTED, YES_VALUE,
            CO_RESPONDENT_REPRESENTED, YES_VALUE
        );

        mockTasksExecution(
            caseData,
            sendPetitionerGenericUpdateNotificationEmailTask,
            sendRespondentGenericUpdateNotificationEmailTask,
            costOrderCoRespondentSolicitorCoverLetterGenerationTask,
            fetchPrintDocsFromDmStoreTask,
            multiBulkPrinterTask
        );

        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(true);
        when(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(eq(caseData))).thenReturn(true);

        executeWorkflowRun(caseData);

        verifyTasksCalledInOrder(
            caseData,
            sendPetitionerGenericUpdateNotificationEmailTask,
            sendRespondentGenericUpdateNotificationEmailTask,
            costOrderCoRespondentSolicitorCoverLetterGenerationTask,
            fetchPrintDocsFromDmStoreTask,
            multiBulkPrinterTask
        );
        verifyTaskWasNeverCalled(costOrderCoRespondentCoverLetterGenerationTask);
        verifyTaskWasNeverCalled(sendCoRespondentGenericUpdateNotificationEmailTask);
    }

    @Test
    public void givenPaperBasedAndPaperUpdateToggledOnAndCostClaimNotGranted_thenNoBulkPrintTasksAreCalled() throws Exception {
        Map<String, Object> caseData = ImmutableMap.of(
            COSTS_CLAIM_GRANTED, NO_VALUE
        );

        mockTasksExecution(
            caseData,
            sendPetitionerGenericUpdateNotificationEmailTask,
            sendRespondentGenericUpdateNotificationEmailTask
        );

        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(true);

        executeWorkflowRun(caseData);

        verifyNoBulkPrintTasksCalled();
        verifyTasksCalledInOrder(
            caseData,
            sendPetitionerGenericUpdateNotificationEmailTask,
            sendRespondentGenericUpdateNotificationEmailTask
        );
    }

    @Test
    public void givenPaperBasedAndPaperUpdateToggledOff_thenNoBulkPrintTasksAreCalled() throws Exception {
        Map<String, Object> caseData = ImmutableMap.of(CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL, NO_VALUE);

        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(false);

        mockTasksExecution(
            caseData,
            sendPetitionerGenericUpdateNotificationEmailTask,
            sendRespondentGenericUpdateNotificationEmailTask
        );

        executeWorkflowRun(caseData);

        verifyNoBulkPrintTasksCalled();
        verifyTasksCalledInOrder(
            caseData,
            sendPetitionerGenericUpdateNotificationEmailTask,
            sendRespondentGenericUpdateNotificationEmailTask
        );
    }

    @Test
    public void givenPetitionerIsRepresentedBySolicitor_thenSendDecreeNisiNotificationToPetitionerSolicitor() throws Exception {
        Map<String, Object> caseData = buildCaseDataWithPetitionerSolicitor();

        when(featureToggleService.isFeatureEnabled(Features.RESPONDENT_SOLICITOR_DETAILS)).thenReturn(true);

        CaseDetails caseDetails = setupCaseDetails(caseData);

        executeAndVerityTask(caseData, caseDetails, sendDecreeNisiGrantedPetitionerSolicitorNotificationEmailTask);
    }

    @Test(expected = WantedButNotInvoked.class)
    public void givenFeatureToggle_Respondent_Solicitor_Details_isFalse_doNotSendDecreeNisiNotificationToPetitionerSolicitor() throws Exception {
        Map<String, Object> caseData = buildCaseDataWithPetitionerSolicitor();

        when(featureToggleService.isFeatureEnabled(Features.RESPONDENT_SOLICITOR_DETAILS)).thenReturn(false);

        CaseDetails caseDetails = setupCaseDetails(caseData);

        executeAndVerityTask(caseData, caseDetails, sendDecreeNisiGrantedPetitionerSolicitorNotificationEmailTask);
    }

    @Test
    public void givenRespondentIsRepresentedBySolicitor_thenSendDecreeNisiNotificationToRespondentSolicitor() throws Exception {
        Map<String, Object> caseData = buildCaseDataWithRespondentSolicitor();
        caseData.put(RESPONDENT_SOLICITOR_ORGANISATION_POLICY, buildOrganisationPolicy());

        when(featureToggleService.isFeatureEnabled(Features.RESPONDENT_SOLICITOR_DETAILS)).thenReturn(true);

        CaseDetails caseDetails = setupCaseDetails(caseData);

        executeAndVerityTask(caseData, caseDetails, sendDecreeNisiGrantedRespondentSolicitorNotificationEmailTask);
    }

    @Test(expected = WantedButNotInvoked.class)
    public void givenFeatureToggle_Respondent_Solicitor_Details_isFalse_doNotSendDecreeNisiNotificationToRespondentSolicitor() throws Exception {
        Map<String, Object> caseData = buildCaseDataWithPetitionerSolicitor();
        caseData.put(RESPONDENT_SOLICITOR_EMAIL_ADDRESS, TEST_RESPONDENT_SOLICITOR_EMAIL);
        caseData.put(RESP_SOL_REPRESENTED, YES_VALUE);

        when(featureToggleService.isFeatureEnabled(Features.RESPONDENT_SOLICITOR_DETAILS)).thenReturn(false);

        CaseDetails caseDetails = setupCaseDetails(caseData);

        executeAndVerityTask(caseData, caseDetails, sendDecreeNisiGrantedRespondentSolicitorNotificationEmailTask);
    }

    private void executeWorkflowRun(Map<String, Object> caseData) throws WorkflowException {
        Map<String, Object> returnedPayload = sendDnPronouncedNotificationWorkflow.run(buildCaseDetails(caseData), AUTH_TOKEN);
        assertThat(returnedPayload, is(caseData));
    }

    private Map<String, Object> buildCaseDataWithCoRespondentAsAddressee() {
        Map<String, Object> caseData = AddresseeDataExtractorTest.buildCaseDataWithCoRespondent();
        caseData.putAll(buildCaseDataWithRespondent());
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

    private void verifyNoBulkPrintTasksCalled() throws TaskException {
        verifyTaskWasNeverCalled(costOrderCoRespondentSolicitorCoverLetterGenerationTask);
        verifyTaskWasNeverCalled(costOrderCoRespondentCoverLetterGenerationTask);
        verifyTaskWasNeverCalled(dnGrantedRespondentSolicitorCoverLetterGenerationTask);
        verifyTaskWasNeverCalled(dnGrantedRespondentCoverLetterGenerationTask);
        verifyTaskWasNeverCalled(fetchPrintDocsFromDmStoreTask);
        verifyTaskWasNeverCalled(multiBulkPrinterTask);
    }

    private Map<String, Object> executeWorkflow(CaseDetails caseDetails)
        throws WorkflowException {
        Map<String, Object> returnedData = sendDnPronouncedNotificationWorkflow.run(caseDetails, AUTH_TOKEN);
        assertThat(returnedData, is(notNullValue()));

        return returnedData;
    }

    private void executeAndVerityTask(Map<String, Object> caseData, CaseDetails caseDetails, Task task) throws WorkflowException {
        mockTasksExecution(caseData, task);

        Map<String, Object> returnedCaseData = executeWorkflow(caseDetails);

        verifyTaskWasCalled(returnedCaseData, task);
    }

    private CaseDetails setupCaseDetails(Map<String, Object> caseData) {
        return CaseDetails.builder()
            .caseData(caseData)
            .caseId(TEST_CASE_ID)
            .build();
    }
}
