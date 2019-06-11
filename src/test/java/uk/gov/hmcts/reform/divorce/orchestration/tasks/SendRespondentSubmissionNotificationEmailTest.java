package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.Invocation;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.D8_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_COURT_ADDRESS_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_HUSBAND_OR_WIFE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RDC_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.RESPONDENT_DEFENDED_AOS_SUBMISSION_NOTIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.RESPONDENT_UNDEFENDED_AOS_SUBMISSION_NOTIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

@RunWith(MockitoJUnitRunner.class)
public class SendRespondentSubmissionNotificationEmailTest {

    private static final String FORM_SUBMISSION_DUE_DATE = "20 September 2018";

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private TaskCommons taskCommons;

    @Mock
    private CcdUtil ccdUtil;

    @Captor
    private ArgumentCaptor<Map<String, String>> templateParametersCaptor;

    @InjectMocks
    private SendRespondentSubmissionNotificationForDefendedDivorceEmail defendedDivorceNotificationEmailTask;

    @InjectMocks
    private SendRespondentSubmissionNotificationForUndefendedDivorceEmail undefendedDivorceNotificationEmailTask;

    private Court testCourt;

    @Before
    public void setUp() throws TaskException {
        when(ccdUtil.getFormattedDueDate(any(), any())).thenReturn(FORM_SUBMISSION_DUE_DATE);
        testCourt = new Court();
        testCourt.setDivorceCentreName("West Midlands Regional Divorce Centre");
        testCourt.setPoBox("PO Box 3650");
        testCourt.setCourtCity("Stoke-on-Trent");
        testCourt.setPostCode("ST4 9NH");

        when(taskCommons.getCourt("westMidlands")).thenReturn(testCourt);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRightEmailIsSent_WhenRespondentChoosesToDefendDivorce()
        throws TaskException, IOException {
        CcdCallbackRequest incomingPayload = getJsonFromResourceFile(
            "/jsonExamples/payloads/respondentAcknowledgesServiceDefendingDivorce.json", CcdCallbackRequest.class);
        Map<String, Object> caseData = spy(incomingPayload.getCaseDetails().getCaseData());
        String caseId = incomingPayload.getCaseDetails().getCaseId();
        DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, caseId);

        Map<String, Object> returnedPayload = defendedDivorceNotificationEmailTask.execute(context, caseData);

        assertThat(caseData, is(sameInstance(returnedPayload)));
        verify(taskCommons).sendEmail(eq(RESPONDENT_DEFENDED_AOS_SUBMISSION_NOTIFICATION),
            eq("respondent submission notification email - defended divorce"),
            eq("respondent@divorce.co.uk"),
            templateParametersCaptor.capture());
        Map<String, String> templateParameters = templateParametersCaptor.getValue();
        assertThat(templateParameters, hasEntry(NOTIFICATION_CASE_NUMBER_KEY, D8_CASE_ID));
        assertThat(templateParameters, allOf(
            hasEntry("email address", "respondent@divorce.co.uk"),
            hasEntry(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, "Ted"),
            hasEntry(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, "Jones"),
            hasEntry(NOTIFICATION_HUSBAND_OR_WIFE, "wife"),
            hasEntry(NOTIFICATION_RDC_NAME_KEY, testCourt.getIdentifiableCentreName()),
            hasEntry(NOTIFICATION_COURT_ADDRESS_KEY, testCourt.getFormattedAddress()),
            hasEntry("form submission date limit", FORM_SUBMISSION_DUE_DATE)
        ));
        assertThat(templateParameters.size(), equalTo(8));
        checkThatPropertiesAreCheckedBeforeBeingRetrieved(caseData);
    }

    @Test
    public void testExceptionIsThrown_WhenCaseIdIsMissing_ForDefendedDivorce()
        throws IOException, TaskException {
        expectedException.expect(TaskException.class);
        expectedException.expectMessage("Could not evaluate value of mandatory property \"D8caseReference\"");

        CcdCallbackRequest incomingPayload = getJsonFromResourceFile(
            "/jsonExamples/payloads/defendedDivorceAOSMissingCaseId.json", CcdCallbackRequest.class);
        Map<String, Object> caseData = incomingPayload.getCaseDetails().getCaseData();
        String caseId = (String) incomingPayload.getCaseDetails().getCaseData()
            .get(D_8_CASE_REFERENCE);
        DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(D_8_CASE_REFERENCE, caseId);

        defendedDivorceNotificationEmailTask.execute(context, caseData);
    }

    @Test
    public void testExceptionIsThrown_WhenMandatoryFieldIsMissing_ForDefendedDivorce()
        throws IOException, TaskException {
        expectedException.expect(TaskException.class);
        expectedException.expectMessage(
            "Could not evaluate value of mandatory property \"D8InferredPetitionerGender\""
        );

        CcdCallbackRequest incomingPayload = getJsonFromResourceFile(
            "/jsonExamples/payloads/defendedDivorceAOSMissingFields.json", CcdCallbackRequest.class);
        Map<String, Object> caseData = incomingPayload.getCaseDetails().getCaseData();
        String caseId = incomingPayload.getCaseDetails().getCaseId();
        DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, caseId);

        defendedDivorceNotificationEmailTask.execute(context, caseData);
    }

    @Test
    public void testRightEmailIsSent_WhenRespondentChoosesNotToDefendDivorce()
        throws TaskException, IOException {
        CcdCallbackRequest incomingPayload = getJsonFromResourceFile(
            "/jsonExamples/payloads/respondentAcknowledgesServiceNotDefendingDivorce.json", CcdCallbackRequest.class);
        Map<String, Object> caseData = spy(incomingPayload.getCaseDetails().getCaseData());
        String caseId = incomingPayload.getCaseDetails().getCaseId();
        DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, caseId);

        Map<String, Object> returnedPayload = undefendedDivorceNotificationEmailTask.execute(context, caseData);

        assertThat(caseData, is(sameInstance(returnedPayload)));
        verify(taskCommons).sendEmail(eq(RESPONDENT_UNDEFENDED_AOS_SUBMISSION_NOTIFICATION),
            eq("respondent submission notification email - undefended divorce"),
            eq("respondent@divorce.co.uk"),
            templateParametersCaptor.capture());
        Map<String, String> templateParameters = templateParametersCaptor.getValue();
        assertThat(templateParameters, hasEntry(NOTIFICATION_CASE_NUMBER_KEY, D8_CASE_ID));
        assertThat(templateParameters, allOf(
            hasEntry("email address", "respondent@divorce.co.uk"),
            hasEntry(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, "Sarah"),
            hasEntry(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, "Jones"),
            hasEntry(NOTIFICATION_HUSBAND_OR_WIFE, "husband"),
            hasEntry(NOTIFICATION_RDC_NAME_KEY, testCourt.getIdentifiableCentreName())
        ));
        assertThat(templateParameters.size(), equalTo(6));
        checkThatPropertiesAreCheckedBeforeBeingRetrieved(caseData);
    }

    @Test
    public void testExceptionIsThrown_WhenMandatoryFieldIsMissing_ForUndefendedDivorce()
        throws IOException, TaskException {
        expectedException.expect(TaskException.class);
        expectedException.expectMessage("Could not evaluate value of mandatory property \"D8DivorceUnit\"");

        CcdCallbackRequest incomingPayload = getJsonFromResourceFile(
            "/jsonExamples/payloads/undefendedDivorceAOSMissingFields.json", CcdCallbackRequest.class);
        Map<String, Object> caseData = incomingPayload.getCaseDetails().getCaseData();
        String caseId = incomingPayload.getCaseDetails().getCaseId();
        DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, caseId);

        undefendedDivorceNotificationEmailTask.execute(context, caseData);
    }

    private void checkThatPropertiesAreCheckedBeforeBeingRetrieved(Map<String, Object> mockCaseData) {
        List<String> listOfMethodInvoked = mockingDetails(mockCaseData).getInvocations().stream()
            .map(Invocation::getMethod)
            .map(Method::getName)
            .collect(Collectors.toList());

        long amountOfPropertiesChecked = listOfMethodInvoked.stream()
            .filter("containsKey"::equalsIgnoreCase)
            .count();
        long amountOfPropertiesRetrieved = listOfMethodInvoked.stream()
            .filter("get"::equalsIgnoreCase)
            .count();

        assertThat("Properties should be checked before they are retrieved",
            amountOfPropertiesChecked,
            equalTo(amountOfPropertiesRetrieved));
    }

}
