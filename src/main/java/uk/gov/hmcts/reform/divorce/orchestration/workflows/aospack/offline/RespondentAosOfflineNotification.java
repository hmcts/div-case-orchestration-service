package uk.gov.hmcts.reform.divorce.orchestration.workflows.aospack.offline;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.GenericEmailContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.service.TemplateConfigService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GenericEmailNotificationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aos.AosReceivedPetitionerSolicitorEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;

import java.util.List;
import java.util.Map;

import static com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_RESPONDENT_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RELATIONSHIP_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_TEMPLATE_VARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_WELSH_HUSBAND_OR_WIFE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.constants.TaskContextConstants.CCD_CASE_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isPetitionerRepresented;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.usingRespondentSolicitor;

@Component
@Slf4j
@AllArgsConstructor
public class RespondentAosOfflineNotification {

    private final TemplateConfigService templateConfigService;

    private final CaseDataUtils caseDataUtils;

    private final GenericEmailNotificationTask emailNotificationTask;

    private final AosReceivedPetitionerSolicitorEmailTask aosReceivedPetitionerSolicitorEmailTask;

    public void addAOSEmailTasks(final Map<String, Object> contextTransientObjects, final List<Task<Map<String, Object>>> tasks,
                                 CaseDetails caseDetails, final String authToken) throws WorkflowException {
        // AOS for respondent has been uploaded by a court admin
        final String caseId = caseDetails.getCaseId();
        final Map<String, Object> caseData = caseDetails.getCaseData();

        log.info("CaseId: {} About to add notification email tasks about offline respondent AoS submission", caseId);

        if (isPetitionerRepresented(caseData)) {
            // notify petitioner solicitor
            log.info("CaseId: {} Adding notification email task for petitioner solicitor about offline respondent AoS submission", caseId);
            tasks.add(aosReceivedPetitionerSolicitorEmailTask);
            updateTaskContext(contextTransientObjects, caseDetails, authToken); //not clear from online equivalent which ones are actually needed
        } else {
            if (isNotEmpty(getOptionalPropertyValueAsString(caseData, D_8_PETITIONER_EMAIL, EMPTY_STRING))) {
                // notify petitioner
                if (usingRespondentSolicitor(caseData)) {
                    log.info("CaseId: {} Adding email task for petitioner about offline respondent solicitor AoS submission", caseId);
                    tasks.add(emailNotificationTask);
                    updateTaskContext(contextTransientObjects, caseDetails, authToken);
                    updateTaskContextGenericEmail(contextTransientObjects, caseDetails);
                } else if (caseDataUtils.isRespondentNotDefending(caseDetails.getCaseData())) {
                    log.info("CaseId: {} Adding email task for petitioner about offline respondent undefended AoS submission", caseId);
                    tasks.add(emailNotificationTask);
                    updateTaskContext(contextTransientObjects, caseDetails, authToken);
                    updateTaskContextGenericEmail(contextTransientObjects, caseDetails);
                }
            }
        }

    }

    private void updateTaskContext(final Map<String, Object> contextTransientObjects,
                                   final CaseDetails caseDetails, final String authToken) {
        contextTransientObjects.put(AUTH_TOKEN_JSON_KEY, authToken);
        contextTransientObjects.put(CASE_ID_JSON_KEY, caseDetails.getCaseId());
        contextTransientObjects.put(CCD_CASE_DATA, caseDetails.getCaseData());
    }

    private void updateTaskContextGenericEmail(final Map<String, Object> contextTransientObjects, CaseDetails caseDetails) throws WorkflowException {
        // the equivalent code for online updated the TaskContext in an inconsistent manner for the same templates
        // now use the largest set of data for all - should really do the pet sol task differently
        GenericEmailContext notificationContext = getGenericEmailContext(caseDetails);

        contextTransientObjects.put(NOTIFICATION_EMAIL, notificationContext.getDestinationEmailAddress());
        contextTransientObjects.put(NOTIFICATION_TEMPLATE, notificationContext.getTemplateId());
        contextTransientObjects.put(NOTIFICATION_TEMPLATE_VARS, notificationContext.getTemplateFields());
    }

    private GenericEmailContext getGenericEmailContext(CaseDetails caseDetails) throws WorkflowException {
        final Map<String, Object> caseData = caseDetails.getCaseData();

        try {
            ImmutableMap.Builder<String, String> notificationTemplateVars = ImmutableMap.builder();
            notificationTemplateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY,
                getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_FIRST_NAME));
            notificationTemplateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY,
                getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_LAST_NAME));
            notificationTemplateVars.put(NOTIFICATION_RELATIONSHIP_KEY,
                getRespondentRelationship(caseDetails));
            notificationTemplateVars.put(NOTIFICATION_WELSH_HUSBAND_OR_WIFE,
                getWelshRespondentRelationship(caseDetails));
            notificationTemplateVars.put(NOTIFICATION_REFERENCE_KEY,
                getMandatoryPropertyValueAsString(caseData, D_8_CASE_REFERENCE));

            String emailToSendTo = getOptionalPropertyValueAsString(caseData, D_8_PETITIONER_EMAIL, EMPTY_STRING);
            EmailTemplateNames template = getRespondentAosTemplate(caseDetails);

            return new GenericEmailContext(emailToSendTo, template, notificationTemplateVars.build());
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new WorkflowException(exception.getMessage());
        }
    }

    private String getRespondentRelationship(CaseDetails caseDetails) {
        String gender = getOptionalPropertyValueAsString(caseDetails.getCaseData(), D_8_INFERRED_RESPONDENT_GENDER, null);
        return templateConfigService.getRelationshipTermByGender(gender, LanguagePreference.ENGLISH);
    }

    private String getWelshRespondentRelationship(CaseDetails caseDetails) {
        String gender = getOptionalPropertyValueAsString(caseDetails.getCaseData(), D_8_INFERRED_RESPONDENT_GENDER, null);
        return templateConfigService.getRelationshipTermByGender(gender, LanguagePreference.WELSH);
    }

    private EmailTemplateNames getRespondentAosTemplate(CaseDetails caseDetails) {
        EmailTemplateNames template = EmailTemplateNames.RESPONDENT_SUBMISSION_CONSENT;
        if (caseDataUtils.isAdulteryAndNoConsent(caseDetails.getCaseData())) {
            if (caseDataUtils.isCoRespNamedAndNotReplied(caseDetails.getCaseData())) {
                template = EmailTemplateNames.AOS_RECEIVED_UNDEFENDED_NO_ADMIT_ADULTERY_CORESP_NOT_REPLIED;
            } else {
                template = EmailTemplateNames.AOS_RECEIVED_UNDEFENDED_NO_ADMIT_ADULTERY;
            }
        } else if (caseDataUtils.isSep2YrAndNoConsent(caseDetails.getCaseData())) {
            template = EmailTemplateNames.AOS_RECEIVED_UNDEFENDED_NO_CONSENT_2_YEARS;
        } else if (caseDataUtils.isCoRespNamedAndNotReplied(caseDetails.getCaseData())) {
            template = EmailTemplateNames.RESPONDENT_SUBMISSION_CONSENT_CORESP_NOT_REPLIED;
        }

        return template;
    }

}
