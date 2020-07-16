package uk.gov.hmcts.reform.divorce.orchestration.tasks.notification;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.hamcrest.HamcrestArgumentMatcher;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.TestConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AOS_AWAITING_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_RESPONDENT_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_FEES_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_HUSBAND_OR_WIFE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_FEE_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class SendPetitionerAmendEmailTaskTest {
    private static final String RELATION = "husband";
    private static final FeeResponse TEST_FEES = FeeResponse.builder().amount(50.00).build();
    private static final String FEE_AMOUNT_AS_STRING = "50";

    private Map<String, Object> incomingPayload;
    private TaskContext taskContext;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private SendPetitionerAmendEmailTask sendPetitionerAmendEmailTask;

    @Before
    public void setUp() {
        incomingPayload = new HashMap<>();
        incomingPayload.put(D_8_PETITIONER_FIRST_NAME, TestConstants.TEST_USER_FIRST_NAME);
        incomingPayload.put(D_8_PETITIONER_LAST_NAME, TestConstants.TEST_USER_LAST_NAME);
        incomingPayload.put(D_8_PETITIONER_EMAIL, TestConstants.TEST_PETITIONER_EMAIL);
        incomingPayload.put(D_8_CASE_REFERENCE, TestConstants.TEST_CASE_FAMILY_MAN_ID);
        incomingPayload.put(D_8_INFERRED_RESPONDENT_GENDER, "male");

        taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        taskContext.setTransientObject(PETITION_FEE_JSON_KEY, TEST_FEES);
        taskContext.setTransientObject(CASE_DETAILS_JSON_KEY, CaseDetails.builder().state(AOS_AWAITING_STATE).build());
    }

    @Test
    public void should_SendPetitionerAmendEmail_whenValid() throws TaskException {
        executeTask();

        verify(emailService).sendEmail(
            eq(TestConstants.TEST_PETITIONER_EMAIL),
            eq(EmailTemplateNames.PETITIONER_AMEND_APPLICATION.name()),
            argThat(new HamcrestArgumentMatcher<>(
                    allOf(
                        hasEntry(NOTIFICATION_CASE_NUMBER_KEY, TestConstants.TEST_CASE_FAMILY_MAN_ID),
                        hasEntry(NOTIFICATION_PET_NAME, TestConstants.TEST_USER_FIRST_NAME + " " + TestConstants.TEST_USER_LAST_NAME),
                        hasEntry(NOTIFICATION_FEES_KEY, FEE_AMOUNT_AS_STRING),
                        hasEntry(NOTIFICATION_HUSBAND_OR_WIFE, RELATION)
                    )
                )
            ),
            anyString(),
            eq(LanguagePreference.ENGLISH)
        );
    }

    @Test
    public void should_SendPetitionerAmendEmail_whenValidAnd_NoCaseNumber() throws TaskException {
        incomingPayload.put(D_8_CASE_REFERENCE, null);

        executeTask();

        verify(emailService).sendEmail(
            eq(TestConstants.TEST_PETITIONER_EMAIL),
            eq(EmailTemplateNames.PETITIONER_AMEND_APPLICATION.name()),
            argThat(new HamcrestArgumentMatcher<>(
                    allOf(
                        hasEntry(NOTIFICATION_CASE_NUMBER_KEY, null),
                        hasEntry(NOTIFICATION_PET_NAME, TestConstants.TEST_USER_FIRST_NAME + " " + TestConstants.TEST_USER_LAST_NAME),
                        hasEntry(NOTIFICATION_FEES_KEY, FEE_AMOUNT_AS_STRING),
                        hasEntry(NOTIFICATION_HUSBAND_OR_WIFE, RELATION)
                    )
                )
            ),
            anyString()
        );
    }

    @Test
    public void should_SendPetitionerAmendEmail_whenValidAnd_EmptyString() throws TaskException {
        incomingPayload.put(D_8_CASE_REFERENCE, "");

        executeTask();

        verify(emailService).sendEmail(
            eq(TestConstants.TEST_PETITIONER_EMAIL),
            eq(EmailTemplateNames.PETITIONER_AMEND_APPLICATION.name()),
            argThat(new HamcrestArgumentMatcher<>(
                    allOf(
                        hasEntry(NOTIFICATION_CASE_NUMBER_KEY, ""),
                        hasEntry(NOTIFICATION_PET_NAME, TestConstants.TEST_USER_FIRST_NAME + " " + TestConstants.TEST_USER_LAST_NAME),
                        hasEntry(NOTIFICATION_FEES_KEY, FEE_AMOUNT_AS_STRING),
                        hasEntry(NOTIFICATION_HUSBAND_OR_WIFE, RELATION)
                    )
                )
            ),
            anyString()
        );
    }

    private void executeTask() throws TaskException {
        Map<String, Object> returnedPayload = sendPetitionerAmendEmailTask.execute(taskContext, incomingPayload);

        assertThat(returnedPayload, equalTo(incomingPayload));
    }
}