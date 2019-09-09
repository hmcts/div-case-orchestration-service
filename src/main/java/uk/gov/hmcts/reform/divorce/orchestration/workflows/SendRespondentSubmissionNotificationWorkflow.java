
package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GenericEmailNotification;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentSubmissionNotificationForDefendedDivorceEmail;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentSubmissionNotificationForUndefendedDivorceEmail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CO_RESPONDENT_NAMED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_RESPONDENT_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RELATIONSHIP_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_TEMPLATE_VARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PET_SOL_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PET_SOL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_TO_FACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SEPARATION_2YRS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.getRelationshipTermByGender;

@Component
@Slf4j
public class SendRespondentSubmissionNotificationWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private static final String NOT_DEFENDING_NOT_ADMITTING = "NoNoAdmission";

    @Autowired
    private GenericEmailNotification emailNotificationTask;

    @Autowired
    private SendRespondentSubmissionNotificationForDefendedDivorceEmail
        sendRespondentSubmissionNotificationForDefendedDivorceEmailTask;
    @Autowired
    private SendRespondentSubmissionNotificationForUndefendedDivorceEmail
        sendRespondentSubmissionNotificationForUndefendedDivorceEmailTask;

    public Map<String, Object> run(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getCaseData();

        final String relationship = getRespondentRelationship(caseDetails);

        String petSolicitorEmail = getFieldAsStringOrNull(caseDetails, PET_SOL_EMAIL);
        String petitionerEmail = getFieldAsStringOrNull(caseDetails, D_8_PETITIONER_EMAIL);
        String ref = getFieldAsStringOrNull(caseDetails, D_8_CASE_REFERENCE);

        String petitionerFirstName = getFieldAsStringOrNull(caseDetails, D_8_PETITIONER_FIRST_NAME);
        String petitionerLastName = getFieldAsStringOrNull(caseDetails, D_8_PETITIONER_LAST_NAME);

        EmailTemplateNames template = null;
        String emailToBeSentTo = null;

        Map<String, String> notificationTemplateVars = new HashMap<>();

        List<Task> tasks = new ArrayList<>();

        if (respondentIsDefending(caseDetails)) {
            tasks.add(sendRespondentSubmissionNotificationForDefendedDivorceEmailTask);
        } else if (respondentIsNotDefending(caseDetails)) {
            if (StringUtils.isNotEmpty(petSolicitorEmail)) {
                String respFirstName = getFieldAsStringOrNull(caseDetails, RESP_FIRST_NAME_CCD_FIELD);
                String respLastName = getFieldAsStringOrNull(caseDetails, RESP_LAST_NAME_CCD_FIELD);
                String solicitorName = getFieldAsStringOrNull(caseDetails, PET_SOL_NAME);

                notificationTemplateVars.put(NOTIFICATION_EMAIL, petSolicitorEmail);
                notificationTemplateVars.put(NOTIFICATION_PET_NAME, petitionerFirstName + " " + petitionerLastName);
                notificationTemplateVars.put(NOTIFICATION_RESP_NAME, respFirstName + " " + respLastName);
                notificationTemplateVars.put(NOTIFICATION_SOLICITOR_NAME, solicitorName);
                notificationTemplateVars.put(NOTIFICATION_CCD_REFERENCE_KEY, caseDetails.getCaseId());

                tasks.add(emailNotificationTask);
                template = findTemplateNameToBeSent(caseDetails, true);
                emailToBeSentTo = petSolicitorEmail;
            } else if (StringUtils.isNotEmpty(petitionerEmail)) {
                notificationTemplateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, petitionerFirstName);
                notificationTemplateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, petitionerLastName);
                notificationTemplateVars.put(NOTIFICATION_RELATIONSHIP_KEY, relationship);
                notificationTemplateVars.put(NOTIFICATION_REFERENCE_KEY, ref);

                tasks.add(emailNotificationTask);
                template = findTemplateNameToBeSent(caseDetails, false);
                emailToBeSentTo = petitionerEmail;
            }

            tasks.add(sendRespondentSubmissionNotificationForUndefendedDivorceEmailTask);
        } else {
            String errorMessage = String.format("%s field doesn't contain a valid value: %s",
                RESP_WILL_DEFEND_DIVORCE, caseData.get(RESP_WILL_DEFEND_DIVORCE));
            log.error(String.format("%s. %n Case id: %s.", errorMessage, ccdCallbackRequest.getCaseDetails().getCaseId()));
            throw new WorkflowException(errorMessage);
        }

        return execute(tasks.toArray(new Task[tasks.size()]),
            caseData,
            ImmutablePair.of(CASE_ID_JSON_KEY, caseDetails.getCaseId()),
            ImmutablePair.of(NOTIFICATION_EMAIL, emailToBeSentTo),
            ImmutablePair.of(NOTIFICATION_TEMPLATE_VARS, notificationTemplateVars),
            ImmutablePair.of(NOTIFICATION_TEMPLATE, template)
        );
    }

    private boolean respondentIsDefending(CaseDetails caseDetails) {
        final String respWillDefendDivorce = (String) caseDetails.getCaseData().get(RESP_WILL_DEFEND_DIVORCE);
        return YES_VALUE.equalsIgnoreCase(respWillDefendDivorce);
    }

    private boolean respondentIsNotDefending(CaseDetails caseDetails) {
        final String respWillDefendDivorce = (String) caseDetails.getCaseData().get(RESP_WILL_DEFEND_DIVORCE);
        return NO_VALUE.equalsIgnoreCase(respWillDefendDivorce)
            || NOT_DEFENDING_NOT_ADMITTING.equalsIgnoreCase(respWillDefendDivorce);
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

    private EmailTemplateNames findTemplateNameToBeSent(CaseDetails caseDetails, boolean isSolicitor) {
        EmailTemplateNames template = EmailTemplateNames.RESPONDENT_SUBMISSION_CONSENT;
        if (isSolicitor) {
            template = EmailTemplateNames.SOL_APPLICANT_AOS_RECEIVED;
        } else if (isAdulteryAndNoConsent(caseDetails)) {
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
        return StringUtils.equalsIgnoreCase(ADULTERY, reasonForDivorce) && StringUtils.equalsIgnoreCase(NO_VALUE, respAdmitOrConsentToFact);
    }

    private boolean isSep2YrAndNoConsent(CaseDetails caseDetails) {
        String reasonForDivorce = getFieldAsStringOrNull(caseDetails, D_8_REASON_FOR_DIVORCE);
        String respAdmitOrConsentToFact = getFieldAsStringOrNull(caseDetails, RESP_ADMIT_OR_CONSENT_TO_FACT);
        return StringUtils.equalsIgnoreCase(SEPARATION_2YRS, reasonForDivorce) && StringUtils.equalsIgnoreCase(NO_VALUE, respAdmitOrConsentToFact);
    }

    private boolean isCoRespNamedAndNotReplied(CaseDetails caseDetails) {
        String isCoRespNamed = getFieldAsStringOrNull(caseDetails, D_8_CO_RESPONDENT_NAMED);
        String receivedAosFromCoResp = getFieldAsStringOrNull(caseDetails, RECEIVED_AOS_FROM_CO_RESP);
        return StringUtils.equalsIgnoreCase(isCoRespNamed, YES_VALUE) && !StringUtils.equalsIgnoreCase(receivedAosFromCoResp, YES_VALUE);
    }
}