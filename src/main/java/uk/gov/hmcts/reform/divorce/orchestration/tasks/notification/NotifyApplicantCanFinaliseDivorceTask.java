package uk.gov.hmcts.reform.divorce.orchestration.tasks.notification;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PET_SOL_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PET_SOL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.APPLICANT_DA_ELIGIBLE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@Slf4j
@Component
public class NotifyApplicantCanFinaliseDivorceTask implements Task<Map<String, Object>> {

    private static final String EMAIL_DESC = "Email to inform applicant they can finalise divorce";
    private static final String SOL_EMAIL_DESC = "Email to inform solicitor the applicant can finalise divorce";

    @Autowired
    EmailService emailService;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {

        String solicitorEmail = (String) caseData.get(PET_SOL_EMAIL);
        String petitionerEmail = (String) caseData.get(D_8_PETITIONER_EMAIL);
        String petFirstName = getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_FIRST_NAME);
        String petLastName = getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_LAST_NAME);
        Optional<LanguagePreference> welshLanguagePreference = CaseDataUtils.getLanguagePreference(caseData.get(LANGUAGE_PREFERENCE_WELSH));
        Map<String, String> templateVars = new HashMap<>();

        if (StringUtils.isNotBlank(solicitorEmail)) {

            String ccdReference = context.getTransientObject(CASE_ID_JSON_KEY);
            String solicitorName = (String) caseData.get(PET_SOL_NAME);
            String respFirstName = getMandatoryPropertyValueAsString(caseData, RESP_FIRST_NAME_CCD_FIELD);
            String respLastName = getMandatoryPropertyValueAsString(caseData, RESP_LAST_NAME_CCD_FIELD);

            templateVars.put(NOTIFICATION_EMAIL, solicitorEmail);
            templateVars.put(NOTIFICATION_SOLICITOR_NAME, solicitorName);
            templateVars.put(NOTIFICATION_CCD_REFERENCE_KEY, ccdReference);
            templateVars.put(NOTIFICATION_PET_NAME, petFirstName + " " + petLastName);
            templateVars.put(NOTIFICATION_RESP_NAME, respFirstName + " " + respLastName);

            emailService.sendEmail(
                    solicitorEmail,
                    EmailTemplateNames.SOL_APPLICANT_DA_ELIGIBLE.name(),
                    templateVars,
                    SOL_EMAIL_DESC,
                    welshLanguagePreference
            );

        } else if (StringUtils.isNotBlank(petitionerEmail)) {

            String caseNumber = getMandatoryPropertyValueAsString(caseData, D_8_CASE_REFERENCE);

            templateVars.put(NOTIFICATION_EMAIL, petitionerEmail);
            templateVars.put(NOTIFICATION_CASE_NUMBER_KEY, caseNumber);
            templateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, petFirstName);
            templateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, petLastName);

            emailService.sendEmail(
                    petitionerEmail,
                    APPLICANT_DA_ELIGIBLE.name(),
                    templateVars,
                    EMAIL_DESC,
                    welshLanguagePreference
            );
        }

        return caseData;
    }
}