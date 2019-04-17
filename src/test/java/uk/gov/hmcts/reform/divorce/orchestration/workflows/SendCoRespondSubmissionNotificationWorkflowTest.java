package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GenericEmailNotification;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerCoRespondentRespondedNotificationEmail;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.TaskCommons;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_FAMILY_MAN_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EXPECTED_DUE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EXPECTED_DUE_DATE_FORMATTED;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_DEFENDS_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_DUE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_COURT_ADDRESS_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_FORM_SUBMISSION_DATE_LIMIT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RDC_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_TEMPLATE_VARS;

@RunWith(MockitoJUnitRunner.class)
public class SendCoRespondSubmissionNotificationWorkflowTest {

    @Mock
    private GenericEmailNotification emailTask;

    @Mock
    private SendPetitionerCoRespondentRespondedNotificationEmail petitionerEmailTask;

    @Mock
    private TaskCommons taskCommons;

    @InjectMocks
    private SendCoRespondSubmissionNotificationWorkflow classToTest;

    @Test
    public void givenUndefendedCoResp_whenSendEmail_thenSendUndefendedTemplate() throws WorkflowException {

        CcdCallbackRequest ccdCallbackRequest = createSubmittedCoEvent(ImmutableMap.of(CO_RESPONDENT_DEFENDS_DIVORCE, "No"));

        classToTest.run(ccdCallbackRequest);

        ArgumentCaptor<TaskContext> argument = ArgumentCaptor.forClass(TaskContext.class);
        verify(emailTask).execute(argument.capture(), eq(ccdCallbackRequest.getCaseDetails().getCaseData()));

        TaskContext capturedTask = argument.getValue();

        DefaultTaskContext expectedContext = createdExpectedContext(Collections.EMPTY_MAP,
            EmailTemplateNames.CO_RESPONDENT_UNDEFENDED_AOS_SUBMISSION_NOTIFICATION);

        assertThat(expectedContext, equalTo(capturedTask));
    }

    @Test
    public void givenDefendedCoResp_whenSendEmail_thenSendDefendedTemplate() throws Exception {

        CcdCallbackRequest ccdCallbackRequest = createSubmittedCoEvent(ImmutableMap.of(
            CO_RESPONDENT_DEFENDS_DIVORCE, "Yes",
            CO_RESPONDENT_DUE_DATE, TEST_EXPECTED_DUE_DATE));
        Court court = new Court();
        court.setDivorceCentreName(TEST_COURT);
        when(taskCommons.getCourt(TEST_COURT)).thenReturn(court);

        classToTest.run(ccdCallbackRequest);

        ArgumentCaptor<TaskContext> argument = ArgumentCaptor.forClass(TaskContext.class);
        verify(emailTask).execute(argument.capture(), eq(ccdCallbackRequest.getCaseDetails().getCaseData()));

        TaskContext capturedTask = argument.getValue();

        DefaultTaskContext expectedContext = createdExpectedContext(ImmutableMap.of(
                NOTIFICATION_RDC_NAME_KEY, court.getIdentifiableCentreName(),
                NOTIFICATION_FORM_SUBMISSION_DATE_LIMIT_KEY, TEST_EXPECTED_DUE_DATE_FORMATTED,
                NOTIFICATION_COURT_ADDRESS_KEY, court.getFormattedAddress()
                ),
            EmailTemplateNames.CO_RESPONDENT_DEFENDED_AOS_SUBMISSION_NOTIFICATION);

        assertThat(expectedContext, equalTo(capturedTask));
    }

    private DefaultTaskContext createdExpectedContext(Map<String, Object> additionalData, EmailTemplateNames template) {

        Map<String, Object> expectedTemplateVars = new HashMap<>(additionalData);
        expectedTemplateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TEST_USER_FIRST_NAME);
        expectedTemplateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TEST_USER_LAST_NAME);
        expectedTemplateVars.put(NOTIFICATION_CASE_NUMBER_KEY, TEST_CASE_FAMILY_MAN_ID);

        DefaultTaskContext expectedContext = new DefaultTaskContext();
        expectedContext.setTransientObjects(ImmutableMap
            .of(NOTIFICATION_EMAIL, TEST_EMAIL,
                CASE_ID_JSON_KEY, TEST_CASE_ID,
                NOTIFICATION_TEMPLATE, template,
                NOTIFICATION_TEMPLATE_VARS, expectedTemplateVars
            ));
        return expectedContext;
    }

    private CcdCallbackRequest createSubmittedCoEvent(Map<String,Object> extraFields) {

        Map<String, Object> caseData = new HashMap<>(extraFields);

        caseData.put(D_8_CASE_REFERENCE, TEST_CASE_FAMILY_MAN_ID);
        caseData.put(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME, TEST_USER_FIRST_NAME) ;
        caseData.put(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME, TEST_USER_LAST_NAME);
        caseData.put(CO_RESP_EMAIL_ADDRESS, TEST_EMAIL);
        caseData.put(D_8_DIVORCE_UNIT, TEST_COURT);

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(caseData)
            .build();

        return  CcdCallbackRequest
            .builder()
            .caseDetails(caseDetails)
            .build();
    }
}
