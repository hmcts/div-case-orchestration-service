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
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.DnCourt;
import uk.gov.hmcts.reform.divorce.orchestration.exception.CourtDetailsNotFound;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_CLAIM_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COURT_NAME_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATE_OF_HEARING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_PETITIONER_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LIMIT_DATE_TO_CONTACT_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_HUSBAND_OR_WIFE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_OPTIONAL_TEXT_NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_OPTIONAL_TEXT_YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.RESPONDENT_CERTIFICATE_OF_ENTITLEMENT_NOTIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.SOL_RESP_COE_NOTIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

@RunWith(MockitoJUnitRunner.class)
public class SendRespondentCertificateOfEntitlementNotificationEmailTest {

    private static final String CASE_LISTED_FOR_HEARING_JSON = "/jsonExamples/payloads/caseListedForHearing.json";
    private static final String SOL_CASE_LISTED_FOR_HEARING_JSON = "/jsonExamples/payloads/solCaseListedForHearing.json";

    @Mock
    private TaskCommons taskCommons;

    @InjectMocks
    private SendRespondentCertificateOfEntitlementNotificationEmail sendRespondentCertificateOfEntitlementNotificationEmail;

    private DefaultTaskContext testContext;

    private List<String> mandatoryFields = asList(
        D_8_CASE_REFERENCE,
        RESP_FIRST_NAME_CCD_FIELD,
        RESP_LAST_NAME_CCD_FIELD,
        D_8_INFERRED_PETITIONER_GENDER,
        DATETIME_OF_HEARING_CCD_FIELD,
        COURT_NAME_CCD_FIELD);

    private List<String> solMandatoryFields = asList(
        D_8_PETITIONER_FIRST_NAME,
        D_8_PETITIONER_LAST_NAME,
        RESP_FIRST_NAME_CCD_FIELD,
        RESP_LAST_NAME_CCD_FIELD,
        DATETIME_OF_HEARING_CCD_FIELD,
        COURT_NAME_CCD_FIELD);

    @Before
    public void setUp() throws CourtDetailsNotFound {
        testContext = new DefaultTaskContext();
        testContext.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);

        DnCourt dnCourt = new DnCourt();
        dnCourt.setName("Court Name");
        when(taskCommons.getDnCourt(anyString())).thenReturn(dnCourt);
    }

    @Test
    public void testThatNotificationServiceIsCalled_WhenCostsClaimIsGranted() throws TaskException, IOException {
        Map<String, Object> incomingPayload = getJsonFromResourceFile(
                CASE_LISTED_FOR_HEARING_JSON, CcdCallbackRequest.class).getCaseDetails().getCaseData();

        Map<String, Object> returnedPayload = sendRespondentCertificateOfEntitlementNotificationEmail.execute(testContext, incomingPayload);

        verifyEmailParameters(allOf(
                hasEntry(COSTS_CLAIM_GRANTED, NOTIFICATION_OPTIONAL_TEXT_YES_VALUE)
        ));

        assertThat(returnedPayload, is(equalTo(incomingPayload)));
    }

    @Test
    public void testThatSolNotificationServiceIsCalled_WhenCostsClaimIsGranted() throws TaskException, IOException {
        Map<String, Object> incomingPayload = getJsonFromResourceFile(
            SOL_CASE_LISTED_FOR_HEARING_JSON, CcdCallbackRequest.class).getCaseDetails().getCaseData();

        Map<String, Object> returnedPayload = sendRespondentCertificateOfEntitlementNotificationEmail.execute(testContext, incomingPayload);

        verifySolEmailParameters(allOf(
            hasEntry(COSTS_CLAIM_GRANTED, NOTIFICATION_OPTIONAL_TEXT_YES_VALUE)
        ));

        assertThat(returnedPayload, is(equalTo(incomingPayload)));
    }

    @Test
    public void testThatNotificationServiceIsCalled_WhenCostsClaimIsNotGranted() throws TaskException, IOException {
        Map<String, Object> incomingPayload = getJsonFromResourceFile(
                CASE_LISTED_FOR_HEARING_JSON, CcdCallbackRequest.class).getCaseDetails().getCaseData();

        incomingPayload.put("CostsClaimGranted", "No");

        Map<String, Object> returnedPayload = sendRespondentCertificateOfEntitlementNotificationEmail.execute(testContext, incomingPayload);

        verifyEmailParameters(allOf(
                hasEntry(COSTS_CLAIM_GRANTED, NOTIFICATION_OPTIONAL_TEXT_NO_VALUE)
        ));

        assertThat(returnedPayload, is(equalTo(incomingPayload)));
    }

    @Test
    public void testThatSolNotificationServiceIsCalled_WhenCostsClaimIsNotGranted() throws TaskException, IOException {
        Map<String, Object> incomingPayload = getJsonFromResourceFile(
            SOL_CASE_LISTED_FOR_HEARING_JSON, CcdCallbackRequest.class).getCaseDetails().getCaseData();

        incomingPayload.put("CostsClaimGranted", "No");

        Map<String, Object> returnedPayload = sendRespondentCertificateOfEntitlementNotificationEmail.execute(testContext, incomingPayload);

        verifySolEmailParameters(allOf(
            hasEntry(COSTS_CLAIM_GRANTED, NOTIFICATION_OPTIONAL_TEXT_NO_VALUE)
        ));

        assertThat(returnedPayload, is(equalTo(incomingPayload)));
    }

    @Test
    public void shouldThrowException_WhenMandatoryFieldIsMissing() throws IOException, TaskException {
        Map<String, Object> referencePayload = Collections.unmodifiableMap(getJsonFromResourceFile(
                CASE_LISTED_FOR_HEARING_JSON, CcdCallbackRequest.class).getCaseDetails().getCaseData());

        for (String mandatoryFieldToRemove : mandatoryFields) {
            HashMap<String, Object> payloadToBeModified = new HashMap<>(referencePayload);
            payloadToBeModified.remove(mandatoryFieldToRemove);

            try {
                sendRespondentCertificateOfEntitlementNotificationEmail.execute(testContext, payloadToBeModified);
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

    @Test
    public void shouldThrowException_WhenSolMandatoryFieldIsMissing() throws IOException, TaskException {
        Map<String, Object> referencePayload = Collections.unmodifiableMap(getJsonFromResourceFile(
            SOL_CASE_LISTED_FOR_HEARING_JSON, CcdCallbackRequest.class).getCaseDetails().getCaseData());

        for (String mandatoryFieldToRemove : solMandatoryFields) {
            HashMap<String, Object> payloadToBeModified = new HashMap<>(referencePayload);
            payloadToBeModified.remove(mandatoryFieldToRemove);

            try {
                sendRespondentCertificateOfEntitlementNotificationEmail.execute(testContext, payloadToBeModified);
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
        verify(taskCommons).sendEmail(eq(RESPONDENT_CERTIFICATE_OF_ENTITLEMENT_NOTIFICATION),
            notNull(),
            eq("respondent@justice.uk"),
            argThat(new HamcrestArgumentMatcher<>(
                allOf(
                    hasEntry(NOTIFICATION_EMAIL, "respondent@justice.uk"),
                    hasEntry(NOTIFICATION_CASE_NUMBER_KEY, "HR290831"),
                    hasEntry(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, "Jane"),
                    hasEntry(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, "Jamed"),
                    optionalTextParametersMatcher,
                    hasEntry(NOTIFICATION_HUSBAND_OR_WIFE, "husband"),
                    hasEntry(DATE_OF_HEARING, "21 April 2019"),
                    hasEntry(LIMIT_DATE_TO_CONTACT_COURT, "07 April 2019"),
                    hasEntry(COURT_NAME_TEMPLATE_ID, "Court Name")
                )
            )));
    }

    private void verifySolEmailParameters(Matcher<Map<? extends String, ?>> optionalTextParametersMatcher) throws TaskException {
        verify(taskCommons).sendEmail(eq(SOL_RESP_COE_NOTIFICATION),
            notNull(),
            eq("respsolicitor@justice.uk"),
            argThat(new HamcrestArgumentMatcher<>(
                allOf(
                    hasEntry(NOTIFICATION_EMAIL, "respsolicitor@justice.uk"),
                    hasEntry(NOTIFICATION_CCD_REFERENCE_KEY, "test.case.id"),
                    hasEntry(NOTIFICATION_PET_NAME, "James Johnson"),
                    hasEntry(NOTIFICATION_RESP_NAME, "Resp First Name Resp Last Name"),
                    hasEntry(NOTIFICATION_SOLICITOR_NAME, "Respondent Solicitor name"),
                    optionalTextParametersMatcher,
                    hasEntry(DATE_OF_HEARING, "21 April 2019"),
                    hasEntry(LIMIT_DATE_TO_CONTACT_COURT, "07 April 2019"),
                    hasEntry(COURT_NAME_TEMPLATE_ID, "Court Name")
                )
            )));
    }

}