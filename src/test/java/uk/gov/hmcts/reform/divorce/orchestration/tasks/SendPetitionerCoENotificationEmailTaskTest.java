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
import uk.gov.hmcts.reform.divorce.orchestration.util.LocalDateToWelshStringConverter;

import java.io.IOException;
import java.time.LocalDate;
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
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_D8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_WESLH_DATE_COE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_WESLH_LIMIT_DATE_COE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.COURT_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_CLAIM_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_CLAIM_NOT_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COURT_NAME_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATE_OF_HEARING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LIMIT_DATE_TO_CONTACT_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_OPTIONAL_TEXT_NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_OPTIONAL_TEXT_YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WELSH_DATE_OF_HEARING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WELSH_LIMIT_DATE_TO_CONTACT_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.PETITIONER_CERTIFICATE_OF_ENTITLEMENT_NOTIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.SOL_APPLICANT_COE_NOTIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

@RunWith(MockitoJUnitRunner.class)
public class SendPetitionerCoENotificationEmailTaskTest {

    private static final String PET_CASE_LISTED_FOR_HEARING_JSON = "/jsonExamples/payloads/caseListedForHearing.json";
    private static final String PET_CASE_LISTED_FOR_HEARING_WELSH_JSON = "/jsonExamples/payloads/caseListedForHearingWelsh.json";
    private static final String SOL_CASE_LISTED_FOR_HEARING_JSON = "/jsonExamples/payloads/solCaseListedForHearing.json";

    @Mock
    private TaskCommons taskCommons;

    @Mock
    LocalDateToWelshStringConverter localDateToWelshStringConverter;

    @InjectMocks
    private SendPetitionerCoENotificationEmailTask sendPetitionerCoENotificationEmailTask;

    private DefaultTaskContext testContext;

    private final List<String> petMandatoryFields = asList(
        D_8_CASE_REFERENCE,
        D_8_PETITIONER_FIRST_NAME,
        D_8_PETITIONER_LAST_NAME,
        DATETIME_OF_HEARING_CCD_FIELD,
        COURT_NAME);

    private final List<String> solMandatoryFields = asList(
        D_8_PETITIONER_FIRST_NAME,
        D_8_PETITIONER_LAST_NAME,
        RESP_FIRST_NAME_CCD_FIELD,
        RESP_LAST_NAME_CCD_FIELD,
        PETITIONER_SOLICITOR_NAME,
        DATETIME_OF_HEARING_CCD_FIELD,
        COURT_NAME);

    @Before
    public void setUp() throws CourtDetailsNotFound {
        testContext = new DefaultTaskContext();
        testContext.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);

        DnCourt dnCourt = new DnCourt();
        dnCourt.setName("Court Name");
        when(taskCommons.getDnCourt(anyString())).thenReturn(dnCourt);
        LocalDate dateOfHearing = LocalDate.of(2019, 4,21);
        LocalDate limitDate = LocalDate.of(2019, 4,6);
        when(localDateToWelshStringConverter.convert(dateOfHearing)).thenReturn(TEST_WESLH_DATE_COE);
        when(localDateToWelshStringConverter.convert(limitDate)).thenReturn(TEST_WESLH_LIMIT_DATE_COE);
    }

    @Test
    public void testThatPetNotificationServiceIsCalled_WhenCostsClaimIsGrantedInWelsh() throws TaskException, IOException {
        Map<String, Object> incomingPayload = getJsonFromResourceFile(
            PET_CASE_LISTED_FOR_HEARING_WELSH_JSON, CcdCallbackRequest.class).getCaseDetails().getCaseData();

        Map<String, Object> returnedPayload = sendPetitionerCoENotificationEmailTask.execute(testContext, incomingPayload);

        verifyPetEmailParametersWelsh(allOf(
            hasEntry(COSTS_CLAIM_GRANTED, NOTIFICATION_OPTIONAL_TEXT_YES_VALUE),
            hasEntry(COSTS_CLAIM_NOT_GRANTED, NOTIFICATION_OPTIONAL_TEXT_NO_VALUE)
        ));
        assertThat(returnedPayload, is(equalTo(incomingPayload)));
    }

    @Test
    public void testThatPetNotificationServiceIsCalled_WhenCostsClaimIsGranted() throws TaskException, IOException {
        Map<String, Object> incomingPayload = getJsonFromResourceFile(
            PET_CASE_LISTED_FOR_HEARING_JSON, CcdCallbackRequest.class).getCaseDetails().getCaseData();

        Map<String, Object> returnedPayload = sendPetitionerCoENotificationEmailTask.execute(testContext, incomingPayload);

        verifyPetEmailParameters(allOf(
            hasEntry(COSTS_CLAIM_GRANTED, NOTIFICATION_OPTIONAL_TEXT_YES_VALUE),
            hasEntry(COSTS_CLAIM_NOT_GRANTED, NOTIFICATION_OPTIONAL_TEXT_NO_VALUE)
        ));
        assertThat(returnedPayload, is(equalTo(incomingPayload)));
    }

    @Test
    public void testThatSolNotificationServiceIsCalled_WhenCostsClaimIsGranted() throws TaskException, IOException {
        Map<String, Object> incomingPayload = getJsonFromResourceFile(
            SOL_CASE_LISTED_FOR_HEARING_JSON, CcdCallbackRequest.class).getCaseDetails().getCaseData();

        Map<String, Object> returnedPayload = sendPetitionerCoENotificationEmailTask.execute(testContext, incomingPayload);

        verifySolEmailParameters(allOf(
            hasEntry(COSTS_CLAIM_GRANTED, NOTIFICATION_OPTIONAL_TEXT_YES_VALUE),
            hasEntry(COSTS_CLAIM_NOT_GRANTED, NOTIFICATION_OPTIONAL_TEXT_NO_VALUE)
        ));
        assertThat(returnedPayload, is(equalTo(incomingPayload)));
    }

    @Test
    public void testThatPetNotificationServiceIsCalled_WhenCostsClaimIsNotGranted() throws TaskException, IOException {
        Map<String, Object> incomingPayload = getJsonFromResourceFile(
            PET_CASE_LISTED_FOR_HEARING_JSON, CcdCallbackRequest.class).getCaseDetails().getCaseData();
        incomingPayload.put(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, "NO");

        Map<String, Object> returnedPayload = sendPetitionerCoENotificationEmailTask.execute(testContext, incomingPayload);

        verifyPetEmailParameters(allOf(
            hasEntry(COSTS_CLAIM_NOT_GRANTED, NOTIFICATION_OPTIONAL_TEXT_YES_VALUE),
            hasEntry(COSTS_CLAIM_GRANTED, NOTIFICATION_OPTIONAL_TEXT_NO_VALUE)
        ));
        assertThat(returnedPayload, is(equalTo(incomingPayload)));
    }


    @Test
    public void testThatSolNotificationServiceIsCalled_WhenCostsClaimIsNotGranted() throws TaskException, IOException {
        Map<String, Object> incomingPayload = getJsonFromResourceFile(
            SOL_CASE_LISTED_FOR_HEARING_JSON, CcdCallbackRequest.class).getCaseDetails().getCaseData();
        incomingPayload.put(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, "NO");

        Map<String, Object> returnedPayload = sendPetitionerCoENotificationEmailTask.execute(testContext, incomingPayload);

        verifySolEmailParameters(allOf(
            hasEntry(COSTS_CLAIM_NOT_GRANTED, NOTIFICATION_OPTIONAL_TEXT_YES_VALUE),
            hasEntry(COSTS_CLAIM_GRANTED, NOTIFICATION_OPTIONAL_TEXT_NO_VALUE)
        ));
        assertThat(returnedPayload, is(equalTo(incomingPayload)));
    }

    @Test
    public void testThatPetNotificationServiceIsCalled_WhenCostsAreNotClaimed_ByAbsenceOfValues() throws TaskException, IOException {
        Map<String, Object> incomingPayload = getJsonFromResourceFile(
            PET_CASE_LISTED_FOR_HEARING_JSON, CcdCallbackRequest.class).getCaseDetails().getCaseData();
        incomingPayload.remove(DIVORCE_COSTS_CLAIM_CCD_FIELD);
        incomingPayload.remove(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD);

        Map<String, Object> returnedPayload = sendPetitionerCoENotificationEmailTask.execute(testContext, incomingPayload);

        verifyPetEmailParameters(allOf(
            hasEntry(COSTS_CLAIM_NOT_GRANTED, NOTIFICATION_OPTIONAL_TEXT_NO_VALUE),
            hasEntry(COSTS_CLAIM_GRANTED, NOTIFICATION_OPTIONAL_TEXT_NO_VALUE)
        ));
        assertThat(returnedPayload, is(equalTo(incomingPayload)));
    }

    @Test
    public void testThatSolNotificationServiceIsCalled_WhenCostsAreNotClaimed_ByAbsenceOfValues() throws TaskException, IOException {
        Map<String, Object> incomingPayload = getJsonFromResourceFile(
            SOL_CASE_LISTED_FOR_HEARING_JSON, CcdCallbackRequest.class).getCaseDetails().getCaseData();
        incomingPayload.remove(DIVORCE_COSTS_CLAIM_CCD_FIELD);
        incomingPayload.remove(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD);

        Map<String, Object> returnedPayload = sendPetitionerCoENotificationEmailTask.execute(testContext, incomingPayload);

        verifySolEmailParameters(allOf(
            hasEntry(COSTS_CLAIM_NOT_GRANTED, NOTIFICATION_OPTIONAL_TEXT_NO_VALUE),
            hasEntry(COSTS_CLAIM_GRANTED, NOTIFICATION_OPTIONAL_TEXT_NO_VALUE)
        ));
        assertThat(returnedPayload, is(equalTo(incomingPayload)));
    }

    @Test
    public void testThatPetNotificationServiceIsCalled_WhenCostsAreNotClaimed_ByNegationOfValues() throws TaskException, IOException {
        Map<String, Object> incomingPayload = getJsonFromResourceFile(
            PET_CASE_LISTED_FOR_HEARING_JSON, CcdCallbackRequest.class).getCaseDetails().getCaseData();
        incomingPayload.put(DIVORCE_COSTS_CLAIM_CCD_FIELD, NO_VALUE);
        incomingPayload.put(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, NO_VALUE);

        Map<String, Object> returnedPayload = sendPetitionerCoENotificationEmailTask.execute(testContext, incomingPayload);

        verifyPetEmailParameters(allOf(
            hasEntry(COSTS_CLAIM_NOT_GRANTED, NOTIFICATION_OPTIONAL_TEXT_NO_VALUE),
            hasEntry(COSTS_CLAIM_GRANTED, NOTIFICATION_OPTIONAL_TEXT_NO_VALUE)
        ));
        assertThat(returnedPayload, is(equalTo(incomingPayload)));
    }

    @Test
    public void testThatSolNotificationServiceIsCalled_WhenCostsAreNotClaimed_ByNegationOfValues() throws TaskException, IOException {
        Map<String, Object> incomingPayload = getJsonFromResourceFile(
            SOL_CASE_LISTED_FOR_HEARING_JSON, CcdCallbackRequest.class).getCaseDetails().getCaseData();
        incomingPayload.put(DIVORCE_COSTS_CLAIM_CCD_FIELD, NO_VALUE);
        incomingPayload.put(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, NO_VALUE);

        Map<String, Object> returnedPayload = sendPetitionerCoENotificationEmailTask.execute(testContext, incomingPayload);

        verifySolEmailParameters(allOf(
            hasEntry(COSTS_CLAIM_NOT_GRANTED, NOTIFICATION_OPTIONAL_TEXT_NO_VALUE),
            hasEntry(COSTS_CLAIM_GRANTED, NOTIFICATION_OPTIONAL_TEXT_NO_VALUE)
        ));
        assertThat(returnedPayload, is(equalTo(incomingPayload)));
    }

    @Test
    public void shouldThrowException_WhenPetCaseMandatoryFieldIsMissing() throws IOException, TaskException {
        Map<String, Object> referencePayload = Collections.unmodifiableMap(getJsonFromResourceFile(
            PET_CASE_LISTED_FOR_HEARING_JSON, CcdCallbackRequest.class).getCaseDetails().getCaseData());

        for (String mandatoryFieldToRemove : petMandatoryFields) {
            HashMap<String, Object> payloadToBeModified = new HashMap<>(referencePayload);
            payloadToBeModified.remove(mandatoryFieldToRemove);

            try {
                sendPetitionerCoENotificationEmailTask.execute(testContext, payloadToBeModified);
                fail("Should have caught exception");
            } catch (TaskException taskException) {
                assertThat(taskException.getMessage(), is(format("Could not evaluate value of mandatory property \"%s\"", mandatoryFieldToRemove)));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                fail("Should have caught TaskException, but got the following: " + throwable.getClass().getName());
            }

            verify(taskCommons, never()).sendEmail(any(), any(), any(), any(), any());
        }
    }

    @Test
    public void shouldThrowException_WhenSolCaseMandatoryFieldIsMissing() throws IOException, TaskException {
        Map<String, Object> referencePayload = Collections.unmodifiableMap(getJsonFromResourceFile(
            SOL_CASE_LISTED_FOR_HEARING_JSON, CcdCallbackRequest.class).getCaseDetails().getCaseData());

        for (String mandatoryFieldToRemove : solMandatoryFields) {
            HashMap<String, Object> payloadToBeModified = new HashMap<>(referencePayload);
            payloadToBeModified.remove(mandatoryFieldToRemove);

            try {
                sendPetitionerCoENotificationEmailTask.execute(testContext, payloadToBeModified);
                fail("Should have caught exception");
            } catch (TaskException taskException) {
                assertThat(taskException.getMessage(), is(format("Could not evaluate value of mandatory property \"%s\"", mandatoryFieldToRemove)));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                fail("Should have caught TaskException, but got the following: " + throwable.getClass().getName());
            }

            verify(taskCommons, never()).sendEmail(any(), any(), any(), any(), any());
        }
    }

    private void verifyPetEmailParametersWelsh(Matcher<Map<? extends String, ?>> optionalTextParametersMatcher) throws TaskException {
        verify(taskCommons).sendEmail(eq(PETITIONER_CERTIFICATE_OF_ENTITLEMENT_NOTIFICATION),
            notNull(),
            eq(TEST_PETITIONER_EMAIL),
            argThat(new HamcrestArgumentMatcher<>(
                allOf(
                    hasEntry(NOTIFICATION_EMAIL, TEST_PETITIONER_EMAIL),
                    hasEntry(NOTIFICATION_CASE_NUMBER_KEY, TEST_D8_CASE_REFERENCE),
                    hasEntry(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TEST_PETITIONER_FIRST_NAME),
                    hasEntry(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TEST_PETITIONER_LAST_NAME),
                    optionalTextParametersMatcher,
                    hasEntry(DATE_OF_HEARING, "21 April 2019"),
                    hasEntry(LIMIT_DATE_TO_CONTACT_COURT, "6 April 2019"),
                    hasEntry(WELSH_DATE_OF_HEARING, "21 Ebrill 2019"),
                    hasEntry(WELSH_LIMIT_DATE_TO_CONTACT_COURT, "6 Ebrill 2019"),
                    hasEntry(COURT_NAME_TEMPLATE_ID, "Court Name")
                )
            )),any());
    }

    private void verifyPetEmailParameters(Matcher<Map<? extends String, ?>> optionalTextParametersMatcher) throws TaskException {
        verify(taskCommons).sendEmail(eq(PETITIONER_CERTIFICATE_OF_ENTITLEMENT_NOTIFICATION),
            notNull(),
            eq(TEST_PETITIONER_EMAIL),
            argThat(new HamcrestArgumentMatcher<>(
                allOf(
                    hasEntry(NOTIFICATION_EMAIL, TEST_PETITIONER_EMAIL),
                    hasEntry(NOTIFICATION_CASE_NUMBER_KEY, TEST_D8_CASE_REFERENCE),
                    hasEntry(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TEST_PETITIONER_FIRST_NAME),
                    hasEntry(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TEST_PETITIONER_LAST_NAME),
                    optionalTextParametersMatcher,
                    hasEntry(DATE_OF_HEARING, "21 April 2019"),
                    hasEntry(LIMIT_DATE_TO_CONTACT_COURT, "6 April 2019"),
                    hasEntry(COURT_NAME_TEMPLATE_ID, "Court Name")
                )
            )),any());
    }

    private void verifySolEmailParameters(Matcher<Map<? extends String, ?>> optionalTextParametersMatcher) throws TaskException {
        verify(taskCommons).sendEmail(eq(SOL_APPLICANT_COE_NOTIFICATION),
            notNull(),
            eq("solicitor@justice.uk"),
            argThat(new HamcrestArgumentMatcher<>(
                allOf(
                    hasEntry(NOTIFICATION_EMAIL, "solicitor@justice.uk"),
                    hasEntry(NOTIFICATION_CCD_REFERENCE_KEY, "test.case.id"),
                    hasEntry(NOTIFICATION_PET_NAME, TEST_PETITIONER_FULL_NAME),
                    hasEntry(NOTIFICATION_RESP_NAME, TEST_RESPONDENT_FULL_NAME),
                    hasEntry(NOTIFICATION_SOLICITOR_NAME, "Petitioner Solicitor name"),
                    optionalTextParametersMatcher,
                    hasEntry(DATE_OF_HEARING, "21 April 2019"),
                    hasEntry(LIMIT_DATE_TO_CONTACT_COURT, "6 April 2019"),
                    hasEntry(COURT_NAME_TEMPLATE_ID, "Court Name")
                )
            )),any());
    }

}