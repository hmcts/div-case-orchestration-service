package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GenericEmailNotification;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentAnswersGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_AOS_2_YR_CONSENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_AOS_ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SEPARATION_2YRS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_AOS_RECEIVED_NO_ADCON_STARTED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_AOS_SUBMITTED_DEFENDED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_AOS_SUBMITTED_UNDEFENDED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.getRelationshipTermByGender;

@Component
public class RespondentSubmittedCallbackWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final CaseMaintenanceClient caseMaintenanceClient;
    private final GenericEmailNotification emailNotificationTask;
    private final RespondentAnswersGenerator respondentAnswersGenerator;
    private final CaseFormatterAddDocuments caseFormatterAddDocuments;
    private final CcdUtil ccdUtil;
    private final List<Task> tasks = new ArrayList<>();

    private Map<String, String> notificationTemplateVars = new HashMap<>();

    private String eventId;
    private EmailTemplateNames template = null;
    private String emailToBeSentTo = null;

    @Autowired
    public RespondentSubmittedCallbackWorkflow(CaseMaintenanceClient caseMaintenanceClient,
                                               GenericEmailNotification emailNotificationTask,
                                               RespondentAnswersGenerator respondentAnswersGenerator,
                                               CaseFormatterAddDocuments caseFormatterAddDocuments,
                                               CcdUtil ccdUtil) {
        this.caseMaintenanceClient = caseMaintenanceClient;
        this.emailNotificationTask = emailNotificationTask;
        this.respondentAnswersGenerator = respondentAnswersGenerator;
        this.caseFormatterAddDocuments = caseFormatterAddDocuments;
        this.ccdUtil = ccdUtil;
    }

    public Map<String, Object> run(CcdCallbackRequest ccdCallbackRequest, String authToken) throws WorkflowException {
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();

        sendNotificationEmail(caseDetails);

        if (isSolicitorRepresentingRespondent(caseDetails)) {
            mapSolicitorRespAosValues(caseDetails);
            setEventIdForSolicitor(authToken, caseDetails);
        }

        tasks.add(respondentAnswersGenerator);
        tasks.add(caseFormatterAddDocuments);

        Task[] taskArr = new Task[tasks.size()];

        return this.execute(
            tasks.toArray(taskArr),
            caseDetails.getCaseData(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(NOTIFICATION_EMAIL, emailToBeSentTo),
            ImmutablePair.of(NOTIFICATION_TEMPLATE_VARS, notificationTemplateVars),
            ImmutablePair.of(NOTIFICATION_TEMPLATE, template),
            ImmutablePair.of(ID, caseDetails.getCaseId())
        );
    }

    private void sendNotificationEmail(CaseDetails caseDetails) {
        String petSolicitorEmail = getFieldAsStringOrNull(caseDetails, PET_SOL_EMAIL);
        String petitionerEmail = getFieldAsStringOrNull(caseDetails, D_8_PETITIONER_EMAIL);
        String petitionerFirstName = getFieldAsStringOrNull(caseDetails, D_8_PETITIONER_FIRST_NAME);
        String petitionerLastName = getFieldAsStringOrNull(caseDetails, D_8_PETITIONER_LAST_NAME);

        // only send an email to pet / solicitor. if respondent is not defending
        if (!respondentIsDefending(caseDetails)) {
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
                String ref = getFieldAsStringOrNull(caseDetails, D_8_CASE_REFERENCE);

                notificationTemplateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, petitionerFirstName);
                notificationTemplateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, petitionerLastName);
                notificationTemplateVars.put(NOTIFICATION_RELATIONSHIP_KEY, getRespondentRelationship(caseDetails));
                notificationTemplateVars.put(NOTIFICATION_REFERENCE_KEY, ref);

                tasks.add(emailNotificationTask);
                template = findTemplateNameToBeSent(caseDetails, false);
                emailToBeSentTo = petitionerEmail;
            }
        }
    }

    private void setEventIdForSolicitor(String authToken, CaseDetails caseDetails) {

        if (respondentIsDefending(caseDetails)) {
            if (respAdmitsOrConsentsToFact(caseDetails)) {
                eventId = SOL_AOS_SUBMITTED_DEFENDED_EVENT_ID;
            } else {
                eventId = SOL_AOS_RECEIVED_NO_ADCON_STARTED_EVENT_ID;
            }
        } else {
            if (respAdmitsOrConsentsToFact(caseDetails)) {
                eventId = SOL_AOS_SUBMITTED_UNDEFENDED_EVENT_ID;
            } else {
                eventId = SOL_AOS_RECEIVED_NO_ADCON_STARTED_EVENT_ID;
            }
        }

        Map<String, Object> updateCase = caseMaintenanceClient.updateCase(
                authToken,
                caseDetails.getCaseId(),
                eventId,
                caseDetails.getCaseData()
        );

        if (updateCase != null) {
            updateCase.remove(CCD_CASE_DATA_FIELD);
        }
    }

    private void mapSolicitorRespAosValues(CaseDetails caseDetails) {
        // Maps CCD values of RespAOS2yrConsent & RespAOSAdultery -> RespAdmitOrConsentToFact & RespWillDefendDivorce fields in Case Data

        final String reasonForDivorce = getReasonForDivorce(caseDetails);
        final String respAos2yrConsent = (String)caseDetails.getCaseData().get(RESP_AOS_2_YR_CONSENT);
        final String respAosAdultery = (String)caseDetails.getCaseData().get(RESP_AOS_ADULTERY);

        if ((SEPARATION_2YRS.equalsIgnoreCase(reasonForDivorce) && YES_VALUE.equalsIgnoreCase(respAos2yrConsent))
                || (ADULTERY.equalsIgnoreCase(reasonForDivorce) && YES_VALUE.equalsIgnoreCase(respAosAdultery))) {

            Map<String, Object> caseInfo = caseDetails.getCaseData();

            caseInfo.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);
            caseInfo.put(RESP_ADMIT_OR_CONSENT_TO_FACT, YES_VALUE);
        }
    }

    private String getReasonForDivorce(CaseDetails caseDetails) {
        final String reasonForDivorce = (String)caseDetails.getCaseData().get(D_8_REASON_FOR_DIVORCE);
        return reasonForDivorce;
    }

    private boolean respondentIsDefending(CaseDetails caseDetails) {
        final String respWillDefendDivorce = (String)caseDetails.getCaseData().get(RESP_WILL_DEFEND_DIVORCE);
        return YES_VALUE.equalsIgnoreCase(respWillDefendDivorce);
    }

    private boolean respAdmitsOrConsentsToFact(CaseDetails caseDetails) {
        final String respAdmitOrConsentsToFact = (String)caseDetails.getCaseData().get(RESP_ADMIT_OR_CONSENT_TO_FACT);
        return YES_VALUE.equalsIgnoreCase(respAdmitOrConsentsToFact);
    }

    private String getRespondentRelationship(CaseDetails caseDetails) {
        String gender = getFieldAsStringOrNull(caseDetails, D_8_INFERRED_RESPONDENT_GENDER);
        return getRelationshipTermByGender(gender);
    }

    private boolean isSolicitorRepresentingRespondent(CaseDetails caseDetails) {
        final String respondentSolicitorRepresented = (String)caseDetails.getCaseData().get(RESP_SOL_REPRESENTED);
        return YES_VALUE.equalsIgnoreCase(respondentSolicitorRepresented);
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
