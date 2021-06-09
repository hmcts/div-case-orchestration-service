package uk.gov.hmcts.reform.divorce.orchestration.workflows.aospack.offline;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.service.TemplateConfigService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GenericEmailNotificationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aos.AosReceivedPetitionerSolicitorEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RELATIONSHIP_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_TEMPLATE_VARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_WELSH_HUSBAND_OR_WIFE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.constants.TaskContextConstants.CCD_CASE_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.SEPARATION_TWO_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@RunWith(MockitoJUnitRunner.class)
public class RespondentAosOfflineNotificationTest {

    @Mock
    private TemplateConfigService templateConfigService;

    @Mock
    private GenericEmailNotificationTask emailNotificationTask;

    @Mock
    private AosReceivedPetitionerSolicitorEmailTask aosReceivedPetitionerSolicitorEmailTask;

    private RespondentAosOfflineNotification respondentAosOfflineNotification;

    private Map<String, Object> contextTransientObjects;

    private List<Task<Map<String, Object>>> tasks;

    private final CaseDataUtils caseDataUtils = new CaseDataUtils();

    private final OfflineAosTestFixture testFixture = new OfflineAosTestFixture();

    @Before
    public void setUp() {
        contextTransientObjects = new HashMap<>();
        tasks = new LinkedList<>();

        // don't boot spring for a unit test
        respondentAosOfflineNotification = new RespondentAosOfflineNotification(templateConfigService, caseDataUtils, emailNotificationTask, aosReceivedPetitionerSolicitorEmailTask);

        when(templateConfigService.getRelationshipTermByGender(anyString(), any())).then(AdditionalAnswers.returnsFirstArg());
    }

    @Test
    public void test_PetitionerSolicitorEmail() {
        Map<String, Object> caseData = testFixture.getCaseDataForPetitionerSolicitorEmail();

        String caseId = getMandatoryPropertyValueAsString(caseData, D_8_CASE_REFERENCE);

        CaseDetails caseDetails = mock(CaseDetails.class);
        when(caseDetails.getCaseId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(caseData);

        try {
            respondentAosOfflineNotification.addAOSEmailTasks(contextTransientObjects, tasks, caseDetails, OfflineAosTestFixture.AUTH_TOKEN);
        } catch (WorkflowException e) {
            e.printStackTrace();
            fail("no exception expected");
        }

        verify(caseDetails, atLeast(1)).getCaseId();
        verify(caseDetails, atLeast(1)).getCaseData();

        assertThat(tasks, hasSize(1));
        assertThat(tasks.get(0), is(aosReceivedPetitionerSolicitorEmailTask));

        assertThat(contextTransientObjects.get(AUTH_TOKEN_JSON_KEY), is(OfflineAosTestFixture.AUTH_TOKEN));
        assertThat(contextTransientObjects.get(CASE_ID_JSON_KEY), is(caseId));
        assertThat(contextTransientObjects.get(CCD_CASE_DATA), is(caseData));
    }

    @Test
    public void test_PetitionerEmail_RespondentSolicitor_Defending() {
        Map<String, Object> caseData = testFixture.getCaseDataForPetitionerEmail(true, true, null, EMPTY, EMPTY, EMPTY);

        String caseId = getMandatoryPropertyValueAsString(caseData, D_8_CASE_REFERENCE);
        String petitionerEmail = getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_EMAIL);

        CaseDetails caseDetails = mock(CaseDetails.class);
        when(caseDetails.getCaseId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(caseData);

        try {
            respondentAosOfflineNotification.addAOSEmailTasks(contextTransientObjects, tasks, caseDetails, OfflineAosTestFixture.AUTH_TOKEN);
        } catch (WorkflowException e) {
            e.printStackTrace();
            fail("no exception expected");
        }

        verify(caseDetails, atLeast(1)).getCaseId();
        verify(caseDetails, atLeast(1)).getCaseData();

        assertThat(tasks, hasSize(1));
        assertThat(tasks.get(0), is(emailNotificationTask));

        assertThat(contextTransientObjects.get(AUTH_TOKEN_JSON_KEY), is(OfflineAosTestFixture.AUTH_TOKEN));
        assertThat(contextTransientObjects.get(CASE_ID_JSON_KEY), is(caseId));
        assertThat(contextTransientObjects.get(CCD_CASE_DATA), is(caseData));
        assertThat(contextTransientObjects.get(NOTIFICATION_EMAIL), is(petitionerEmail));
        assertThat(contextTransientObjects.get(NOTIFICATION_TEMPLATE), is(EmailTemplateNames.RESPONDENT_SUBMISSION_CONSENT));

        checkTemplateVariables(contextTransientObjects);
    }

    @Test
    public void test_PetitionerEmail_RespondentSolicitor_NotDefending_AdulteryNotAdmitted_CoRespNotReplied() {
        Map<String, Object> caseData = testFixture.getCaseDataForPetitionerEmail(true, false, ADULTERY, NO_VALUE, YES_VALUE, NO_VALUE);

        String caseId = getMandatoryPropertyValueAsString(caseData, D_8_CASE_REFERENCE);
        String petitionerEmail = getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_EMAIL);

        CaseDetails caseDetails = mock(CaseDetails.class);
        when(caseDetails.getCaseId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(caseData);

        try {
            respondentAosOfflineNotification.addAOSEmailTasks(contextTransientObjects, tasks, caseDetails, OfflineAosTestFixture.AUTH_TOKEN);
        } catch (WorkflowException e) {
            e.printStackTrace();
            fail("no exception expected");
        }

        verify(caseDetails, atLeast(1)).getCaseId();
        verify(caseDetails, atLeast(1)).getCaseData();

        assertThat(tasks, hasSize(1));
        assertThat(tasks.get(0), is(emailNotificationTask));

        assertThat(contextTransientObjects.get(AUTH_TOKEN_JSON_KEY), is(OfflineAosTestFixture.AUTH_TOKEN));
        assertThat(contextTransientObjects.get(CASE_ID_JSON_KEY), is(caseId));
        assertThat(contextTransientObjects.get(CCD_CASE_DATA), is(caseData));
        assertThat(contextTransientObjects.get(NOTIFICATION_EMAIL), is(petitionerEmail));
        assertThat(contextTransientObjects.get(NOTIFICATION_TEMPLATE), is(EmailTemplateNames.AOS_RECEIVED_UNDEFENDED_NO_ADMIT_ADULTERY_CORESP_NOT_REPLIED));

        checkTemplateVariables(contextTransientObjects);
    }

    @Test
    public void test_PetitionerEmail_RespondentSolicitor_NotDefending_AdulteryNotAdmitted_CoRespReplied() {
        Map<String, Object> caseData = testFixture.getCaseDataForPetitionerEmail(true, false, ADULTERY, NO_VALUE, YES_VALUE, YES_VALUE);

        String caseId = getMandatoryPropertyValueAsString(caseData, D_8_CASE_REFERENCE);
        String petitionerEmail = getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_EMAIL);

        CaseDetails caseDetails = mock(CaseDetails.class);
        when(caseDetails.getCaseId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(caseData);

        try {
            respondentAosOfflineNotification.addAOSEmailTasks(contextTransientObjects, tasks, caseDetails, OfflineAosTestFixture.AUTH_TOKEN);
        } catch (WorkflowException e) {
            e.printStackTrace();
            fail("no exception expected");
        }

        verify(caseDetails, atLeast(1)).getCaseId();
        verify(caseDetails, atLeast(1)).getCaseData();

        assertThat(tasks, hasSize(1));
        assertThat(tasks.get(0), is(emailNotificationTask));

        assertThat(contextTransientObjects.get(AUTH_TOKEN_JSON_KEY), is(OfflineAosTestFixture.AUTH_TOKEN));
        assertThat(contextTransientObjects.get(CASE_ID_JSON_KEY), is(caseId));
        assertThat(contextTransientObjects.get(CCD_CASE_DATA), is(caseData));
        assertThat(contextTransientObjects.get(NOTIFICATION_EMAIL), is(petitionerEmail));
        assertThat(contextTransientObjects.get(NOTIFICATION_TEMPLATE), is(EmailTemplateNames.AOS_RECEIVED_UNDEFENDED_NO_ADMIT_ADULTERY));

        checkTemplateVariables(contextTransientObjects);
    }

    @Test
    public void test_PetitionerEmail_RespondentSolicitor_2YearSeparation() {
        Map<String, Object> caseData = testFixture.getCaseDataForPetitionerEmail(true, true, SEPARATION_TWO_YEARS, NO_VALUE, EMPTY, EMPTY);

        String caseId = getMandatoryPropertyValueAsString(caseData, D_8_CASE_REFERENCE);
        String petitionerEmail = getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_EMAIL);

        CaseDetails caseDetails = mock(CaseDetails.class);
        when(caseDetails.getCaseId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(caseData);

        try {
            respondentAosOfflineNotification.addAOSEmailTasks(contextTransientObjects, tasks, caseDetails, OfflineAosTestFixture.AUTH_TOKEN);
        } catch (WorkflowException e) {
            e.printStackTrace();
            fail("no exception expected");
        }

        verify(caseDetails, atLeast(1)).getCaseId();
        verify(caseDetails, atLeast(1)).getCaseData();

        assertThat(tasks, hasSize(1));
        assertThat(tasks.get(0), is(emailNotificationTask));

        assertThat(contextTransientObjects.get(AUTH_TOKEN_JSON_KEY), is(OfflineAosTestFixture.AUTH_TOKEN));
        assertThat(contextTransientObjects.get(CASE_ID_JSON_KEY), is(caseId));
        assertThat(contextTransientObjects.get(CCD_CASE_DATA), is(caseData));
        assertThat(contextTransientObjects.get(NOTIFICATION_EMAIL), is(petitionerEmail));
        assertThat(contextTransientObjects.get(NOTIFICATION_TEMPLATE), is(EmailTemplateNames.AOS_RECEIVED_UNDEFENDED_NO_CONSENT_2_YEARS));

        checkTemplateVariables(contextTransientObjects);
    }

    @Test
    public void test_PetitionerEmail_RespondentSolicitor_CoRespNamedNotReplied() {
        Map<String, Object> caseData = testFixture.getCaseDataForPetitionerEmail(true, true, null, EMPTY, YES_VALUE, NO_VALUE);

        String caseId = getMandatoryPropertyValueAsString(caseData, D_8_CASE_REFERENCE);
        String petitionerEmail = getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_EMAIL);

        CaseDetails caseDetails = mock(CaseDetails.class);
        when(caseDetails.getCaseId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(caseData);

        try {
            respondentAosOfflineNotification.addAOSEmailTasks(contextTransientObjects, tasks, caseDetails, OfflineAosTestFixture.AUTH_TOKEN);
        } catch (WorkflowException e) {
            e.printStackTrace();
            fail("no exception expected");
        }

        verify(caseDetails, atLeast(1)).getCaseId();
        verify(caseDetails, atLeast(1)).getCaseData();

        assertThat(tasks, hasSize(1));
        assertThat(tasks.get(0), is(emailNotificationTask));

        assertThat(contextTransientObjects.get(AUTH_TOKEN_JSON_KEY), is(OfflineAosTestFixture.AUTH_TOKEN));
        assertThat(contextTransientObjects.get(CASE_ID_JSON_KEY), is(caseId));
        assertThat(contextTransientObjects.get(CCD_CASE_DATA), is(caseData));
        assertThat(contextTransientObjects.get(NOTIFICATION_EMAIL), is(petitionerEmail));
        assertThat(contextTransientObjects.get(NOTIFICATION_TEMPLATE), is(EmailTemplateNames.RESPONDENT_SUBMISSION_CONSENT_CORESP_NOT_REPLIED));

        checkTemplateVariables(contextTransientObjects);
    }

    @Test
    public void test_PetitionerEmail_Respondent_NotDefending() {
        Map<String, Object> caseData = testFixture.getCaseDataForPetitionerEmail(false, false, null, EMPTY, EMPTY, EMPTY);

        String caseId = getMandatoryPropertyValueAsString(caseData, D_8_CASE_REFERENCE);
        String petitionerEmail = getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_EMAIL);

        CaseDetails caseDetails = mock(CaseDetails.class);
        when(caseDetails.getCaseId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(caseData);

        try {
            respondentAosOfflineNotification.addAOSEmailTasks(contextTransientObjects, tasks, caseDetails, OfflineAosTestFixture.AUTH_TOKEN);
        } catch (WorkflowException e) {
            e.printStackTrace();
            fail("no exception expected");
        }

        verify(caseDetails, atLeast(1)).getCaseId();
        verify(caseDetails, atLeast(1)).getCaseData();

        assertThat(tasks, hasSize(1));
        assertThat(tasks.get(0), is(emailNotificationTask));

        assertThat(contextTransientObjects.get(AUTH_TOKEN_JSON_KEY), is(OfflineAosTestFixture.AUTH_TOKEN));
        assertThat(contextTransientObjects.get(CASE_ID_JSON_KEY), is(caseId));
        assertThat(contextTransientObjects.get(CCD_CASE_DATA), is(caseData));
        assertThat(contextTransientObjects.get(NOTIFICATION_EMAIL), is(petitionerEmail));
        assertThat(contextTransientObjects.get(NOTIFICATION_TEMPLATE), is(EmailTemplateNames.RESPONDENT_SUBMISSION_CONSENT));

        checkTemplateVariables(contextTransientObjects);
    }

    @Test
    public void test_PetitionerEmail_Respondent_NotDefending_AdulteryNotAdmitted_CoRespNotReplied() {
        Map<String, Object> caseData = testFixture.getCaseDataForPetitionerEmail(false, false, ADULTERY, NO_VALUE, YES_VALUE, NO_VALUE);

        String caseId = getMandatoryPropertyValueAsString(caseData, D_8_CASE_REFERENCE);
        String petitionerEmail = getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_EMAIL);

        CaseDetails caseDetails = mock(CaseDetails.class);
        when(caseDetails.getCaseId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(caseData);

        try {
            respondentAosOfflineNotification.addAOSEmailTasks(contextTransientObjects, tasks, caseDetails, OfflineAosTestFixture.AUTH_TOKEN);
        } catch (WorkflowException e) {
            e.printStackTrace();
            fail("no exception expected");
        }

        verify(caseDetails, atLeast(1)).getCaseId();
        verify(caseDetails, atLeast(1)).getCaseData();

        assertThat(tasks, hasSize(1));
        assertThat(tasks.get(0), is(emailNotificationTask));

        assertThat(contextTransientObjects.get(AUTH_TOKEN_JSON_KEY), is(OfflineAosTestFixture.AUTH_TOKEN));
        assertThat(contextTransientObjects.get(CASE_ID_JSON_KEY), is(caseId));
        assertThat(contextTransientObjects.get(CCD_CASE_DATA), is(caseData));
        assertThat(contextTransientObjects.get(NOTIFICATION_EMAIL), is(petitionerEmail));
        assertThat(contextTransientObjects.get(NOTIFICATION_TEMPLATE), is(EmailTemplateNames.AOS_RECEIVED_UNDEFENDED_NO_ADMIT_ADULTERY_CORESP_NOT_REPLIED));

        checkTemplateVariables(contextTransientObjects);
    }

    @Test
    public void test_PetitionerEmail_Respondent_NotDefending_AdulteryNotAdmitted_CoRespReplied() {
        Map<String, Object> caseData = testFixture.getCaseDataForPetitionerEmail(false, false, ADULTERY, NO_VALUE, YES_VALUE, YES_VALUE);

        String caseId = getMandatoryPropertyValueAsString(caseData, D_8_CASE_REFERENCE);
        String petitionerEmail = getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_EMAIL);

        CaseDetails caseDetails = mock(CaseDetails.class);
        when(caseDetails.getCaseId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(caseData);

        try {
            respondentAosOfflineNotification.addAOSEmailTasks(contextTransientObjects, tasks, caseDetails, OfflineAosTestFixture.AUTH_TOKEN);
        } catch (WorkflowException e) {
            e.printStackTrace();
            fail("no exception expected");
        }

        verify(caseDetails, atLeast(1)).getCaseId();
        verify(caseDetails, atLeast(1)).getCaseData();

        assertThat(tasks, hasSize(1));
        assertThat(tasks.get(0), is(emailNotificationTask));

        assertThat(contextTransientObjects.get(AUTH_TOKEN_JSON_KEY), is(OfflineAosTestFixture.AUTH_TOKEN));
        assertThat(contextTransientObjects.get(CASE_ID_JSON_KEY), is(caseId));
        assertThat(contextTransientObjects.get(CCD_CASE_DATA), is(caseData));
        assertThat(contextTransientObjects.get(NOTIFICATION_EMAIL), is(petitionerEmail));
        assertThat(contextTransientObjects.get(NOTIFICATION_TEMPLATE), is(EmailTemplateNames.AOS_RECEIVED_UNDEFENDED_NO_ADMIT_ADULTERY));

        checkTemplateVariables(contextTransientObjects);
    }

    @Test
    public void test_PetitionerEmail_Respondent_NotDefending_2YearSeparation() {
        Map<String, Object> caseData = testFixture.getCaseDataForPetitionerEmail(false, false, SEPARATION_TWO_YEARS, NO_VALUE, EMPTY, EMPTY);

        String caseId = getMandatoryPropertyValueAsString(caseData, D_8_CASE_REFERENCE);
        String petitionerEmail = getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_EMAIL);

        CaseDetails caseDetails = mock(CaseDetails.class);
        when(caseDetails.getCaseId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(caseData);

        try {
            respondentAosOfflineNotification.addAOSEmailTasks(contextTransientObjects, tasks, caseDetails, OfflineAosTestFixture.AUTH_TOKEN);
        } catch (WorkflowException e) {
            e.printStackTrace();
            fail("no exception expected");
        }

        verify(caseDetails, atLeast(1)).getCaseId();
        verify(caseDetails, atLeast(1)).getCaseData();

        assertThat(tasks, hasSize(1));
        assertThat(tasks.get(0), is(emailNotificationTask));

        assertThat(contextTransientObjects.get(AUTH_TOKEN_JSON_KEY), is(OfflineAosTestFixture.AUTH_TOKEN));
        assertThat(contextTransientObjects.get(CASE_ID_JSON_KEY), is(caseId));
        assertThat(contextTransientObjects.get(CCD_CASE_DATA), is(caseData));
        assertThat(contextTransientObjects.get(NOTIFICATION_EMAIL), is(petitionerEmail));
        assertThat(contextTransientObjects.get(NOTIFICATION_TEMPLATE), is(EmailTemplateNames.AOS_RECEIVED_UNDEFENDED_NO_CONSENT_2_YEARS));

        checkTemplateVariables(contextTransientObjects);
    }

    @Test
    public void test_PetitionerEmail_Respondent_NotDefending_CoRespNamedNotReplied() {
        Map<String, Object> caseData = testFixture.getCaseDataForPetitionerEmail(false, false, null, EMPTY, YES_VALUE, NO_VALUE);

        String caseId = getMandatoryPropertyValueAsString(caseData, D_8_CASE_REFERENCE);
        String petitionerEmail = getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_EMAIL);

        CaseDetails caseDetails = mock(CaseDetails.class);
        when(caseDetails.getCaseId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(caseData);

        try {
            respondentAosOfflineNotification.addAOSEmailTasks(contextTransientObjects, tasks, caseDetails, OfflineAosTestFixture.AUTH_TOKEN);
        } catch (WorkflowException e) {
            e.printStackTrace();
            fail("no exception expected");
        }

        verify(caseDetails, atLeast(1)).getCaseId();
        verify(caseDetails, atLeast(1)).getCaseData();

        assertThat(tasks, hasSize(1));
        assertThat(tasks.get(0), is(emailNotificationTask));

        assertThat(contextTransientObjects.get(AUTH_TOKEN_JSON_KEY), is(OfflineAosTestFixture.AUTH_TOKEN));
        assertThat(contextTransientObjects.get(CASE_ID_JSON_KEY), is(caseId));
        assertThat(contextTransientObjects.get(CCD_CASE_DATA), is(caseData));
        assertThat(contextTransientObjects.get(NOTIFICATION_EMAIL), is(petitionerEmail));
        assertThat(contextTransientObjects.get(NOTIFICATION_TEMPLATE), is(EmailTemplateNames.RESPONDENT_SUBMISSION_CONSENT_CORESP_NOT_REPLIED));

        checkTemplateVariables(contextTransientObjects);
    }

    @Test
    public void test_PetitionerEmail_Respondent_Defending_CoRespNamedNotReplied() {
        Map<String, Object> caseData = testFixture.getCaseDataForPetitionerEmail(false, true, null, EMPTY, EMPTY, EMPTY);

        String caseId = getMandatoryPropertyValueAsString(caseData, D_8_CASE_REFERENCE);
        //String petitionerEmail = getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_EMAIL);

        CaseDetails caseDetails = mock(CaseDetails.class);
        when(caseDetails.getCaseId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(caseData);

        try {
            respondentAosOfflineNotification.addAOSEmailTasks(contextTransientObjects, tasks, caseDetails, OfflineAosTestFixture.AUTH_TOKEN);
        } catch (WorkflowException e) {
            e.printStackTrace();
            fail("no exception expected");
        }

        verify(caseDetails, atLeast(1)).getCaseId();
        verify(caseDetails, atLeast(1)).getCaseData();

        assertThat(tasks, hasSize(0));
    }

    private void checkTemplateVariables(final Map<String, Object> contextTransientObjects) {
        assertThat(contextTransientObjects.get(NOTIFICATION_TEMPLATE_VARS), is(not(nullValue())));

        Map<String, String> templateVars = (Map<String, String>)contextTransientObjects.get(NOTIFICATION_TEMPLATE_VARS);

        assertThat(templateVars, aMapWithSize(5));
        assertThat(templateVars.get(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY), is(not(emptyOrNullString())));
        assertThat(templateVars.get(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY), is(not(emptyOrNullString())));
        assertThat(templateVars.get(NOTIFICATION_RELATIONSHIP_KEY), is(not(emptyOrNullString())));
        assertThat(templateVars.get(NOTIFICATION_WELSH_HUSBAND_OR_WIFE), is(not(emptyOrNullString())));
        assertThat(templateVars.get(NOTIFICATION_REFERENCE_KEY), is(not(emptyOrNullString())));
    }
}
