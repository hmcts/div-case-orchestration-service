package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PET_SOL_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PET_SOL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@Component
public class SendPetitionerCoRespondentRespondedNotificationEmail implements Task<Map<String, Object>> {

    private final EmailService emailService;

    @Autowired
    public SendPetitionerCoRespondentRespondedNotificationEmail(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {

        String petSolicitorEmail = (String) caseData.get(PET_SOL_EMAIL);
        String petitionerEmail = (String) caseData.get(D_8_PETITIONER_EMAIL);

        String ccdReference = getMandatoryPropertyValueAsString(caseData, D_8_CASE_REFERENCE);
        String petitionerFirstName = getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_FIRST_NAME);
        String petitionerLastName = getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_LAST_NAME);

        Map<String, String> templateVars = new HashMap<>();

        if (StringUtils.isNotBlank(petSolicitorEmail)) {
            String respFirstName = getMandatoryPropertyValueAsString(caseData, RESP_FIRST_NAME_CCD_FIELD);
            String respLastName = getMandatoryPropertyValueAsString(caseData, RESP_LAST_NAME_CCD_FIELD);
            String solicitorName = getMandatoryPropertyValueAsString(caseData, PET_SOL_NAME);

            templateVars.put(NOTIFICATION_CCD_REFERENCE_KEY, ccdReference);
            templateVars.put(NOTIFICATION_EMAIL, petSolicitorEmail);
            templateVars.put(NOTIFICATION_PET_NAME, petitionerFirstName + " " + petitionerLastName);
            templateVars.put(NOTIFICATION_RESP_NAME, respFirstName + " " + respLastName);
            templateVars.put(NOTIFICATION_SOLICITOR_NAME, solicitorName);

            emailService.sendEmail(petSolicitorEmail,
                EmailTemplateNames.SOL_APPLICANT_CORESP_RESPONDED.name(),
                templateVars,
                "co-respondent responded - notification to solicitor");

        } else if (StringUtils.isNotBlank(petitionerEmail)) {

            templateVars.put(NOTIFICATION_REFERENCE_KEY, (String) caseData.get(D_8_CASE_REFERENCE));
            templateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, (String) caseData.get(D_8_PETITIONER_FIRST_NAME));
            templateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, (String) caseData.get(D_8_PETITIONER_LAST_NAME));

            if (isRespondentRespondedCase(caseData)) {
                if (!isRespondentDefendedCase(caseData)) {
                    emailService.sendEmail(petitionerEmail,
                        EmailTemplateNames.APPLICANT_CO_RESPONDENT_RESPONDS_AOS_SUBMITTED_NO_DEFEND.name(),
                        templateVars,
                        "co-respondent responded when aos is undefended");
                }
            } else {
                emailService.sendEmail(petitionerEmail,
                    EmailTemplateNames.APPLICANT_CO_RESPONDENT_RESPONDS_AOS_NOT_SUBMITTED.name(),
                    templateVars,
                    "co-respondent responded but respondent has not");
            }
        }

        return caseData;
    }

    private boolean isRespondentDefendedCase(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase((String) caseData.get(RESP_WILL_DEFEND_DIVORCE));
    }

    private boolean isRespondentRespondedCase(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase((String) caseData.get(RECEIVED_AOS_FROM_RESP));
    }
}