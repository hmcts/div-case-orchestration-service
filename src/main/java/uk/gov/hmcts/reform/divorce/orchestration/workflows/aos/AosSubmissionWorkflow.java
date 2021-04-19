package uk.gov.hmcts.reform.divorce.orchestration.workflows.aos;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.GenericEmailContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.service.TemplateConfigService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GenericEmailNotificationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.QueueAosSolicitorSubmitTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentSubmissionNotificationForDefendedDivorceEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentSubmissionNotificationForUndefendedDivorceEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aos.AosReceivedPetitionerSolicitorEmailTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.microsoft.applicationinsights.web.dependencies.apachecommons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_COMPANY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CO_RESPONDENT_NAMED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_RESPONDENT_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RELATIONSHIP_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_TEMPLATE_VARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_WELSH_HUSBAND_OR_WIFE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOT_DEFENDING_NOT_ADMITTING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_TO_FACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.constants.TaskContextConstants.CCD_CASE_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.SEPARATION_TWO_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isPetitionerRepresented;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isRespondentRepresented;

@Component
@AllArgsConstructor
@Slf4j
public class AosSubmissionWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final GenericEmailNotificationTask emailNotificationTask;
    private final SendRespondentSubmissionNotificationForDefendedDivorceEmailTask
        sendRespondentSubmissionNotificationForDefendedDivorceEmailTask;
    private final SendRespondentSubmissionNotificationForUndefendedDivorceEmailTask
        sendRespondentSubmissionNotificationForUndefendedDivorceEmailTask;
    private final AosReceivedPetitionerSolicitorEmailTask aosReceivedPetitionerSolicitorEmailTask;

    private final QueueAosSolicitorSubmitTask queueAosSolicitorSubmitTask;
    private final TemplateConfigService templateConfigService;

    public Map<String, Object> run(CcdCallbackRequest ccdCallbackRequest, final String authToken) throws WorkflowException {
        final List<Task<Map<String, Object>>> tasks = new ArrayList<>();
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getCaseData();
        String caseId = caseDetails.getCaseId();
        String caseState = caseDetails.getState();

        GenericEmailContext notificationContext = getGenericEmailContext(caseDetails);

        if (isPetitionerRepresented(caseData)) {
            tasks.add(aosReceivedPetitionerSolicitorEmailTask);
        }

        if (usingRespondentSolicitor(caseData)) {
            log.info("CaseId: {} Adding generic notification email task for petitioner on solicitor AoS submission", caseId);
            addNotificationEmailTaskForNonRepresentedPetitioner(tasks, caseData);

            log.info("CaseId: {} Queueing solicitor AoS submission, case state: {}", caseId, caseState);
            tasks.add(queueAosSolicitorSubmitTask);

            return execute(
                tasks.toArray(new Task[0]),
                caseData,
                ImmutablePair.of(CASE_ID_JSON_KEY, caseId),
                ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
                ImmutablePair.of(CCD_CASE_DATA, caseData),
                ImmutablePair.of(NOTIFICATION_EMAIL, notificationContext.getDestinationEmailAddress()),
                ImmutablePair.of(NOTIFICATION_TEMPLATE_VARS, notificationContext.getTemplateFields()),
                ImmutablePair.of(NOTIFICATION_TEMPLATE, notificationContext.getTemplateId())
            );
        }

        log.info("CaseId: {} Attempting to process AoS submission tasks, case state: {}", caseId, caseState);
        processAosSubmissionTasks(ccdCallbackRequest, tasks);

        return execute(
            tasks.toArray(new Task[0]),
            caseData,
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId),
            ImmutablePair.of(NOTIFICATION_EMAIL, notificationContext.getDestinationEmailAddress()),
            ImmutablePair.of(NOTIFICATION_TEMPLATE_VARS, notificationContext.getTemplateFields()),
            ImmutablePair.of(NOTIFICATION_TEMPLATE, notificationContext.getTemplateId())
        );
    }


    private void processAosSubmissionTasks(
        CcdCallbackRequest ccdCallbackRequest, List<Task<Map<String, Object>>> tasks) throws WorkflowException {
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();

        final Map<String, Object> caseData = caseDetails.getCaseData();
        final String caseId = caseDetails.getCaseId();
        final String state = caseDetails.getState();

        if (respondentIsDefending(caseDetails)) {
            log.info("CaseId: {} Respondent is defending, case state: {}", caseId, state);
            tasks.add(sendRespondentSubmissionNotificationForDefendedDivorceEmailTask);
        } else if (respondentIsNotDefending(caseDetails)) {
            log.info("CaseId: {} Adding generic notification email task for petitioner on AoS submission", caseId);
            addNotificationEmailTaskForNonRepresentedPetitioner(tasks, caseData);

            log.info("CaseId: {} Respondent is not defending, case state: {}", caseId, state);
            tasks.add(sendRespondentSubmissionNotificationForUndefendedDivorceEmailTask);
        } else {
            final String errorMessage = String.format("%s field doesn't contain a valid value: %s",
                RESP_WILL_DEFEND_DIVORCE, caseData.get(RESP_WILL_DEFEND_DIVORCE));
            log.error(String.format("%s. %n Case id: %s.", errorMessage, caseId));
            throw new WorkflowException(errorMessage);
        }
    }

    private void addNotificationEmailTaskForNonRepresentedPetitioner(List<Task<Map<String, Object>>> tasks, Map<String, Object> caseData) {
        String petitionerEmail = getOptionalPropertyValueAsString(caseData, D_8_PETITIONER_EMAIL, EMPTY_STRING);
        if (!isPetitionerRepresented(caseData) && isNotEmpty(petitionerEmail)) {
            tasks.add(emailNotificationTask);
        }
    }

    private GenericEmailContext getGenericEmailContext(CaseDetails caseDetails) throws WorkflowException {
        EmailTemplateNames template = null;
        String emailToSendTo = null;
        ImmutableMap.Builder<String, String> notificationTemplateVars = ImmutableMap.builder();
        try {
            final String relationship = getRespondentRelationship(caseDetails);
            final String welshRelationship = getWelshRespondentRelationship(caseDetails);

            final Map<String, Object> caseData = caseDetails.getCaseData();
            final String ref = getMandatoryPropertyValueAsString(caseData, D_8_CASE_REFERENCE);
            final String petitionerFirstName = getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_FIRST_NAME);
            final String petitionerLastName = getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_LAST_NAME);
            final String petitionerEmail = getOptionalPropertyValueAsString(caseData, D_8_PETITIONER_EMAIL, EMPTY_STRING);

            notificationTemplateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, petitionerFirstName);
            notificationTemplateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, petitionerLastName);
            notificationTemplateVars.put(NOTIFICATION_RELATIONSHIP_KEY, relationship);
            notificationTemplateVars.put(NOTIFICATION_WELSH_HUSBAND_OR_WIFE, welshRelationship);
            notificationTemplateVars.put(NOTIFICATION_REFERENCE_KEY, ref);

            template = findTemplateNameToBeSent(caseDetails);
            emailToSendTo = petitionerEmail;

        } catch (Exception exception) {
            throw new WorkflowException(exception.getMessage());
        }

        return new GenericEmailContext(emailToSendTo, template, notificationTemplateVars.build());
    }

    private boolean usingRespondentSolicitor(Map<String, Object> caseData) {
        // temporary fix until we implement setting respondentSolicitorRepresented from CCD for RespSols
        return isRespondentRepresented(caseData) || hasRespondentSolicitorDetail(caseData);
    }

    private boolean hasRespondentSolicitorDetail(Map<String, Object> caseData) {
        String respondentSolicitorName = getOptionalPropertyValueAsString(caseData, D8_RESPONDENT_SOLICITOR_NAME, EMPTY_STRING);
        String respondentSolicitorCompany = getOptionalPropertyValueAsString(caseData, D8_RESPONDENT_SOLICITOR_COMPANY, EMPTY_STRING);
        return isNotEmpty(respondentSolicitorName) && isNotEmpty(respondentSolicitorCompany);
    }

    private boolean respondentIsDefending(CaseDetails caseDetails) {
        String respWillDefendDivorce = getOptionalPropertyValueAsString(caseDetails.getCaseData(), RESP_WILL_DEFEND_DIVORCE, EMPTY_STRING);
        return YES_VALUE.equalsIgnoreCase(respWillDefendDivorce);
    }

    private boolean respondentIsNotDefending(CaseDetails caseDetails) {
        String respWillDefendDivorce = getOptionalPropertyValueAsString(caseDetails.getCaseData(), RESP_WILL_DEFEND_DIVORCE, EMPTY_STRING);
        return NO_VALUE.equalsIgnoreCase(respWillDefendDivorce)
            || NOT_DEFENDING_NOT_ADMITTING.equalsIgnoreCase(respWillDefendDivorce);
    }

    private String getRespondentRelationship(CaseDetails caseDetails) {
        String gender = getFieldAsStringOrNull(caseDetails, D_8_INFERRED_RESPONDENT_GENDER);
        return templateConfigService.getRelationshipTermByGender(gender, LanguagePreference.ENGLISH);
    }

    private String getWelshRespondentRelationship(CaseDetails caseDetails) {
        String gender = getFieldAsStringOrNull(caseDetails, D_8_INFERRED_RESPONDENT_GENDER);
        return templateConfigService.getRelationshipTermByGender(gender, LanguagePreference.WELSH);
    }

    private String getFieldAsStringOrNull(final CaseDetails caseDetails, String fieldKey) {
        Object fieldValue = caseDetails.getCaseData().get(fieldKey);
        if (fieldValue == null) {
            return null;
        }
        return fieldValue.toString();
    }

    private EmailTemplateNames findTemplateNameToBeSent(CaseDetails caseDetails) {
        EmailTemplateNames template = EmailTemplateNames.RESPONDENT_SUBMISSION_CONSENT;
        if (isAdulteryAndNoConsent(caseDetails)) {
            if (isCoRespNamedAndNotReplied(caseDetails)) {
                template = EmailTemplateNames.AOS_RECEIVED_UNDEFENDED_NO_ADMIT_ADULTERY_CORESP_NOT_REPLIED;
            } else {
                template = EmailTemplateNames.AOS_RECEIVED_UNDEFENDED_NO_ADMIT_ADULTERY;
            }
        } else if (isSep2YrAndNoConsent(caseDetails)) {
            template = EmailTemplateNames.AOS_RECEIVED_UNDEFENDED_NO_CONSENT_2_YEARS;
        } else if (isCoRespNamedAndNotReplied(caseDetails)) {
            template = EmailTemplateNames.RESPONDENT_SUBMISSION_CONSENT_CORESP_NOT_REPLIED;
        }

        return template;
    }

    private boolean isAdulteryAndNoConsent(CaseDetails caseDetails) {
        String reasonForDivorce = getFieldAsStringOrNull(caseDetails, D_8_REASON_FOR_DIVORCE);
        String respAdmitOrConsentToFact = getFieldAsStringOrNull(caseDetails, RESP_ADMIT_OR_CONSENT_TO_FACT);
        return equalsIgnoreCase(ADULTERY.getValue(), reasonForDivorce) && equalsIgnoreCase(NO_VALUE,
            respAdmitOrConsentToFact);
    }

    private boolean isSep2YrAndNoConsent(CaseDetails caseDetails) {
        String reasonForDivorce = getFieldAsStringOrNull(caseDetails, D_8_REASON_FOR_DIVORCE);
        String respAdmitOrConsentToFact = getFieldAsStringOrNull(caseDetails, RESP_ADMIT_OR_CONSENT_TO_FACT);
        return equalsIgnoreCase(SEPARATION_TWO_YEARS.getValue(), reasonForDivorce)
            && equalsIgnoreCase(NO_VALUE, respAdmitOrConsentToFact);
    }

    private boolean isCoRespNamedAndNotReplied(CaseDetails caseDetails) {
        String isCoRespNamed = getFieldAsStringOrNull(caseDetails, D_8_CO_RESPONDENT_NAMED);
        String receivedAosFromCoResp = getFieldAsStringOrNull(caseDetails, RECEIVED_AOS_FROM_CO_RESP);
        return equalsIgnoreCase(isCoRespNamed, YES_VALUE) && !equalsIgnoreCase(receivedAosFromCoResp, YES_VALUE);
    }
}
