package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.config.NotificationServiceEmailTemplate;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GenericEmailNotification;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentAnswersGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CO_RESPONDENT_NAMED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_RESPONDENT_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RELATIONSHIP_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_TEMPLATE_VARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.getRelationshipTermByGender;

@Component
public class RespondentSubmittedCallbackWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final GenericEmailNotification emailNotificationTask;
    private final RespondentAnswersGenerator respondentAnswersGenerator;
    private final CaseFormatterAddDocuments caseFormatterAddDocuments;

    @Autowired
    public RespondentSubmittedCallbackWorkflow(GenericEmailNotification emailNotificationTask,
                                               RespondentAnswersGenerator respondentAnswersGenerator,
                                               CaseFormatterAddDocuments caseFormatterAddDocuments) {
        this.emailNotificationTask = emailNotificationTask;
        this.respondentAnswersGenerator = respondentAnswersGenerator;
        this.caseFormatterAddDocuments = caseFormatterAddDocuments;
    }

    public Map<String, Object> run(CcdCallbackRequest ccdCallbackRequest, String authToken) throws WorkflowException {
        final List<Task> tasks = new ArrayList<>();

        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        final String relationship = getRespondentRelationship(caseDetails);

        // only send an email to pet. if respondent is not defending
        if (!respondentIsDefending(caseDetails)) {
            tasks.add(emailNotificationTask);
        }

        tasks.add(respondentAnswersGenerator);
        tasks.add(caseFormatterAddDocuments);

        String firstName = getFieldAsStringOrNull(caseDetails, D_8_PETITIONER_FIRST_NAME);
        String lastName = getFieldAsStringOrNull(caseDetails, D_8_PETITIONER_LAST_NAME);
        String isCoRespNamed = getFieldAsStringOrNull(caseDetails, D_8_CO_RESPONDENT_NAMED);
        String receivedAosFromCoResp = getFieldAsStringOrNull(caseDetails, RECEIVED_AOS_FROM_CO_RESP);

        NotificationServiceEmailTemplate template = NotificationServiceEmailTemplate.RESPONDENT_SUBMISSION_CONSENT;
        if (StringUtils.equalsIgnoreCase(isCoRespNamed, YES_VALUE) && !StringUtils.equalsIgnoreCase(receivedAosFromCoResp, YES_VALUE)) {
            template = NotificationServiceEmailTemplate.RESPONDENT_SUBMISSION_CONSENT_CORESP_NOT_REPLIED;
        }

        Map<String, String> notificationTemplateVars = new HashMap<>();
        String ref = getFieldAsStringOrNull(caseDetails, D_8_CASE_REFERENCE);
        notificationTemplateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, firstName);
        notificationTemplateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, lastName);
        notificationTemplateVars.put(NOTIFICATION_RELATIONSHIP_KEY, relationship);
        notificationTemplateVars.put(NOTIFICATION_REFERENCE_KEY, ref);

        String petitionerEmail = getFieldAsStringOrNull(caseDetails, D_8_PETITIONER_EMAIL);
        Task[] taskArr = new Task[tasks.size()];

        return this.execute(
            tasks.toArray(taskArr),
            ccdCallbackRequest.getCaseDetails().getCaseData(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(NOTIFICATION_EMAIL, petitionerEmail),
            ImmutablePair.of(NOTIFICATION_TEMPLATE_VARS, notificationTemplateVars),
            ImmutablePair.of(NOTIFICATION_TEMPLATE, template),
            ImmutablePair.of(ID, caseDetails.getCaseId())
        );
    }

    private boolean respondentIsDefending(CaseDetails caseDetails) {
        final String respWillDefendDivorce = (String)caseDetails.getCaseData().get(RESP_WILL_DEFEND_DIVORCE);
        return YES_VALUE.equalsIgnoreCase(respWillDefendDivorce);
    }

    private String getRespondentRelationship(CaseDetails caseDetails) {
        String gender = getFieldAsStringOrNull(caseDetails, D_8_INFERRED_RESPONDENT_GENDER);
        return getRelationshipTermByGender(gender);
    }

    private String getFieldAsStringOrNull(final CaseDetails caseDetails, String fieldKey) {
        Object fieldValue = caseDetails.getCaseData().get(fieldKey);
        if (fieldValue == null) {
            return null;
        }
        return fieldValue.toString();
    }
}
