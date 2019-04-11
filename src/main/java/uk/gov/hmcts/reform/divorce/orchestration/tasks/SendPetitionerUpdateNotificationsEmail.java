package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_TO_FACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SEPARATION_2YRS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
public class SendPetitionerUpdateNotificationsEmail implements Task<Map<String, Object>> {

    private final EmailService emailService;
    private final boolean featureToggle520;

    @Autowired
    public SendPetitionerUpdateNotificationsEmail(
            EmailService emailService,
            @Value("${feature-toggle.toggle.feature-toggle-520}") String featureToggle520) {

        this.emailService = emailService;
        this.featureToggle520 = Boolean.valueOf(featureToggle520);
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {

        String petitionerEmail = (String) caseData.get(D_8_PETITIONER_EMAIL);
        String reasonForDivorce = (String) caseData.get(D_8_REASON_FOR_DIVORCE);
        String respAdmitOrConsentToFact = (String) caseData.get(RESP_ADMIT_OR_CONSENT_TO_FACT);
        String relationship = (String) caseData.get(D_8_DIVORCED_WHO);
        String isCoRespNamed = (String) caseData.get(D_8_CO_RESPONDENT_NAMED);
        String receivedAosFromCoResp = (String) caseData.get(RECEIVED_AOS_FROM_CO_RESP);

        Map<String, String> templateVars = new HashMap<>();

        templateVars.put("email address", petitionerEmail);
        templateVars.put("first name", (String) caseData.get(D_8_PETITIONER_FIRST_NAME));
        templateVars.put("last name", (String) caseData.get(D_8_PETITIONER_LAST_NAME));
        templateVars.put("CCD reference", (String) caseData.get(D_8_CASE_REFERENCE));

        if (StringUtils.isNotBlank(petitionerEmail)) {
            if (featureToggle520) {
                if (reasonForDivorce.equals(ADULTERY) && NO_VALUE.equalsIgnoreCase(respAdmitOrConsentToFact)) {
                    templateVars.put("relationship", relationship);

                    if (StringUtils.equalsIgnoreCase(isCoRespNamed, YES_VALUE) && !StringUtils.equalsIgnoreCase(receivedAosFromCoResp, YES_VALUE)) {
                        emailService.sendEmail(petitionerEmail,
                                EmailTemplateNames.AOS_RECEIVED_NO_ADMIT_ADULTERY_CORESP_NOT_REPLIED.name(),
                                templateVars, "resp does not admit adultery update notification - no reply from co-resp");
                    } else {
                        emailService.sendEmail(petitionerEmail,
                                EmailTemplateNames.AOS_RECEIVED_NO_ADMIT_ADULTERY.name(),
                                templateVars, "resp does not admit adultery update notification");
                    }

                } else if (reasonForDivorce.equals(SEPARATION_2YRS)
                        && NO_VALUE.equalsIgnoreCase(respAdmitOrConsentToFact)) {
                    templateVars.put("relationship", relationship);

                    emailService.sendEmail(petitionerEmail,
                            EmailTemplateNames.AOS_RECEIVED_NO_CONSENT_2_YEARS.name(),
                            templateVars,
                            "resp does not consent to 2 year separation update notification");

                } else {
                    emailService.sendEmail(petitionerEmail, EmailTemplateNames.GENERIC_UPDATE.name(), templateVars, "generic update notification");

                }
            } else {
                emailService.sendEmail(petitionerEmail, EmailTemplateNames.GENERIC_UPDATE.name(), templateVars, "generic update notification");
            }
        }
        return caseData;
    }
}