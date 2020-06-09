package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendCoRespondentGenericUpdateNotificationEmail;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerGenericUpdateNotificationEmail;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentGenericUpdateNotificationEmail;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.SendCostOrderGenerationTask;

import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_TYPE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_CLAIM_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_CCD_CODE_FOR_BOTH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_CCD_CODE_FOR_CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_CCD_CODE_FOR_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_COSTS_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.CaseDataTestHelper.createCollectionMemberDocumentAsMap;

@RunWith(MockitoJUnitRunner.class)
public class SendDnPronouncedNotificationWorkflowTest {

    @Mock
    private SendPetitionerGenericUpdateNotificationEmail sendPetitionerGenericUpdateNotificationEmail;

    @Mock
    private SendRespondentGenericUpdateNotificationEmail sendRespondentGenericUpdateNotificationEmail;

    @Mock
    private SendCoRespondentGenericUpdateNotificationEmail sendCoRespondentGenericUpdateNotificationEmail;

    @Mock
    private SendCostOrderGenerationTask sendCostOrderGenerationTask;

    @InjectMocks
    private SendDnPronouncedNotificationWorkflow sendDnPronouncedNotificationWorkflow;

    private CcdCallbackRequest ccdCallbackRequestRequest;
    private TaskContext context;

    @Before
    public void setup() { // TODO use same casedata for all
        CaseDetails caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .caseData(buildCaseData(YES_VALUE, NO_VALUE))
                .state(TEST_STATE)
                .build();
        ccdCallbackRequestRequest =
                CcdCallbackRequest.builder()
                        .eventId(TEST_EVENT_ID)
                        .token(TEST_TOKEN)
                        .caseDetails(
                                caseDetails
                        )
                        .build();

        context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
    }

    @Test
    public void genericEmailTaskShouldExecuteAndReturnPayload() throws Exception {
        Map<String, Object> testPayload = singletonMap("testKey", "testValue");
        ccdCallbackRequestRequest.getCaseDetails().setCaseData(testPayload);

        when(sendPetitionerGenericUpdateNotificationEmail.execute(notNull(), eq(testPayload)))
                .thenReturn(testPayload);

        when(sendRespondentGenericUpdateNotificationEmail.execute(notNull(), eq(testPayload)))
                .thenReturn(testPayload);

        Map<String, Object> returnedPayload = sendDnPronouncedNotificationWorkflow.run(ccdCallbackRequestRequest);
        assertThat(returnedPayload, is(equalTo(testPayload)));

        verify(sendPetitionerGenericUpdateNotificationEmail).execute(context, testPayload);
        verify(sendRespondentGenericUpdateNotificationEmail).execute(context, testPayload);
        verify(sendCoRespondentGenericUpdateNotificationEmail, never()).execute(context, testPayload);
    }

    @Test
    public void givenWhoPaysCostsIsRespondent_whenWorkflowExecutes_thenSendGenericEmails() throws Exception {
        Map<String, Object> testPayload = singletonMap(WHO_PAYS_COSTS_CCD_FIELD, WHO_PAYS_CCD_CODE_FOR_RESPONDENT);
        ccdCallbackRequestRequest.getCaseDetails().setCaseData(testPayload);

        when(sendPetitionerGenericUpdateNotificationEmail.execute(notNull(), eq(testPayload)))
                .thenReturn(testPayload);

        when(sendRespondentGenericUpdateNotificationEmail.execute(notNull(), eq(testPayload)))
                .thenReturn(testPayload);

        Map<String, Object> returnedPayload = sendDnPronouncedNotificationWorkflow.run(ccdCallbackRequestRequest);
        assertThat(returnedPayload, is(equalTo(testPayload)));

        verify(sendPetitionerGenericUpdateNotificationEmail).execute(context, testPayload);
        verify(sendRespondentGenericUpdateNotificationEmail).execute(context, testPayload);
        verify(sendCoRespondentGenericUpdateNotificationEmail, never()).execute(context, testPayload);
    }

    @Test
    public void givenWhoPaysCostsIsCoRespondent_whenWorkflowExecutes_thenSendGenericEmails() throws Exception {
        Map<String, Object> testPayload = singletonMap(WHO_PAYS_COSTS_CCD_FIELD, WHO_PAYS_CCD_CODE_FOR_CO_RESPONDENT);
        ccdCallbackRequestRequest.getCaseDetails().setCaseData(testPayload);

        when(sendPetitionerGenericUpdateNotificationEmail.execute(notNull(), eq(testPayload)))
                .thenReturn(testPayload);

        when(sendRespondentGenericUpdateNotificationEmail.execute(notNull(), eq(testPayload)))
                .thenReturn(testPayload);

        when(sendCoRespondentGenericUpdateNotificationEmail.execute(notNull(), eq(testPayload)))
                .thenReturn(testPayload);

        Map<String, Object> returnedPayload = sendDnPronouncedNotificationWorkflow.run(ccdCallbackRequestRequest);
        assertThat(returnedPayload, is(equalTo(testPayload)));

        verify(sendPetitionerGenericUpdateNotificationEmail).execute(context, testPayload);
        verify(sendRespondentGenericUpdateNotificationEmail).execute(context, testPayload);
        verify(sendCoRespondentGenericUpdateNotificationEmail).execute(context, testPayload);
    }

    @Test
    public void givenWhoPaysCostsIsRespondentAndCoRespondent_whenWorkflowExecutes_thenSendGenericEmails() throws Exception {
        Map<String, Object> testPayload = singletonMap(WHO_PAYS_COSTS_CCD_FIELD, WHO_PAYS_CCD_CODE_FOR_BOTH);
        ccdCallbackRequestRequest.getCaseDetails().setCaseData(testPayload);

        when(sendPetitionerGenericUpdateNotificationEmail.execute(notNull(), eq(testPayload)))
                .thenReturn(testPayload);

        when(sendRespondentGenericUpdateNotificationEmail.execute(notNull(), eq(testPayload)))
                .thenReturn(testPayload);

        when(sendCoRespondentGenericUpdateNotificationEmail.execute(notNull(), eq(testPayload)))
                .thenReturn(testPayload);

        Map<String, Object> returnedPayload = sendDnPronouncedNotificationWorkflow.run(ccdCallbackRequestRequest);
        assertThat(returnedPayload, is(equalTo(testPayload)));

        verify(sendPetitionerGenericUpdateNotificationEmail).execute(context, testPayload);
        verify(sendRespondentGenericUpdateNotificationEmail).execute(context, testPayload);
        verify(sendCoRespondentGenericUpdateNotificationEmail).execute(context, testPayload);
    }

    @Test
    public void genericEmailTaskShouldExecuteAndReturnPayloadWhenCoRespondentIsDigital() throws Exception {

        Map<String, Object> caseData = buildCaseData(YES_VALUE, NO_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData);

        ccdCallbackRequestRequest.setCaseDetails(caseDetails);
        String caseId = ccdCallbackRequestRequest.getCaseDetails().getCaseId();

        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, caseId);

        when(sendPetitionerGenericUpdateNotificationEmail.execute(notNull(), eq(caseData)))
            .thenReturn(caseData);

        when(sendRespondentGenericUpdateNotificationEmail.execute(notNull(), eq(caseData)))
            .thenReturn(caseData);

        Map<String, Object> returnedPayload = sendDnPronouncedNotificationWorkflow.run(ccdCallbackRequestRequest);
        assertThat(returnedPayload, is(equalTo(caseData)));

        verify(sendPetitionerGenericUpdateNotificationEmail).execute(context, caseData);
        verify(sendRespondentGenericUpdateNotificationEmail).execute(context, caseData);
        verify(sendCoRespondentGenericUpdateNotificationEmail, never()).execute(context, caseData);
    }

    @Test
    public void genericEmailTaskShouldNotExecuteWhenCoRespondentIsNotDigital() throws Exception {

        Map<String, Object> caseData = buildCaseData(NO_VALUE, NO_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData);

        ccdCallbackRequestRequest.setCaseDetails(caseDetails);
        String caseId = ccdCallbackRequestRequest.getCaseDetails().getCaseId();

        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, caseId);

        when(sendPetitionerGenericUpdateNotificationEmail.execute(notNull(), eq(caseData)))
            .thenReturn(caseData);

        when(sendRespondentGenericUpdateNotificationEmail.execute(notNull(), eq(caseData)))
            .thenReturn(caseData);

        Map<String, Object> returnedPayload = sendDnPronouncedNotificationWorkflow.run(ccdCallbackRequestRequest);
        assertThat(returnedPayload, is(equalTo(caseData)));

        verify(sendPetitionerGenericUpdateNotificationEmail, never()).execute(context, caseData);
        verify(sendRespondentGenericUpdateNotificationEmail, never()).execute(context, caseData);
        verify(sendCoRespondentGenericUpdateNotificationEmail, never()).execute(context, caseData);
    }

    @Test
    public void givenCoRespondentIsNotDigitalAndCostsClaimIsNotGranted_thenBulkPrintIsNotCalled() throws Exception {
        Map<String, Object> caseData = buildCaseData(NO_VALUE, NO_VALUE);

        CaseDetails caseDetails = buildCaseDetails(caseData);

        ccdCallbackRequestRequest.setCaseDetails(caseDetails);
        String caseId = ccdCallbackRequestRequest.getCaseDetails().getCaseId();

        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, caseId);

        when(sendPetitionerGenericUpdateNotificationEmail.execute(notNull(), eq(caseData)))
            .thenReturn(caseData);

        when(sendRespondentGenericUpdateNotificationEmail.execute(notNull(), eq(caseData)))
            .thenReturn(caseData);

        Map<String, Object> returnedPayload = sendDnPronouncedNotificationWorkflow.run(ccdCallbackRequestRequest);
        assertThat(returnedPayload, is(equalTo(caseData)));

        verify(sendPetitionerGenericUpdateNotificationEmail, never()).execute(context, caseData);
        verify(sendRespondentGenericUpdateNotificationEmail, never()).execute(context, caseData);
        verify(sendCoRespondentGenericUpdateNotificationEmail, never()).execute(context, caseData);
        verify(sendCostOrderGenerationTask, never()).execute(context, caseData);
        // TODO add solicitor task
    }


    private Map<String, Object> buildCaseData(String isDigital, String isCostClaimGranted) {
        return ImmutableMap.of(
            CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL, isDigital,
            DIVORCE_COSTS_CLAIM_CCD_FIELD, isCostClaimGranted,
            D8DOCUMENTS_GENERATED, asList( // TODO change generated document to DNPronouned...
                createCollectionMemberDocumentAsMap("http://dn-pronounced.com", DECREE_ABSOLUTE_DOCUMENT_TYPE, DECREE_ABSOLUTE_FILENAME)
            )
        );
    }

    private CaseDetails buildCaseDetails(Map<String, Object> casePayload) {
        return CaseDetails.builder()
            .caseId(CASE_TYPE_ID)
            .caseData(casePayload)
            .build();
    }
}
