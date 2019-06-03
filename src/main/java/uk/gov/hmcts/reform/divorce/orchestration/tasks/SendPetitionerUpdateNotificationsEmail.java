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

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CO_RESPONDENT_NAMED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCED_WHO;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RELATIONSHIP_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_TO_FACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SEPARATION_2YRS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@Component
public class SendPetitionerUpdateNotificationsEmail implements Task<Map<String, Object>> {

    private static final String GENERIC_UPDATE_EMAIL_DESC = "Generic Update Notification - Petitioner";
    private static final String AOS_RECEIVED_NO_ADMIT_ADULTERY_EMAIL_DESC =
            "Resp does not admit adultery update notification";
    private static final String AOS_RECEIVED_NO_ADMIT_ADULTERY_CORESP_NOT_REPLIED_EMAIL_DESC =
            "Resp does not admit adultery update notification - no reply from co-resp";
    private static final String AOS_RECEIVED_NO_CONSENT_2_YEARS_EMAIL_DESC =
            "Resp does not consent to 2 year separation update notification";

    private final EmailService emailService;

    @Autowired
    public SendPetitionerUpdateNotificationsEmail(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {

        String petitionerEmail = (String) caseData.get(D_8_PETITIONER_EMAIL);

        if (StringUtils.isNotBlank(petitionerEmail)) {

            String petitionerFirstName = getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_FIRST_NAME);
            String petitionerLastName = getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_LAST_NAME);
            String ccdReference = getMandatoryPropertyValueAsString(caseData, D_8_CASE_REFERENCE);

            Map<String, String> templateVars = new HashMap<>();

            templateVars.put("email address", petitionerEmail);
            templateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, petitionerFirstName);
            templateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, petitionerLastName);
            templateVars.put(NOTIFICATION_CCD_REFERENCE_KEY, ccdReference);

            String reasonForDivorce = getMandatoryPropertyValueAsString(caseData, D_8_REASON_FOR_DIVORCE);
            String relationship = getMandatoryPropertyValueAsString(caseData, D_8_DIVORCED_WHO);
            String respAdmitOrConsentToFact = (String) caseData.get(RESP_ADMIT_OR_CONSENT_TO_FACT);
            String isCoRespNamed = (String) caseData.get(D_8_CO_RESPONDENT_NAMED);
            String receivedAosFromCoResp = (String) caseData.get(RECEIVED_AOS_FROM_CO_RESP);

            if (reasonForDivorce.equals(ADULTERY) && NO_VALUE.equalsIgnoreCase(respAdmitOrConsentToFact)) {
                templateVars.put(NOTIFICATION_RELATIONSHIP_KEY, relationship);

                if (StringUtils.equalsIgnoreCase(isCoRespNamed, YES_VALUE) && !StringUtils.equalsIgnoreCase(receivedAosFromCoResp, YES_VALUE)) {
                    emailService.sendEmail(petitionerEmail,
                            EmailTemplateNames.AOS_RECEIVED_NO_ADMIT_ADULTERY_CORESP_NOT_REPLIED.name(),
                            templateVars, AOS_RECEIVED_NO_ADMIT_ADULTERY_CORESP_NOT_REPLIED_EMAIL_DESC);
                } else {
                    emailService.sendEmail(petitionerEmail,
                            EmailTemplateNames.AOS_RECEIVED_NO_ADMIT_ADULTERY.name(),
                            templateVars, AOS_RECEIVED_NO_ADMIT_ADULTERY_EMAIL_DESC);
                }

            } else if (reasonForDivorce.equals(SEPARATION_2YRS)
                    && NO_VALUE.equalsIgnoreCase(respAdmitOrConsentToFact)) {
                templateVars.put(NOTIFICATION_RELATIONSHIP_KEY, relationship);

                emailService.sendEmail(petitionerEmail,
                        EmailTemplateNames.AOS_RECEIVED_NO_CONSENT_2_YEARS.name(),
                        templateVars, AOS_RECEIVED_NO_CONSENT_2_YEARS_EMAIL_DESC);

            } else {
                emailService.sendEmail(
                        petitionerEmail,
                        EmailTemplateNames.GENERIC_UPDATE.name(),
                        templateVars,
                        GENERIC_UPDATE_EMAIL_DESC);
            }
        }
        return caseData;
    }
}