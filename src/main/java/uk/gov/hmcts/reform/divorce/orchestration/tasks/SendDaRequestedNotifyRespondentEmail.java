package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_PETITIONER_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL_ADDRESS_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_HUSBAND_OR_WIFE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.getRelationshipTermByGender;

@Component
@Slf4j
public class SendDaRequestedNotifyRespondentEmail implements Task<Map<String, Object>> {

    private static final String EMAIL_DESC = "Decree Absolute Requested Notification - Applicant Requested Decree Absolute";

    private final EmailService emailService;

    @Autowired
    public SendDaRequestedNotifyRespondentEmail(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {

        String emailAddress = (String) caseData.get(RESPONDENT_EMAIL_ADDRESS);
        String firstName = (String) caseData.get(RESP_FIRST_NAME_CCD_FIELD);
        String lastName = (String) caseData.get(RESP_LAST_NAME_CCD_FIELD);
        String d8Reference = (String) caseData.get(D_8_CASE_REFERENCE);
        String petitionerInferredGender = getMandatoryPropertyValueAsString(caseData,
            D_8_INFERRED_PETITIONER_GENDER);
        String petitionerRelationshipToRespondent = getRelationshipTermByGender(petitionerInferredGender);

        if (StringUtils.isNotBlank(emailAddress)) {
            Map<String, String> templateVars = new HashMap<>();

            templateVars.put(NOTIFICATION_EMAIL_ADDRESS_KEY, emailAddress);
            templateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, firstName);
            templateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, lastName);
            templateVars.put(NOTIFICATION_CASE_NUMBER_KEY, d8Reference);
            templateVars.put(NOTIFICATION_HUSBAND_OR_WIFE, petitionerRelationshipToRespondent);

            try {
                emailService.sendEmailAndReturnExceptionIfFails(
                    emailAddress,
                    EmailTemplateNames.DECREE_ABSOLUTE_REQUESTED_NOTIFICATION.name(),
                    templateVars, EMAIL_DESC
                );
            } catch (NotificationClientException e) {
                throw new TaskException(e.getMessage(), e);
            }

        } else {
            log.warn("no respondent email present for case reference, divorce may be using paper journey: {}", d8Reference);
        }

        return caseData;
    }

}
