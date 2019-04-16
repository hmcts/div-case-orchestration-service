package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.hamcrest.HamcrestArgumentMatcher;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.PETITIONER_CERTIFICATE_OF_ENTITLEMENT_NOTIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.CaseLinkedForHearingWorkflow.CASE_ID_KEY;

@RunWith(MockitoJUnitRunner.class)
public class PetitionerCertificateOfEntitlementNotificationTest {

    private static final String NOTIFICATION_OPTIONAL_TEXT_YES_VALUE = "yes";
    private static final String NOTIFICATION_OPTIONAL_TEXT_NO_VALUE = "no";

    @Mock
    private TaskCommons taskCommons;

    @InjectMocks
    private PetitionerCertificateOfEntitlementNotification petitionerCertificateOfEntitlementNotification;

    private DefaultTaskContext testContext;

    private List<String> mandatoryFields = asList("D8PetitionerEmail",
        "D8caseReference",
        "D8PetitionerFirstName",
        "D8PetitionerLastName",
        "DateOfHearing");

    @Before
    public void setUp() {
        testContext = new DefaultTaskContext();
        testContext.setTransientObject(CASE_ID_KEY, "testCaseIdValue");
    }

    @Test
    public void testThatNotificationServiceIsCalled_WhenCostsClaimIsGranted() throws TaskException, IOException {
        Map<String, Object> incomingPayload = getJsonFromResourceFile(
            "/jsonExamples/payloads/caseListedForHearing.json", CcdCallbackRequest.class).getCaseDetails().getCaseData();

        Map<String, Object> returnedPayload = petitionerCertificateOfEntitlementNotification.execute(testContext, incomingPayload);

        verifyEmailParameters(allOf(
            hasEntry("costs claim granted", NOTIFICATION_OPTIONAL_TEXT_YES_VALUE),
            hasEntry("costs claim not granted", NOTIFICATION_OPTIONAL_TEXT_NO_VALUE)
        ));
        assertThat(returnedPayload, is(equalTo(incomingPayload)));
    }

    @Test
    public void testThatNotificationServiceIsCalled_WhenCostsClaimIsNotGranted() throws TaskException, IOException {
        Map<String, Object> incomingPayload = getJsonFromResourceFile(
            "/jsonExamples/payloads/caseListedForHearing.json", CcdCallbackRequest.class).getCaseDetails().getCaseData();
        incomingPayload.put("CostsClaimGranted", "NO");

        Map<String, Object> returnedPayload = petitionerCertificateOfEntitlementNotification.execute(testContext, incomingPayload);

        verifyEmailParameters(allOf(
            hasEntry("costs claim not granted", NOTIFICATION_OPTIONAL_TEXT_YES_VALUE),
            hasEntry("costs claim granted", NOTIFICATION_OPTIONAL_TEXT_NO_VALUE)
        ));
        assertThat(returnedPayload, is(equalTo(incomingPayload)));
    }

    @Test
    public void testThatNotificationServiceIsCalled_WhenCostsAreNotClaimed_ByAbsenceOfValues() throws TaskException, IOException {
        Map<String, Object> incomingPayload = getJsonFromResourceFile(
            "/jsonExamples/payloads/caseListedForHearing.json", CcdCallbackRequest.class).getCaseDetails().getCaseData();
        incomingPayload.remove("D8DivorceCostsClaim");
        incomingPayload.remove("CostsClaimGranted");

        Map<String, Object> returnedPayload = petitionerCertificateOfEntitlementNotification.execute(testContext, incomingPayload);

        verifyEmailParameters(allOf(
            hasEntry("costs claim not granted", NOTIFICATION_OPTIONAL_TEXT_NO_VALUE),
            hasEntry("costs claim granted", NOTIFICATION_OPTIONAL_TEXT_NO_VALUE)
        ));
        assertThat(returnedPayload, is(equalTo(incomingPayload)));
    }

    @Test
    public void testThatNotificationServiceIsCalled_WhenCostsAreNotClaimed_ByNegationOfValues() throws TaskException, IOException {
        Map<String, Object> incomingPayload = getJsonFromResourceFile(
            "/jsonExamples/payloads/caseListedForHearing.json", CcdCallbackRequest.class).getCaseDetails().getCaseData();
        incomingPayload.put("D8DivorceCostsClaim", "NO");
        incomingPayload.put("CostsClaimGranted", "NO");

        Map<String, Object> returnedPayload = petitionerCertificateOfEntitlementNotification.execute(testContext, incomingPayload);

        verifyEmailParameters(allOf(
            hasEntry("costs claim not granted", NOTIFICATION_OPTIONAL_TEXT_NO_VALUE),
            hasEntry("costs claim granted", NOTIFICATION_OPTIONAL_TEXT_NO_VALUE)
        ));
        assertThat(returnedPayload, is(equalTo(incomingPayload)));
    }

    @Test
    public void shouldThrowException_WhenMandatoryFieldIsMissing() throws IOException, TaskException {
        Map<String, Object> referencePayload = Collections.unmodifiableMap(getJsonFromResourceFile(
            "/jsonExamples/payloads/caseListedForHearing.json", CcdCallbackRequest.class).getCaseDetails().getCaseData());

        for (String mandatoryFieldToRemove : mandatoryFields) {
            HashMap<String, Object> payloadToBeModified = new HashMap<>(referencePayload);
            payloadToBeModified.remove(mandatoryFieldToRemove);

            try {
                petitionerCertificateOfEntitlementNotification.execute(testContext, payloadToBeModified);
                fail("Should have caught exception");
            } catch (TaskException taskException) {
                assertThat(taskException.getMessage(), is(format("Could not evaluate value of mandatory property \"%s\"", mandatoryFieldToRemove)));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                fail("Should have caught TaskException, but got the following: " + throwable.getClass().getName());
            }

            verify(taskCommons, never()).sendEmail(any(), any(), any(), any());
        }
    }

    private void verifyEmailParameters(Matcher<Map<? extends String, ?>> optionalTextParametersMatcher) throws TaskException {
        verify(taskCommons).sendEmail(eq(PETITIONER_CERTIFICATE_OF_ENTITLEMENT_NOTIFICATION),
            notNull(),
            eq("petitioner@justice.uk"),
            argThat(new HamcrestArgumentMatcher<>(
                allOf(
                    hasEntry("email address", "petitioner@justice.uk"),
                    hasEntry("case number", "HR290831"),
                    hasEntry("first name", "James"),
                    hasEntry("last name", "Johnson"),
                    optionalTextParametersMatcher,
                    hasEntry("date of hearing", "21 April 2019"),
                    hasEntry("limit date to contact court", "07 April 2019")
                )
            )));
    }

}