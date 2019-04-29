package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CoRespondentAnswersGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GenericEmailNotification;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerCoRespondentRespondedNotificationEmail;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.TaskCommons;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
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

@Component
public class SendCoRespondSubmissionNotificationWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final GenericEmailNotification emailTask;
    private final SendPetitionerCoRespondentRespondedNotificationEmail petitionerEmailTask;
    private final TaskCommons taskCommons;
    private final CoRespondentAnswersGenerator coRespondentAnswersGenerator;
    private final CaseFormatterAddDocuments caseFormatterAddDocuments;

    @Autowired
    public SendCoRespondSubmissionNotificationWorkflow(GenericEmailNotification emailTask,
                                                       SendPetitionerCoRespondentRespondedNotificationEmail petitionerEmailTask,
                                                       TaskCommons taskCommons,
                                                       CoRespondentAnswersGenerator coRespondentAnswersGenerator,
                                                       CaseFormatterAddDocuments caseFormatterAddDocuments) {
        this.emailTask = emailTask;
        this.petitionerEmailTask = petitionerEmailTask;
        this.taskCommons = taskCommons;
        this.coRespondentAnswersGenerator = coRespondentAnswersGenerator;
        this.caseFormatterAddDocuments = caseFormatterAddDocuments;
    }

    public Map<String, Object> run(CcdCallbackRequest ccdCallbackRequest, String authorizationToken) throws WorkflowException {
        final List<Task> tasks = new ArrayList<>();

        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();

        tasks.add(coRespondentAnswersGenerator);
        tasks.add(caseFormatterAddDocuments);
        tasks.add(emailTask);
        tasks.add(petitionerEmailTask);

        String caseNumber = (String) caseData.get(D_8_CASE_REFERENCE);
        String firstName = (String) caseData.get(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME);
        String lastName = (String)  caseData.get(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME);

        Map<String, Object> templateVars = new HashMap<>();

        templateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, firstName);
        templateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, lastName);
        templateVars.put(NOTIFICATION_CASE_NUMBER_KEY, caseNumber);
        String corespondentEmail = (String) caseData.get(CO_RESP_EMAIL_ADDRESS);

        EmailTemplateNames template = EmailTemplateNames.CO_RESPONDENT_UNDEFENDED_AOS_SUBMISSION_NOTIFICATION;

        if (isDefended(caseData)) {
            String rdcName = (String) caseData.get(D_8_DIVORCE_UNIT);
            try {
                Court assignedCourt = taskCommons.getCourt(rdcName);
                templateVars.put(NOTIFICATION_RDC_NAME_KEY, assignedCourt.getIdentifiableCentreName());
                String formSubmissionDateLimit = CcdUtil.getFormattedDueDate(caseData, CO_RESPONDENT_DUE_DATE);

                templateVars.put(NOTIFICATION_FORM_SUBMISSION_DATE_LIMIT_KEY, formSubmissionDateLimit);
                templateVars.put(NOTIFICATION_COURT_ADDRESS_KEY, assignedCourt.getFormattedAddress());
                template = EmailTemplateNames.CO_RESPONDENT_DEFENDED_AOS_SUBMISSION_NOTIFICATION;
            } catch (TaskException e) {
                throw new WorkflowException("Unable to send co-respondent notification",e);
            }
        }

        Task[] taskArr = new Task[tasks.size()];

        return execute(
            tasks.toArray(taskArr),
            caseData,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authorizationToken),
            ImmutablePair.of(CASE_ID_JSON_KEY, ccdCallbackRequest.getCaseDetails().getCaseId()),
            ImmutablePair.of(NOTIFICATION_EMAIL, corespondentEmail),
            ImmutablePair.of(NOTIFICATION_TEMPLATE, template),
            ImmutablePair.of(NOTIFICATION_TEMPLATE_VARS, templateVars)
        );
    }

    private boolean isDefended(Map<String, Object> caseData) {
        return "YES".equalsIgnoreCase((String)caseData.get(CO_RESPONDENT_DEFENDS_DIVORCE));
    }
}
