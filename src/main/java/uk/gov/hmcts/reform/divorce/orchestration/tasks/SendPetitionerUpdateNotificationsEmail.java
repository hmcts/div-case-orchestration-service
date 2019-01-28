package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RELATIONSHIP_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_TO_FACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SEPARATION_2_YEARS;

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
        String reasonForDivorce = (String) caseData.get(D8_REASON_FOR_DIVORCE);
        String respAdmitOrConsentToFact = (String) caseData.get(RESP_ADMIT_OR_CONSENT_TO_FACT);
        String relationship = (String) caseData.get(NOTIFICATION_RELATIONSHIP_KEY);

        Map<String, String> templateVars = new HashMap<>();

        templateVars.put("email address", petitionerEmail);
        templateVars.put("first name", (String) caseData.get(D_8_PETITIONER_FIRST_NAME));
        templateVars.put("last name", (String) caseData.get(D_8_PETITIONER_LAST_NAME));
        templateVars.put("CCD reference", (String) caseData.get(D_8_CASE_REFERENCE));

        if (StringUtils.isNotBlank(petitionerEmail)) {
            if (featureToggle520) {
                if (reasonForDivorce.equals(ADULTERY) && NO_VALUE.equalsIgnoreCase(respAdmitOrConsentToFact)) {
                    templateVars.put("relationship", relationship);

                    emailService.sendPetitionerRespDoesNotAdmitAdulteryUpdateNotificationEmail(petitionerEmail,
                            templateVars);

                } else if (reasonForDivorce.equals(SEPARATION_2_YEARS)
                        && NO_VALUE.equalsIgnoreCase(respAdmitOrConsentToFact)) {
                    templateVars.put("relationship", relationship);

                    emailService.sendPetitionerRespDoesNotConsent2YrsSepUpdateNotificationEmail(petitionerEmail,
                            templateVars);

                } else {
                    emailService.sendPetitionerGenericUpdateNotificationEmail(petitionerEmail, templateVars);

                }
            } else {
                emailService.sendPetitionerGenericUpdateNotificationEmail(petitionerEmail, templateVars);
            }
        }
        return caseData;
    }
}
