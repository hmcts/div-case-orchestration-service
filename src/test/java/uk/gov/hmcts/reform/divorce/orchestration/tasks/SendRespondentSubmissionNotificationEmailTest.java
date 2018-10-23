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
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.RESPONDENT_DEFENDED_AOS_SUBMISSION_NOTIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.RESPONDENT_UNDEFENDED_AOS_SUBMISSION_NOTIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

@RunWith(MockitoJUnitRunner.class)
public class SendRespondentSubmissionNotificationEmailTest {

    private static final String FORMATTED_CASE_ID = "0123-4567-8901-2345";

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private TaskCommons taskCommons;

    @Captor
    private ArgumentCaptor<Map<String, String>> templateParametersCaptor;

    @InjectMocks
    private SendRespondentSubmissionNotificationForDefendedDivorceEmail defendedDivorceNotificationEmailTask;

    @InjectMocks
    private SendRespondentSubmissionNotificationForUndefendedDivorceEmail undefendedDivorceNotificationEmailTask;

    private Court testCourt;

    @Before
    public void setUp() throws TaskException {
        testCourt = new Court();
        testCourt.setDivorceCentreName("East Midlands Regional Divorce Centre");
        testCourt.setPoBox("PO Box 10447");
        testCourt.setCourtCity("Nottingham");
        testCourt.setPostCode("NG2 9QN");

        when(taskCommons.getCourt("eastMidlands")).thenReturn(testCourt);
    }

    @Test
    public void testRightEmailIsSent_WhenRespondentChoosesToDefendDivorce()
            throws TaskException, IOException {
        CreateEvent incomingPayload = getJsonFromResourceFile(
                "/jsonExamples/payloads/respondentAcknowledgesServiceDefendingDivorce.json", CreateEvent.class);
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
        assertThat(templateParameters, hasEntry("case number", FORMATTED_CASE_ID));
        assertThat(templateParameters, allOf(
                hasEntry("email address", "respondent@divorce.co.uk"),
                hasEntry("first name", "Ted"),
                hasEntry("last name", "Jones"),
                hasEntry("husband or wife", "wife"),
                hasEntry("RDC name", testCourt.getDivorceCentreName()),
                hasEntry("court address", testCourt.getFormattedAddress()),
                hasEntry("form submission date limit", "11 October 2018")
        ));
        assertThat(templateParameters.size(), equalTo(8));
        checkThatPropertiesAreCheckedBeforeBeingRetrieved(caseData);
    }

    @Test
    public void testExceptionIsThrown_WhenCaseIdIsMissing_ForDefendedDivorce()
            throws IOException, TaskException {
        expectedException.expect(TaskException.class);
        expectedException.expectMessage("Could not evaluate value of mandatory property \"caseId\"");

        CreateEvent incomingPayload = getJsonFromResourceFile(
                "/jsonExamples/payloads/defendedDivorceAOSMissingCaseId.json", CreateEvent.class);
        Map<String, Object> caseData = incomingPayload.getCaseDetails().getCaseData();
        String caseId = incomingPayload.getCaseDetails().getCaseId();
        DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, caseId);

        defendedDivorceNotificationEmailTask.execute(context, caseData);
    }

    @Test
    public void testExceptionIsThrown_WhenMandatoryFieldIsMissing_ForDefendedDivorce()
            throws IOException, TaskException {
        expectedException.expect(TaskException.class);
        expectedException.expectMessage(
                "Could not evaluate value of mandatory property \"D8InferredPetitionerGender\""
        );

        CreateEvent incomingPayload = getJsonFromResourceFile(
                "/jsonExamples/payloads/defendedDivorceAOSMissingFields.json", CreateEvent.class);
        Map<String, Object> caseData = incomingPayload.getCaseDetails().getCaseData();
        String caseId = incomingPayload.getCaseDetails().getCaseId();
        DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, caseId);

        defendedDivorceNotificationEmailTask.execute(context, caseData);
    }

    @Test
    public void testRightEmailIsSent_WhenRespondentChoosesNotToDefendDivorce()
            throws TaskException, IOException {
        CreateEvent incomingPayload = getJsonFromResourceFile(
                "/jsonExamples/payloads/respondentAcknowledgesServiceNotDefendingDivorce.json", CreateEvent.class);
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
        assertThat(templateParameters, allOf(
                hasEntry("email address", "respondent@divorce.co.uk"),
                hasEntry("first name", "Sarah"),
                hasEntry("last name", "Jones"),
                hasEntry("husband or wife", "husband"),
                hasEntry("RDC name", testCourt.getDivorceCentreName())
        ));
        assertThat(templateParameters.size(), equalTo(5));
        checkThatPropertiesAreCheckedBeforeBeingRetrieved(caseData);
    }

    @Test
    public void testExceptionIsThrown_WhenMandatoryFieldIsMissing_ForUndefendedDivorce()
            throws IOException, TaskException {
        expectedException.expect(TaskException.class);
        expectedException.expectMessage("Could not evaluate value of mandatory property \"D8DivorceUnit\"");

        CreateEvent incomingPayload = getJsonFromResourceFile(
                "/jsonExamples/payloads/undefendedDivorceAOSMissingFields.json", CreateEvent.class);
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
                .filter(name -> name.equalsIgnoreCase("containsKey"))
                .count();
        long amountOfPropertiesRetrieved = listOfMethodInvoked.stream()
                .filter(name -> name.equalsIgnoreCase("get"))
                .count();

        assertThat("Properties should be checked before they are retrieved",
                amountOfPropertiesChecked,
                equalTo(amountOfPropertiesRetrieved));
    }

}