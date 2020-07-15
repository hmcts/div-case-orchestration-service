package uk.gov.hmcts.reform.divorce.orchestration.tasks.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AMEND_PETITION_FEE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_RESPONDENT_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_FEES_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_HUSBAND_OR_WIFE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ExtractorHelper.getMandatoryStringValue;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.PreviousAmendPetitionStateLoggerHelper.getAmendPetitionPreviousState;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.getRelationshipTermByGender;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendPetitionerAmendEmailTask implements Task<Map<String, Object>> {
    private static final String EMAIL_DESCRIPTION = "Petitioner - You can amend your application notification";

    private final EmailService emailService;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        return sendAmendApplicationEmailToPetitioner(context, caseData);
    }

    private Map<String, Object> sendAmendApplicationEmailToPetitioner(TaskContext context, Map<String, Object> payload) throws TaskException {
        String petitionerEmail = getMandatoryStringValue(payload, D_8_PETITIONER_EMAIL);

        logEvent(context);

        emailService.sendEmail(
            petitionerEmail,
            EmailTemplateNames.PETITIONER_AMEND_APPLICATION.name(),
            getPersonalisation(context, payload),
            EMAIL_DESCRIPTION
        );

        return payload;
    }

    private Map<String, String> getPersonalisation(TaskContext context, Map<String, Object> payload) throws TaskException {
        Map<String, String> personalisation = new HashMap<>();

        String familyManCaseId = getOptionalPropertyValueAsString(payload, D_8_CASE_REFERENCE, null);

        personalisation.put(NOTIFICATION_CASE_NUMBER_KEY, familyManCaseId);
        personalisation.put(NOTIFICATION_PET_NAME, getPetitionerFullName(payload));
        personalisation.put(NOTIFICATION_FEES_KEY, getFormattedFeeAmount(context));
        personalisation.put(NOTIFICATION_HUSBAND_OR_WIFE, getHusbandOrWife(payload));

        return personalisation;
    }

    private void logEvent(TaskContext context) throws TaskException {
        final CaseDetails caseDetails = context.getTransientObject(CASE_DETAILS_JSON_KEY);
        String caseId = getCaseId(context);
        String stateId = caseDetails.getState();

        log.info("CaseId: {}. " + EMAIL_DESCRIPTION + " Previous state " + getAmendPetitionPreviousState(stateId) + ". Task executed", caseId);
    }

    private String getFormattedFeeAmount(TaskContext context) {
        return ((FeeResponse) context.getTransientObject(AMEND_PETITION_FEE_JSON_KEY)).getFormattedFeeAmount();
    }

    private String getHusbandOrWife(Map<String, Object> payload) throws TaskException {
        return getRelationshipTermByGender(getMandatoryPropertyValueAsString(payload,
            D_8_INFERRED_RESPONDENT_GENDER));
    }
}
