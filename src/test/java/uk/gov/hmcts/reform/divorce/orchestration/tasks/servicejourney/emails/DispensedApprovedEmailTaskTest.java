package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.DnCourt;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.exception.CourtDetailsNotFound;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.TaskCommons;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CaseDataExtractor.CaseDataKeys.PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerSolicitorFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getRespondentFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DeemedApprovedEmailTaskTest.getTaskContext;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@RunWith(MockitoJUnitRunner.class)
public class DispensedApprovedEmailTaskTest {

    //    private static final String SOL_DISPENSED_APPROVED_JSON = "/jsonExamples/payloads/dispensedApprovedSolicitor.json";

    @Mock
    private EmailService emailService;

    @Mock
    private TaskCommons taskCommons;

    private DefaultTaskContext testContext;
    private Map<String, String> expectedTemplateVars;

    @InjectMocks
    private DispensedApprovedEmailTask dispensedApprovedEmailTask;

    private Map<String, Object> caseData = new HashMap<>();

    private Map<String, Object> setupSolicitorCaseData() {
        caseData.put(PETITIONER_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);
        caseData.put(PETITIONER_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        caseData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME);
        caseData.put(CCD_CASE_ID, TEST_CASE_ID);

        return caseData;
    }

    private Map<String, Object> setupCitizenCaseData() {
        caseData.remove(PETITIONER_SOLICITOR_EMAIL);
        caseData.remove(PETITIONER_SOLICITOR_NAME);
        caseData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME);
        caseData.put(CCD_CASE_ID, TEST_CASE_ID);

        return caseData;
    }

    @Before
    public void setUp() throws CourtDetailsNotFound {
        caseData = new HashMap<>();
        testContext = new DefaultTaskContext();
        testContext.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);

        DnCourt dnCourt = new DnCourt();
        dnCourt.setName("Court Name");
        when(taskCommons.getDnCourt(anyString())).thenReturn(dnCourt);
    }

    @Test
    public void whenExecuteEmailNotificationTask_thenSendEmail_ToCitizen() {
        Map<String, Object> caseData = buildCaseData(false);
        caseData.put(PETITIONER_EMAIL, TEST_PETITIONER_EMAIL);

        dispensedApprovedEmailTask.execute(getTaskContext(), caseData);

        verify(emailService).sendEmail(
            TEST_USER_EMAIL,
            EmailTemplateNames.CITIZEN_DISPENSED_APPROVED.name(),
            getExpectedNotificationTemplateVars(false, testContext, caseData),
            dispensedApprovedEmailTask.getSubject(caseData),
            LanguagePreference.ENGLISH
        );
    }

    @Test
    public void shouldEmailSolicitor() throws TaskException, IOException {
        Map<String, Object> caseData = buildCaseData(true);

        dispensedApprovedEmailTask.execute(getTaskContext(), caseData);

        verify(emailService).sendEmail(
            TEST_USER_EMAIL,
            EmailTemplateNames.SOL_DISPENSED_APPROVED.name(),
            getExpectedNotificationTemplateVars(true, testContext, caseData),
            dispensedApprovedEmailTask.getSubject(caseData),
            any()
        );
    }


    @Test
    public void whenEmptyRecipientEmail_thenDoNotSendEmail() {
        Map<String, Object> caseData = buildCaseData(false);
        caseData.remove(PETITIONER_EMAIL);
        caseData.remove(PETITIONER_SOLICITOR_EMAIL);

        dispensedApprovedEmailTask.execute(getTaskContext(), caseData);

        verifyZeroInteractions(emailService);
    }

//    @Test
//    public void shouldReturnPersonalisationFor_Citizen() {}
//
//    @Test
//    public void shouldReturnSubjectFor_Solicitor() {}
//
//    @Test
//    public void shouldReturnSubjectFor_Citizen() {}
//
//    @Test
//    public void shouldReturnTemplateFor_Solicitor() {}
//
//    @Test
//    public void shouldReturnTemplateFor_Citizen() {}
//
//    private void verifySolicitorEmailParameters() throws TaskException {
//        verify(emailService).sendEmail(
//            eq(TEST_SOLICITOR_EMAIL),
//            eq(EmailTemplateNames.SOL_DISPENSED_APPROVED.name()),
//            eq(getExpectedNotificationTemplateVars(false, testContext, caseData)),
//            any(),
//            eq(LanguagePreference.ENGLISH));
//    }

    private Map<String, Object> buildCaseData(boolean isRespondentRepresented) {
        Map<String, Object> caseData = isRespondentRepresented
            ? AddresseeDataExtractorTest.buildCaseDataWithPetitionerSolicitor()
            : AddresseeDataExtractorTest.buildCaseDataWithRespondent();

        caseData.put(PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME);

        return caseData;
    }

    private static Map<String, String> getExpectedNotificationTemplateVars(boolean isRespondentRepresented, TaskContext taskContext, Map<String, Object> caseData) {
        if (isRespondentRepresented){
            return ImmutableMap.of(
                NOTIFICATION_PET_NAME, getPetitionerFullName(caseData),
                NOTIFICATION_RESP_NAME, getRespondentFullName(caseData),
                NOTIFICATION_CCD_REFERENCE_KEY, getCaseId(taskContext),
                NOTIFICATION_SOLICITOR_NAME, getPetitionerSolicitorFullName(caseData)
            );
        } else {
            return ImmutableMap.of(
                NOTIFICATION_PET_NAME, TEST_PETITIONER_FIRST_NAME + " " + TEST_PETITIONER_LAST_NAME
            );
        }
    }
}

